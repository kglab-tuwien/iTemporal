package at.ac.tuwien.dbai.kg.iTemporal.core.assignments

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericCoreMultiEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.UnionEdge

/**
 * Converts a given GenericCoreMultiEdge to an UnionEdge
 */
object UnionRuleAssignment : RuleAssignment {
    override fun getPriority(): Int = 36

    override fun getNumberOfExistingEdges(dependencyGraph: DependencyGraph): Int {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<UnionEdge>().count()/2
    }

    override fun getTypeStatisticProperty(): Double {
        return Registry.properties.unionRules
    }

    override fun getNewEdge(edge: Edge, isFirstEdge: Boolean): Edge {
        if (edge !is GenericCoreMultiEdge) {
            throw RuntimeException("Invalid edge for conversion provided")
        }

        return UnionEdge(
            from = edge.from,
            to = edge.to,
            isCyclic = edge.isCyclic,
            termOrder = edge.termOrder, termOrderShuffleAllowed = edge.termOrderShuffleAllowed, termOrderReference = edge.termOrderReference
        )
    }

}