package at.ac.tuwien.dbai.kg.iTemporal.core.edges

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node

class LinearEdge(override var from: Node,
                 override var to: Node,
                 override var isCyclic: Boolean = false,
                 override var termOrder: List<Int> = listOf(),
                 override var uniqueId: String = NameGenerator.getUniqueName(),
                 override var termOrderShuffleAllowed: Boolean = true,
                 override var termOrderReference: String? = null,
                 override var existentialCount: Int = 0,
) :CoreSingleEdge {
    override fun backwardPropagateData() {
        val newData = mutableListOf<List<Double>>()


        for(entry in to.data) {
            val newEntry = Array(from.minArity+2){index -> -100.0}
            for ((fromIndex, orderId) in this.termOrder.withIndex()) {
                if (orderId < 0) {
                    newEntry[fromIndex] = RandomGenerator.generateTerm(Registry.properties.cardinalityTermDomain)
                } else if (orderId >= 0 && orderId < to.minArity) {
                    newEntry[fromIndex] = entry[orderId]
                } else {
                    newEntry[fromIndex] = RandomGenerator.generateTerm(Registry.properties.cardinalityTermDomain)
                }
            }

            // Add time interval
            newEntry[from.minArity] = entry[to.minArity]
            newEntry[from.minArity+1] = entry[to.minArity+1]
            newData.add(newEntry.toList())
        }


        this.from.data = this.from.data + newData
    }

    override fun forwardPropagateData() {
        val newData = mutableListOf<List<Double>>()

        for(entry in from.data) {
            val newEntry = Array(to.minArity+2){index -> -100.0}

            for ((fromIndex, orderId) in this.termOrder.withIndex()) {
                // Not relevant term
                if (orderId < 0 || orderId >= to.minArity) {
                    continue
                }
                if (orderId < to.minArity) {
                    newEntry[orderId] = entry[fromIndex]
                }
            }

            // Add time points
            newEntry[to.minArity] = entry[from.minArity]
            newEntry[to.minArity+1] = entry[from.minArity+1]
            newData.add(newEntry.toList())
        }

        this.to.data = this.to.data + newData
    }

    override fun getLabel(): String {
        return "Linear"
    }

    override fun copy(): Edge {
        return LinearEdge(from, to, isCyclic, termOrder, termOrderShuffleAllowed = termOrderShuffleAllowed, termOrderReference = termOrderReference)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LinearEdge

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

    override fun toString(): String {
        return "LinearEdge(from=$from, to=$to, isCyclic=$isCyclic, termOrder=$termOrder, uniqueId='$uniqueId')"
    }


}