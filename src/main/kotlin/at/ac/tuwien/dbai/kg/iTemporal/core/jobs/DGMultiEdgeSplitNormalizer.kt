package at.ac.tuwien.dbai.kg.iTemporal.core.jobs

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.GraphNormalization
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.EdgeFactory
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node

/**
 * The goal of this normalization step is:
 * * In case there is a node with multiple edges, normalize the type to the specific type given by a single edge.
 */
object DGMultiEdgeSplitNormalizer : GraphNormalization {
    override fun getPriority(): Int = 30

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

            val type = inEdges.first().javaClass.simpleName

            // Always combine two edges together
            /* This looks as follows for 4 edges: N:-a,b,c,d
                    a---|
                    b---N1---|
                    c-------N2---|
                    d------------N
             */


            while (inEdges.size > 2) {
                val edgeX = inEdges[0]
                val edgeY = inEdges[1]

                val middleNode = Node(NameGenerator.getUniqueName())

                val edgeXNew = edgeX.copy()
                edgeXNew.to=middleNode
                val edgeYNew = edgeY.copy()
                edgeYNew.to=middleNode

                // Remove old and add new edges
                addedNodes.add(middleNode)
                mutableDependencyGraph.removeEdge(edgeX)
                mutableDependencyGraph.removeEdge(edgeY)
                mutableDependencyGraph.addEdge(edgeXNew)
                mutableDependencyGraph.addEdge(edgeYNew)


                // Insert new outgoing edge to middleNode
                val edgeAfter = EdgeFactory.create(type, middleNode, node)
                mutableDependencyGraph.addEdge(edgeAfter)
            }
        }
        mutableDependencyGraph.nodes.addAll(addedNodes)


        return mutableDependencyGraph
    }
}