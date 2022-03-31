package at.ac.tuwien.dbai.kg.iTemporal.core.assignments

import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.ArityPropertyAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.IntersectionEdge
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Assigns the number of join terms and number of required non-join terms to the nodes/edges.
 */
object IntersectionPropertyAssigner : ArityPropertyAssignment {
    override fun getPriority(): Int = 100



    fun selectEdge(intersectionNodes: List<Node>, dependencyGraph: DependencyGraph): Node {
        for (intersectionNode in intersectionNodes) {
            if (selectEdgeHelper(intersectionNode, dependencyGraph)) {
                return intersectionNode
            }
        }

        throw RuntimeException("Circular dependency of reference nodes not allowed")

    }
    fun selectEdgeHelper(intersectionNode: Node, dependencyGraph: DependencyGraph):Boolean {
        val e1 = dependencyGraph.inEdges[intersectionNode]!![0] as IntersectionEdge
        val e2 = dependencyGraph.inEdges[intersectionNode]!![1] as IntersectionEdge

        var referenceEdge1:IntersectionEdge? = null
        var referenceEdge2:IntersectionEdge? = null

        if (e1.termOrderReference != null) {
            val tmpReferenceEdge1 = dependencyGraph.inEdges.values.flatten().first { it.uniqueId == e1.termOrderReference }
            if (tmpReferenceEdge1 is IntersectionEdge) {
                referenceEdge1 = tmpReferenceEdge1
            }
        }

        if (e2.termOrderReference != null) {
            val tmpReferenceEdge2 = dependencyGraph.inEdges.values.flatten().first { it.uniqueId == e2.termOrderReference }
            if (tmpReferenceEdge2 is IntersectionEdge) {
                referenceEdge2 = tmpReferenceEdge2
            }
        }

        if (referenceEdge1 == null && referenceEdge2 == null) {
            return true
        }

        // Check that all reference edges are handled

        if (referenceEdge1 != null) {
            // Too early, other edges have to be handled first
            if (referenceEdge1.overlappingTerms == -1 || referenceEdge1.nonOverlappingTerms == -1) {
                return false;
            }
        }

        if (referenceEdge2 != null) {
            // Too early, other edges have to be handled first
            if (referenceEdge2.overlappingTerms == -1 || referenceEdge2.nonOverlappingTerms == -1) {
                return false;
            }
        }

        if (referenceEdge1 != null && referenceEdge2 != null) {
            // validation check that both reference edges match
            if(referenceEdge1.overlappingTerms != referenceEdge2.overlappingTerms) {
                throw RuntimeException("Incompatible reference edges")
            }
        }

        if (referenceEdge1 != null) {
            e1.overlappingTerms = referenceEdge1.overlappingTerms
            e1.nonOverlappingTerms = referenceEdge1.nonOverlappingTerms
            e2.overlappingTerms = referenceEdge1.overlappingTerms
            e2.nonOverlappingTerms = intersectionNode.minArity - e2.overlappingTerms - e1.nonOverlappingTerms
            return true
        }

        if (referenceEdge2 != null) {
            e2.overlappingTerms = referenceEdge2.overlappingTerms
            e2.nonOverlappingTerms = referenceEdge2.nonOverlappingTerms
            e1.overlappingTerms = referenceEdge2.overlappingTerms
            e1.nonOverlappingTerms = intersectionNode.minArity - e1.overlappingTerms - e2.nonOverlappingTerms
            return true
        }

        throw RuntimeException("You should not be here")

    }

