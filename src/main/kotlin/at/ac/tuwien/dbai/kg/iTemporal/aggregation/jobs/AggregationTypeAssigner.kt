package at.ac.tuwien.dbai.kg.iTemporal.aggregation.jobs

import at.ac.tuwien.dbai.kg.iTemporal.aggregation.AggregationType
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.ITAEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.OtherPropertyAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import kotlin.random.Random

/**
 * Assigns an aggregation type to each aggregation edge, e.g. min, max, sum, count
 */
object AggregationTypeAssigner : OtherPropertyAssignment {
    override fun getPriority(): Int = 99

    override fun run(dependencyGraph: DependencyGraph): DependencyGraph {
        val aggregationEdges = dependencyGraph.inEdges.values.flatten()
            .filter { edge -> edge is ITAEdge && (edge.aggregationType == AggregationType.Unknown) }
            .map { it as ITAEdge }

        if (aggregationEdges.isEmpty()) {
            return dependencyGraph
        }

        for (aggregationEdge in aggregationEdges) {
            val randomSelection = Random.nextInt(0, 4)
            aggregationEdge.aggregationType = AggregationType.values()[randomSelection]
        }

        return dependencyGraph
    }

}