package at.ac.tuwien.dbai.kg.iTemporal.temporal.assignments

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.BoxPlusEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.GenericTemporalSingleEdge

/**
 * Converts a given GenericTemporalSingleEdge to a BoxPlusEdge
 */
object BoxPlusRuleAssignment : RuleAssignment {
    override fun getPriority(): Int = 9

    override fun getNumberOfExistingEdges(dependencyGraph: DependencyGraph): Int {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<BoxPlusEdge>().count()
    }

    override fun getTypeStatisticProperty(): Double {
        return Registry.properties.boxPlusRules
    }

    override fun getNewEdge(edge: Edge, isFirstEdge: Boolean): Edge {
        if (edge !is GenericTemporalSingleEdge) {
            throw RuntimeException("Invalid edge for conversion provided")
        }

        return BoxPlusEdge(
            from = edge.from,
            to = edge.to,
            isCyclic = edge.isCyclic,
            termOrder = edge.termOrder,
            t1 = edge.t1,
            t2 = edge.t2, termOrderShuffleAllowed = edge.termOrderShuffleAllowed, termOrderReference = edge.termOrderReference
        )
    }

}