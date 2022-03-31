package at.ac.tuwien.dbai.kg.iTemporal.temporal.assignments

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.CategoryAssignmentSingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.GenericTemporalSingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.TemporalSingleEdge

/**
 * This class provides the details for the temporal category assignment of single-nodes.
 */
object SingleEdgeTemporalCategoryAssignment : CategoryAssignmentSingleEdge {
    override fun getPriority(): Int = 24

    override var ruleAssignments: MutableList<RuleAssignment> = mutableListOf()

    override fun getNumberOfExistingEdges(dependencyGraph: DependencyGraph): Int {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<TemporalSingleEdge>().count()
    }

    override fun getGenericCategoryEdges(dependencyGraph: DependencyGraph): List<Edge> {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<GenericTemporalSingleEdge>()
    }

    override fun getTypeStatisticProperty(): Double {
        return Registry.properties.singleEdgeTemporalRules
    }

    override fun getNewEdge(edge: Edge): Edge {
        return GenericTemporalSingleEdge(
            from = edge.from,
            to = edge.to,
            isCyclic = edge.isCyclic,
            termOrder = edge.termOrder, termOrderShuffleAllowed = edge.termOrderShuffleAllowed, termOrderReference = edge.termOrderReference
        )
    }

}