import at.ac.tuwien.dbai.kg.iTemporal.Main
import at.ac.tuwien.dbai.kg.iTemporal.core.BenchmarkGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.graphGenerator.GraphGenerator
import at.ac.tuwien.dbai.kg.iTemporal.graphGenerator.GraphGenerator2
import at.ac.tuwien.dbai.kg.iTemporal.ruleGenerators.questdbGenerator.QuestDBGenerator
import org.junit.jupiter.api.Test
import java.io.File

class ICDEGenerationTest {

    @Test
    fun generateDiamondExample() {
        Main.initRegistry()
        Registry.properties.path = File("out/diamond")
        val dg = DependencyGraph.parseFromJson(File("src/test/resources/examples/diamond.json"))
        val generator = BenchmarkGenerator()
        generator.run(dg)
    }

    @Test
    fun runDiamondExampleBenchmark() {
        Main.initRegistry()
        Registry.properties.generateTimePoints = false
        Registry.properties.averageAmountOfGeneratedOutputs = 10
        Registry.properties.varianceAmountOfGeneratedOutputs = 0.0
        Registry.properties.path = File("out/diamond")
        val dg = DependencyGraph.parseFromJson(File("src/test/resources/examples/diamond_benchmark.json"))
        val generator = BenchmarkGenerator()
        generator.run(dg)
    }

    @Test
    fun generateSTAExample() {
        Main.initRegistry()
        Registry.addRuleGenerator(QuestDBGenerator)
        Registry.properties.path = File("out/sta")
        val dg =
            DependencyGraph.parseFromJson(File("src/test/resources/examples/sta_aggregation.json"))
        val generator = BenchmarkGenerator()
        generator.run(dg)
    }

    @Test
    fun generateMWTAExample() {
        Main.initRegistry()
        // Currently not supported for QuestDB, see https://github.com/questdb/questdb/issues/1351
        //Registry.addRuleGenerator(QuestDBGenerator)
        Registry.properties.path = File("out/mwta")
        val dg =
            DependencyGraph.parseFromJson(File("src/test/resources/examples/mwta_aggregation.json"))
        val generator = BenchmarkGenerator()
        generator.run(dg)
    }

    @Test
    fun generateITAExample() {
        Main.initRegistry()
        Registry.addRuleGenerator(QuestDBGenerator)
        Registry.properties.path = File("out/ita")
        val dg =
            DependencyGraph.parseFromJson(File("src/test/resources/examples/ita_aggregation.json"))
        val generator = BenchmarkGenerator()
        generator.run(dg)
    }

    @Test
    fun runSTAExampleBenchmark() {
        Main.initRegistry()
        Registry.addRuleGenerator(QuestDBGenerator)
        Registry.properties.path = File("out/sta")
        Registry.properties.unionInclusionPercentage = 1.0
        Registry.properties.averageAmountOfGeneratedOutputs=10
        Registry.properties.outputCsvHeader=true
        Registry.properties.outputQuestDB=true
        val dg =
            DependencyGraph.parseFromJson(File("src/test/resources/examples/sta_aggregation_benchmark.json"))
        val generator = BenchmarkGenerator()
        generator.run(dg)

    }

    @Test
    fun runITAExampleBenchmark() {
        Main.initRegistry()
        Registry.addRuleGenerator(QuestDBGenerator)
        Registry.properties.path = File("out/ita")
        Registry.properties.unionInclusionPercentage = 1.0
        Registry.properties.averageAmountOfGeneratedOutputs=10
        Registry.properties.outputCsvHeader=true
        Registry.properties.outputQuestDB=true
        val dg =
            DependencyGraph.parseFromJson(File("src/test/resources/examples/ita_aggregation_benchmark.json"))
        val generator = BenchmarkGenerator()
        generator.run(dg)
    }

    @Test
    fun runMWTAExampleBenchmark() {
        Main.initRegistry()
        //Registry.addRuleGenerator(QuestDBGenerator)
        Registry.properties.path = File("out/mwta")
        Registry.properties.unionInclusionPercentage = 1.0
        Registry.properties.averageAmountOfGeneratedOutputs=10
        Registry.properties.outputCsvHeader=true
        Registry.properties.outputQuestDB=true
        val dg =
            DependencyGraph.parseFromJson(File("src/test/resources/examples/mwta_aggregation_benchmark.json"))
        val generator = BenchmarkGenerator()
        generator.run(dg)
    }

    @Test
    fun generateCompanyControlExample() {
        Main.initRegistry()
        Registry.properties.path = File("out/companyControl")
        val dg = DependencyGraph.parseFromJson(File("src/test/resources/examples/company_control.json"))
        val generator = BenchmarkGenerator()
        generator.run(dg)

    }

    @Test
    fun runCompanyControlExampleBenchmark() {
        Main.initRegistry()
        Registry.debug = true
        Registry.properties.path = File("out/companyControl")
        Registry.properties.averageAmountOfGeneratedOutputs = 10000
        val dg = DependencyGraph.parseFromJson(File("src/test/resources/examples/company_control_benchmark.json"))
        val generator = BenchmarkGenerator()
        generator.run(dg)
    }

    @Test
    fun testGraphGenerator() {
        Main.initRegistry()
        Registry.properties.inputNodes = 2
        Registry.properties.outputNodes = 1
        Registry.properties.nodes = 10
        Registry.properties.recursiveComplexity = 0.6
        Registry.properties.multiEdgeRules = 0.4
        Registry.properties.recursiveRules = 0.2
        val g = GraphGenerator2()
        val dg = g.generate()
        //dg.draw("graph_generator")
    }



}