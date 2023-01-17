package at.ac.tuwien.dbai.kg.iTemporal.temporal.edges

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.SingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.temporal.TriangleUnit
import com.google.gson.JsonObject

class TriangleUpEdge(override var from: Node,
                     override var to: Node,
                     override var isCyclic: Boolean = false,
                     override var termOrder: List<Int> = listOf(),
                     var unit: TriangleUnit = TriangleUnit.Unknown,
                     override var uniqueId: String = NameGenerator.getUniqueName(),
                     override var termOrderShuffleAllowed: Boolean = true,
                     override var termOrderReference: String? = null,
                     override var existentialCount: Int = 0,
) : SingleEdge {

    override fun toJson(): JsonObject {
        val data = super.toJson()
        data.addProperty("unit", unit.name)
        return data
    }

    override fun setJsonProperties(data: JsonObject) {
        super.setJsonProperties(data)
        if (data.has("unit")) {
            this.unit = TriangleUnit.valueOf(data.get("unit").asString)
        }
    }

    override fun backwardPropagateData() {
        // We ignore this operator as sub processed data will require normalized data and this one comes to late looking from output->input
        // So we see the actual output data as the actual input data, knowing that the actual generated output will be different, which per definition will be always the case
        // The goal is just to generate at least some output data by some heuristics

        val newData = mutableListOf<List<Double>>()

        for(entry in to.data) {
            val newEntry = Array(from.minArity+2){index -> -100.0}
            for ((fromIndex, orderId) in this.termOrder.withIndex()) {
                if (orderId < 0) {
                    newEntry[fromIndex] = RandomGenerator.generateTerm(Registry.properties.cardinalityTermDomain)
                } else if (orderId >= 0 && orderId < to.minArity) {
                    newEntry[fromIndex] = entry[orderId]
                } else {
                    newEntry[fromIndex] = RandomGenerator.generateTerm(Registry.properties.cardinalityTermDomain)
                }
            }

            // Add time interval
            newEntry[from.minArity] = entry[to.minArity]
            newEntry[from.minArity+1] = entry[to.minArity+1]
            newData.add(newEntry.toList())
        }

        this.from.data = this.from.data + newData
    }

    override fun forwardPropagateData() {
        // We reverse the logic and just move the data
        val newData = mutableListOf<List<Double>>()

        for(entry in from.data) {
            val newEntry = Array(to.minArity+2){index -> -100.0}

            for ((fromIndex, orderId) in this.termOrder.withIndex()) {
                // Not relevant term
                if (orderId < 0 || orderId >= to.minArity) {
                    continue
                }
                if (orderId < to.minArity) {
                    newEntry[orderId] = entry[fromIndex]
                }
            }

            // Add time interval
            newEntry[to.minArity] = entry[from.minArity]
            newEntry[to.minArity+1] = entry[from.minArity+1]
            newData.add(newEntry.toList())
        }

        this.to.data = this.to.data + newData
    }


    override fun getLabel(): String {
        return "/\\ ${unit.getTypeString()}"
    }

    override fun copy(): Edge {
        return TriangleUpEdge(from=from, to=to, isCyclic=isCyclic, termOrder=termOrder, unit=unit, termOrderShuffleAllowed = termOrderShuffleAllowed, termOrderReference = termOrderReference)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TriangleUpEdge) return false

        if (from != other.from) return false
        if (to != other.to) return false
        if (isCyclic != other.isCyclic) return false
        if (termOrder != other.termOrder) return false
        if (unit != other.unit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + isCyclic.hashCode()
        result = 31 * result + termOrder.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }

    override fun toString(): String {
        return "TriangleUpEdge(from=$from, to=$to, isCyclic=$isCyclic, termOrder=$termOrder, unit=$unit, uniqueId='$uniqueId')"
    }


}