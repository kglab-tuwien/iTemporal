package at.ac.tuwien.dbai.kg.iTemporal

import at.ac.tuwien.dbai.kg.iTemporal.ruleGenerators.datalogGenerator.VadalogGenerator
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.assignments.*
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.GenericAggregationEdge
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.ITAEdge
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.MWTAEdge
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.STAEdge
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.jobs.AggregationNormalization
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.jobs.AggregationPropertyAssigner
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.jobs.AggregationTypeAssigner
import at.ac.tuwien.dbai.kg.iTemporal.core.BenchmarkGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.jobs.DataGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.assignments.*
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.*
import at.ac.tuwien.dbai.kg.iTemporal.core.jobs.*
import at.ac.tuwien.dbai.kg.iTemporal.graphGenerator.GraphGenerator2
import at.ac.tuwien.dbai.kg.iTemporal.ruleGenerators.datalogGenerator.MeteorGenerator
import at.ac.tuwien.dbai.kg.iTemporal.ruleGenerators.postgresdbGenerator.PostgresSQLGenerator
import at.ac.tuwien.dbai.kg.iTemporal.temporal.assignments.*
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.*
import at.ac.tuwien.dbai.kg.iTemporal.temporal.jobs.TemporalIntervalAssigner
import at.ac.tuwien.dbai.kg.iTemporal.temporal.jobs.TemporalNormalization
import at.ac.tuwien.dbai.kg.iTemporal.temporal.jobs.TriangleUnitAssigner
import org.springframework.boot.runApplication
import java.io.File

object Main {
    private fun initRegistryAggregationModule() {
        // Rule Assignment
        AggregationCategoryAssignment.registerRuleAssignment(ITARuleAssignment)
        AggregationCategoryAssignment.registerRuleAssignment(MWTARuleAssignment)
        AggregationCategoryAssignment.registerRuleAssignment(STARuleAssignment)

        // Category Assignment
        Registry.addCategoryAssignmentSingleEdge(AggregationCategoryAssignment)

        // Rule Decomposition
        Registry.addRuleDecomposition(AggregationNormalization)

        // Property Assignment
        Registry.addPropertyAssignmentBefore(AggregationTypeAssigner)
        Registry.addPropertyAssignmentB(AggregationPropertyAssigner)

        // Aggregation Edges
        Registry.addEdge(ITAEdge::class)
        Registry.addEdge(MWTAEdge::class)
        Registry.addEdge(STAEdge::class)
        Registry.addEdge(GenericAggregationEdge::class)
    }

    private fun initRegistryTemporalModule() {
        // Rule Assignment
        SingleEdgeTemporalCategoryAssignment.registerRuleAssignment(BoxMinusRuleAssignment)
        SingleEdgeTemporalCategoryAssignment.registerRuleAssignment(BoxPlusRuleAssignment)
        SingleEdgeTemporalCategoryAssignment.registerRuleAssignment(DiamondMinusRuleAssignment)
        SingleEdgeTemporalCategoryAssignment.registerRuleAssignment(DiamondPlusRuleAssignment)

        MultiEdgeTemporalCategoryAssignment.registerRuleAssignment(SinceRuleAssignment)
        MultiEdgeTemporalCategoryAssignment.registerRuleAssignment(UntilRuleAssignment)

        // Category Assignment
        Registry.addCategoryAssignmentSingleEdge(SingleEdgeTemporalCategoryAssignment)
        Registry.addCategoryAssignmentMultiEdge(MultiEdgeTemporalCategoryAssignment)

        // Rule Decomposition
        Registry.addRuleDecomposition(TemporalNormalization)

        // Property Assignment
        Registry.addPropertyAssignmentBefore(TriangleUnitAssigner)
        Registry.addPropertyAssignmentAfter(TemporalIntervalAssigner)
        Registry.addPropertyAssignmentAfter(MetaIntervalAssigner)

        // Edges
        Registry.addEdge(DiamondMinusEdge::class)
        Registry.addEdge(DiamondPlusEdge::class)
        Registry.addEdge(BoxMinusEdge::class)
        Registry.addEdge(BoxPlusEdge::class)
        Registry.addEdge(ClosingEdge::class)
        Registry.addEdge(SinceEdge::class)
        Registry.addEdge(UntilEdge::class)
        Registry.addEdge(GenericTemporalSingleEdge::class)
        Registry.addEdge(GenericTemporalMultiEdge::class)
        Registry.addEdge(TriangleUpEdge::class)
    }

    private fun initRegistryCoreModule() {
        Registry.setGraphGenerator(GraphGenerator2())

        // Graph Normalization
        Registry.addGraphNormalization(DGGenericEdgeNormalizer)
        Registry.addGraphNormalization(DGMultiEdgeOperationNormalizer)
        Registry.addGraphNormalization(DGMultiEdgeSplitNormalizer)
        Registry.addGraphNormalization(DGSingleEdgeNormalizer)

        // Rule Assignment
        SingleEdgeCoreCategoryAssignment.registerRuleAssignment(LinearRuleAssignment)
        MultiEdgeCoreCategoryAssignment.registerRuleAssignment(IntersectionRuleAssignment)
        MultiEdgeCoreCategoryAssignment.registerRuleAssignment(UnionRuleAssignment)

        // Category Assignment
        Registry.addCategoryAssignmentSingleEdge(SingleEdgeCoreCategoryAssignment)
        Registry.addCategoryAssignmentMultiEdge(MultiEdgeCoreCategoryAssignment)

        // Property Assignment
        Registry.addPropertyAssignmentBefore(InputOutputAssigner)
        Registry.addPropertyAssignmentB(IntersectionPropertyAssigner)
        Registry.addPropertyAssignmentAfter(TermOrderPropertyAssigner)

        // Data Generation
        Registry.addDataGenerator(DataGenerator)

        // Edges
        Registry.addEdge(GenericEdge::class)
        Registry.addEdge(GenericMultiEdge::class)
        Registry.addEdge(GenericSingleEdge::class)
        Registry.addEdge(GenericCoreMultiEdge::class)
        Registry.addEdge(GenericCoreSingleEdge::class)
        Registry.addEdge(IntersectionEdge::class)
        Registry.addEdge(UnionEdge::class)
        Registry.addEdge(LinearEdge::class)
        Registry.addEdge(ConditionalEdge::class)
        Registry.addEdge(ExistentialEdge::class)

    }

    fun initRegistry() {
        Registry.clean()
        initRegistryCoreModule()
        initRegistryTemporalModule()
        initRegistryAggregationModule()
        Registry.addRuleGenerator(VadalogGenerator)
        Registry.addRuleGenerator(MeteorGenerator)
        Registry.addRuleGenerator(PostgresSQLGenerator)
    }

    /*@JvmStatic
    fun main(args: Array<String>) {
        Main.initRegistry()
        // Set your desired properties here
        // Registry.properties.xxx = yyy
        Registry.properties.path = File("out/GENERATION_NAME")
        val benchmarkGenerator = BenchmarkGenerator()
        benchmarkGenerator.run()
    }*/

    @JvmStatic
    fun main(args: Array<String>) {
        runApplication<Demo>(*args)
    }
}

