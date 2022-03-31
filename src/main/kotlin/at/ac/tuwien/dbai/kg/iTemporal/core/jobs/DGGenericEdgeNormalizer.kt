package at.ac.tuwien.dbai.kg.iTemporal.core.jobs

import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.GraphNormalization
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.MultiEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.SingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericEdge

/**
 * The goal of this normalization step is:
 * 1. Each GenericEdge which target node has a single incoming edge is a GenericSingleEdge
 * 2. Each GenericEdge which target node has multiple incoming edges is a GenericMultiEdge
 */
object DGGenericEdgeNormalizer : GraphNormalization {
    override fun getPriority(): Int = 1

    override fun run(dependencyGraph: DependencyGraph): DependencyGraph {
        val mutableDependencyGraph = dependencyGraph.toMutableDependencyGraph()




        // We only adapt the edge and run over the edges afterwards to update the nodes
        for (node in mutableDependencyGraph.nodes) {
            // No change required if at most one incoming edge
            val inEdges = mutableDependencyGraph.inEdges[node].orEmpty()

            if (inEdges.isEmpty()) {
                continue
            }

            val addedEdges = mutableListOf<Edge>()
            val removedEdges = mutableListOf<Edge>()

            for (edge in inEdges) {
                if (inEdges.size == 1) {

                    if (edge is SingleEdge) {
                        continue
                    }
                    if (edge is MultiEdge) {
                        throw RuntimeException("Invalid incoming edge type for node ${node.name}")
                    }
                    // It must be a generic edge
                    if (edge !is GenericEdge) {
                        throw RuntimeException("Invalid edge type for node ${node.name}")
                    }

                    val singleEdge = edge.toGenericSingleEdge()
                    removedEdges.add(edge)
                    addedEdges.add(singleEdge)

                } else {
                    if (edge is MultiEdge) {
                        continue
                    }

                    // We do not throw an exception as this can be converted in a later step.
                    /*if (edge is SingleEdge) {
                        throw RuntimeException("Invalid incoming edge type for node ${node.name}")
                    }*/
                    if (edge is SingleEdge) {
                        continue
                    }

                    // It must be a generic edge
                    if (edge !is GenericEdge) {
                        throw RuntimeException("Invalid edge type for node ${node.name}")
                    }

                    val multiEdge = edge.toGenericMultiEdge()
                    removedEdges.add(edge)
                    addedEdges.add(multiEdge)
                }
            }

            for(edge in addedEdges) {
                mutableDependencyGraph.addEdge(edge)
            }

            for(edge in removedEdges) {
                mutableDependencyGraph.removeEdge(edge)
            }
        }

        return mutableDependencyGraph
    }
}