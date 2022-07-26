package at.ac.tuwien.dbai.kg.iTemporal.core.assignments

import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.OtherPropertyAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.IntersectionEdge
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator

/**
 * Calls the assignment of the term order for a given edge.
 * Handles intersection edges with a special treatment.
 */
object TermOrderPropertyAssigner : OtherPropertyAssignment {
    override fun getPriority(): Int = 65

    private fun assignReferenceEdge(edge: Edge, dependencyGraph: DependencyGraph): Boolean {
        val referenceEdge = dependencyGraph.inEdges.values.flatten().first { it.uniqueId == edge.termOrderReference }
        if (referenceEdge.termOrder.size != referenceEdge.from.minArity) {
            return false
        }
        if (referenceEdge.termOrder.size != edge.termOrder.size) {
            throw RuntimeException("Invalid reference, reference does not match properties")
        }
        // Copy term order
        edge.termOrder = ArrayList(referenceEdge.termOrder)
        return true
    }

    override fun run(dependencyGraph: DependencyGraph): DependencyGraph {

        val edges = dependencyGraph.inEdges.values.flatten()

        val referenceEdges = mutableListOf<Edge>()

        for (edge in edges) {
            if (edge.termOrderReference != null) {
                referenceEdges.add(edge)
                continue
            }
            if (edge is IntersectionEdge) {
                val node = edge.to
                if (dependencyGraph.inEdges[node].orEmpty().size != 2) {
                    throw RuntimeException("invalid number of intersection edges")
                }

                val edge1 = dependencyGraph.inEdges[node]!![0] as IntersectionEdge
                val edge2 = dependencyGraph.inEdges[node]!![1] as IntersectionEdge

                if (edge1.termOrder.size == edge1.from.minArity && edge2.termOrder.size == edge2.from.minArity) {
                    continue
                }

                val requiredNodes = Array(node.minArity) { index -> index }.toList()//.shuffled(RandomGenerator.sharedRandom)

                val overlappingIndices = requiredNodes.take(edge1.overlappingTerms)
                val edge1Indices = requiredNodes.drop(edge1.overlappingTerms).take(edge1.nonOverlappingTerms)
                val edge2Indices = requiredNodes.drop(edge1.overlappingTerms + edge1.nonOverlappingTerms)
                    .take(edge2.nonOverlappingTerms)

                edge1.termOrder =
                    (overlappingIndices + edge1Indices + (Array(edge1.from.minArity - edge1.overlappingTerms - edge1.nonOverlappingTerms) { index -> -1 }.toList()))
                if (edge1.from.sccId != edge1.to.sccId) {
                    edge1.termOrder = edge1.termOrder.shuffled(RandomGenerator.sharedRandom)
                }

                edge2.termOrder =
                    (overlappingIndices + edge2Indices + (Array(edge2.from.minArity - edge2.overlappingTerms - edge2.nonOverlappingTerms) { index -> -1 }.toList()))
                if (edge2.from.sccId != edge2.to.sccId) {
                    edge2.termOrder = edge2.termOrder.shuffled(RandomGenerator.sharedRandom)
                }

            } else {
                // We only allow shuffling if it is not recursive to reduce data possibilities caused for example by aggregation
                edge.assignOrder(edge.from.sccId != edge.to.sccId)
            }
        }

        while (referenceEdges.isNotEmpty()) {
            val edge = referenceEdges.removeFirst()
            val added = assignReferenceEdge(edge, dependencyGraph)

            if (!added) {
                referenceEdges.add(edge)
            }
        }



        return dependencyGraph
    }

}