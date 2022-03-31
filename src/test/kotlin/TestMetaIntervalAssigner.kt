import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.NodeType
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.LinearEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.assignments.MetaIntervalAssigner
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.BoxMinusEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.DiamondMinusEdge
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestMetaIntervalAssigner {

    @Test
    fun testSingleChain() {
        val node1 = Node("a",type=NodeType.Input)
        val node2 = Node("b")
        val node3 = Node("c")
        val edge1 = DiamondMinusEdge(from=node1, to=node2,t1 = 2.0,t2=5.0)
        val edge2 = BoxMinusEdge(from=node2, to=node3,t1=1.0,t2=2.0)

        val dg = DependencyGraph(
            mutableSetOf(node1, node2, node3),
            hashMapOf(
                Pair(node3, listOf(edge2)),
                Pair(node2, listOf(edge1)),
            ),
            hashMapOf(
                Pair(node1, listOf(edge1)),
                Pair(node2, listOf(edge2))
            )
        )

        MetaIntervalAssigner.run(dg)

        val intervalInformation = MetaIntervalAssigner.getIntervalInformations()
        Assertions.assertEquals(0.0, intervalInformation.get(node1)!!.intervalOffset.getDuration())
        Assertions.assertEquals(3.0, intervalInformation.get(node2)!!.intervalOffset.getDuration())
        Assertions.assertEquals(2.0, intervalInformation.get(node3)!!.intervalOffset.getDuration())

    }

    @Test
    fun testSimpleRecursiveChain() {
        val node1 = Node("a",type=NodeType.Input)
        val node2 = Node("b")
        val node3 = Node("c")
        val node4 = Node("d")

        val edge1 = LinearEdge(from=node1, to=node2)
        val edge2 = DiamondMinusEdge(from=node2, to=node3,t1 = 2.0,t2=5.0)
        val edge3 = BoxMinusEdge(from=node3, to=node2,t1=1.0,t2=2.0)
        val edge4 = LinearEdge(from=node3, to=node4)

        val dg = DependencyGraph(
            mutableSetOf(node1, node2, node3, node4),
            hashMapOf(
                Pair(node4, listOf(edge4)),
                Pair(node3, listOf(edge2)),
                Pair(node2, listOf(edge1,edge3)),
            ),
            hashMapOf(
                Pair(node1, listOf(edge1)),
                Pair(node2, listOf(edge2)),
                Pair(node3, listOf(edge3,edge4)),
            )
        )

        MetaIntervalAssigner.run(dg)

        val intervalInformation = MetaIntervalAssigner.getIntervalInformations()

        Assertions.assertEquals(0.0, intervalInformation[node1]!!.intervalOffset.getDuration())
        Assertions.assertEquals(0.0, intervalInformation[node2]!!.intervalOffset.getDuration())
        Assertions.assertEquals(3.0, intervalInformation[node3]!!.intervalOffset.getDuration())
        Assertions.assertEquals(3.0, intervalInformation[node4]!!.intervalOffset.getDuration())

    }


}