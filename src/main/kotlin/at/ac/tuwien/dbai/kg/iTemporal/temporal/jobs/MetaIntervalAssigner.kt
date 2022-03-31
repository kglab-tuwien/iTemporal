package at.ac.tuwien.dbai.kg.iTemporal.temporal.assignments

import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.OtherPropertyAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.NodeType
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.IntersectionEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.Interval
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.TemporalSingleEdge
import kotlin.math.min


data class IntervalInformation(
    var intervalOffset: IntervalDuration,
    //val intervalOffsets: MutableSet<IntervalDuration> = mutableSetOf(intervalOffset),
)

class IntervalDuration(
    val interval: Interval
)
{

    constructor(duration:Double) : this(Interval(0.0, duration))
    constructor(duration:IntervalDuration) : this(Interval(0.0, duration.getDuration()))

    fun getDuration():Double {
        return this.interval.t2-this.interval.t1
    }

    fun isNegative(): Boolean {
        return this.interval.t2 < this.interval.t1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntervalDuration) return false

        if (this.getDuration() != other.getDuration()) return false

        return true
    }

    override fun hashCode(): Int {
        return getDuration().hashCode()
    }

    override fun toString(): String {
        return this.getDuration().toString()
    }
}

/**
 * This job computes the minimum interval length of the data according to the temporal edges of the program
 */
object MetaIntervalAssigner : OtherPropertyAssignment {
    override fun getPriority(): Int = 10000

    private var intervalInformations: Map<Node, IntervalInformation> = mapOf()

    fun backpropagate(node: Node, dependencyGraph: DependencyGraph, visited: MutableSet<Node>) {
        val edges = dependencyGraph.inEdges[node].orEmpty()

        if (edges.isEmpty()) {
            return
        }

        if (node in visited) {
            return
        }

        visited.add(node)

        if (edges.size > 2) {
            throw RuntimeException("invalid graph detected")
        }

        for (edge in edges) {
            val newDuration: IntervalDuration
            if (edge is TemporalSingleEdge) {
                newDuration = IntervalDuration(edge.applyIntervalBackward(intervalInformations[edge.to]!!.intervalOffset.interval))
            } else {
                newDuration = IntervalDuration(intervalInformations[edge.to]!!.intervalOffset)
            }

            val existingDuration = intervalInformations[edge.from]!!.intervalOffset

            if (newDuration.getDuration() > existingDuration.getDuration()) {
                intervalInformations[edge.from]!!.intervalOffset = newDuration

                this.backpropagate(edge.from, dependencyGraph, visited)
            }
        }

        visited.remove(node)
    }

    fun forwardPropagate(node: Node, dependencyGraph: DependencyGraph, visited: MutableSet<Node>) {
        val inEdges = dependencyGraph.inEdges[node].orEmpty()
        val outEdges = dependencyGraph.outEdges[node].orEmpty()

        if (inEdges.isEmpty()) {
            return
        }
        // Do not forward propagate, if already visited
        if (visited.contains(node)) {
            return
        }

        visited.add(node)


        // Compute minimal duration from all incoming edges


        val maxDuration = inEdges.map { inEdge ->
            val newDuration: Double

            if (inEdge is TemporalSingleEdge) {
                val tmpDuration =
                    IntervalDuration(inEdge.applyInterval(intervalInformations[inEdge.from]!!.intervalOffset.interval))

                if (tmpDuration.isNegative()) {
                    newDuration = 0.0
                    // Update edge
                    intervalInformations[inEdge.from]!!.intervalOffset = IntervalDuration(intervalInformations[inEdge.from]!!.intervalOffset.getDuration() + (tmpDuration.getDuration()*-1.0))
                    backpropagate(inEdge.from, dependencyGraph, mutableSetOf())
                } else {
                    newDuration = tmpDuration.getDuration()
                }
            } else {
                newDuration = intervalInformations[inEdge.from]!!.intervalOffset.getDuration()
            }

            return@map newDuration
        }.maxOf { it }

        if (maxDuration > intervalInformations[node]!!.intervalOffset.getDuration()) {
            intervalInformations[node]!!.intervalOffset = IntervalDuration(maxDuration)
        }

        // Trigger forward propagation for all outgoing edges
        for (outEdge in outEdges) {
            forwardPropagate(outEdge.to, dependencyGraph, visited)
        }

        visited.remove(node)


    }

