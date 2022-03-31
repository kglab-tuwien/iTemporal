package at.ac.tuwien.dbai.kg.iTemporal.temporal.jobs

import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.OtherPropertyAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.temporal.TriangleUnit
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.TriangleUpEdge
import kotlin.random.Random

/**
 * This job assigns the triangle unit property.
 */
object TriangleUnitAssigner : OtherPropertyAssignment {
    override fun getPriority(): Int = 32

    override fun run(dependencyGraph: DependencyGraph): DependencyGraph {
        val triangleUpEdges = dependencyGraph.inEdges.values.flatten().filter { edge -> edge is TriangleUpEdge && (edge.unit == TriangleUnit.Unknown) }.map { it as TriangleUpEdge }

        if (triangleUpEdges.isEmpty()) {
            return dependencyGraph
        }

        for (triangleUpEdge in triangleUpEdges) {
            val randomSelection = Random.nextInt(0, 4)
            triangleUpEdge.unit = TriangleUnit.values()[randomSelection]
        }

        return dependencyGraph
    }

}