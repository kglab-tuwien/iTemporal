package at.ac.tuwien.dbai.kg.iTemporal.core.edges

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node

class GenericEdge(override var from: Node,
                  override var to: Node,
                  override var isCyclic: Boolean = false,
                  override var termOrder: List<Int> = listOf(),
                  override var uniqueId: String = NameGenerator.getUniqueName(),
                  override var termOrderShuffleAllowed: Boolean = true,
                  override var termOrderReference: String? = null,
) :Edge {
    override fun backwardPropagateData() {
        throw RuntimeException("Backward Propagation not allowed for this edge type")
    }

    override fun forwardPropagateData() {
        throw RuntimeException("Forward Propagation not allowed for this edge type")
    }

    override fun getLabel(): String {
        return ""
    }

    override fun copy(): Edge {
        return GenericEdge(from, to, isCyclic, termOrder, termOrderShuffleAllowed = termOrderShuffleAllowed, termOrderReference = termOrderReference)
    }

    fun toGenericSingleEdge():GenericSingleEdge {
        return GenericSingleEdge(from, to, isCyclic, termOrder)
    }

    fun toGenericMultiEdge():GenericMultiEdge {
        return GenericMultiEdge(from, to, isCyclic, termOrder)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GenericEdge) return false

        if (from != other.from) return false
        if (to != other.to) return false
        if (isCyclic != other.isCyclic) return false
        if (termOrder != other.termOrder) return false
        if (uniqueId != other.uniqueId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + isCyclic.hashCode()
        result = 31 * result + termOrder.hashCode()
        result = 31 * result + uniqueId.hashCode()
        return result
    }

    override fun toString(): String {
        return "$from -> $to"
    }


}