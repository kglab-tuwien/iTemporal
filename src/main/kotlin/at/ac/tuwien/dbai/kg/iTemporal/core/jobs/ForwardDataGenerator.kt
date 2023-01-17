package at.ac.tuwien.dbai.kg.iTemporal.core.jobs

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.DataGeneration
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraphHelper
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.NodeType
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.IntersectionEdge

class ForwardDataGenerator(val variableReplaceMode: VariableReplaceMode = VariableReplaceMode.MINIMAL) : DataGeneration {

    enum class VariableReplaceMode {
        /* Only replace, if chain is replacing and node has only one outgoing edge */
        MINIMAL,
        /* Replace along the chain independent whether there are multiple outgoing edges */
        MAXIMAL,
    }
    var varcount = 0;

    fun getNextVariable(): NodeVariable {
        return NodeVariable(varcount++)
    }

    val termMapping = mutableMapOf<Node, NodeTerms>()

    data class NodeTerms(val nodeTerms: MutableSet<NodeTerm> = mutableSetOf()) {}

    data class NodeTerm(val variables: List<NodeVariable>, var replaced: Boolean = false) {}

    data class NodeVariable(val id: Int) {
        override fun toString(): String {
            return id.toString()
        }
    }

    override fun getPriority(): Int = 46


    private fun propagateVariables(dependencyGraph: DependencyGraph, node: Node): Boolean {
        val inEdges = dependencyGraph.inEdges[node].orEmpty()

        if (inEdges.size > 1) {
            val edge1 = inEdges[0]
            val edge2 = inEdges[1]

            if (edge1 is IntersectionEdge) {
                return propagteIntersection(dependencyGraph, edge1, edge2)
            } else {
                return propagateUnion(edge1, edge2)
            }
        } else {
            return propagateLinear(inEdges.single())
        }
    }


    private fun propagateUnion(edge1: Edge, edge2: Edge): Boolean {
        println("propagateUnion")
        val mappings1 = computeForwardTermMapping(edge1)
        val mappings2 = computeForwardTermMapping(edge2)

        val map1Result = termMapping[edge1.to]!!.nodeTerms.addAll(mappings1.nodeTerms)
        val map2Result = termMapping[edge2.to]!!.nodeTerms.addAll(mappings2.nodeTerms)


        return map1Result || map2Result
    }

    private fun propagteIntersection(dependencyGraph: DependencyGraph, edge1: Edge, edge2: Edge): Boolean {
        println("propagteIntersection")
        val mappings1 = computeForwardTermMapping(edge1)
        val mappings2 = computeForwardTermMapping(edge2)

        var joinResult = false


        // Merge Mappings, we always add the smaller variable for consistency, as both variables required the same value.
        mappings1.nodeTerms.forEach { map1NodeTerm ->
            mappings2.nodeTerms.forEach { map2NodeTerm ->
                val variableList = mutableListOf<NodeVariable>()

                for ((idx, value) in map2NodeTerm.variables.withIndex()) {
                    if (value.id == -1 && map1NodeTerm.variables[idx].id == -1) {
                        throw RuntimeException("intersection cannot introduce new variable")
                    } else if (value.id == -1) {
                        variableList.add(map1NodeTerm.variables[idx])
                    } else if (map1NodeTerm.variables[idx].id == -1) {
                        variableList.add(value)
                    } else if (value.id < map1NodeTerm.variables[idx].id) {
                        variableList.add(value)
                    } else {
                        variableList.add(map1NodeTerm.variables[idx])
                    }
                }

                val newTerm = NodeTerm(variableList)
                val isAdded = termMapping[edge1.to]!!.nodeTerms.add(newTerm)
                if (isAdded) {
                    propagateVariablesBackward(dependencyGraph, edge1, newTerm, map1NodeTerm)
                    propagateVariablesBackward(dependencyGraph, edge2, newTerm, map2NodeTerm)
                    joinResult = true
                }
            }
        }

        return joinResult
    }

    private fun propagateLinear(edge: Edge): Boolean {
        println("propagateLinear")
        val mappings = computeForwardTermMapping(edge)
        return termMapping[edge.to]!!.nodeTerms.addAll(
            mappings.nodeTerms.map { nodeTerm ->
                // Introduce new variables for aggregation results, etc.
                // In recursion this has to be limited to a specific variable,
                NodeTerm(nodeTerm.variables.map {
                    if (it.id == -1) {
                        // TODO: Limitation of variable
                        NodeVariable(-1) //getNextVariable()
                    } else {
                        it
                    }
                })
            }
        )


    }

