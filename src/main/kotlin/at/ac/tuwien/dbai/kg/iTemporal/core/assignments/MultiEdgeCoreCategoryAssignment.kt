package at.ac.tuwien.dbai.kg.iTemporal.core.assignments

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.CategoryAssignmentMultiEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.CoreMultiEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericCoreMultiEdge

/**
 * This class provides the details for the core multi edge category assignment.
 */
object MultiEdgeCoreCategoryAssignment : CategoryAssignmentMultiEdge {
    override fun getPriority():Int = 39

    override var ruleAssignments: MutableList<RuleAssignment> = mutableListOf()

    override fun getNumberOfExistingEdges(dependencyGraph: DependencyGraph): Int {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<CoreMultiEdge>().count()/2
    }

    override fun getGenericCategoryEdges(dependencyGraph: DependencyGraph): List<Edge> {
        return dependencyGraph.inEdges.values.flatten().filterIsInstance<GenericCoreMultiEdge>()
    }

    override fun getTypeStatisticProperty(): Double {
        return Registry.properties.coreMultiEdgeRules
    }

    override fun getNewEdge(edge: Edge):Edge {
        return GenericCoreMultiEdge(from=edge.from, to=edge.to,isCyclic = edge.isCyclic, termOrder = edge.termOrder, termOrderShuffleAllowed = edge.termOrderShuffleAllowed, termOrderReference = edge.termOrderReference)
    }

}