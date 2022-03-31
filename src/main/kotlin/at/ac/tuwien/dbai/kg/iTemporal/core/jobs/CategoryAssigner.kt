package at.ac.tuwien.dbai.kg.iTemporal.core.jobs

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.CategoryAssignmentMultiEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.CategoryAssignmentSingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.MultiEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.SingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericMultiEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericSingleEdge

/**
 * This job handles the assignment of the category edges to the generic edges.
 */
object CategoryAssigner {

    fun run(dependencyGraph: DependencyGraph):DependencyGraph {
        val singleEdgeGenerationOrder = mutableListOf<CategoryAssignmentSingleEdge>()
        val multiEdgeGenerationOrder = mutableListOf<CategoryAssignmentMultiEdge>()

        val totalSingleEdges = dependencyGraph.inEdges.values.flatten().filterIsInstance<SingleEdge>().count()
        val totalMultiEdges = dependencyGraph.inEdges.values.flatten().filterIsInstance<MultiEdge>().count()/2

        for (assigner in Registry.getCategoryAssignmentsSingleEdge()) {
            val requiredEdges = assigner.getNumberOfRequiredEdges(dependencyGraph, totalSingleEdges)
            for (i in 0 until requiredEdges) {
                singleEdgeGenerationOrder.add(assigner)
            }
        }

        for (assigner in Registry.getCategoryAssignmentsMultiEdge()) {
            val requiredEdges = assigner.getNumberOfRequiredEdges(dependencyGraph, totalMultiEdges)
            for (i in 0 until requiredEdges) {
                multiEdgeGenerationOrder.add(assigner)
            }
        }

        singleEdgeGenerationOrder.shuffle()
        multiEdgeGenerationOrder.shuffle()

        val singleEdges = dependencyGraph.inEdges.values.flatten().filterIsInstance<GenericSingleEdge>()
        val multiNodes = dependencyGraph.inEdges.values.flatten().filterIsInstance<GenericMultiEdge>().map { it.to }.distinct()

        val mutableDependencyGraph = dependencyGraph.toMutableDependencyGraph()

        for ((i, edge) in singleEdges.withIndex()) {
            val assigner = singleEdgeGenerationOrder[i]
            mutableDependencyGraph.removeEdge(edge)
            mutableDependencyGraph.addEdge(assigner.getNewEdge(edge))
        }

        for ((i, node) in multiNodes.withIndex()) {
            val assigner = multiEdgeGenerationOrder[i]
            assert(mutableDependencyGraph.inEdges[node]!!.size == 2)
            val edge1 = mutableDependencyGraph.inEdges[node]!![0]
            val edge2 = mutableDependencyGraph.inEdges[node]!![1]
            mutableDependencyGraph.removeEdge(edge1)
            mutableDependencyGraph.removeEdge(edge2)
            mutableDependencyGraph.addEdge(assigner.getNewEdge(edge1))
            mutableDependencyGraph.addEdge(assigner.getNewEdge(edge2))
        }

        return mutableDependencyGraph
    }
}