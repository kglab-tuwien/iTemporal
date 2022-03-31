package at.ac.tuwien.dbai.kg.iTemporal.temporal.assignments

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.*

/**
 * Converts a given GenericTemporalMultiEdge to a UntilEdge
 */
object UntilRuleAssignment : RuleAssignment {
    override fun getPriority(): Int = 33

    override fun getNumberOfExistingEdges(dependencyGraph: DependencyGraph): Int {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<UntilEdge>().count()/2
    }

    override fun getTypeStatisticProperty(): Double {
        return Registry.properties.untilRules
    }

    override fun getNewEdge(edge: Edge, isFirstEdge: Boolean): Edge {
        if (edge !is GenericTemporalMultiEdge) {
            throw RuntimeException("Invalid edge for conversion provided")
        }

        return UntilEdge(
            from = edge.from,
            to = edge.to,
            isCyclic = edge.isCyclic,
            termOrder = edge.termOrder,
            t1 = edge.t1,
            t2 = edge.t2,
            isLeftEdge = isFirstEdge, termOrderShuffleAllowed = edge.termOrderShuffleAllowed, termOrderReference = edge.termOrderReference
        )
    }

}