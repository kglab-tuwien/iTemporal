package at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph

import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import com.google.gson.*
import java.lang.reflect.Type

class DGSerializer : JsonSerializer<DependencyGraph>, JsonDeserializer<DependencyGraph> {
    override fun serialize(src: DependencyGraph?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val dgObject = JsonObject()

        val nodes = JsonArray()

        for (node in src!!.nodes) {
            nodes.add(node.toJson())
        }

        val edges = JsonArray()
        for (edge in src.inEdges.values.flatten()) {
            edges.add(edge.toJson())
        }

        dgObject.add("nodes", nodes)
        dgObject.add("edges", edges)

        return dgObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): DependencyGraph {

        val nodes = json!!.asJsonObject.get("nodes").asJsonArray
        val newNodes = nodes.map {
            Node.parse(it.asJsonObject)
        }

        val newInEdges = hashMapOf<Node, List<Edge>>()
        val newOutEdges = hashMapOf<Node, List<Edge>>()
        val edges = json.asJsonObject.get("edges").asJsonArray

        edges.forEach {
            // First sets global edge parameters, second class specific ones
            val edge = Edge.parse(it.asJsonObject, newNodes)
            newOutEdges[edge.from] = newOutEdges[edge.from].orEmpty() + listOf(edge)
            newInEdges[edge.to] = newInEdges[edge.to].orEmpty() + listOf(edge)
        }

        return DependencyGraph(newNodes.toSet(), newInEdges, newOutEdges)
    }

}