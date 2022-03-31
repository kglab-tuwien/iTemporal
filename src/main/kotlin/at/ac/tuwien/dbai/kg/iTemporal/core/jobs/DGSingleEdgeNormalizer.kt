package at.ac.tuwien.dbai.kg.iTemporal.core.jobs

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericMultiEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.GraphNormalization
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.SingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node

/**
 * The goal of this normalization step is:
 * * Each SingleEdge where a MultiEdge is required will be transformed by adding a middle node
 */
object DGSingleEdgeNormalizer : GraphNormalization {
    override fun getPriority(): Int = 10

    override fun run(dependencyGraph: DependencyGraph): DependencyGraph {
        val mutableDependencyGraph = dependencyGraph.toMutableDependencyGraph()


        val addedNodes = mutableListOf<Node>()

        // We only adapt the edge and run over the edges afterwards to update the nodes
        for (node in mutableDependencyGraph.nodes) {

            val inEdges = mutableDependencyGraph.inEdges[node].orEmpty()

            // No change required if at most one incoming edge
            if (inEdges.size <= 1) {
                continue
            }

            val addedEdges = mutableListOf<Edge>()
            val removedEdges = mutableListOf<Edge>()

            for (edge in inEdges) {
                if (edge is SingleEdge) {
                    // Normalize edge, i.e., insert a node, which edge before is this edge and the edge afterwards has an unknown type
                    val middleNode = Node(NameGenerator.getUniqueName())
                    addedNodes.add(middleNode)

                    // Source -> Middle
                    val edge1 = edge.copy()
                    edge1.to=middleNode


                    val edge2 = GenericMultiEdge(from=middleNode,to=edge.to)
                    // Do not change anything in the term order for the inserted node
                    edge2.termOrderShuffleAllowed = false

                    removedEdges.add(edge)
                    addedEdges.add(edge1)
                    addedEdges.add(edge2)

                }
            }

            for(edge in addedEdges) {
                mutableDependencyGraph.addEdge(edge)
            }
            for(edge in removedEdges) {
                mutableDependencyGraph.removeEdge(edge)
            }
        }

        mutableDependencyGraph.nodes.addAll(addedNodes)


        return mutableDependencyGraph
    }
}