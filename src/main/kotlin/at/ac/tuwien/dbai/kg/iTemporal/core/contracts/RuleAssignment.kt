package at.ac.tuwien.dbai.kg.iTemporal.core.contracts

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import kotlin.math.floor

/**
 * Contract used for registering an additional rule assignment for a category.
 */
interface RuleAssignment:RegistryInformation {

    /***************Rule Assignment Logic *****************/

    /**
     * Returns the number of existing edges of the corresponding type.
     */
    fun getNumberOfExistingEdges(dependencyGraph: DependencyGraph): Int

    /**
     * Returns the percent of required edges of total amount of edges
     */
    fun getTypeStatisticProperty(): Double

    /**
     * Returns a new edge using the properties from the given generic edge.
     */
    fun getNewEdge(edge: Edge, isFirstEdge: Boolean):Edge

    /**
     * Calculate the number of required edges of this assignment type
     */
    fun getNumberOfRequiredEdges(dependencyGraph: DependencyGraph, totalEdges: Int):Int {
        val currentEdges = this.getNumberOfExistingEdges(dependencyGraph)
        val requiredPercent = this.getTypeStatisticProperty()
        return this.calculateMissingEdges(currentEdges, totalEdges, requiredPercent)
    }

    /**
     * Helper function for calculating the amount of edges
     */
    private fun calculateMissingEdges(currentEdges: Int, totalEdges: Int, requiredPercent: Double): Int {
        val currentPercent = currentEdges.toDouble() / totalEdges.toDouble()
        val missingPercent = requiredPercent - currentPercent

        return if (missingPercent <= 0) {
            0
        } else {
            floor(totalEdges * missingPercent).toInt() + 1
        }
    }
}