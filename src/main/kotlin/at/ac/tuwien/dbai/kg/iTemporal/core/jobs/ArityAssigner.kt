package at.ac.tuwien.dbai.kg.iTemporal.core.jobs

import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraphHelper
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.NodeType
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.IntersectionEdge
import kotlin.math.max
import kotlin.math.min

/**
 * This job handles the logic of the arity assignment by iterating over the registerd functions
 */
object ArityAssigner {

    fun updateWithReferenceEdge(edge: Edge, dependencyGraph: DependencyGraph): Boolean {
        if (edge.termOrderReference != null) {
            val referenceEdge = dependencyGraph.inEdges.values.flatten().first { it.uniqueId == edge.termOrderReference }

            val oldEdgeMinArityFrom = edge.from.minArity
            val oldEdgeMinArityTo = edge.to.minArity
            val oldEdgeMaxArityFrom = edge.from.maxArity
            val oldEdgeMaxArityTo = edge.to.maxArity

            val oldReferenceMinArityFrom = referenceEdge.from.minArity
            val oldReferenceMinArityTo = referenceEdge.to.minArity
            val oldReferenceMaxArityFrom = referenceEdge.from.maxArity
            val oldReferenceMaxArityTo = referenceEdge.to.maxArity


            // Sync arity of both nodes
            edge.from.minArity = max(edge.from.minArity, referenceEdge.from.minArity)
            edge.to.minArity = max(edge.to.minArity, referenceEdge.to.minArity)
            edge.from.maxArity = min(edge.from.maxArity, referenceEdge.from.maxArity)
            edge.to.maxArity = min(edge.to.maxArity, referenceEdge.to.maxArity)
            referenceEdge.from.minArity = edge.from.minArity
            referenceEdge.from.maxArity = edge.from.maxArity
            referenceEdge.to.minArity = edge.to.minArity
            referenceEdge.to.maxArity = edge.to.maxArity


            if (oldEdgeMinArityFrom != edge.from.minArity) {
                return true
            }

            if (oldEdgeMinArityTo != edge.to.minArity) {
                return true
            }

            if (oldEdgeMaxArityFrom != edge.from.maxArity) {
                return true
            }

            if (oldEdgeMaxArityTo != edge.to.maxArity) {
                return true
            }

            if (oldReferenceMinArityFrom != referenceEdge.from.minArity) {
                return true
            }

            if (oldReferenceMinArityTo != referenceEdge.to.minArity) {
                return true
            }

            if (oldReferenceMaxArityFrom != referenceEdge.from.maxArity) {
                return true
            }

            if (oldReferenceMaxArityTo != referenceEdge.to.maxArity) {
                return true
            }

        }

        return false
    }



