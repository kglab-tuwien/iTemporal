import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import at.ac.tuwien.dbai.kg.iTemporal.util.Utils
import at.ac.tuwien.dbai.kg.iTemporal.Main
import at.ac.tuwien.dbai.kg.iTemporal.core.BenchmarkGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.graphGenerator.GraphGenerator2
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.BoxMinusEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.DiamondMinusEdge
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

internal class GeneralTest {

    @Test
    fun testFCGraphGeneration() {
        // Some tests for fully connected graph generation

        RandomGenerator.setSeed(3724)
        val graph = GraphGenerator2().generate()
        Assertions.assertTrue(graph.isFullyConnected())


    }


    @Test
    fun testJsonStoreAndWrite() {
        val node1 = Node()
        val node2 = Node()
        val node3 = Node()
        val edge1 = DiamondMinusEdge(to = node3, from = node1)
        val edge2 = BoxMinusEdge(to = node3, from = node2)

        val dg = DependencyGraph(
            mutableSetOf(node1, node2, node3),
            hashMapOf(
                Pair(node3, listOf(edge1, edge2)),
            ),
            hashMapOf(
                Pair(node1, listOf(edge1)),
                Pair(node2, listOf(edge2))
            )
        )

        Registry.addEdge(DiamondMinusEdge::class)
        Registry.addEdge(BoxMinusEdge::class)
        dg.store(File("out/test.json"))

        val readDg = DependencyGraph.parseFromJson(File("out/test.json"))

        Assertions.assertEquals(dg, readDg)
    }

    @Test
    fun testIntersectionIssue() {
        Main.initRegistry()
        Registry.properties.path = File("out/random")
        val dg =
            DependencyGraph.parseFromJson(File("src/test/resources/examples/intersection_issue.json"))
        val generator = BenchmarkGenerator()
        generator.run(dg)
    }


    @Test
    fun runRecursiveTemporalExample() {
        // Try to produce error
        /*for (i in 0 until 100) {
            Main.initRegistry()
            Registry.properties.generateTimePoints = false
            Registry.properties.path = File("out/recursive_temporal")
            Registry.properties.unionInclusionPercentage = 1.0

            val dg =
            //DependencyGraph.parseFromJson(File("src/test/resources/examples/recursive_temporal_intersection.json"))
                //DependencyGraph.parseFromJson(File("src/test/resources/examples/recursive_temporal.json"))
                DependencyGraph.parseFromJson(File("src/test/resources/examples/recursive_temporal_aggregation.json"))

            val generator = BenchmarkGenerator()
            val dgAfter = generator.run(dg)

        }*/
        println("Ignored")
    }


    @Test
    fun runRandomGenerator() {
        val counter = mutableListOf<Int>()
        for (i in 1..1000) {
            counter.add(RandomGenerator.getNextArity(3, 0.5))
        }

        println(counter.groupingBy { it }.eachCount())
    }

    @Test
    fun mergeTest() {
        val entry = listOf(0.7007025, 0.7131419)
        val data = listOf(
            listOf(0.6517415, 0.6517964),
            listOf(0.7007025, 0.7131419),
            listOf(0.7063408, 0.7076335),
            listOf(0.7063408, 0.7100473),
            listOf(0.7079335, 0.7082103),
            listOf(0.7103473, 0.7104851),
            listOf(0.7106446, 0.7111181),
            listOf(0.7106446, 0.7117752),
            listOf(0.7108136, 0.7108181),
            listOf(0.7209569, 0.7245581),
            listOf(0.7219188, 0.7242581),
            listOf(0.7667868, 0.766955),
            listOf(0.7884366, 0.7885683),
            listOf(0.7884366, 0.7894633)
        )

        Utils.cleanIntervals(data)

        val x = Utils.mergeIntervals(Utils.cleanIntervals(data))
            .filter { it[it.size - 2] <= entry[it.size - 1] && it[it.size - 1] >= entry[it.size - 2] }

        Assertions.assertEquals(1, x.size)
    }

    @Test
    fun testFilterIntervals() {
        val data = listOf(
            listOf(1.0, 3.0),
            listOf(8.0, 12.0),
            listOf(17.0, 25.0),
            listOf(32.0, 38.0),
            listOf(40.0, 45.0),
        )

        val filters = listOf(
            listOf(1.0, 3.0),
            listOf(4.0, 6.0),
            listOf(7.0, 9.0), // Start before, End Inside
            listOf(11.0, 14.0), // Start inside, End After
            listOf(17.0, 21.0), //Start exact, End Inside
            listOf(25.0, 27.0), // Start where end
            listOf(33.0, 36.0), // Start inside, End Inside
            listOf(40.0, 45.0), // Start exact, End Exact
        )

        val answer = Utils.filterIntervals(data, filters)
    }

    @Test
    fun testExsistentials() {
        // Check seed
        var seed = 0
        //for (i in 0 until 100) {
        //    seed = i
        //    println(seed)
            Main.initRegistry()
            Registry.properties.path = File("out/GENERATION_NAME")
            Registry.properties.outputCsvHeader = true
            Registry.properties.generateTimePoints = false
            Registry.properties.nodes = 10
            Registry.properties.multiEdgeRules = 0.6
            Registry.properties.aggregationRules = 0.0
            Registry.properties.singleEdgeTemporalRules = 0.5
            Registry.properties.coreSingleEdgeRules = 0.5
            Registry.properties.weaklyAcyclicProgram = true
            Registry.properties.averageExistentials = 3
            Registry.properties.outputTimestampStart = 0
            Registry.properties.outputTimestampEnd = 1000000
            RandomGenerator.setSeed(seed)
            val benchmarkGenerator = BenchmarkGenerator()
            benchmarkGenerator.run()
        //}

    }

}