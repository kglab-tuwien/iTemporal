package at.ac.tuwien.dbai.kg.iTemporal.core.contracts

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import kotlin.math.floor

/**
 * Contract used for registering an additional category assignment for edges.
 */
interface CategoryAssignment:RegistryInformation {

    var ruleAssignments:MutableList<RuleAssignment>

    /**
     * Registers a specific rule type for this category assignment type
     */
    fun registerRuleAssignment(ruleAssignment: RuleAssignment) {
        this.ruleAssignments.add(ruleAssignment)
    }

    fun runRuleAssignment(dependencyGraph: DependencyGraph): DependencyGraph

    /***************Category Assignment Logic *****************/

    /**
     * Returns the number of existing edges of the corresponding type.
     */
    fun getNumberOfExistingEdges(dependencyGraph: DependencyGraph): Int

    /**
     * Returns the percent of required edges of total amount of edges
     */
    fun getTypeStatisticProperty(): Double

    /**
     * Returns the generic edges of the category type, which can be replaced by a rule type.
     */
    fun getGenericCategoryEdges(dependencyGraph: DependencyGraph): List<Edge>

    /**
     * Returns a new edge using the properties from the given generic edge.
     */
    fun getNewEdge(edge: Edge):Edge

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