    fun run(dependencyGraph: DependencyGraph): DependencyGraph {

        val sCCs = DependencyGraphHelper.calculateSCC(dependencyGraph)

        val sccOrder = DependencyGraphHelper.createSCCOrder(dependencyGraph, sCCs)
        val sccLookup = sCCs.flatMapIndexed { index, nodes -> nodes.map { Pair(it, index) } }.toMap()

        // Set sccIds for nodes
        for (x in sccLookup) {
            x.key.sccId = x.value
        }

        // Set cyclic information to edges
        for (edge in dependencyGraph.inEdges.values.flatten()) {
            // Set cyclic information
            edge.isCyclic = (sccLookup[edge.from] == sccLookup[edge.to])
        }

        // Set init arity values if not set
        for (node in dependencyGraph.nodes) {
            if (node.maxArity < 0) {
                node.maxArity = Int.MAX_VALUE
            }
            if (node.minArity < 0) {
                node.minArity = -1
            }
        }

        for (sccId in sccOrder) {
            val nodes: List<Node> = sCCs[sccId]

            // Init node arity of SCC
            for (node in nodes) {
                if (node.type == NodeType.Output) {
                    if (node.minArity == -1) {
                        node.minArity = RandomGenerator.getNextArityWith0(
                            Registry.properties.averageOutputArity,
                            Registry.properties.varianceOutputArity
                        )
                    }
                    node.maxArity = node.minArity
                    continue
                }
            }


            // Start the update process
            var changed = true

            while (changed) {
                changed = false

                // Max und Min Arity propagation
                for (node in nodes) {
                    // Maximum is forward
                    val maxArity: Int = if (dependencyGraph.inEdges[node].orEmpty().any { it is IntersectionEdge }) {
                        min(Int.MAX_VALUE.toLong(),
                            dependencyGraph.inEdges[node].orEmpty().sumOf { it.from.maxArity.toLong() }).toInt()
                    } else {
                        // We only consider max arity from edges from the same component, it cannot get lower than the current minArity
                        dependencyGraph.inEdges[node].orEmpty()
                            .filter { it.from.sccId == node.sccId }
                            .map { it.from.maxArity + it.getNumberOfAdditionalTerms() }.minOrNull() ?: node.maxArity
                    }

                    if (maxArity < node.maxArity) {
                        node.maxArity = maxArity
                        changed = true
                    }
                    // Minimum is backward
                    for (outEdge in dependencyGraph.outEdges[node].orEmpty()) {
                        val minArity: Int = if (outEdge is IntersectionEdge) {
                            outEdge.to.minArity - dependencyGraph.inEdges[outEdge.to].orEmpty()
                                .first { it != outEdge }.from.maxArity
                        } else {
                            outEdge.to.minArity - outEdge.getNumberOfAdditionalTerms()
                        }

                        if (minArity > node.minArity) {
                            node.minArity = minArity
                            changed = true
                        }
                    }

                }


                if (changed) {
                    continue
                }

                // Reference Arity update
                for (edge in dependencyGraph.inEdges.values.flatten()) {
                    changed = updateWithReferenceEdge(edge, dependencyGraph)
                }

                if (changed) {
                    continue
                }


                // Edge Selection A Scripts
                for (propertyAssignment in Registry.getPropertyAssignmentsA()) {
                    changed = propertyAssignment.run(nodes, dependencyGraph)
                    if (changed) {
                        break
                    }
                }

                if (changed) {
                    continue
                }

                // Arity Selection Phase A: Select a node which edge is connected to a different SCC with highest minArity.
                val relevantNodes = nodes.filter { it.maxArity == Int.MAX_VALUE }.filter { node ->
                    dependencyGraph.outEdges[node].orEmpty().map { edge -> sccLookup[edge.to] }.any { it != sccId }
                }
                if (relevantNodes.isNotEmpty()) {
                    val selectedNode = relevantNodes.maxByOrNull { it.minArity }!!
                    selectedNode.maxArity = selectedNode.minArity
                    changed = true
                    continue
                }

                // Edge Selection B Scripts
                for (propertyAssignment in Registry.getPropertyAssignmentsB()) {
                    changed = propertyAssignment.run(nodes, dependencyGraph)
                    if (changed) {
                        break
                    }
                }

                if (changed) {
                    continue
                }

                // Arity Selection Phase B
                val relevantNodes2 = nodes.filter { it.minArity < it.maxArity }

                // We first try to fix those nodes which are connected to next SCC
                val relevantNodes3 = relevantNodes2.filter { node ->
                    dependencyGraph.outEdges[node].orEmpty().map { edge -> sccLookup[edge.to] }.any { it != sccId }
                }

                if (relevantNodes3.isNotEmpty()) {
                    val selectedNode = relevantNodes3.maxByOrNull { it.minArity }!!
                    selectedNode.maxArity = selectedNode.minArity
                    changed = true
                    continue
                }

                // If there is no node remaining, we select some SCC
                if (relevantNodes2.isNotEmpty()) {
                    val selectedNode = relevantNodes2.maxByOrNull { it.minArity }!!
                    selectedNode.maxArity = selectedNode.minArity
                    changed = true
                    continue
                }
            }

            // If it is a single node, we may want to increase the minimum and maximum arity. Keep attention for intersections, etc.
            //if (nodes.size == 1) {
            // change +0 for increasing the arity
            //nodes[0].maxArity = nodes[0].minArity + 0
            //continue
            //}
        }

        return dependencyGraph
    }

}