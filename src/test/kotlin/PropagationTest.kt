import at.ac.tuwien.dbai.kg.iTemporal.util.Properties
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.AggregationType
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.ITAEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.LinearEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.BoxMinusEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.BoxPlusEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.DiamondMinusEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.DiamondPlusEdge
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PropagationTest {

    @Test
    fun testBackwardPropagationLinear() {
        val testData = listOf(listOf(1.0, 2.0, 3.0, 100.0, 110.0))
        val from = Node("from", minArity = testData[0].size - 2 + 2)
        val to = Node("to", minArity = testData[0].size - 2, data = testData)
        val order = listOf(2, 1, 0, 3, -1)
        val edge = LinearEdge(from=from, to=to, termOrder = order)
        edge.backwardPropagateData()

        Assertions.assertEquals(1, from.data.size)
        Assertions.assertEquals(7, from.data[0].size)
        Assertions.assertEquals(3.0, from.data[0][0])
        Assertions.assertEquals(2.0, from.data[0][1])
        Assertions.assertEquals(1.0, from.data[0][2])
        Assertions.assertEquals(100.0, from.data[0][5])
        Assertions.assertEquals(110.0, from.data[0][6])
    }

    @Test
    fun testForwardPropagationLinear() {
        val testData = listOf(listOf(3.0, 2.0, 1.0, 111.0, 112.0, 100.0, 110.0))
        val from = Node("from", minArity = testData[0].size - 2, data = testData)
        val to = Node("to", minArity = testData[0].size - 2 - 2)
        val order = listOf(2, 1, 0, 3, -1)
        val edge = LinearEdge(from=from, to=to, termOrder = order)
        edge.forwardPropagateData()

        Assertions.assertEquals(1, to.data.size)
        Assertions.assertEquals(5, to.data[0].size)
        Assertions.assertEquals(1.0, to.data[0][0])
        Assertions.assertEquals(2.0, to.data[0][1])
        Assertions.assertEquals(3.0, to.data[0][2])
        Assertions.assertEquals(100.0, to.data[0][3])
        Assertions.assertEquals(110.0, to.data[0][4])
    }

    @Test
    fun testBackwardPropagationBoxMinus() {
        Registry.properties.temporalInclusionPercentage = 1.0
        val testData = listOf(listOf(1.0, 2.0, 3.0, 100.0, 110.0))
        val from = Node("from", minArity = testData[0].size - 2 + 2)
        val to = Node("to", minArity = testData[0].size - 2, data = testData)
        val order = listOf(2, 1, 0, 3, -1)
        val edge = BoxMinusEdge(from=from, to=to, t1 = 3.0, t2 = 5.0, termOrder = order)
        edge.backwardPropagateData()

        Assertions.assertEquals(1, from.data.size)
        Assertions.assertEquals(7, from.data[0].size)
        Assertions.assertEquals(3.0, from.data[0][0])
        Assertions.assertEquals(2.0, from.data[0][1])
        Assertions.assertEquals(1.0, from.data[0][2])
        Assertions.assertEquals(95.0, from.data[0][5])
        Assertions.assertEquals(107.0, from.data[0][6])
    }

    @Test
    fun testForwardPropagationBoxMinus() {
        val testData = listOf(listOf(3.0, 2.0, 1.0, 111.0, 112.0, 100.0, 110.0))
        val from = Node("from", minArity = testData[0].size - 2, data = testData)
        val to = Node("to", minArity = testData[0].size - 2 - 2)
        val order = listOf(2, 1, 0, 3, -1)
        val edge = BoxMinusEdge(from=from, to=to, t1 = 3.0, t2 = 5.0, termOrder = order)
        edge.forwardPropagateData()

        Assertions.assertEquals(1, to.data.size)
        Assertions.assertEquals(5, to.data[0].size)
        Assertions.assertEquals(1.0, to.data[0][0])
        Assertions.assertEquals(2.0, to.data[0][1])
        Assertions.assertEquals(3.0, to.data[0][2])
        Assertions.assertEquals(105.0, to.data[0][3])
        Assertions.assertEquals(113.0, to.data[0][4])
    }

    @Test
    fun testBackwardPropagationBoxPlus() {
        val properties = Properties()
        properties.temporalInclusionPercentage = 1.0
        val testData = listOf(listOf(1.0, 2.0, 3.0, 100.0, 110.0))
        val from = Node("from", minArity = testData[0].size - 2 + 2)
        val to = Node("to", minArity = testData[0].size - 2, data = testData)
        val order = listOf(2, 1, 0, 3, -1)
        val edge = BoxPlusEdge(from=from, to=to, t1 = 3.0, t2 = 5.0, termOrder = order)
        edge.backwardPropagateData()

        Assertions.assertEquals(1, from.data.size)
        Assertions.assertEquals(7, from.data[0].size)
        Assertions.assertEquals(3.0, from.data[0][0])
        Assertions.assertEquals(2.0, from.data[0][1])
        Assertions.assertEquals(1.0, from.data[0][2])
        Assertions.assertEquals(103.0, from.data[0][5])
        Assertions.assertEquals(115.0, from.data[0][6])
    }

    @Test
    fun testForwardPropagationBoxPlus() {
        val testData = listOf(listOf(3.0, 2.0, 1.0, 111.0, 112.0, 100.0, 110.0))
        val from = Node("from", minArity = testData[0].size - 2, data = testData)
        val to = Node("to", minArity = testData[0].size - 2 - 2)
        val order = listOf(2, 1, 0, 3, -1)
        val edge = BoxPlusEdge(from=from, to=to, t1 = 3.0, t2 = 5.0, termOrder = order)
        edge.forwardPropagateData()

        Assertions.assertEquals(1, to.data.size)
        Assertions.assertEquals(5, to.data[0].size)
        Assertions.assertEquals(1.0, to.data[0][0])
        Assertions.assertEquals(2.0, to.data[0][1])
        Assertions.assertEquals(3.0, to.data[0][2])
        Assertions.assertEquals(97.0, to.data[0][3])
        Assertions.assertEquals(105.0, to.data[0][4])
    }

    @Test
    fun testBackwardPropagationDiamondMinus() {
        Registry.properties.temporalInclusionPercentage = 1.0
        val testData = listOf(listOf(1.0, 2.0, 3.0, 100.0, 110.0))
        val from = Node("from", minArity = testData[0].size - 2 + 2)
        val to = Node("to", minArity = testData[0].size - 2, data = testData)
        val order = listOf(2, 1, 0, 3, -1)
        val edge = DiamondMinusEdge(from=from, to=to, t1 = 3.0, t2 = 5.0, termOrder = order)
        edge.backwardPropagateData()

        Assertions.assertEquals(1, from.data.size)
        Assertions.assertEquals(7, from.data[0].size)
        Assertions.assertEquals(3.0, from.data[0][0])
        Assertions.assertEquals(2.0, from.data[0][1])
        Assertions.assertEquals(1.0, from.data[0][2])
        Assertions.assertEquals(97.0, from.data[0][5])
        Assertions.assertEquals(105.0, from.data[0][6])
    }

    @Test
    fun testForwardPropagationDiamondMinus() {
        val testData = listOf(listOf(3.0, 2.0, 1.0, 111.0, 112.0, 100.0, 110.0))
        val from = Node("from", minArity = testData[0].size - 2, data = testData)
        val to = Node("to", minArity = testData[0].size - 2 - 2)
        val order = listOf(2, 1, 0, 3, -1)
        val edge = DiamondMinusEdge(from=from, to=to, t1 = 3.0, t2 = 5.0, termOrder = order)
        edge.forwardPropagateData()

        Assertions.assertEquals(1, to.data.size)
        Assertions.assertEquals(5, to.data[0].size)
        Assertions.assertEquals(1.0, to.data[0][0])
        Assertions.assertEquals(2.0, to.data[0][1])
        Assertions.assertEquals(3.0, to.data[0][2])
        Assertions.assertEquals(103.0, to.data[0][3])
        Assertions.assertEquals(115.0, to.data[0][4])
    }

    @Test
    fun testBackwardPropagationDiamondPlus() {
        Registry.properties.temporalInclusionPercentage = 1.0
        val testData = listOf(listOf(1.0, 2.0, 3.0, 100.0, 110.0))
        val from = Node("from", minArity = testData[0].size - 2 + 2)
        val to = Node("to", minArity = testData[0].size - 2, data = testData)
        val order = listOf(2, 1, 0, 3, -1)
        val edge = DiamondPlusEdge(from=from, to=to, t1 = 3.0, t2 = 5.0, termOrder = order)
        edge.backwardPropagateData()

        Assertions.assertEquals(1, from.data.size)
        Assertions.assertEquals(7, from.data[0].size)
        Assertions.assertEquals(3.0, from.data[0][0])
        Assertions.assertEquals(2.0, from.data[0][1])
        Assertions.assertEquals(1.0, from.data[0][2])
        Assertions.assertEquals(105.0, from.data[0][5])
        Assertions.assertEquals(113.0, from.data[0][6])
    }

    @Test
    fun testForwardPropagationDiamondPlus() {
        val testData = listOf(listOf(3.0, 2.0, 1.0, 111.0, 112.0, 100.0, 110.0))
        val from = Node("from", minArity = testData[0].size - 2, data = testData)
        val to = Node("to", minArity = testData[0].size - 2 - 2)
        val order = listOf(2, 1, 0, 3, -1)
        val edge = DiamondPlusEdge(from=from, to=to, t1 = 3.0, t2 = 5.0, termOrder = order)
        edge.forwardPropagateData()

        Assertions.assertEquals(1, to.data.size)
        Assertions.assertEquals(5, to.data[0].size)
        Assertions.assertEquals(1.0, to.data[0][0])
        Assertions.assertEquals(2.0, to.data[0][1])
        Assertions.assertEquals(3.0, to.data[0][2])
        Assertions.assertEquals(95.0, to.data[0][3])
        Assertions.assertEquals(107.0, to.data[0][4])
    }

    @Test
    fun testBackwardPropagationAggregationSum() {
        val testData = listOf(listOf(3.0, 2.0, 1027.0, 100.0, 110.0))
        // 2 group by, 1 contributor, 1 value => arity 4

        val from = Node("from", minArity = 4)
        val to = Node("to", minArity = 3, data=testData)
        val edge = ITAEdge(
            from=from,
            to=to,
            numberOfGroupingTerms = 2,
            numberOfContributors = 1,
            aggregationType = AggregationType.Sum,
            termOrder = listOf(1, 0, 4, 3)
        )
        edge.backwardPropagateData()
        println(from.data)
    }

    @Test
    fun testBackwardPropagationAggregationCountNoContributor() {
        val testData = listOf(listOf(3.0, 2.0, 8.0, 100.0, 110.0))
        // 2 group by, 1 value => arity 4

        val from = Node("from", minArity = 3)
        val to = Node("to", minArity = 3, data=testData)
        val edge = ITAEdge(
            from = from,
            to = to,
            numberOfGroupingTerms = 2,
            numberOfContributors = 0,
            aggregationType = AggregationType.Count,
            termOrder = listOf(1, 0, 3)
        )
        edge.backwardPropagateData()
        println(from.data)
    }
}