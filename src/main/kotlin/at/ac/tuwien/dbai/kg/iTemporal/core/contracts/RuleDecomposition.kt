package at.ac.tuwien.dbai.kg.iTemporal.core.contracts

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph

/**
 * This interface is used for the graph decomposition phase.
 * Each decomposer has to implement this interface.
 */
interface RuleDecomposition:RegistryInformation {
    fun run(dependencyGraph: DependencyGraph): DependencyGraph
}