package at.ac.tuwien.dbai.kg.iTemporal.core.contracts

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.EdgeFactory
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericEdge
import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * The main interface for an edge in a dependency graph.
 */
interface Edge {

    companion object {
        /**
         * Parses a json object and connects it to the list of nodes per name.
         * Dynamically creates a object via the edge factory (via reflection).
         */
        fun parse(data: JsonObject, nodes: List<Node>): Edge {
            val fromName = data.get("from").asString
            val toName = data.get("to").asString
            val fromNode = nodes.first { it.name == fromName }
            val toNode = nodes.first { it.name == toName }
            val typeName = if (data.has("type")) data.get("type").asString else GenericEdge::class.simpleName!!
            val isCyclic = if (data.has("isCyclic")) data.get("isCyclic").asBoolean else false
            val order = if (data.has("termOrder")) data.getAsJsonArray("termOrder").map { it.asInt } else emptyList()
            val uniqueId = if (data.has("uniqueId")) data.get("uniqueId").asString else NameGenerator.getUniqueName()
            val termOrderShuffleAllowed =
                if (data.has("termOrderShuffleAllowed")) data.get("termOrderShuffleAllowed").asBoolean else true
            val termOrderReference =
                if (data.has("termOrderReference")) data.get("termOrderReference").asString else null

            val edge = EdgeFactory.create(typeName, fromNode, toNode, isCyclic, order)
            edge.setJsonProperties(data)
            edge.uniqueId = uniqueId
            edge.termOrderShuffleAllowed = termOrderShuffleAllowed
            edge.termOrderReference = termOrderReference
            return edge
        }
    }

    var uniqueId: String
    var from: Node
    var to: Node
    var isCyclic: Boolean
    var termOrder: List<Int>
    var termOrderShuffleAllowed: Boolean
    var termOrderReference: String?

    fun backwardPropagateData()
    fun forwardPropagateData()

    fun getNumberOfAdditionalTerms(): Int = 0


    /**
     * Update the edge with the properties of the json object.
     * Usually used for loading a dependency graph from file.
     * @param data the json object containing the properties
     */
    fun setJsonProperties(data: JsonObject) {}

    /**
     * Converts the data of the edge to a json object.
     */
    fun toJson(): JsonObject {
        val data = JsonObject()
        data.addProperty("from", this.from.name)
        data.addProperty("to", this.to.name)
        data.addProperty("type", this.javaClass.simpleName)
        data.addProperty("isCyclic", this.isCyclic)
        data.addProperty("uniqueId", this.uniqueId)
        data.addProperty("termOrderShuffleAllowed", this.termOrderShuffleAllowed)
        if (this.termOrderReference != null) {
            data.addProperty("termOrderReference", this.termOrderReference)
        }

        val jsonElement = JsonArray()
        termOrder.forEach { jsonElement.add(it) }
        data.add("termOrder", jsonElement)
        return data
    }

    /**
     * Returns the label of the edge
     * Used in DG Drawer for labeling the drawn edge
     */
    fun getLabel(): String

    /**
     * Copies the given edge.
     */
    fun copy(): Edge

    /**
     * computes the term order for this edge, i.e., which from term is mapped to which to term
     */
    fun assignOrder(shuffle: Boolean) {
        if (this.termOrder.size != this.from.minArity) {
            val fromArity = this.from.minArity

            // Calculate random order of input properties.
            val data = Array(fromArity) { index -> index }.toList()
            if (shuffle && this.termOrderShuffleAllowed) {
                this.termOrder = data.shuffled(RandomGenerator.sharedRandom)
            } else {
                this.termOrder = data
            }
        }
    }

}