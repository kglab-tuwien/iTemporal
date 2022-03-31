package at.ac.tuwien.dbai.kg.iTemporal.core.contracts

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node

/**
 * This contract is used for creating a property assignment that depends on the arity.
 */
interface ArityPropertyAssignment:RegistryInformation {

    /**
     * @param nodes the nodes of the current strongly connected component
     * @param dependencyGraph the complete dependency graph
     */
    fun run(nodes: List<Node>, dependencyGraph: DependencyGraph):Boolean
}