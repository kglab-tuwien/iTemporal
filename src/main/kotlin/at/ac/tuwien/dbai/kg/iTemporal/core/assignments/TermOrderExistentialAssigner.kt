package at.ac.tuwien.dbai.kg.iTemporal.core.assignments

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.OtherPropertyAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.SingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.IntersectionEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.UnionEdge
import at.ac.tuwien.dbai.kg.iTemporal.util.GenericMutableDependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.util.IEdge
import at.ac.tuwien.dbai.kg.iTemporal.util.INode
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import kotlin.math.roundToInt

/**
 * Calls the assignment of the term order for a given edge.
 * Handles intersection edges with a special treatment.
 */
object TermOrderExistentialAssigner : OtherPropertyAssignment {
    override fun getPriority(): Int = 165


    data class TermNode(val node: Node, val pos: Int) : INode {
        override fun getLabel(): String {
            return "${node.getLabel()}_$pos"
        }

        override fun getStyle(node: guru.nidi.graphviz.model.Node): guru.nidi.graphviz.model.Node {
            return node
        }
    }

    data class TermEdge(
        override val from: TermNode,
        override val to: TermNode,
        val special: Boolean = false,
        val isAdded: Boolean = false
    ) : IEdge<TermNode> {
        override fun getStyle(edge: guru.nidi.graphviz.model.Link): guru.nidi.graphviz.model.Link {
            return if (special)
                edge.with(guru.nidi.graphviz.attribute.Color.PURPLE)
            else if (isAdded)
                edge.with(guru.nidi.graphviz.attribute.Color.BLUE)
            else edge
        }
    }

