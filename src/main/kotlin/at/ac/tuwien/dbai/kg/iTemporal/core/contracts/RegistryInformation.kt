package at.ac.tuwien.dbai.kg.iTemporal.core.contracts

/**
 * The meta interface for each task that can be registered to the registry at some point.
 */
interface RegistryInformation {

    fun getPriority():Int
}