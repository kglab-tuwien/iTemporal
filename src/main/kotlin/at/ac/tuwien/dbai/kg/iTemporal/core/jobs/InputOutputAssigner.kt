package at.ac.tuwien.dbai.kg.iTemporal.core.jobs

import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.OtherPropertyAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.NodeType


/**
 * This job assigns the input and output type to each node, if not provided correctly.
 */
object InputOutputAssigner:OtherPropertyAssignment {

    override fun run(dependencyGraph: DependencyGraph): DependencyGraph {

        val nodesWithoutIngoingEdges = dependencyGraph.nodes.filter { dependencyGraph.inEdges[it].orEmpty().isEmpty() }
        val nodesWithoutOutgoingEdges =
            dependencyGraph.nodes.filter { dependencyGraph.outEdges[it].orEmpty().isEmpty() }

        if (nodesWithoutIngoingEdges.isEmpty()) {
            throw RuntimeException("No input node detected. Graph has no node containing no ingoing edges")
        }

        if (nodesWithoutOutgoingEdges.isEmpty()) {
            throw RuntimeException("No output node detected. Graph has no node containing no outgoing edges")
        }

        for (node in nodesWithoutOutgoingEdges) {
            node.type = NodeType.Output
        }

        for (node in nodesWithoutIngoingEdges) {
            node.type = NodeType.Input
        }

        return dependencyGraph
    }

    override fun getPriority(): Int = 5

}