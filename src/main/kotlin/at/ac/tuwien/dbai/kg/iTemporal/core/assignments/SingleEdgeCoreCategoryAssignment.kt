package at.ac.tuwien.dbai.kg.iTemporal.core.assignments

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.CategoryAssignmentSingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.CoreSingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericCoreSingleEdge

/**
 * This class provides the details for the core single edge category assignment.
 */
object SingleEdgeCoreCategoryAssignment : CategoryAssignmentSingleEdge {
    override fun getPriority(): Int = 140

    override var ruleAssignments: MutableList<RuleAssignment> = mutableListOf()

    override fun getNumberOfExistingEdges(dependencyGraph: DependencyGraph): Int {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<CoreSingleEdge>().count()
    }

    override fun getGenericCategoryEdges(dependencyGraph: DependencyGraph): List<Edge> {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<GenericCoreSingleEdge>()
    }

    override fun getTypeStatisticProperty(): Double {
        return Registry.properties.coreSingleEdgeRules
    }

    override fun getNewEdge(edge: Edge): Edge {
        return GenericCoreSingleEdge(
            from = edge.from,
            to = edge.to,
            isCyclic = edge.isCyclic,
            termOrder = edge.termOrder, termOrderShuffleAllowed = edge.termOrderShuffleAllowed, termOrderReference = edge.termOrderReference
        )
    }

}