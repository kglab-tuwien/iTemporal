package at.ac.tuwien.dbai.kg.iTemporal.core

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraphHelper
import at.ac.tuwien.dbai.kg.iTemporal.core.jobs.ArityAssigner
import at.ac.tuwien.dbai.kg.iTemporal.core.jobs.CategoryAssigner
import java.io.File

/**
 * The main generation part iterating over the phases and calling each registered function.
 */
class BenchmarkGenerator() {

    enum class Step {
        GRAPH,
        NORMALIZATION,
        CATEGORY,
        RULE,
        RULE_DECOMPOSITION,
        PROPERTY_BEFORE,
        PROPERTY_ARITY,
        PROPERTY_AFTER,
        ALL,
        ;
    }

    fun generatePlainGraph():DependencyGraph {
        val dg = Registry.getGraphGenerator().generate()
        if (Registry.debug) {
            println("Step ${Step.GRAPH} finished")
        }
        return dg
    }

    fun runGraphTransformation(step:Step, dependencyGraph: DependencyGraph):DependencyGraph {
        var dg = dependencyGraph
        if (step >= Step.NORMALIZATION) {
            for(normalizer in Registry.getGraphNormalizations()) {
                dg = normalizer.run(dg)
            }
            if (Registry.debug) {
                println("Step ${Step.NORMALIZATION} finished")
            }
        }
        if (step >= Step.CATEGORY) {
            dg = CategoryAssigner.run(dg)
            if (Registry.debug) {
                println("Step ${Step.CATEGORY} finished")
            }
        }
        if (step >= Step.RULE) {
            for (assigner in Registry.getCategoryAssignmentsSingleEdge()) {
                dg = assigner.runRuleAssignment(dg)
            }
            for (assigner in Registry.getCategoryAssignmentsMultiEdge()) {
                dg = assigner.runRuleAssignment(dg)
            }
            if (Registry.debug) {
                println("Step ${Step.RULE} finished")
            }
        }
        if (step >= Step.RULE_DECOMPOSITION) {
            for(decomposer in Registry.getRuleDecompositions()) {
                dg = decomposer.run(dg)
            }
            if (Registry.debug) {
                println("Step ${Step.RULE_DECOMPOSITION} finished")
            }
        }
        if (step >= Step.PROPERTY_BEFORE) {
            for(propertyAssignment in Registry.getPropertyAssignmentsBefore()) {
                dg = propertyAssignment.run(dg)
            }
            if (Registry.debug) {
                println("Step ${Step.PROPERTY_BEFORE} finished")
            }
        }

        // Now we calculate the SCC
        DependencyGraphHelper.calculateSCC(dependencyGraph, true)

        if (step >= Step.PROPERTY_ARITY) {
            dg = ArityAssigner.run(dg)
            if (Registry.debug) {
                println("Step ${Step.PROPERTY_ARITY} finished")
            }
        }
        if (step >= Step.PROPERTY_AFTER) {
            for(propertyAssignment in Registry.getPropertyAssignmentsAfter()) {
                dg = propertyAssignment.run(dg)
            }
            if (Registry.debug) {
                println("Step ${Step.PROPERTY_AFTER} finished")
            }
        }
        return dg
    }

    fun runRuleGeneration(dg: DependencyGraph):Map<String,String> {
        val rules = mutableMapOf<String,String>()
        for (ruleGenerators in Registry.getRuleGenerators()) {
            //6. Run Rule Composition
            val dgConverted = ruleGenerators.convert(dg)
            // 7. Run Rule Generation
            rules.put(ruleGenerators.getLanguage(),ruleGenerators.run(dgConverted, false))
        }
        return rules
    }

    fun runDataGeneration(dependencyGraph: DependencyGraph):Map<String, String> {
        val data = mutableMapOf<String,String>()
        var dg = dependencyGraph
        for (dataGenerator in Registry.getDataGenerators()) {
            dg = dataGenerator.run(dg, false)
        }

        return dg.getData(true)
    }

    fun run(dependencyGraph: DependencyGraph):DependencyGraph {
        var dg = runGraphTransformation(Step.PROPERTY_AFTER,dependencyGraph)
        dg.store(File("out/debug_5.json"))

        // 8. Run Data Generation
        // Do before composition
        for (dataGenerator in Registry.getDataGenerators()) {
            dg = dataGenerator.run(dg)
        }

        if (Registry.debug) {
            println("data complete")
        }

        for (ruleGenerators in Registry.getRuleGenerators()) {
            //6. Run Rule Composition
            val dgConverted = ruleGenerators.convert(dg)
            // 7. Run Rule Generation
            ruleGenerators.run(dgConverted)
        }

        if (Registry.debug) {
            println("rules complete")
        }


        return dg
    }


    fun run():DependencyGraph {
        return this.run(generatePlainGraph())
    }
}