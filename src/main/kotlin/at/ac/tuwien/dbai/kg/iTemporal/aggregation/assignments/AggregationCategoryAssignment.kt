package at.ac.tuwien.dbai.kg.iTemporal.aggregation.assignments

import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.AggregationEdge
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.GenericAggregationEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.CategoryAssignmentSingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph

/**
 * This class provides the details for the aggregation category assignment.
 */
object AggregationCategoryAssignment : CategoryAssignmentSingleEdge {
    override fun getPriority():Int = 59

    override var ruleAssignments: MutableList<RuleAssignment> = mutableListOf()


    override fun getNumberOfExistingEdges(dependencyGraph: DependencyGraph): Int {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<AggregationEdge>().count()
    }

    override fun getGenericCategoryEdges(dependencyGraph: DependencyGraph): List<Edge> {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<GenericAggregationEdge>()
    }

    override fun getTypeStatisticProperty(): Double {
        return Registry.properties.aggregationRules
    }

    override fun getNewEdge(edge: Edge):Edge {
        return GenericAggregationEdge(from=edge.from, to=edge.to, isCyclic = edge.isCyclic, termOrder = edge.termOrder, termOrderShuffleAllowed = edge.termOrderShuffleAllowed, termOrderReference = edge.termOrderReference)
    }

}