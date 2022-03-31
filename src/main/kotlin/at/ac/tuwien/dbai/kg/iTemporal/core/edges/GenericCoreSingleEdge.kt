package at.ac.tuwien.dbai.kg.iTemporal.core.edges

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node

class GenericCoreSingleEdge(override var from: Node,
                            override var to: Node,
                            override var isCyclic: Boolean = false,
                            override var termOrder: List<Int> = listOf(),
                            override var uniqueId: String = NameGenerator.getUniqueName(),
                            override var termOrderShuffleAllowed: Boolean = true,
                            override var termOrderReference: String? = null,
) :CoreSingleEdge {
    override fun backwardPropagateData() {
        throw RuntimeException("Backward Propagation not allowed for this edge type")
    }

    override fun forwardPropagateData() {
        throw RuntimeException("Backward Propagation not allowed for this edge type")
    }

    override fun getLabel(): String {
        return "GenericCoreSingleEdge"
    }

    override fun copy(): Edge {
        return GenericCoreSingleEdge(from, to, isCyclic, termOrder, termOrderShuffleAllowed = termOrderShuffleAllowed, termOrderReference = termOrderReference)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenericCoreSingleEdge

        if (from != other.from) return false
        if (to != other.to) return false
        if (isCyclic != other.isCyclic) return false
        if (termOrder != other.termOrder) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + isCyclic.hashCode()
        result = 31 * result + termOrder.hashCode()
        return result
    }


}