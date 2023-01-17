package at.ac.tuwien.dbai.kg.iTemporal.core.edges

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.lang.RuntimeException


enum class ConditionOperator {
    LOWER,
    LOWER_EQUAL,
    EQUAL,
    GREATER,
    GREATER_EQUAL,
    NOTEQUAL,
    ;

    fun getOperator() : String {
        return when(this) {
            LOWER -> "<"
            LOWER_EQUAL -> "<="
            EQUAL -> "=="
            GREATER -> ">"
            GREATER_EQUAL -> ">="
            NOTEQUAL -> "<>"
        }
    }

    fun getOperatorSQL() : String {
        return when(this) {
            LOWER -> "<"
            LOWER_EQUAL -> "<="
            EQUAL -> "="
            GREATER -> ">"
            GREATER_EQUAL -> ">="
            NOTEQUAL -> "<>"
        }
    }
}

abstract class Condition() {
    abstract val variable: Int
    abstract val type:ConditionOperator

    companion object {
        // We simplify as only two types exist
        fun parse(data: JsonObject): Condition {
            val variable = data.get("variable").asInt
            val type = data.get("type").asString
            val conditionOperator = ConditionOperator.valueOf(data.get("operator").asString)

            return when(type) {
                "value" -> ValueCondition(variable, conditionOperator,  data.get("value").asDouble )
                "variable" -> VarCondition(variable, conditionOperator,  data.get("variable2").asInt )
                else -> throw RuntimeException("invalid condition type")
            }
        }
    }

    open fun toJson(): JsonObject {
        val data = JsonObject()
        data.addProperty("variable", variable)
        data.addProperty("operator", type.name)
        return data
    }


}

data class ValueCondition(override val variable: Int, override  val type:ConditionOperator, val value:Double) : Condition() {
    override fun toJson(): JsonObject {
        val data = super.toJson()
        data.addProperty("value", value)
        data.addProperty("type", "value")
        return data
    }
}

data class VarCondition(override val variable: Int, override val type:ConditionOperator, val variable2:Int) : Condition() {
    override fun toJson(): JsonObject {
        val data = super.toJson()
        data.addProperty("variable2", variable2)
        data.addProperty("type", "variable")
        return data
    }
}

/**
 * At the moment, we ignore conditions here, and only use in rule generator
 */
class ConditionalEdge(override var from: Node,
                      override var to: Node,
                      override var isCyclic: Boolean = false,
                      override var termOrder: List<Int> = listOf(),
                      override var uniqueId: String = NameGenerator.getUniqueName(),
                      var conditions: Set<Condition> = setOf(),
                      override var termOrderShuffleAllowed: Boolean = true,
                      override var termOrderReference: String? = null,
                      override var existentialCount: Int = 0,
) :CoreSingleEdge {


    override fun toJson(): JsonObject {
        val data = super.toJson()
        val conditions = JsonArray()
        this.conditions.forEach {
            conditions.add(it.toJson())
        }
        data.add("conditions",conditions)
        return data
    }

    override fun setJsonProperties(data: JsonObject) {
        super.setJsonProperties(data)
        if(data.has("conditions")) {
            this.conditions = data.getAsJsonArray("conditions").map {
                Condition.parse(it.asJsonObject)
            }.toSet()
        }
    }

    override fun backwardPropagateData() {
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

            // Add time points
            newEntry[to.minArity] = entry[from.minArity]
            newEntry[to.minArity+1] = entry[from.minArity+1]
            newData.add(newEntry.toList())
        }

        this.to.data = this.to.data + newData
    }

    override fun getLabel(): String {
        return "Conditional"
    }

    override fun copy(): Edge {
        return ConditionalEdge(from, to, isCyclic, termOrder, termOrderShuffleAllowed = termOrderShuffleAllowed, termOrderReference = termOrderReference)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConditionalEdge

        if (from != other.from) return false
        if (to != other.to) return false
        if (isCyclic != other.isCyclic) return false
        if (termOrder != other.termOrder) return false
        if (conditions != other.conditions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + isCyclic.hashCode()
        result = 31 * result + termOrder.hashCode()
        result = 31 * result + conditions.hashCode()
        return result
    }

    override fun toString(): String {
        return "ConditionalEdge(from=$from, to=$to, isCyclic=$isCyclic, termOrder=$termOrder, uniqueId='$uniqueId', conditions='$conditions')"
    }


}