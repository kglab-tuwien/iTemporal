package at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.AggregationType
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import com.google.gson.JsonObject

class MWTAEdge(
    override var from: Node,
    override var to: Node,
    override var isCyclic: Boolean = false,
    override var termOrder: List<Int> = emptyList(),
    override var numberOfGroupingTerms: Int = -1,
    override var numberOfContributors: Int = -1,
    override var aggregationType: AggregationType = AggregationType.Unknown,
    var t1: Double = -1.0,
    var t2: Double = -1.0,
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
        return "MWTA"
    }

    override fun toJson(): JsonObject {
        val data = super.toJson()
        data.addProperty("t1", t1)
        data.addProperty("t2", t2)
        return data
    }

    override fun setJsonProperties(data: JsonObject) {
        super.setJsonProperties(data)
        if (data.has("t1")) {
            this.t1 = data.get("t1").asDouble
        }
        if (data.has("t2")) {
            this.t2 = data.get("t2").asDouble
        }
    }


    override fun copy(): Edge {
        return MWTAEdge(from=from, to=to, isCyclic=isCyclic, termOrder=termOrder, numberOfGroupingTerms=numberOfGroupingTerms, numberOfContributors=numberOfContributors,aggregationType=aggregationType,t1=t1,t2=t2, termOrderShuffleAllowed = termOrderShuffleAllowed, termOrderReference = termOrderReference)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MWTAEdge

        if (from != other.from) return false
        if (to != other.to) return false
        if (isCyclic != other.isCyclic) return false
        if (termOrder != other.termOrder) return false
        if (numberOfGroupingTerms != other.numberOfGroupingTerms) return false
        if (numberOfContributors != other.numberOfContributors) return false
        if (aggregationType != other.aggregationType) return false
        if (t1 != other.t1) return false
        if (t2 != other.t2) return false

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
        result = 31 * result + t1.hashCode()
        result = 31 * result + t2.hashCode()
        return result
    }

    override fun toString(): String {
        return "MWTAEdge(from=$from, to=$to, isCyclic=$isCyclic, termOrder=$termOrder, numberOfGroupingTerms=$numberOfGroupingTerms, numberOfContributors=$numberOfContributors, aggregationType=$aggregationType, t1=$t1, t2=$t2)"
    }


}