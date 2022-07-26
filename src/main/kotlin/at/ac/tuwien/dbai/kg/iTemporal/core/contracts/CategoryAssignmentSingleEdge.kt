package at.ac.tuwien.dbai.kg.iTemporal.core.contracts

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator

/**
 * Contract used for registering an additional category assignment for single edges.
 */
interface CategoryAssignmentSingleEdge:CategoryAssignment {

    override fun runRuleAssignment(dependencyGraph: DependencyGraph):DependencyGraph {
        val generationOrder = mutableListOf<RuleAssignment>()

        val totalEdges = this.getNumberOfExistingEdges(dependencyGraph)

        for (assigner in this.ruleAssignments) {
            val requiredEdges = assigner.getNumberOfRequiredEdges(dependencyGraph, totalEdges)
            for (i in 0 until requiredEdges) {
                generationOrder.add(assigner)
            }
        }

        generationOrder.shuffle(RandomGenerator.sharedRandom)

        val edges = this.getGenericCategoryEdges(dependencyGraph)

        val mutableDependencyGraph = dependencyGraph.toMutableDependencyGraph()

        for ((i, edge) in edges.withIndex()) {
            val assigner = generationOrder[i]
            mutableDependencyGraph.removeEdge(edge)
            mutableDependencyGraph.addEdge(assigner.getNewEdge(edge, true))
        }

        return mutableDependencyGraph
    }
}