    override fun run(dependencyGraph: DependencyGraph): DependencyGraph {

        // The goal is to get the required interval length of the input nodes.
        // This helps to gerenate benchmark data which is valid at least once for each node as intervals are not chosen to short
        // In case there are no negative intervals on the way (i.e. no backtracking necessary), it also helps to generate benchmark data which start with punctual intervals


        val inputNodes = dependencyGraph.nodes.filter { it.type == NodeType.Input }

        /**
         * New logic, because previous one has not worked great, see oldRun for details.
         *
         * We start by initialising the start node with 0,0
         * We propagate the information forward per edge.
         * In case there are multiple incoming edges, we chose the largest interval so that each input node gets data
         * optional: performance improvement, do per SCC
         *
         * There are 3 options:
         * - temporal edges: we apply the interval, in case the interval duration is negative, we have to backtrack and update the previous intervals with the bigger interval size and stop where the interval is long enough.
         * - union/intersection: we take the larger interval duration. In case of backtracking the backtracking is split and continued on both parts.
         * - other edges: just forward interval length
         *
         * Backtracking:
         * - node with multiple outgoing edges: largest duration is favored so that each node gets data
         *
         * Negative recursive cycle:
         * - first check, whether we have reached a cycle in forward tracking, in such a case, we do not start backpropagating information, as each node already has some data
         * - optional, for future work: wwe can establish minimum amount of simple circles required by counting how often the node is currently in the cycle.
         */

        intervalInformations = dependencyGraph.nodes.associateWith {
            IntervalInformation(IntervalDuration(0.0))
        }

        println("Meta Interval assigner started")

        for (node in inputNodes) {
            for (edge in dependencyGraph.outEdges[node].orEmpty())
            forwardPropagate(edge.to, dependencyGraph, mutableSetOf())
        }

        println("Meta Interval assigner completed")

        /*for (node in dependencyGraph.nodes) {
            forwardPropagate(node, dependencyGraph, mutableSetOf())
        }*/

        println(intervalInformations)

        return dependencyGraph





    }

