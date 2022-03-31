package at.ac.tuwien.dbai.kg.iTemporal.core.contracts

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph

/**
 * Contract used for registering an additional data generation logic.
 */
interface DataGeneration:RegistryInformation {

    fun run(dependencyGraph: DependencyGraph, storeFile:Boolean=true): DependencyGraph
}