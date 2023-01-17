package at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.AggregationType
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.temporal.TriangleUnit
import com.google.gson.JsonObject

class STAEdge(
    override var from: Node,
    override var to: Node,
    override var isCyclic: Boolean = false,
    override var termOrder: List<Int> = emptyList(),
    override var numberOfGroupingTerms: Int = -1,
    override var numberOfContributors: Int = -1,
    override var aggregationType: AggregationType = AggregationType.Unknown,
    var unit: TriangleUnit = TriangleUnit.Unknown,
    override var uniqueId: String = NameGenerator.getUniqueName(),
    override var termOrderShuffleAllowed: Boolean = true,
    override var termOrderReference: String? = null,
    override var existentialCount: Int = 0,
) : AggregationEdge {

    override fun backwardPropagateData() {
        throw RuntimeException("Backward Propagation not allowed for this edge type")
    }

    override fun forwardPropagateData() {
        throw RuntimeException("Forward Propagation not allowed for this edge type")
    }

    override fun getLabel(): String {
        return "STA"
    }

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

    override fun copy(): Edge {
        return STAEdge(from=from, to=to, isCyclic=isCyclic, termOrder=termOrder, numberOfGroupingTerms=numberOfGroupingTerms, numberOfContributors=numberOfContributors,aggregationType=aggregationType,unit=unit, termOrderShuffleAllowed = termOrderShuffleAllowed, termOrderReference = termOrderReference)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as STAEdge

        if (from != other.from) return false
        if (to != other.to) return false
        if (isCyclic != other.isCyclic) return false
        if (termOrder != other.termOrder) return false
        if (numberOfGroupingTerms != other.numberOfGroupingTerms) return false
        if (numberOfContributors != other.numberOfContributors) return false
        if (aggregationType != other.aggregationType) return false
        if (unit != other.unit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + isCyclic.hashCode()
        result = 31 * result + termOrder.hashCode()
        result = 31 * result + numberOfGroupingTerms
        result = 31 * result + numberOfContributors
        result = 31 * result + aggregationType.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }

    override fun toString(): String {
        return "STAEdge(from=$from, to=$to, isCyclic=$isCyclic, termOrder=$termOrder, numberOfGroupingTerms=$numberOfGroupingTerms, numberOfContributors=$numberOfContributors, aggregationType=$aggregationType, unit=$unit)"
    }


}