package at.ac.tuwien.dbai.kg.iTemporal.core.contracts

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph

/**
 * This interface is used for the graph normalization phase.
 * Each normalizer has to implement this interface.
 */
interface GraphNormalization:RegistryInformation {

    fun run(dependencyGraph: DependencyGraph): DependencyGraph
}