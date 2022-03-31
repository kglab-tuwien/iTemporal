package at.ac.tuwien.dbai.kg.iTemporal.core.contracts

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph

/**
 * This contract is used for creating a property assignment that depends NOT on the arity.
 */
interface OtherPropertyAssignment:RegistryInformation {

    fun run(dependencyGraph: DependencyGraph):DependencyGraph
}