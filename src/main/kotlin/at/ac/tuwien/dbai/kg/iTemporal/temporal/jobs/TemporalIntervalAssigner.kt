package at.ac.tuwien.dbai.kg.iTemporal.temporal.jobs

import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.OtherPropertyAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.TemporalSingleEdge
import kotlin.math.max
import kotlin.math.min

/**
 * This job assigns the temporal interval property.
 */
object TemporalIntervalAssigner : OtherPropertyAssignment {
    override fun getPriority(): Int = 27

    override fun run(dependencyGraph: DependencyGraph): DependencyGraph {
        // Unhandled Intersection Nodes
        val temporalEdges = dependencyGraph.inEdges.values.flatten()
            .filter { edge -> edge is TemporalSingleEdge && (edge.t1 < 0 || edge.t2 < 0) }
            .map { it as TemporalSingleEdge }

        if (temporalEdges.isEmpty()) {
            return dependencyGraph
        }

        for (temporalEdge in temporalEdges) {
            if (temporalEdge.t1 < 0) {
                temporalEdge.t1 = Registry.properties.temporalFactor * RandomGenerator.getNextDoubleWithPrecision(
                    Registry.properties.averageNumberOfTemporalUnitsT1,
                    Registry.properties.varianceNumberOfTemporalUnitsT1,
                    Registry.properties.temporalMaxPrecision
                )
            }

            if (temporalEdge.t2 < 0) {
                // Random value must be at least as high as t1
                temporalEdge.t2 = max(
                    temporalEdge.t1,
                    Registry.properties.temporalFactor * RandomGenerator.getNextDoubleWithPrecision(
                        Registry.properties.averageNumberOfTemporalUnitsT2,
                        Registry.properties.varianceNumberOfTemporalUnitsT2,
                        Registry.properties.temporalMaxPrecision
                    )
                )
            }

            // In case t2 was set by external source, t1 must be at most as high as t2
            temporalEdge.t1 = min(temporalEdge.t1,temporalEdge.t2)
        }

        return dependencyGraph
    }

}