package at.ac.tuwien.dbai.kg.iTemporal

import at.ac.tuwien.dbai.kg.iTemporal.core.BenchmarkGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.util.Properties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@SpringBootApplication
open class Demo


@Configuration
open class WebServerConfiguration {

    //@Value("\${cors.originPatterns:default}")
    private val corsOriginPatterns: String = "http://localhost:3000"

    @Bean
    open fun addCorsConfig(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                val allowedOrigins = corsOriginPatterns.split(",").toTypedArray()
                registry.addMapping("/**")
                    .allowedMethods("*")
                    .allowedOriginPatterns(*allowedOrigins)
                    .allowCredentials(true)
            }
        }
    }
}


data class Message(
    val step: BenchmarkGenerator.Step = BenchmarkGenerator.Step.GRAPH,
    val properties: Properties,
    val dependencyGraph: String?
)

data class RuleResponse(
    val graph: String,
    val rules: Map<String,String>,
)

data class DataResponse(
    val graph: String,
    val data: Map<String,String>,
)

@RestController
class BenchmarkGeneratorResource {
    @GetMapping
    fun index():String {
        return "Running Benchmark Generator v0.1"
    }


    @PostMapping
    fun runGenerator(@RequestBody message: Message):String {
        Main.initRegistry()
        Registry.properties = message.properties
        val benchmarkGenerator = BenchmarkGenerator()
        val dg = if(message.dependencyGraph != null) DependencyGraph.parseFromJson(message.dependencyGraph) else benchmarkGenerator.generatePlainGraph()
        val finalGraph = benchmarkGenerator.runGraphTransformation(message.step, dg)
        return finalGraph.toJson()
    }


    @PostMapping("/rules")
    fun generateRules(@RequestBody message: Message):RuleResponse {
        Main.initRegistry()
        Registry.properties = message.properties
        val benchmarkGenerator = BenchmarkGenerator()
        var dg = if(message.dependencyGraph != null) DependencyGraph.parseFromJson(message.dependencyGraph) else benchmarkGenerator.generatePlainGraph()
        if (dg.nodes.isEmpty()) {
            dg = benchmarkGenerator.generatePlainGraph()
        }
        val finalGraph = benchmarkGenerator.runGraphTransformation(message.step, dg)
        return RuleResponse(
            graph=finalGraph.toJson(),
            rules =benchmarkGenerator.runRuleGeneration(finalGraph)
        )
    }

    @PostMapping("/data")
    fun generateData(@RequestBody message: Message):DataResponse {
        Main.initRegistry()
        Registry.properties = message.properties
        val benchmarkGenerator = BenchmarkGenerator()
        var dg = if(message.dependencyGraph != null) DependencyGraph.parseFromJson(message.dependencyGraph) else benchmarkGenerator.generatePlainGraph()
        if (dg.nodes.isEmpty()) {
            dg = benchmarkGenerator.generatePlainGraph()
        }
        val finalGraph = benchmarkGenerator.runGraphTransformation(message.step, dg)
        return DataResponse(
            graph=finalGraph.toJson(),
            data =benchmarkGenerator.runDataGeneration(finalGraph)
        )
    }

}

fun main(args: Array<String>) {
    runApplication<Demo>(*args)
}