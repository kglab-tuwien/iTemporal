package at.ac.tuwien.dbai.kg.iTemporal.core.jobs

import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericMultiEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.GraphNormalization
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.EdgeFactory

/**
 * The goal of this normalization step is:
 * * In case there is a node with multiple edges, normalize the type to the specific type given by a single edge.
 */
object DGMultiEdgeOperationNormalizer : GraphNormalization {
    override fun getPriority(): Int = 20

    override fun run(dependencyGraph: DependencyGraph): DependencyGraph {
        val mutableDependencyGraph = dependencyGraph.toMutableDependencyGraph()


        // We only adapt the edge and run over the edges afterwards to update the nodes
        for (node in mutableDependencyGraph.nodes) {

            val inEdges = mutableDependencyGraph.inEdges[node].orEmpty()

            // No change required if at most one incoming edge
            if (inEdges.size <= 1) {
                continue
            }


            val types = inEdges.filter { it !is GenericMultiEdge }.groupBy { it.javaClass.simpleName }

            if (types.isEmpty()) {
                continue
            }

            if (types.size > 1) {
                throw RuntimeException("Multiple edge types for a single node ${node.name} found")
            }


            val addedEdges = mutableListOf<Edge>()
            val removedEdges = mutableListOf<Edge>()

            val type = types.keys.first()

            for (edge in inEdges) {
                if (edge is GenericMultiEdge) {
                    // Create a plain new edge of the same type
                    val convertedEdge = EdgeFactory.create(type, edge.from, edge.to)
                    removedEdges.add(edge)
                    addedEdges.add(convertedEdge)
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