    override fun run(dependencyGraph: DependencyGraph): DependencyGraph {
        if (!Registry.properties.weaklyAcyclicProgram) {
            return dependencyGraph
        }

        val termDependencyGraph = GenericMutableDependencyGraph<TermNode, TermEdge>()

        dependencyGraph.nodes.forEach { node ->
            for (i in 0 until node.minArity) {
                termDependencyGraph.addNode(TermNode(node, i))
            }
        }

        dependencyGraph.inEdges.values.flatten().forEach { edge ->
            edge.termOrder.withIndex().forEach {
                val from = TermNode(edge.from, it.index)
                val to = TermNode(edge.to, it.value)

                if (termDependencyGraph.nodes.contains(from) && termDependencyGraph.nodes.contains(to)) {
                    termDependencyGraph.addEdge(TermEdge(from, to))
                } else {
                    if (Registry.debug) {
                        println("One of the nodes is not in dependency graph ${TermEdge(from, to)}")
                    }
                }

            }
        }


        val numberOfExistentials = RandomGenerator.getNextDoubleWithPrecision(
            Registry.properties.averageExistentials.toDouble(),
            Registry.properties.varianceExistentials,
            0
        ).roundToInt()

        //termDependencyGraph.draw("termDependencyGraph")

        val allExistentials = mutableListOf<TermNode>()

        for (i in 0 until numberOfExistentials) {
            // Any edge can be an existential edge which is not a self loop, but we only allow single edges to take this form
            val existentialEdge =
                dependencyGraph.outEdges.values.flatten().filter { it.from != it.to && it is SingleEdge }
                    .randomOrNull(RandomGenerator.sharedRandom)

            if (existentialEdge == null) {
                if (Registry.debug) {
                    println("No single edge found")
                }
                return dependencyGraph
            }
            if (Registry.debug) {
                println("Selected existential edge: $existentialEdge")
            }

            val chainLength = RandomGenerator.getNextDoubleWithPrecision(
                Registry.properties.averageExistentialsChainLength.toDouble(),
                Registry.properties.varianceExistentialsChainLength,
                0
            ).roundToInt()

            existentialEdge.existentialCount++
            existentialEdge.to.minArity++
            existentialEdge.to.maxArity++

            val existentitalTermNode = TermNode(existentialEdge.to, existentialEdge.to.minArity - 1)

            this.addExistentialEdge(termDependencyGraph, existentialEdge)

            val visited = mutableSetOf(existentialEdge.to)

            val createdExistentials = mutableListOf(existentitalTermNode)
            // Propagate chain
            val queue = ArrayDeque<Pair<Node, Int>>()
            queue.add(Pair(existentialEdge.to, 0))
            while (queue.isNotEmpty()) {
                val (node, currentChainLength) = queue.removeFirst()

                // 1. Update termOrder of outgoing edges to include existential term
                dependencyGraph.outEdges[node].orEmpty().forEach { edge ->
                    edge.termOrder = edge.termOrder + listOf(-1)
                }


                // 2. Check number of outgoing edges, if > 1 add to chain with 50% of probability, if empty to choose random one
                if (currentChainLength >= chainLength) {
                    continue
                }
                if (dependencyGraph.outEdges[node].orEmpty().isEmpty()) continue;

                val possibleNextEdges = dependencyGraph.outEdges[node].orEmpty().filter {edge ->
                    if (edge !is UnionEdge) true
                    else {
                        // No self loops on other path
                        val otherEdge = dependencyGraph.inEdges[edge.to].orEmpty().filter {otherEdge -> edge != otherEdge }.first()
                        otherEdge.from != otherEdge.to
                    }
                }
                var nextEdges =
                    possibleNextEdges.filter { RandomGenerator.sharedRandom.nextBoolean() }
                if (nextEdges.isEmpty()) {
                    nextEdges = possibleNextEdges.shuffled(RandomGenerator.sharedRandom).take(1)
                }

                // 3. For each selected edge, propagate exsistential edge
                nextEdges.forEach { nextEdge ->
                    // We only allow each node to be once in the chain
                    val targetNode = nextEdge.to
                    if (visited.contains(targetNode)) {
                        return@forEach
                    }
                    visited.add(targetNode)

                    val allowedTerms = termDependencyGraph.nodes
                        .filter { it.node == targetNode && !nextEdge.termOrder.contains(it.pos) } // We do not allow to reference to an existing position in the current term order
                        .filter {
                            !termDependencyGraph.getReachableNodes(it).any { createdExistentials.contains(it) }
                        } // We are not allowed to reference to a node which closes the cycle
                        .filter {
                            nextEdge !is IntersectionEdge || !allExistentials.contains(it)
                        } // We do not allow to reference to a term which is an existential node
                    if (allowedTerms.isNotEmpty() && RandomGenerator.sharedRandom.nextBoolean() ) { //
                        val targetTerm = allowedTerms.random(RandomGenerator.sharedRandom)
                        if (Registry.debug) {
                            println("Using allowed term $targetTerm for edge $nextEdge")
                        }
                        nextEdge.termOrder =
                            nextEdge.termOrder.take(nextEdge.termOrder.size - 1) + listOf(targetTerm.pos)
                    } else {
                        targetNode.minArity++
                        targetNode.maxArity++

                        nextEdge.termOrder =
                            nextEdge.termOrder.take(nextEdge.termOrder.size - 1) + listOf(targetNode.minArity - 1)

                        // Find other edges in case the node has multiple incoming edges
                        if (nextEdge is UnionEdge) {
                            if (Registry.debug) {
                                println("Union edge $nextEdge")
                            }
                            val otherEdge =
                                dependencyGraph.inEdges[targetNode].orEmpty().filter { it != nextEdge }.first()
                            // Add existential to other edge
                            otherEdge.existentialCount++;
                            val cEN = this.addExistentialEdge(termDependencyGraph, otherEdge)
                            createdExistentials.add(cEN)
                        }

                        queue.add(Pair(targetNode, currentChainLength + 1))
                    }

                    termDependencyGraph.addEdge(
                        TermEdge(
                            TermNode(nextEdge.from, nextEdge.termOrder.size - 1),
                            TermNode(nextEdge.to, nextEdge.termOrder.last()),
                            isAdded = true,
                        )
                    )
                }
            }

            //termDependencyGraph.draw("termDependencyGraph")

            // It is not allowed to have a cycle to the current node, as in such a case an existential edge is not weakly acyclic.
            //println(createdExistentials)
            createdExistentials.forEach {
                assert(!termDependencyGraph.isCyclic(it))
            }
            allExistentials.addAll(createdExistentials)

        }

        return dependencyGraph
    }

    private fun addExistentialEdge(
        termDependencyGraph: GenericMutableDependencyGraph<TermNode, TermEdge>,
        existentialEdge: Edge
    ): TermNode {
        // Update graph
        val existentialTermNode = TermNode(existentialEdge.to, existentialEdge.to.minArity - 1)
        termDependencyGraph.addNode(existentialTermNode)
        termDependencyGraph.nodes.filter { it.node == existentialEdge.from }.forEach { termNode ->
            termDependencyGraph.addEdge(TermEdge(termNode, existentialTermNode, special = true))
        }
        return existentialTermNode
    }

}