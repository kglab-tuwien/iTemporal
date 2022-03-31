package at.ac.tuwien.dbai.kg.iTemporal.ruleGenerators.questdbGenerator

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import java.lang.RuntimeException

abstract class BasicEdge(override var from: Node,
                         override var to: Node,
                         override var isCyclic: Boolean = false,
                         override var termOrder: List<Int> = emptyList(),
                         override var uniqueId: String = NameGenerator.getUniqueName(),
                         override var termOrderShuffleAllowed: Boolean = true,
                         override var termOrderReference: String? = null,
                         ) :Edge {
    override fun backwardPropagateData() {
        throw RuntimeException("Invalid operation")
    }

    override fun forwardPropagateData() {
        throw RuntimeException("Invalid operation")
    }

    override fun copy(): Edge {
        throw RuntimeException("Invalid operation")
    }

}