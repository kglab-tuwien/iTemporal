package at.ac.tuwien.dbai.kg.iTemporal.aggregation.assignments

import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.GenericAggregationEdge
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.MWTAEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph


/**
 * Converts a given GenericAggregationEdge to a MWTAEdge.
 */
object MWTARuleAssignment : RuleAssignment {
    override fun getPriority(): Int = 53

    override fun getNumberOfExistingEdges(dependencyGraph: DependencyGraph): Int {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<MWTAEdge>().count()
    }

    override fun getTypeStatisticProperty(): Double {
        return Registry.properties.movingWindowTemporalAggregationRules
    }

    override fun getNewEdge(edge: Edge, isFirstEdge: Boolean): Edge {
        if (edge !is GenericAggregationEdge) {
            throw RuntimeException("Invalid edge for conversion provided")
        }

        return MWTAEdge(
            from = edge.from,
            to = edge.to,
            isCyclic = edge.isCyclic,
            termOrder = edge.termOrder,
            numberOfGroupingTerms = edge.numberOfGroupingTerms,
            numberOfContributors = edge.numberOfContributors,
            aggregationType = edge.aggregationType
            , termOrderShuffleAllowed = edge.termOrderShuffleAllowed, termOrderReference = edge.termOrderReference
        )
    }

}