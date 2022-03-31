package at.ac.tuwien.dbai.kg.iTemporal.temporal.edges

import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import com.google.gson.JsonObject

interface TemporalEdge : Edge {
    var t1: Double
    var t2: Double

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
}