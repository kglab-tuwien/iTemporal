package at.ac.tuwien.dbai.kg.iTemporal.aggregation.jobs

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.AggregationEdge
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.ITAEdge
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.MWTAEdge
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.STAEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleDecomposition
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.DiamondMinusEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.TriangleUpEdge

/**
 * This class converts STAEdge and MWTAEdge to ITAEdge and the other required edges.
 */
object AggregationNormalization:RuleDecomposition {

    override fun getPriority(): Int = 48

    override fun run(dependencyGraph: DependencyGraph): DependencyGraph {

        val mutableDependencyGraph = dependencyGraph.toMutableDependencyGraph()

        val addedNodes = mutableListOf<Node>()

        // We only adapt the edge and run over the edges afterwards to update the nodes
        for (node in mutableDependencyGraph.nodes) {


            // No change required if it is not an aggregation edge
            val inEdges = mutableDependencyGraph.inEdges[node].orEmpty()

            if (inEdges.size != 1) {
                continue
            }

            val edge = inEdges[0]

            if (edge !is AggregationEdge) {
                continue
            }

            // Already correct type
            if (edge is ITAEdge) {
                continue
            }

            val middleNode = Node(NameGenerator.getUniqueName())
            addedNodes.add(middleNode)

            // Remove existing edge
            mutableDependencyGraph.removeEdge(edge)



            if (edge is STAEdge) {
                // Source -> Middle
                val edge1 = TriangleUpEdge(from=edge.from, to=middleNode,unit=edge.unit)
                mutableDependencyGraph.removeEdge(edge)
                mutableDependencyGraph.addEdge(edge1)
            }

            if (edge is MWTAEdge) {
                // Source -> Middle
                val edge1 = DiamondMinusEdge(from=edge.from, to=middleNode,t1=edge.t1,t2=edge.t2)
                mutableDependencyGraph.addEdge(edge1)
            }

            // Middle -> Target
            val edge2 = ITAEdge(
                from=middleNode,
                to=edge.to,
                numberOfContributors = edge.numberOfContributors,
                numberOfGroupingTerms = edge.numberOfGroupingTerms,
                aggregationType = edge.aggregationType
            )
            mutableDependencyGraph.addEdge(edge2)
        }

        mutableDependencyGraph.nodes.addAll(addedNodes)


        return mutableDependencyGraph
    }


}