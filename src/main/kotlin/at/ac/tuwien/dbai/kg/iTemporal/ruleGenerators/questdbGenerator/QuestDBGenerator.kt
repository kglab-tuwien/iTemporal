package at.ac.tuwien.dbai.kg.iTemporal.ruleGenerators.questdbGenerator

import at.ac.tuwien.dbai.kg.iTemporal.aggregation.AggregationType
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.AggregationEdge
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.ITAEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleGeneration
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.MutableDependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.NodeType
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.IntersectionEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.UnionEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.TriangleUnit
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.TemporalSingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.TriangleUpEdge
import java.io.File
import kotlin.RuntimeException


/**
 * We only support:
 * SELECT a,b,c,aggr()
 * FROM table1
 * SAMPLE BY UNITLENGTH UNIT
 */
object QuestDBGenerator:RuleGeneration {
    override fun getPriority(): Int =667

    override fun getLanguage(): String {
        return "QuestDB"
    }

    data class MetaNode(val name:String, val terms:List<String>)

    override fun convert(dependencyGraph: DependencyGraph): DependencyGraph {
        val outputNodes = dependencyGraph.nodes.filter { it.type == NodeType.Output }
        val inputNodes = dependencyGraph.nodes.filter { it.type == NodeType.Input }

        if (outputNodes.size != 1) {
            throw RuntimeException("Invalid number of outputs for QuestDBGenerator")
        }

        if(dependencyGraph.inEdges.values.flatten().any { it is IntersectionEdge }) {
            throw RuntimeException("QuestDBGenerator does not support joins")
        }

        if(dependencyGraph.inEdges.values.flatten().any { it is UnionEdge }) {
            throw RuntimeException("QuestDBGenerator does not support unions")
        }

        if(dependencyGraph.inEdges.values.flatten().any { it is TemporalSingleEdge }) {
            throw RuntimeException("QuestDBGenerator does not support temporal single edges")
        }

        val sampleByEdges = dependencyGraph.inEdges.values.flatten().filterIsInstance<TriangleUpEdge>().map { Pair(it.unit,1) }.distinct()

        if(sampleByEdges.count() > 1) {
            throw RuntimeException("QuestDBGenerator does not allow multiple sample times")
        }

        if(dependencyGraph.inEdges.values.flatten().any { it is AggregationEdge && it.numberOfContributors > 0 }) {
            throw RuntimeException("QuestDBGenerator does not support contributor terms")
        }

        val queue = inputNodes.toMutableList()

        val nodeToMetaNode = mutableMapOf<Node,MetaNode>()


        // Pass term variables
        while(queue.isNotEmpty()) {
            val node = queue.removeFirst()

            var containsAll = true

            val inEdges = dependencyGraph.inEdges[node].orEmpty()
            // Check if all defined
            for (inEdge in inEdges) {
                if (!nodeToMetaNode.contains(inEdge.from)) {
                    containsAll = false
                    break
                }
            }

            // Add at end
            if (!containsAll) {
                queue.add(node)
            }

            // Input node
            if (inEdges.isEmpty()) {
                val terms = Array(node.minArity +2) { index -> node.name+".i"+index }.toList()
                nodeToMetaNode[node] = MetaNode(node.name, terms)
                dependencyGraph.outEdges[node].orEmpty().forEach {
                    if (!queue.contains(it.to)) {
                        queue.add(it.to)
                    }
                }
                continue
            }

            val terms = Array(node.minArity +1) { "" }.toMutableList()
            for (inEdge in inEdges) {


                for ((fromIndex, orderId) in inEdge.termOrder.withIndex()) {
                    // Not relevant term
                    if (orderId < 0 || orderId >= inEdge.to.minArity) {
                        continue
                    }
                    if (orderId < inEdge.to.minArity) {
                        terms[orderId] = nodeToMetaNode[inEdge.from]!!.terms[fromIndex]
                    }
                }

                // Special Handling for aggregation term
                if(inEdge is ITAEdge) {
                    val aggregationIndex = node.minArity - 1
                    val aggregateFromIndex = inEdge.termOrder.withIndex()
                        .first { it.value == inEdge.getAggregationTermFromIndex() }.index

                    val aggregationTerm:String = nodeToMetaNode[inEdge.from]!!.terms[aggregateFromIndex]

                    terms[aggregationIndex] = when(inEdge.aggregationType){
                        AggregationType.Min -> "min($aggregationTerm)"
                        AggregationType.Max -> "max($aggregationTerm)"
                        AggregationType.Count -> "count()"
                        AggregationType.Sum -> "sum($aggregationTerm)"
                        else -> throw RuntimeException("Aggregation Type not supported")
                    }

                }
                // Copy temporal terms (Timepoints allowed, hence only first)
                terms[inEdge.to.minArity] = nodeToMetaNode[inEdge.from]!!.terms[inEdge.from.minArity]
                //terms[node.minArity+1] = nodeToMetaNode[inEdge.from]!!.terms[inEdge.from.minArity+1]


            }
            nodeToMetaNode[node] = MetaNode(node.name, terms)

            dependencyGraph.outEdges[node].orEmpty().forEach {
                if (!queue.contains(it.to)) {
                    queue.add(it.to)
                }
            }
        }
        val newDependencyGraph = MutableDependencyGraph()


        var currentNode = Node()
        var nextNode = Node()

        newDependencyGraph.nodes.add(currentNode)

        inputNodes.forEach {
            val edge = FromEdge(currentNode, nextNode, it.name)
            newDependencyGraph.nodes.add(nextNode)
            newDependencyGraph.addEdge(edge)
            currentNode = nextNode
            nextNode = Node()
        }

        outputNodes.forEach {
            // Add output terms
            val metaNode = nodeToMetaNode[it]!!
            for (term in metaNode.terms) {
                val edge = SelectEdge(currentNode, nextNode, term)
                newDependencyGraph.nodes.add(nextNode)
                newDependencyGraph.addEdge(edge)
                currentNode = nextNode
                nextNode = Node()
            }
        }

        sampleByEdges.forEach {
            val edge = SampleByEdge(currentNode, nextNode, it.first, it.second)
            newDependencyGraph.nodes.add(nextNode)
            newDependencyGraph.addEdge(edge)
            currentNode = nextNode
            nextNode = Node()
        }


        return newDependencyGraph

    }

