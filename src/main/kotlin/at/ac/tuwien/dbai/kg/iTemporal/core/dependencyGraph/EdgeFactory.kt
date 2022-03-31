package at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import kotlin.reflect.full.primaryConstructor

object EdgeFactory {

    fun create(type: String, vararg args: Any?):Edge {
        val edgeType = Registry.getEdgeTypes()[type]
        val params = edgeType!!.primaryConstructor!!.parameters.flatMapIndexed { index, kParameter -> if(index < args.size) listOf(Pair(kParameter, args[index])) else emptyList() }.toMap()
        return edgeType.primaryConstructor!!.callBy(params)
    }
}