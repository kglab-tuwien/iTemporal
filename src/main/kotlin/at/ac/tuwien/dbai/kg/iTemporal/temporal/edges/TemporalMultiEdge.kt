package at.ac.tuwien.dbai.kg.iTemporal.temporal.edges

import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.MultiEdge
import com.google.gson.JsonObject

interface TemporalMultiEdge : MultiEdge, TemporalEdge {

    var isLeftEdge: Boolean

    override fun backwardPropagateData() {
        throw RuntimeException("Backward Propagation not allowed for this edge type")
    }

    override fun forwardPropagateData() {
        throw RuntimeException("Forward Propagation not allowed for this edge type")
    }

    override fun toJson(): JsonObject {
        val data = super<TemporalEdge>.toJson()
        data.addProperty("isLeftEdge", isLeftEdge)
        return data
    }

    override fun setJsonProperties(data: JsonObject) {
        super<TemporalEdge>.setJsonProperties(data)
        if (data.has("isLeftEdge")) {
            this.isLeftEdge = data.get("isLeftEdge").asBoolean
        }
    }

}