package at.ac.tuwien.dbai.kg.iTemporal.temporal.jobs

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleDecomposition
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.IntersectionEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.*

/**
 * This job normalizes the since and until edges.
 */
object TemporalNormalization : RuleDecomposition {

    override fun getPriority(): Int = 49

    override fun run(dependencyGraph: DependencyGraph): DependencyGraph {

        val mutableDependencyGraph = dependencyGraph.toMutableDependencyGraph()

        val addedNodes = mutableListOf<Node>()

        // We only adapt the edge and run over the edges afterwards to update the nodes
        for (node in mutableDependencyGraph.nodes) {

            val inEdges = mutableDependencyGraph.inEdges[node].orEmpty()

            if (inEdges.size != 2) {
                continue
            }

            val edge1 = inEdges[0]
            val edge2 = inEdges[1]

            if (edge1 !is TemporalMultiEdge) {
                continue
            }

            if (edge2 !is TemporalMultiEdge) {
                continue
            }
            if (!((edge1 is SinceEdge && edge2 is SinceEdge) || (edge1 is UntilEdge && edge2 is UntilEdge))) {
                continue
            }


            // Remove existing edges
            mutableDependencyGraph.removeEdge(edge1)
            mutableDependencyGraph.removeEdge(edge2)

            // Add new edges  A S B // A U B
            val aEdge = if (edge1.isLeftEdge) edge1 else edge2
            val bEdge = if (edge1.isLeftEdge) edge2 else edge1

            val closeNode =
                Node(NameGenerator.getUniqueName(), minArity = aEdge.from.minArity, maxArity = aEdge.from.maxArity)
            val intersectionNode =
                Node(NameGenerator.getUniqueName(), minArity = aEdge.to.minArity, maxArity = aEdge.to.maxArity)
            val temporalNode = Node(
                NameGenerator.getUniqueName(),
                minArity = intersectionNode.minArity,
                maxArity = intersectionNode.maxArity
            )
            addedNodes.add(closeNode)
            addedNodes.add(intersectionNode)
            addedNodes.add(temporalNode)

            // we keep the order the same
            mutableDependencyGraph.addEdge(
                ClosingEdge(
                    from = aEdge.from,
                    to = closeNode,
                    termOrderShuffleAllowed = false
                )
            )

            val iEdge1= IntersectionEdge(
                from = closeNode,
                to = aEdge.to,
                termOrder = aEdge.termOrder
            )

            mutableDependencyGraph.addEdge(iEdge1)

            mutableDependencyGraph.addEdge(
                IntersectionEdge(
                    from = closeNode,
                    to = intersectionNode,
                    termOrderReference = iEdge1.uniqueId,
                    //termOrder = aEdge.termOrder
                )
            )
            mutableDependencyGraph.addEdge(
                IntersectionEdge(
                    from = bEdge.from,
                    to = intersectionNode,
                    termOrder = bEdge.termOrder
                )
            )
            if (edge1 is SinceEdge) {
                mutableDependencyGraph.addEdge(
                    DiamondMinusEdge(
                        from = intersectionNode,
                        to = temporalNode,
                        t1 = bEdge.t1,
                        t2 = bEdge.t2,
                        termOrderShuffleAllowed = false
                    )
                )
            }
            if (edge1 is UntilEdge) {
                mutableDependencyGraph.addEdge(
                    DiamondPlusEdge(
                        from = intersectionNode,
                        to = temporalNode,
                        t1 = bEdge.t1,
                        t2 = bEdge.t2,
                        termOrderShuffleAllowed = false
                    )
                )
            }

            mutableDependencyGraph.addEdge(IntersectionEdge(temporalNode, bEdge.to, termOrderShuffleAllowed = false))


        }

        mutableDependencyGraph.nodes.addAll(addedNodes)


        return mutableDependencyGraph
    }


}