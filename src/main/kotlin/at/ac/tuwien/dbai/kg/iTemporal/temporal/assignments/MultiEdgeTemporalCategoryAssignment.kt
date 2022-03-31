package at.ac.tuwien.dbai.kg.iTemporal.temporal.assignments

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.CategoryAssignmentMultiEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.GenericTemporalMultiEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.TemporalMultiEdge

/**
 * This class provides the details for the temporal category assignment of multi-nodes.
 */
object MultiEdgeTemporalCategoryAssignment : CategoryAssignmentMultiEdge {
    override fun getPriority():Int = 18

    override var ruleAssignments: MutableList<RuleAssignment> = mutableListOf()

    override fun getNumberOfExistingEdges(dependencyGraph: DependencyGraph): Int {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<TemporalMultiEdge>().count()/2
    }

    override fun getGenericCategoryEdges(dependencyGraph: DependencyGraph): List<Edge> {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<GenericTemporalMultiEdge>()
    }

    override fun getTypeStatisticProperty(): Double {
        return Registry.properties.multiEdgeTemporalRules
    }

    override fun getNewEdge(edge: Edge):Edge {
        return GenericTemporalMultiEdge(from=edge.from, to=edge.to,isCyclic = edge.isCyclic, termOrder = edge.termOrder, isLeftEdge = true, termOrderShuffleAllowed = edge.termOrderShuffleAllowed, termOrderReference = edge.termOrderReference)
    }

}