    fun oldRun(dependencyGraph: DependencyGraph): DependencyGraph {

        intervalInformations = dependencyGraph.nodes.associateWith {
            IntervalInformation(IntervalDuration(Double.POSITIVE_INFINITY))
        }

        val inputNodes = dependencyGraph.nodes.filter { it.type == NodeType.Input }
        for (node in inputNodes) {
            //intervalInformations[node]!!.intervalOffsets.add(IntervalDuration(Interval(0.0, 0.0)))
            intervalInformations[node]!!.intervalOffset = IntervalDuration(0.0)
        }

        // The logic is as follows:
        /**
         *  1. Temporal edges modify the possible intervals of the previous node by the duration
         *      - the minimum interval length is defined by adding/subtracting the duration caused by the operation
         *  2. Union add the possible intervals of both incoming edges without any changes
         *      - we keep all minimum intervals for variance
         *      - variation: keep only the minimum interval
         *  3. Intersection
         *      - minimum interval length given by min of incoming intervals
         *      - longer interval can be created by extending the interval of the other join partner
         *      - variation: keep only the minimum interval
         *  4. Aggregation/Other edges
         *      - the minimum input length is the minimum output length
         *      - buckets could be merged, but minimum bucket size depends on input interval length
         *      - just forwards the interval length
         *  . TriangleUpEdge (with STA)
         *      - We ignore this edge, why?
         *      - When we have the minimal intervals calculated, this edge can only increase the intervals
         *      - It also has as input a possible smaller interval, i.e., the interval we require as minimal interval
         *      - This means, we only produce additional facts in the output, is this an issue?
         *      - Actually only for aggregation result values as they may change, but when do the change?
         *      - They only change in case it is a count and we have more intervals that are counted at a specific interval, can we prevent it?
         *      - Yes, but at the moment we ignore this issue. FIXME
         */


        var changed = true

        while (changed) {
            changed = false

            for (node in dependencyGraph.nodes) {
                val inEdges = dependencyGraph.inEdges[node].orEmpty()

                if (inEdges.isEmpty()) {
                    continue
                }

                if (inEdges.size == 1) {
                    val inEdge = inEdges[0]

                    val newData: IntervalInformation
                    val newDuration: IntervalDuration
                    if (inEdge is TemporalSingleEdge) {
                        /*newData = IntervalInformation(intervalInformations[inEdge.from]!!.intervalOffsets.map {
                            IntervalDuration(inEdge.applyInterval(it.interval))
                        }.toMutableSet())*/

                        newDuration = IntervalDuration(inEdge.applyInterval(intervalInformations[inEdge.from]!!.intervalOffset.interval))
                    } else {
                        //newData = intervalInformations[inEdge.from]!!
                        newDuration = intervalInformations[inEdge.from]!!.intervalOffset
                    }

                    //if ((newDuration.getDuration() >= 0 || intervalInformations[node]!!.intervalOffset.getDuration() ==Double.POSITIVE_INFINITY) && newDuration.getDuration() < intervalInformations[node]!!.intervalOffset.getDuration() ) {
                    if ((newDuration.getDuration() >= 0) && newDuration.getDuration() < intervalInformations[node]!!.intervalOffset.getDuration() ) {
                        changed = true
                        intervalInformations[node]!!.intervalOffset = newDuration
                    }

                    //changed = intervalInformations[node]!!.intervalOffsets.addAll(newData.intervalOffsets)

                    continue
                }

                // 2 Edges
                if (inEdges.any { it is IntersectionEdge }) {
                    val source1 = intervalInformations[inEdges[0].from]!!
                    val source2 = intervalInformations[inEdges[1].from]!!

                    val newMinDuration = min(source1.intervalOffset.getDuration(),source2.intervalOffset.getDuration())

                    if ((newMinDuration >= 0  || intervalInformations[node]!!.intervalOffset.getDuration() ==Double.POSITIVE_INFINITY) && newMinDuration < intervalInformations[node]!!.intervalOffset.getDuration() ) {
                        changed = true
                        intervalInformations[node]!!.intervalOffset = IntervalDuration(newMinDuration)
                    }

                    /*
                    // Performance improvement by sorting
                    for (x in source1.intervalOffsets) {
                        for (y in source2.intervalOffsets) {
                            if(intervalInformations[node]!!.intervalOffsets.add(IntervalDuration(Interval(0.0, min(x.getDuration(),y.getDuration()))))) {
                                changed = true
                            }
                        }
                    }*/
                } else {
                    for (inEdge in inEdges) {
                        /*if(intervalInformations[node]!!.intervalOffsets.addAll(intervalInformations[inEdge.from]!!.intervalOffsets)) {
                            changed = true
                        }*/

                        if ((intervalInformations[inEdge.from]!!.intervalOffset.getDuration() >= 0 || intervalInformations[node]!!.intervalOffset.getDuration() ==Double.POSITIVE_INFINITY)  && intervalInformations[inEdge.from]!!.intervalOffset.getDuration() < intervalInformations[node]!!.intervalOffset.getDuration() ) {
                            changed = true
                            intervalInformations[node]!!.intervalOffset = intervalInformations[inEdge.from]!!.intervalOffset
                        }
                    }
                }
            }
        }



        return dependencyGraph
    }

    fun getIntervalInformations():Map<Node,IntervalInformation> {
        return this.intervalInformations
    }

}