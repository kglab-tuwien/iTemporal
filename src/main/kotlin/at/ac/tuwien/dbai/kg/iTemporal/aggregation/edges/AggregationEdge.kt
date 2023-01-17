package at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges

import at.ac.tuwien.dbai.kg.iTemporal.aggregation.AggregationType
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.SingleEdge
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import com.google.gson.JsonObject

/**
 * Defines the general properties of an aggregation edge.
 */
interface AggregationEdge:SingleEdge {
    var numberOfContributors: Int
    var numberOfGroupingTerms: Int
    var aggregationType: AggregationType

    override fun toJson(): JsonObject {
        val data = super.toJson()
        data.addProperty("numberOfGroupingTerms", numberOfGroupingTerms)
        data.addProperty("numberOfContributors", numberOfContributors)
        data.addProperty("aggregationType", aggregationType.name)
        return data
    }

    override fun setJsonProperties(data: JsonObject) {
        super.setJsonProperties(data)
        if (data.has("numberOfGroupingTerms")) {
            this.numberOfGroupingTerms = data.get("numberOfGroupingTerms").asInt
        }
        if (data.has("numberOfContributors")) {
            this.numberOfContributors = data.get("numberOfContributors").asInt
        }
        if (data.has("aggregationType")) {
            this.aggregationType = AggregationType.valueOf(data.get("aggregationType").asString)
        }
    }

    override fun assignOrder(shuffle: Boolean) {
        if (this.termOrder.size != this.from.minArity) {
            val fromArity = this.from.minArity

            // Calculate random order of input properties including skipping aggregation properties.
            val data = Array(fromArity) { index -> index + if (index >= this.to.minArity - 1) 1 else 0 }.toList()
            if (shuffle) {
                this.termOrder = data.shuffled(RandomGenerator.sharedRandom)
            } else {
                this.termOrder = data
            }
            this.termOrder = this.termOrder.map { if (it < this.to.minArity-1 || it >= this.to.minArity && it <= this.to.minArity + this.numberOfContributors) it else -1 }
        }
    }
}