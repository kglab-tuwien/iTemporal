import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericMultiEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.IntersectionEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.UnionEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.jobs.DGGenericEdgeNormalizer
import at.ac.tuwien.dbai.kg.iTemporal.core.jobs.DGMultiEdgeOperationNormalizer
import at.ac.tuwien.dbai.kg.iTemporal.core.jobs.DGMultiEdgeSplitNormalizer
import at.ac.tuwien.dbai.kg.iTemporal.core.jobs.DGSingleEdgeNormalizer
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.BoxMinusEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.DiamondMinusEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.SinceEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.jobs.TemporalNormalization
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

internal class DGNormalizerTest {

    @Test
    fun testNormalizeDgIntersectionAndUnion() {
        val node1 = Node("a")
        val node2 = Node("b")
        val node3 = Node("c")
        val edge1 = DiamondMinusEdge(from=node1, to=node3)
        val edge2 = BoxMinusEdge(from=node2, to=node3)

        val dg = DependencyGraph(
            mutableSetOf(node1, node2, node3),
            hashMapOf(
                Pair(node3, listOf(edge1, edge2)),
            ),
            hashMapOf(
                Pair(node1, listOf(edge1)),
                Pair(node2, listOf(edge2))
            )
        )

        var dgNew:DependencyGraph = DGGenericEdgeNormalizer.run(dg)
        dgNew = DGSingleEdgeNormalizer.run(dgNew)
        dgNew = DGMultiEdgeOperationNormalizer.run(dgNew)
        dgNew = DGMultiEdgeSplitNormalizer.run(dgNew)

        // Verify existing nodes get changed accordingly
        Assertions.assertEquals(5, dgNew.nodes.size)
        Assertions.assertEquals(2, dgNew.inEdges[node3]!!.size)
        Assertions.assertEquals(0, dgNew.outEdges[node3].orEmpty().size)
        Assertions.assertEquals(0, dgNew.inEdges[node1].orEmpty().size)
        Assertions.assertEquals(1, dgNew.outEdges[node1]!!.size)
        Assertions.assertEquals(0, dgNew.inEdges[node2].orEmpty().size)
        Assertions.assertEquals(1, dgNew.outEdges[node2]!!.size)
        Assertions.assertTrue(dgNew.inEdges[node3]!![0] is GenericMultiEdge)
        Assertions.assertTrue(dgNew.inEdges[node3]!![1] is GenericMultiEdge)
        Assertions.assertTrue(dgNew.outEdges[node1]!![0] is DiamondMinusEdge)
        Assertions.assertTrue(dgNew.outEdges[node2]!![0] is BoxMinusEdge)

        // Verify newly created nodes
        val newNodes = dgNew.nodes.filter { it != node1 && it != node2 && it != node3 }


        val expectedInNodes = mutableListOf(edge1.from, edge2.from)
        for (newNode in newNodes) {
            Assertions.assertEquals(1, dgNew.outEdges[newNode]!!.size)
            Assertions.assertEquals(1, dgNew.inEdges[newNode]!!.size)
            Assertions.assertTrue(dgNew.outEdges[newNode]!![0] is GenericMultiEdge)
            val inEdge = dgNew.inEdges[newNode]!![0]

            Assertions.assertTrue(expectedInNodes.contains(inEdge.from))
            expectedInNodes.remove(inEdge.from)
        }
        Assertions.assertTrue(expectedInNodes.isEmpty())

        dgNew.store(File("out/debug.json"))

    }


    @Test
    fun testIntersectionChain() {
        val node1 = Node()
        val node2 = Node()
        val node3 = Node()
        val node4 = Node()
        val node5 = Node()
        val edge1 = IntersectionEdge(from=node1, to=node5)
        val edge2 = GenericEdge(from=node2, to=node5)
        val edge3 = GenericEdge(from=node3, to=node5)
        val edge4 = GenericEdge(from=node4, to=node5)

        val dg = DependencyGraph(
            mutableSetOf(node1, node2, node3, node4, node5),
            hashMapOf(
                Pair(node5, listOf(edge1, edge2, edge3, edge4)),
            ),
            hashMapOf(
                Pair(node1, listOf(edge1)),
                Pair(node2, listOf(edge2)),
                Pair(node3, listOf(edge3)),
                Pair(node4, listOf(edge4)),
            )
        )

        Registry.addEdge(IntersectionEdge::class)

        var dgNew = DGGenericEdgeNormalizer.run(dg)
        dgNew = DGSingleEdgeNormalizer.run(dgNew)
        dgNew = DGMultiEdgeOperationNormalizer.run(dgNew)
        dgNew = DGMultiEdgeSplitNormalizer.run(dgNew)

        Assertions.assertEquals(7, dgNew.nodes.size)

        for (node in dgNew.nodes) {
            for (inEdge in dgNew.inEdges[node].orEmpty()) {
                Assertions.assertTrue(inEdge is IntersectionEdge)
                Assertions.assertEquals(node, inEdge.to)
            }
            for (outEdge in dgNew.outEdges[node].orEmpty()) {
                Assertions.assertTrue(outEdge is IntersectionEdge)
            }

            when (node) {
                node1, node2, node3, node4 -> {
                    Assertions.assertEquals(1, dgNew.outEdges[node]!!.size)
                    Assertions.assertEquals(0, dgNew.inEdges[node].orEmpty().size)
                }
                node5 -> {
                    Assertions.assertEquals(0, dgNew.outEdges[node].orEmpty().size)
                    Assertions.assertEquals(2, dgNew.inEdges[node]!!.size)
                }
                else -> {
                    Assertions.assertEquals(1, dgNew.outEdges[node]!!.size)
                    Assertions.assertEquals(2, dgNew.inEdges[node]!!.size)
                }
            }
        }

    }

    @Test
    fun testSinceRewrite() {
        val node1 = Node("a1",minArity = 3,maxArity = 3)
        val node2 = Node("a2",minArity = 2, maxArity = 2)
        val node3 = Node("b",minArity = 4, maxArity = 4)
        val edge1 = SinceEdge(from=node1, to=node3, t1=5.0, t2=3.0, isLeftEdge = false, termOrder = listOf(1,0,2))
        val edge2 = SinceEdge(from=node2, to=node3, isLeftEdge = true, termOrder = listOf(1,3))

        val dg = DependencyGraph(
            mutableSetOf(node1, node2, node3),
            hashMapOf(
                Pair(node3, listOf(edge1, edge2)),
            ),
            hashMapOf(
                Pair(node1, listOf(edge1)),
                Pair(node2, listOf(edge2))
            )
        )

        val x = TemporalNormalization.run(dg)

        println(x)

    }
}