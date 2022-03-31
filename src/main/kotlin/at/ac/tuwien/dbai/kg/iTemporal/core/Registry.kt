package at.ac.tuwien.dbai.kg.iTemporal.core

import at.ac.tuwien.dbai.kg.iTemporal.util.Properties
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.*
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import java.util.SortedSet
import kotlin.Comparator
import kotlin.reflect.KClass

/**
 * A class managing the executed jobs in the generation steps.
 * One can register a job to a specific phase which gets executed.
 */
object Registry {
    var debug: Boolean = false
    private var graphGenerator: GraphGenerator? = null
    private val graphNormalizations: MutableSet<GraphNormalization> = mutableSetOf()
    private val categoryAssignmentsSingleEdge: MutableSet<CategoryAssignmentSingleEdge> = mutableSetOf()
    private val categoryAssignmentsMultiEdge: MutableSet<CategoryAssignmentMultiEdge> = mutableSetOf()
    private val ruleDecompositions: MutableSet<RuleDecomposition> = mutableSetOf()
    private val propertyAssignmentsBefore: MutableSet<OtherPropertyAssignment> = mutableSetOf()
    private val propertyAssignmentsA: MutableSet<ArityPropertyAssignment> = mutableSetOf()
    private val propertyAssignmentsB: MutableSet<ArityPropertyAssignment> = mutableSetOf()
    private val propertyAssignmentsAfter: MutableSet<OtherPropertyAssignment> = mutableSetOf()
    private val ruleGenerators: MutableSet<RuleGeneration> = mutableSetOf()
    private val dataGenerations: MutableSet<DataGeneration> = mutableSetOf()

    private val edgeTypes: MutableMap<String, KClass<out Edge>> = mutableMapOf()
    var properties: Properties = Properties()

    class RegistryComparator : Comparator<RegistryInformation> {
        override fun compare(o1: RegistryInformation?, o2: RegistryInformation?): Int {
            if (o1 == null || o2 == null) {
                return 0
            }
            return o1.getPriority().compareTo(o2.getPriority())
        }
    }

    private val registryComparator = RegistryComparator()

    fun setGraphGenerator(graphGenerator: GraphGenerator) {
        this.graphGenerator = graphGenerator
    }

    fun addGraphNormalization(graphNormalization: GraphNormalization) {
        this.graphNormalizations.add(graphNormalization)
    }

    fun addCategoryAssignmentSingleEdge(categoryAssignment: CategoryAssignmentSingleEdge) {
        this.categoryAssignmentsSingleEdge.add(categoryAssignment)
    }

    fun addCategoryAssignmentMultiEdge(categoryAssignment: CategoryAssignmentMultiEdge) {
        this.categoryAssignmentsMultiEdge.add(categoryAssignment)
    }

    fun addRuleDecomposition(ruleDecomposition: RuleDecomposition) {
        this.ruleDecompositions.add(ruleDecomposition)
    }

    fun addPropertyAssignmentBefore(propertyAssignment: OtherPropertyAssignment) {
        this.propertyAssignmentsBefore.add(propertyAssignment)
    }

    fun addPropertyAssignmentA(propertyAssignment: ArityPropertyAssignment) {
        this.propertyAssignmentsA.add(propertyAssignment)
    }

    fun addPropertyAssignmentB(propertyAssignment: ArityPropertyAssignment) {
        this.propertyAssignmentsB.add(propertyAssignment)
    }

    fun addPropertyAssignmentAfter(propertyAssignment: OtherPropertyAssignment) {
        this.propertyAssignmentsAfter.add(propertyAssignment)
    }

    fun addRuleGenerator(ruleGenerator: RuleGeneration) {
        this.ruleGenerators.add(ruleGenerator)
    }

    fun addDataGenerator(dataGenerator: DataGeneration) {
        this.dataGenerations.add(dataGenerator)
    }

    fun getGraphGenerator(): GraphGenerator {
        return this.graphGenerator!!
    }

    fun getGraphNormalizations(): SortedSet<GraphNormalization> {
        return this.graphNormalizations.toSortedSet(registryComparator)
    }

    fun getCategoryAssignmentsSingleEdge(): SortedSet<CategoryAssignmentSingleEdge> {
        return this.categoryAssignmentsSingleEdge.toSortedSet(registryComparator)
    }

    fun getCategoryAssignmentsMultiEdge(): SortedSet<CategoryAssignmentMultiEdge> {
        return this.categoryAssignmentsMultiEdge.toSortedSet(registryComparator)
    }

    fun getRuleDecompositions(): SortedSet<RuleDecomposition> {
        return this.ruleDecompositions.toSortedSet(registryComparator)
    }

    fun getPropertyAssignmentsBefore(): SortedSet<OtherPropertyAssignment> {
        return this.propertyAssignmentsBefore.toSortedSet(registryComparator)
    }

    fun getPropertyAssignmentsA(): SortedSet<ArityPropertyAssignment> {
        return this.propertyAssignmentsA.toSortedSet(registryComparator)
    }

    fun getPropertyAssignmentsB(): SortedSet<ArityPropertyAssignment> {
        return this.propertyAssignmentsB.toSortedSet(registryComparator)
    }

    fun getPropertyAssignmentsAfter(): SortedSet<OtherPropertyAssignment> {
        return this.propertyAssignmentsAfter.toSortedSet(registryComparator)
    }

    fun getRuleGenerators(): SortedSet<RuleGeneration> {
        return this.ruleGenerators.toSortedSet(registryComparator)
    }

    fun getDataGenerators(): SortedSet<DataGeneration> {
        return this.dataGenerations.toSortedSet(registryComparator)
    }

    fun addEdge(edge: KClass<out Edge>) {
        edgeTypes[edge.simpleName!!] = edge
    }

    fun getEdgeTypes(): Map<String, KClass<out Edge>> {
        return this.edgeTypes
    }

    fun clean() {
        graphGenerator = null
        graphNormalizations.clear()
        categoryAssignmentsSingleEdge.clear()
        categoryAssignmentsMultiEdge.clear()
        ruleDecompositions.clear()
        propertyAssignmentsBefore.clear()
        propertyAssignmentsA.clear()
        propertyAssignmentsB.clear()
        propertyAssignmentsAfter.clear()
        ruleGenerators.clear()
        dataGenerations.clear()
        edgeTypes.clear()
        properties = Properties()
    }

}