package at.ac.tuwien.dbai.kg.iTemporal.core.contracts

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator

/**
 * Contract used for registering an additional category assignment for multi edges.
 */
interface CategoryAssignmentMultiEdge:CategoryAssignment {

    override fun runRuleAssignment(dependencyGraph: DependencyGraph): DependencyGraph {
        val generationOrder = mutableListOf<RuleAssignment>()

        val totalEdges = this.getNumberOfExistingEdges(dependencyGraph)

        for (assigner in this.ruleAssignments) {
            val requiredEdges = assigner.getNumberOfRequiredEdges(dependencyGraph, totalEdges)
            for (i in 0 until requiredEdges) {
                generationOrder.add(assigner)
            }
        }

        generationOrder.shuffle(RandomGenerator.sharedRandom)

        val nodes = this.getGenericCategoryEdges(dependencyGraph).map { it.to }.distinct()

        val mutableDependencyGraph = dependencyGraph.toMutableDependencyGraph()

        for ((i, node) in nodes.withIndex()) {
            val assigner = generationOrder[i]
            assert(mutableDependencyGraph.inEdges[node]!!.size == 2)
            val edge1 = mutableDependencyGraph.inEdges[node]!![0]
            val edge2 = mutableDependencyGraph.inEdges[node]!![1]
            mutableDependencyGraph.removeEdge(edge1)
            mutableDependencyGraph.removeEdge(edge2)
            mutableDependencyGraph.addEdge(assigner.getNewEdge(edge1,true))
            mutableDependencyGraph.addEdge(assigner.getNewEdge(edge2,false))
        }

        return mutableDependencyGraph
    }
}