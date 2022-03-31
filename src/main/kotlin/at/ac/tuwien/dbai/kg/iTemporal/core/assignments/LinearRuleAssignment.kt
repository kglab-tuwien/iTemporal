package at.ac.tuwien.dbai.kg.iTemporal.core.assignments

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericCoreSingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.LinearEdge

/**
 * Converts a given GenericCoreSingleEdge to an LinearEdge
 */
object LinearRuleAssignment : RuleAssignment {
    override fun getPriority(): Int = 21

    override fun getNumberOfExistingEdges(dependencyGraph: DependencyGraph): Int {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<LinearEdge>().count()
    }

    override fun getTypeStatisticProperty(): Double {
        return Registry.properties.linearRules
    }

    override fun getNewEdge(edge: Edge, isFirstEdge: Boolean): Edge {
        if (edge !is GenericCoreSingleEdge) {
            throw RuntimeException("Invalid edge for conversion provided")
        }

        return LinearEdge(
            from = edge.from,
            to = edge.to,
            isCyclic = edge.isCyclic,
            termOrder = edge.termOrder, termOrderShuffleAllowed = edge.termOrderShuffleAllowed, termOrderReference = edge.termOrderReference
        )
    }

}