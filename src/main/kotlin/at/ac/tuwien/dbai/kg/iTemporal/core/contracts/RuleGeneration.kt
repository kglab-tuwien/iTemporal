package at.ac.tuwien.dbai.kg.iTemporal.core.contracts

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph

/**
 * This interface is used for the generation of a query.
 * It contains the composition (convert) to the new format and the production of the program (run)
 */
interface RuleGeneration:RegistryInformation {

    fun getLanguage():String

    fun convert(dependencyGraph: DependencyGraph):DependencyGraph
    fun run(dependencyGraph: DependencyGraph, storeFile:Boolean=true): String
}