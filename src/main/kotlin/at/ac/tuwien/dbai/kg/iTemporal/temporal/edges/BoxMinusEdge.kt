package at.ac.tuwien.dbai.kg.iTemporal.temporal.edges

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node

class BoxMinusEdge(override var from: Node,
                   override var to: Node,
                   override var isCyclic: Boolean = false,
                   override var termOrder: List<Int> = listOf(),
                   override var t1: Double = -1.0,
                   override var t2: Double = -1.0,
                   override var uniqueId: String = NameGenerator.getUniqueName(),
                   override var termOrderShuffleAllowed: Boolean = true,
                   override var termOrderReference: String? = null,
) : TemporalSingleEdge {

    override fun timeIntervalBackward(values: List<Double>): List<Double> {
        return listOf(values[to.minArity] - t2, values[to.minArity+1] - t1)
    }

    override fun timeIntervalForward(values: List<Double>): List<Double> {
        return listOf(values[from.minArity] + t2, values[from.minArity+1] + t1)
    }

    override fun applyInterval(interval: Interval): Interval {
        return Interval(interval.t1 + this.t2, interval.t2 + this.t1)
    }

    override fun applyIntervalBackward(interval: Interval): Interval {
        return Interval(interval.t1 - this.t2, interval.t2 - this.t1)
    }

    override fun getLabel(): String {
        return "[-]"
    }

    override fun copy(): Edge {
        return BoxMinusEdge(from=from, to=to, isCyclic=isCyclic, termOrder=termOrder, t1=t1, t2=t2, termOrderShuffleAllowed = termOrderShuffleAllowed, termOrderReference = termOrderReference)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BoxMinusEdge) return false

        if (from != other.from) return false
        if (to != other.to) return false
        if (isCyclic != other.isCyclic) return false
        if (termOrder != other.termOrder) return false
        if (t1 != other.t1) return false
        if (t2 != other.t2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + isCyclic.hashCode()
        result = 31 * result + termOrder.hashCode()
        result = 31 * result + t1.hashCode()
        result = 31 * result + t2.hashCode()
        return result
    }

    override fun toString(): String {
        return "BoxMinusEdge(from=$from, to=$to, isCyclic=$isCyclic, termOrder=$termOrder, t1=$t1, t2=$t2, uniqueId='$uniqueId')"
    }


}