package at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.util.Utils
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.IntersectionEdge
import com.google.gson.JsonObject
import kotlin.math.max
import kotlin.math.min

data class Node(
    val name: String = NameGenerator.getUniqueName(),
    var type: NodeType = NodeType.General,
    // The minimum arity is the arity required for the following nodes.
    var minArity: Int = -1,
    var maxArity: Int = -1,
    var isCyclic: Boolean = false,
    var sccId: Int = -1,


    // For data generation
    var data: List<List<Double>> = emptyList(),
    var oldData: List<List<Double>> = emptyList(),
    // Used only to keep track of data round
    var oldData2: List<List<Double>> = emptyList(),
    var requiredData: List<List<Double>> = emptyList(),
    var dataRound: Int = 0,
) {

    companion object {
        fun parse(data: JsonObject): Node {
            val name = data.get("name").asString
            var type = NodeType.General
            if(data.has("type")) {
                type = NodeType.valueOf(data.get("type").asString)
            }
            var minArity = -1
            if(data.has("minArity")) {
                minArity = data.get("minArity").asInt
            }
            var maxArity = -1
            if(data.has("maxArity")) {
                maxArity = data.get("maxArity").asInt
            }
            var isCyclic = false
            if(data.has("isCyclic")) {
                isCyclic = data.get("isCyclic").asBoolean
            }
            var sccId = -1
            if(data.has("sccId")) {
                sccId = data.get("sccId").asInt
            }

            return Node(name = name, type = type, minArity = minArity, maxArity = maxArity, isCyclic = isCyclic, sccId = sccId)
        }
    }


    fun toJson(): JsonObject {
        val data = JsonObject()
        data.addProperty("name", this.name)
        data.addProperty("type", this.type.name)
        data.addProperty("minArity", this.minArity)
        data.addProperty("maxArity", this.maxArity)
        data.addProperty("isCyclic", this.isCyclic)
        data.addProperty("sccId", this.sccId)
        return data
    }

    /**
     * Returns the label of the ege
     * Used in DG Drawer for labeling the drawn edge
     */
    fun getLabel():String {
        return  "${this.name}" +
                if(this.minArity >= 0 ) "/${this.minArity}" else ""
    }

    // We override it as only the name is important for matching purposes
    // This also fixes issues when other properties are changed
    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (name != other.name) return false

        return true
    }

    fun addData(entryWithoutRound: List<Double>) {
        // 1. Extract all data which match the given input without considering time
        // 2. Add data to relevant data
        // 3. Merge time of relevant data
        // 4. Extract the biggest interval matching the given entry and insert into data
        // 5. Merge time of data

        val relevantData = this.oldData.filter {
            it.take(it.size-2) == entryWithoutRound.take(entryWithoutRound.size-2)
        }

        val requiredData = Utils.inverseIntervals(relevantData.filter { it[it.size-2] <= entryWithoutRound[it.size-1] && it[it.size-1] >= entryWithoutRound[it.size-2] }, entryWithoutRound)
        // Only add data if required, otherwise data was already generated
        if (requiredData.isEmpty()) {
            return
        }
        this.requiredData = this.requiredData + requiredData


        val relevantDataAdded = relevantData + listOf(entryWithoutRound)


        val mergedRelevantData = Utils.mergeIntervals(Utils.cleanIntervals(relevantDataAdded))

        val newData = mergedRelevantData.filter { it[it.size-2] <= entryWithoutRound[it.size-1] && it[it.size-1] >= entryWithoutRound[it.size-2] }
        assert(newData.size == 1)

        val data1 = this.data.filter {
            it.take(it.size-2) == entryWithoutRound.take(entryWithoutRound.size-2)
        }

        val data2 = this.data.minus(data1)
        this.data = data2 + Utils.mergeIntervals(Utils.cleanIntervals(data1 + newData))
    }

    fun addJoin(edge1: IntersectionEdge, edge2: IntersectionEdge) {
        val newData = mutableListOf<List<Double>>()

        // Join new data with all data
        if (edge1.joinInputData.isNotEmpty()) {
            applyJoin(edge1, edge2, newData)
            edge1.joinInputData = emptyList()
        }

        // Join new data with all data
        if (edge2.joinInputData.isNotEmpty()) {
            applyJoin(edge2, edge1, newData)
            edge2.joinInputData = emptyList()
        }

        this.data = this.data + newData
    }

    private fun applyJoin(edge1: IntersectionEdge, edge2: IntersectionEdge, newData: MutableList<List<Double>>) {
        edge2.joinInputDataAll.forEach { data2 ->
            edge1.joinInputData.forEach forEach2@{ data1 ->
                val data = mutableListOf<Double>()

                val overlaps = data2[data2.size-2] <= data1[data1.size-1] && data2[data2.size-1] >= data1[data1.size-2]

                if (!overlaps) {
                    return@forEach2
                }

                var valid = true
                for (i in 0 until this.minArity) {
                    if (data1[i] < 0) {
                        data.add(data2[i])
                    } else if(data2[i] < 0) {
                        data.add(data1[i])
                    } else if (data1[i] == data2[i]) {
                        data.add(data1[i])
                    } else {
                        valid = false
                        break
                    }
                }
                if (valid) {
                    data.add(max(data2[data2.size-2],data1[data1.size-2]))
                    data.add(min(data2[data2.size-1],data1[data1.size-1]))
                    newData.add(data)
                }
            }
        }
    }

    // Debug only
    override fun toString(): String {
        return "$name/$minArity/$maxArity"
    }


}