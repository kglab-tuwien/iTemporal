package at.ac.tuwien.dbai.kg.iTemporal.core.contracts

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph

/**
 * A graph generator has to implement this interface
 */
interface GraphGenerator:RegistryInformation {
    fun generate(): DependencyGraph
}