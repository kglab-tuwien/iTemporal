import at.ac.tuwien.dbai.kg.iTemporal.Main
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.graphGenerator.GraphGenerator
import at.ac.tuwien.dbai.kg.iTemporal.graphGenerator.GraphGenerator2
import at.ac.tuwien.dbai.kg.iTemporal.main
import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import org.junit.jupiter.api.Test
import kotlin.random.Random

class LoadTest {

    @Test
    fun testMain() {
        // Test main runner
        for (i in 0 until 100) {
            NameGenerator.reset()
            Main.main(arrayOf())
            println("$i completed")
        }
    }

    @Test
    fun testGraphGenerator() {
        // Generate 100 random graphs and try to generate some error
        for (x in 0 until 100) {
            Main.initRegistry()
            Registry.properties.inputNodes = Random.nextInt(1,10)
            Registry.properties.outputNodes = Random.nextInt(1,10)
            Registry.properties.nodes = Registry.properties.inputNodes + Registry.properties.outputNodes + Random.nextInt(1,20)
            Registry.properties.recursiveComplexity = Math.random()
            Registry.properties.multiEdgeRules = Math.random()
            Registry.properties.recursiveRules = Math.random()
            val g = GraphGenerator()
            val dg = g.generate()
        }
    }



    @Test
    fun testGraphGeneratorNew() {
        for (i in 0 until 100) {
            Main.initRegistry()
            Registry.properties.inputNodes = 2
            Registry.properties.outputNodes = 1
            Registry.properties.nodes = 10
            Registry.properties.recursiveComplexity = 0.6
            Registry.properties.multiEdgeRules = 0.4
            Registry.properties.recursiveRules = 0.2

            val g = GraphGenerator2()
            val dg = g.generate()
            //dg.draw("dgDebug")
        }
    }
}