    override fun run(dependencyGraph: DependencyGraph, storeFile:Boolean): String {
        val path: File = Registry.properties.path

        if (storeFile) {
            if (!path.isDirectory) {
                throw RuntimeException("Illegal argument, no folder provided")
            }
        }

        val selectTerms = mutableListOf<String>()
        val fromTables = mutableListOf<String>()
        val sampleByTerms = mutableListOf<String>()

        var current:Node = dependencyGraph.nodes.filter { dependencyGraph.inEdges[it].orEmpty().isEmpty() }.first()

        while(true) {
            val edges = dependencyGraph.outEdges[current].orEmpty()
            if (edges.isEmpty()) {
                break
            } else {
                val edge = edges.first()
                when(edge) {
                    is FromEdge -> fromTables.add(edge.tableName)
                    is SelectEdge -> selectTerms.add(edge.termName)
                    is SampleByEdge -> sampleByTerms.add(when(edge.unit) {
                        TriangleUnit.Month -> edge.unitLength.toString() + "M"
                        TriangleUnit.Day -> edge.unitLength.toString() + "D"
                        TriangleUnit.Week -> (7*edge.unitLength).toString() + "D"
                        TriangleUnit.Year -> (12*edge.unitLength).toString() + "M"
                        TriangleUnit.Unknown -> throw RuntimeException("Invalid unit")
                    })
                }
                current = edge.to
            }
        }

        val query = "SELECT " + selectTerms.joinToString(", ") + "\n" +
                "FROM " + fromTables.joinToString(", ") + "\n" +
                if(sampleByTerms.size > 0) ("SAMPLE BY " + sampleByTerms.joinToString(", ") + " ALIGN TO CALENDAR WITH OFFSET '00:00'\n") else "" +
                ";"

        if (storeFile) {
            path.resolve("questdb.txt").writeText(query)
        }
        return query
    }

}