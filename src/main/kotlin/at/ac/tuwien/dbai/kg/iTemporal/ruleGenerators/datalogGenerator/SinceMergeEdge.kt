package at.ac.tuwien.dbai.kg.iTemporal.ruleGenerators.datalogGenerator

import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node

class SinceMergeEdge(
    override var from: Node,
    var from2: Node,
    override var termOrder: List<Int>,
    var termOrder2: List<Int>,
    var t1: Double,
    var t2: Double,
    override var to: Node,
    override var uniqueId: String = "no uniquenes required",
    override var isCyclic: Boolean = false,
    override var termOrderShuffleAllowed: Boolean = false,
    override var termOrderReference: String? = null
) : Edge {
    override fun backwardPropagateData() {
        throw NotImplementedError()
    }

    override fun forwardPropagateData() {
        throw NotImplementedError()
    }

    override fun getLabel(): String {
        return "S"
    }

    override fun copy(): Edge {
        throw NotImplementedError()
    }

}