package at.ac.tuwien.dbai.kg.iTemporal.temporal.edges

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node

class UntilEdge(override var from: Node,
                override var to: Node,
                override var isCyclic: Boolean = false,
                override var termOrder: List<Int> = listOf(),
                override var t1: Double = -1.0,
                override var t2: Double = -1.0,
                override var isLeftEdge: Boolean = false, // describes the position in the since operation
                override var uniqueId: String = NameGenerator.getUniqueName(),
                override var termOrderShuffleAllowed: Boolean = true,
                override var termOrderReference: String? = null,
                override var existentialCount: Int = 0,
) : TemporalMultiEdge {

    override fun getLabel(): String {
        return "U"
    }

    override fun copy(): Edge {
        return UntilEdge(from=from, to=to, isCyclic=isCyclic, termOrder=termOrder, t1=t1, t2=t2, isLeftEdge=isLeftEdge, termOrderShuffleAllowed = termOrderShuffleAllowed, termOrderReference = termOrderReference)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UntilEdge) return false

        if (from != other.from) return false
        if (to != other.to) return false
        if (isCyclic != other.isCyclic) return false
        if (termOrder != other.termOrder) return false
        if (t1 != other.t1) return false
        if (t2 != other.t2) return false
        if (uniqueId != other.uniqueId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + isCyclic.hashCode()
        result = 31 * result + termOrder.hashCode()
        result = 31 * result + t1.hashCode()
        result = 31 * result + t2.hashCode()
        result = 31 * result + uniqueId.hashCode()
        return result
    }


}