    override fun run(nodes: List<Node>, dependencyGraph: DependencyGraph): Boolean {

        // Unhandled Intersection Nodes with a minimum arity value propagated
        // We first shuffle to get a random sort order
        // Order:
        // - start with these nodes that have a fixed arity already
        // - continue with the nodes having the highest minimum arity

        val intersectionNodes = nodes.filter {
            dependencyGraph.inEdges[it].orEmpty()
                .any { edge -> edge is IntersectionEdge && (edge.overlappingTerms == -1 || edge.nonOverlappingTerms == -1) }
        }.shuffled().sortedWith(compareBy({ !(it.maxArity-it.minArity==0) }, {-it.minArity}))

        if (intersectionNodes.isEmpty()) {
            return false
        }

        val intersectionNode = selectEdge(intersectionNodes, dependencyGraph)

        val e1 = dependencyGraph.inEdges[intersectionNode]!![0] as IntersectionEdge
        val e2 = dependencyGraph.inEdges[intersectionNode]!![1] as IntersectionEdge



        // optional select higher arity, we follow minimum arity pirinicple
        val nodeArity = intersectionNode.minArity
        intersectionNode.maxArity = nodeArity





        val source1 = e1.from
        val source2 = e2.from

        // We can maximal share the minimum arity of both maximum arities as join terms
        val minMaxArity = min(source1.maxArity, source2.maxArity)

        // In case the maximum arity < nodeArity, we have to subtract the individual required ones
        val maxMaxArity = max(source1.maxArity, source2.maxArity)
        var diff1 = nodeArity - maxMaxArity
        if (diff1 < 0) {
            diff1 = 0
        }


        val maxNumberJoinTerms = min(minMaxArity - diff1, nodeArity)

        val numberJoinTerms = Random.nextInt(maxNumberJoinTerms + 1)

        /**
         * Example1: NodeArity: 6, maxArity: 3 for both, then minMaxArity=maxMaxArity=3, diff1=3 and maxNumberJoinTerms=0
         * Example2: NodeArity: 6, maxArity: 4,5, then minMaxArity=4,maxMaxArity=5, diff1=1 and maxNumberJoinTerms=3
         * Example3: NodeArity: 6, maxArity: 7,4, then minMaxArity=4,maxMaxArity=7, diff1=0 and maxNumberJoinTerms=4
         */

        // The maximum number of terms is given by
        val numberNoJoinTerms = nodeArity - numberJoinTerms
        val maxE1 = min(numberNoJoinTerms, source1.maxArity - numberJoinTerms)
        val maxE2 = min(numberNoJoinTerms, source2.maxArity - numberJoinTerms)
        val minE1 = max(0,min(numberNoJoinTerms, source1.minArity - numberJoinTerms))
        val minE2 = max(0,min(numberNoJoinTerms,source2.minArity - numberJoinTerms))

        // The minimum number depends on:
        // (i) the minimum between the minimumArity of the node and the numberNoJoinTerms
        // (ii) we have to add at least numberNoJoinTerms - the maximum number of terms that can be added by the other node
        val minCriteria = max(minE1, numberNoJoinTerms - maxE2)

        // The maximum number depends on:
        // (i) the maximumArity of the node
        // (ii) the minimum number of terms that must be added by the other node (reduces maximum numberNoJoinTerms)
        val maxCriteria = min(maxE1, numberNoJoinTerms - minE2)

        assert(minCriteria <= maxCriteria)

        val noJT1 = max(minCriteria, Random.nextInt(maxCriteria + 1))
        val noJT2 = numberNoJoinTerms - noJT1


        e1.overlappingTerms = numberJoinTerms
        e2.overlappingTerms = numberJoinTerms
        e1.nonOverlappingTerms = noJT1
        e2.nonOverlappingTerms = noJT2

        if (source1 == source2) {
            source1.minArity = max(source1.minArity, max(numberJoinTerms + noJT1, numberJoinTerms + noJT2))
            source1.maxArity = source1.minArity
        } else {
            source1.minArity = max(source1.minArity, numberJoinTerms + noJT1)
            source1.maxArity = source1.minArity
            source2.minArity = max(source2.minArity, numberJoinTerms + noJT2)
            source2.maxArity = source2.minArity
        }

        return true

    }

}