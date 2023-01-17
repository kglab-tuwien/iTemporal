package at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.AggregationType
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node

class GenericAggregationEdge(
    override var from: Node,
    override var to: Node,
    override var isCyclic: Boolean = false,
    override var termOrder: List<Int> = emptyList(),
    override var numberOfGroupingTerms: Int = -1,
    override var numberOfContributors: Int = -1,
    override var aggregationType: AggregationType = AggregationType.Unknown,
    override var uniqueId: String = NameGenerator.getUniqueName(),
    override var termOrderShuffleAllowed: Boolean = true,
    override var termOrderReference: String? = null,
    override var existentialCount: Int = 0,
) : AggregationEdge {

    override fun backwardPropagateData() {
        throw RuntimeException("Backward Propagation not allowed for this edge type")
    }

    override fun forwardPropagateData() {
        throw RuntimeException("Forward Propagation not allowed for this edge type")
    }

    override fun getLabel(): String {
        return "GenericAggregationEdge"
    }

    override fun copy(): Edge {
        return GenericAggregationEdge(from=from, to=to, isCyclic=isCyclic, termOrder=termOrder, numberOfGroupingTerms=numberOfGroupingTerms, numberOfContributors=numberOfContributors,aggregationType=aggregationType, termOrderShuffleAllowed = termOrderShuffleAllowed, termOrderReference = termOrderReference)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenericAggregationEdge

        if (from != other.from) return false
        if (to != other.to) return false
        if (isCyclic != other.isCyclic) return false
        if (termOrder != other.termOrder) return false
        if (numberOfGroupingTerms != other.numberOfGroupingTerms) return false
        if (numberOfContributors != other.numberOfContributors) return false
        if (aggregationType != other.aggregationType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + isCyclic.hashCode()
        result = 31 * result + termOrder.hashCode()
        result = 31 * result + numberOfGroupingTerms
        result = 31 * result + numberOfContributors
        result = 31 * result + aggregationType.hashCode()
        return result
    }


}