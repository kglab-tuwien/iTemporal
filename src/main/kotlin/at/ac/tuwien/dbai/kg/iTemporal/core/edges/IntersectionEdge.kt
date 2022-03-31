package at.ac.tuwien.dbai.kg.iTemporal.core.edges

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.temporal.assignments.MetaIntervalAssigner
import com.google.gson.JsonObject

class IntersectionEdge(
    override var from: Node,
    override var to: Node,
    override var isCyclic: Boolean = false,
    override var termOrder: List<Int> = listOf(),
    var overlappingTerms: Int = -1,
    var nonOverlappingTerms: Int = -1,
    override var uniqueId: String = NameGenerator.getUniqueName(),
    override var termOrderShuffleAllowed: Boolean = true,
    override var termOrderReference: String? = null,
) :CoreMultiEdge {

    var joinInputData:List<List<Double>> = emptyList()
    var joinInputDataAll:List<List<Double>> = emptyList()

    override fun backwardPropagateData() {
        val newData = mutableListOf<List<Double>>()


        for(entry in to.data) {
            // Random terms may child unexpected infinite behavior in loops due to the creation of additional elements after each round.
            // In order to limit the creation of such elements, we check whether a term with the required join attributes for the timestamp exist.
            // If so, we can ignore and do not have to create an additional fact
            val dataFound = this.from.oldData.any { oldDataEntry ->
                this.termOrder.withIndex().all {
                    it.value < 0 || it.value < to.minArity || oldDataEntry[it.index] == entry[it.value]
                } && entry[to.minArity] == oldDataEntry[oldDataEntry.size - 2] && entry[to.minArity + 1] == oldDataEntry[oldDataEntry.size - 1]
            }

            if (dataFound) {
                continue
            }

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

            val minDuration = MetaIntervalAssigner.getIntervalInformations()[from]!!.intervalOffset.getDuration()
            // Add time interval
            newEntry[from.minArity] = entry[to.minArity]
            newEntry[from.minArity+1] = entry[to.minArity+1]

            val currentDuration = newEntry[from.minArity+1] - newEntry[from.minArity]
            if (currentDuration < minDuration) {
                // We simplify by adding the remaining part afterwards
                newEntry[from.minArity+1] += (minDuration-currentDuration)
            }

            newData.add(newEntry.toList())
        }


        this.from.data = newData + this.from.data
    }

    // Attention: This is not the full join, this just maps the data to the "to" format
    override fun forwardPropagateData() {
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

        this.joinInputData = this.joinInputData + newData
        this.joinInputDataAll = this.joinInputDataAll + newData
    }

    override fun getLabel(): String {
       return "∩"
    }

    override fun copy(): Edge {
        return IntersectionEdge(from=from, to=to, isCyclic=isCyclic, termOrder=termOrder, overlappingTerms=overlappingTerms, nonOverlappingTerms=nonOverlappingTerms, termOrderShuffleAllowed = termOrderShuffleAllowed, termOrderReference = termOrderReference)
    }

    override fun toJson(): JsonObject {
        val data = super.toJson()
        data.addProperty("overlappingTerms", overlappingTerms)
        data.addProperty("nonOverlappingTerms", nonOverlappingTerms)
        return data
    }

    override fun setJsonProperties(data: JsonObject) {
        super.setJsonProperties(data)
        if(data.has("overlappingTerms")) {
            this.overlappingTerms = data.get("overlappingTerms").asInt
        }
        if(data.has("nonOverlappingTerms")) {
            this.nonOverlappingTerms = data.get("nonOverlappingTerms").asInt
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntersectionEdge) return false

        if (from != other.from) return false
        if (to != other.to) return false
        if (isCyclic != other.isCyclic) return false
        if (termOrder != other.termOrder) return false
        if (overlappingTerms != other.overlappingTerms) return false
        if (nonOverlappingTerms != other.nonOverlappingTerms) return false
        if (uniqueId != other.uniqueId) return false
        if (joinInputData != other.joinInputData) return false
        if (joinInputDataAll != other.joinInputDataAll) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + isCyclic.hashCode()
        result = 31 * result + termOrder.hashCode()
        result = 31 * result + overlappingTerms
        result = 31 * result + nonOverlappingTerms
        result = 31 * result + uniqueId.hashCode()
        result = 31 * result + joinInputData.hashCode()
        result = 31 * result + joinInputDataAll.hashCode()
        return result
    }

    override fun toString(): String {
        return "IntersectionEdge(from=$from, to=$to, isCyclic=$isCyclic, termOrder=$termOrder, overlappingTerms=$overlappingTerms, nonOverlappingTerms=$nonOverlappingTerms, uniqueId='$uniqueId', joinInputData=$joinInputData, joinInputDataAll=$joinInputDataAll)"
    }


}