    /**
     * This step back-propagates variables in case of joins.
     *
     * This is, we replace variables with other variables from different inputs.
     * For consistency, we use always the lower join variable.
     *
     *
     */
    private fun propagateVariablesBackward(dependencyGraph: DependencyGraph, edge: Edge, newTerm: NodeTerm, oldTerm: NodeTerm, replaceEnabled:Boolean = true) {
        // Step 1: Compare new term with old term. In case there is nothing different we are finished
        // We cannot use == as we can ignore any -1 fields as these fields are not back-mapped

        println("propagateVariablesBackward")

        var isChanged = false
        for ((idx, value) in oldTerm.variables.withIndex()) {
            if (value.id == -1) {
                continue
            }
            if (value.id != newTerm.variables[idx].id) {
                isChanged = true
                break
            }
        }
        if (!isChanged) {
            return
        }


        // Step 2: We compute the back mapping of the old term
        val originalPattern = computeBackwardTermMapping(edge, oldTerm)

        // Step 3: We find all entries that match the pattern in the from node
        // Again note that this pattern may contain -1, which matches any fields as only the other ones have to match
        val applicableTerms = termMapping[edge.from]!!.nodeTerms.filter { nodeTerm ->
            for ((idx, value) in originalPattern.variables.withIndex()) {
                if (value.id == -1) {
                    continue
                }
                if (value.id != nodeTerm.variables[idx].id) {
                    return@filter false
                }
            }
            return@filter true
        }

        // Nothing to replace, we are finished
        if (applicableTerms.isEmpty()) {
            return
        }

        // Step 4: add new term by merging with existing term to fill up pattern
        val newPattern = computeBackwardTermMapping(edge, newTerm)

        applicableTerms.forEach { nodeTerm ->
            val terms = Array(edge.from.minArity) { NodeVariable(-1) }.toMutableList()

            for ((idx, value) in newPattern.variables.withIndex()) {
                if (value.id == -1) {
                    // Use applicableTerms one
                    terms[idx] = nodeTerm.variables[idx]
                } else {
                    terms[idx] = value
                }
            }

            val addingTerm = NodeTerm(terms)

            // We can only remove the term at the end in case this was the only outgoing edge, as it was overwritten with the new setting
            val newReplaceEnabled = (dependencyGraph.outEdges[edge.from].orEmpty().size > 1 && replaceEnabled) || variableReplaceMode == VariableReplaceMode.MAXIMAL
            nodeTerm.replaced = nodeTerm.replaced || newReplaceEnabled
            val isAdded = termMapping[edge.from]!!.nodeTerms.add(addingTerm)
            if (isAdded) {
                // Continue back propagation
                dependencyGraph.inEdges[edge.from].orEmpty().forEach {
                    propagateVariablesBackward(dependencyGraph, it, addingTerm, nodeTerm, newReplaceEnabled)
                }
            }

        }

    }

    /**
     * Maps incoming terms to current node
     */
    private fun computeForwardTermMapping(edge: Edge): NodeTerms {
        val list = termMapping[edge.from]!!.nodeTerms.map {
            val terms = Array(edge.to.minArity) { NodeVariable(-1) }.toMutableList()

            for ((fromIndex, orderId) in edge.termOrder.withIndex()) {
                // Not relevant term
                if (orderId < 0 || orderId >= edge.to.minArity) {
                    continue
                }
                if (orderId < edge.to.minArity) {
                    terms[orderId] = it.variables[fromIndex]
                }
            }

            return@map NodeTerm(terms)
        }

        return NodeTerms(list.toMutableSet())
    }

    private fun computeBackwardTermMapping(edge: Edge, term: NodeTerm): NodeTerm {
        val terms = Array(edge.from.minArity) { NodeVariable(-1) }.toMutableList()

        for ((fromIndex, orderId) in edge.termOrder.withIndex()) {
            // Not relevant term
            if (orderId < 0 || orderId >= edge.to.minArity) {
                continue
            }
            if (orderId < edge.to.minArity) {
                terms[fromIndex] = term.variables[orderId]
            }
        }

        return NodeTerm(terms)
    }

    /*
     * We have following options to consider from the start considering two input nodes with two terms
     * x0,x1 ->
     * x2,x3 ->
     *
     * intersection: -> e.g., x0,x1 remains and x2,x3 has to be updated with x0,x1
     * union: both combinations are valid
     *
     * in addition, we have recursion: here we have to consider all valid patterns
     *
     * !! We do not consider any temporal terms here, only the Datalog variables (finite number of combinations) !!
     *
     * (1) Hence, we compute each strongly connected component ordered by the beginning
     * (2) per strongly connected component, we compute the combinations
     */
    private fun propagateVariables(dependencyGraph: DependencyGraph) {
        println("propagateVariables")
        val sCCs = DependencyGraphHelper.calculateSCC(dependencyGraph)
        val sccOrder = DependencyGraphHelper.createSCCOrder(dependencyGraph, sCCs).reversed()

        for (sccId in sccOrder) {
            val nodes: List<Node> = sCCs[sccId]

            println("sccId ${sccId}")

            if (nodes.isEmpty()) {
                throw RuntimeException("A group should not be empty")
            }

            if (nodes.size == 1) {
                // Simple case, not cyclic:

                val node = nodes.single()

                if (node.type == NodeType.Input) {
                    // Is input node - generate variables
                    val nodeTerm = NodeTerm(Array(node.minArity) { getNextVariable() }.toList())
                    termMapping[node]!!.nodeTerms.add(nodeTerm)
                } else {
                    // Copy variables
                    propagateVariables(dependencyGraph, node)
                }
            } else {
                // Only propagation possible, continue as long as no additional propagation is possible
                var hasChanged = true

                while (hasChanged) {
                    hasChanged = false
                    for (node in nodes) {
                        val updated = propagateVariables(dependencyGraph, node)
                        hasChanged = hasChanged || updated
                    }
                }

            }

        }

        // After we are finished, we clean up replaced ones
        termMapping.forEach{ entry->
            entry.value.nodeTerms.removeIf { it.replaced }
        }

    }


    /**
     * 1. Calculate shared variables between different inputs
     * 2. Calculate minimum required interval length
     * 3. Matching selectivity for joins, determine required join offsets
     *
     *
     */

    override fun run(dependencyGraph: DependencyGraph, storeFile: Boolean): DependencyGraph {
        // Init termMapping with nodes
        dependencyGraph.nodes.forEach {
            termMapping[it] = NodeTerms()
        }

        // Fill out term mapping
        propagateVariables(dependencyGraph)

        println(termMapping)

        throw RuntimeException("Not supported yet")

        //return dependencyGraph
    }

}