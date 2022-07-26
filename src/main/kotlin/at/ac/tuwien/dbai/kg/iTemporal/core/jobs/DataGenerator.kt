package at.ac.tuwien.dbai.kg.iTemporal.core.jobs

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraphHelper
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.DataGeneration
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.NodeType
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.IntersectionEdge
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.TemporalSingleEdge

/**
 * This job handles the generation of the data.
 */
object DataGenerator : DataGeneration {


    override fun getPriority(): Int = 45


    override fun run(dependencyGraph: DependencyGraph, storeFile:Boolean): DependencyGraph {
        val sCCs = DependencyGraphHelper.calculateSCC(dependencyGraph)
        val sccOrder = DependencyGraphHelper.createSCCOrder(dependencyGraph, sCCs)

        /**
         * Calculate data per SCC component, starting with outputs
         * 1. If it is an output, generate random data according to properties and assign to node
         * 2. Forward data to next node with heuristics provided by properties and the behavior of the rule.
         * - How we handle cyclic dependencies:
         * -> We track the data generated in the current round and all data generated in previous rounds.
         * -> We only consider the data generated in the current round, that does not exist in previous rounds as new data to be handled.
         * -> This approach works well for:
         * ----> Linear (only new tuples have to be distributed to its predecessor)
         * ----> Intersection (only new tuples have to be distributed to its predecessors)
         * ----> Union  (only new tuples have to be distributed to its predecessors)
         * ----> TriangleUp (having the larger interval as input causes again the same large interval as output)
         * -> These rules are only allowed in restricted cases in cyclic data as exactly one predicate is allowed per time-interval and group
         *    or in other words, one can only apply this operation if there is no sub-interval that breaks by recursion some monotonicity property along the time axis:
         *    We limit the usage of these rules to edges not in a cycle, i.e., edges between two SCC.
         * ----> MonotonicIncreasing (not allowed to appear in the cycle at all)
         * ----> MonotonicDecreasing (not allowed to appear in the cycle at all)
         * -> This approach has some limitations for cycles containing one of the following rules:
         * ----> BoxMinus (the time is shifted hence always different values are generated)
         * ----> BoxPlus (the time is shifted hence always different values are generated)
         * ----> DiamondMinus (the time is shifted hence always different values are generated)
         * ----> DiamondPlus (the time is shifted hence always different values are generated)
         * ----> ITA (divergence of numeric values)
         * -> How we handle:
         * ----> ITA: As we are back-propagating there is a point where we have the smallest possible value we can use as a basis for generation
         *       that is the point where generation stops. This does not prevent divergence of the numeric values at this point.
         *       One may adapt the aggregation rules in the graph generation to have some filtering condition disallowing divergence
         * ----> Temporal ones: We have to choose a maximal number of iterations after which we do not propagate the temporal information in addition.
         *       We have to remove derived values in the future by carefully selecting the input values to the SCC.
         *       That is, we need some forward propagating step as well to rule out certain data points in the input.
         *       1. Limit amount of backward propagation of values.
         *       -> do with percentage
         *       2. Forward propagation to rule out generated temporal information.

         * 3. If the SCC has completed the forwarding of the data, then delete the data from the SCC to save memory footprint.
         */


        for (sccId in sccOrder) {
            val nodes: List<Node> = sCCs[sccId]

            if (Registry.debug) {
                println("Running backward propagation")
            }
            backwardPropagation(nodes, dependencyGraph)
            if (Registry.debug) {
                println("backward propagation finished")
            }
            // If SCC has only a single node than it is not cyclic, we can clean and continue with next scc
            if (nodes.size <= 1) {
                cleanup(nodes, dependencyGraph)
                continue
            }

            // If SCC is cyclic, but contains no temporal edge, we can clean and continue with next scc
            val hasTemporalNode = nodes.any { node ->
                dependencyGraph.inEdges[node].orEmpty()
                    .any { edge -> nodes.contains(edge.from) && edge is TemporalSingleEdge }
            }
            if (!hasTemporalNode) {
                cleanup(nodes, dependencyGraph)
                continue
            }
            if (Registry.debug) {
                println("Running forward propagation")
            }
            forwardPropagation(nodes, dependencyGraph)

            // Update old data with actual required data
            nodes.forEach { node ->
                node.oldData = node.requiredData
                node.requiredData = emptyList()
            }

            cleanup(nodes, dependencyGraph)
        }

        if (storeFile) {
            dependencyGraph.writeData()
        }

        return dependencyGraph
    }

    private fun cleanup(nodes: List<Node>, dependencyGraph: DependencyGraph) {
        // Propagate information to previous SCC
        nodes.forEach { node ->
            dependencyGraph.inEdges[node].orEmpty().filter { edge -> !nodes.contains(edge.from) }.forEach {
                edge ->
                run {
                    val data = edge.to.data
                    edge.to.data = node.oldData
                    node.oldData = emptyList()
                    edge.backwardPropagateData()
                    node.oldData = edge.to.data
                    edge.to.data = data
                }
            }
        }

        // Reset data
        for (node in nodes) {
            when (node.type) {
                NodeType.Input -> {
                    node.data = emptyList()
                }
                else -> {
                    // Reset data at end to save memory footprint
                    node.oldData = emptyList()
                    node.oldData2 = emptyList()
                    node.data = emptyList()
                }
            }
        }

    }

    private fun backwardPropagation(nodes: List<Node>, dependencyGraph: DependencyGraph) {
        var changed = true

        val maxData = Registry.properties.maxInnerNodeDataFactor * Registry.properties.averageAmountOfGeneratedOutputs


        // After how many round should we stop the generation?
        // The generation is not stopped after rounds but by probability of the likelihood of a temporal recursion.
        // Are all results for all rounds required in the previous component, or which results are required?
        // We start by iterating over the loop and remove all derived results. We continue by applying the next earliest data element
        var dataRound = 0.0
        while (changed) {
            changed = false
            dataRound += 1.0
            if (Registry.debug) {
                println(dataRound)
            }

            for (node in nodes) {
                // Generate data in case it is an output node
                if (node.type == NodeType.Output && node.oldData.isEmpty()) {
                    node.data = RandomGenerator.generateData(node)
                    changed = true
                }

                // If we are over the limit for the node skip
                if (node.oldData.size > maxData) {
                    if (Registry.debug) {
                        println("Skipping $node")
                    }
                    continue
                }

                if (Registry.debug) {
                    println("Handling $node")
                    println("Current size: ${node.oldData.size}")
                }

                // 1. Filter new data with existing data
                node.data = node.data.minus(node.oldData)

                // Limit to maxData so that data generation runs fast. The max parameter can be chosen
                // Discuss: may limit to difference?
                node.data = node.data.shuffled(RandomGenerator.sharedRandom).take(maxData)

                // 2. If new data is not empty, then apply data forwarding
                if (node.data.isNotEmpty()) {
                    //3. Propagate back the new data in case it is not empty
                    val inEdges = dependencyGraph.inEdges[node].orEmpty()
                    for (inEdge in inEdges) {
                        // Limit to same SCC
                        if (nodes.contains(inEdge.from)) {
                            inEdge.backwardPropagateData()
                        }
                    }

                    // 4. Then copy data to oldData
                    node.oldData = node.oldData + node.data
                    node.oldData2 = node.oldData2 + node.data.map { it + listOf(dataRound) }

                    // 5. And reset newly derived data to have a new working set
                    node.data = emptyList()

                    // 6. Last but not least, something has changed, hence set changed to true
                    changed = true
                }

                if (Registry.debug) {
                    println("Node Size after ${node.oldData.size}")
                }
            }
            if (Registry.debug) {
                println("Finished")
            }
        }
    }

    private fun forwardPropagation(nodes: List<Node>, dependencyGraph: DependencyGraph) {
        val nodesWithIngoingEdgesInScc = nodes.filter { node ->
            dependencyGraph.inEdges[node].orEmpty().any { edge -> !nodes.contains(edge.from) }
        }.distinct()

        // Reset node data except of oldData2 as it contains essential information for forward propagating step.
        for (node in nodes) {
            node.oldData = emptyList()
            node.data = emptyList()
            node.requiredData = emptyList()
        }

        // Create list of adding data
        val dataList: MutableList<Pair<Node, List<Double>>> =
            nodesWithIngoingEdgesInScc.flatMap { node -> node.oldData2.map { entry -> Pair(node, entry) } }
                .sortedWith(Comparator { a, b ->
                    return@Comparator a.second[a.second.size - 1].compareTo(b.second[b.second.size - 1]) * -1
                }).toMutableList()


        // Nodes added in the same data round cannot have an impact on each other as the generation step occurred at the same time.
        while (dataList.isNotEmpty()) {
            val nextEntryData = dataList.first().second
            val nextEntryDataRound = nextEntryData[nextEntryData.size - 1]

            // Fill nodes of the same step, filtering out already existing parts of the node
            while (dataList.isNotEmpty()) {
                val currentEntry = dataList.first()
                val currentEntryDataRound = currentEntry.second[currentEntry.second.size - 1]
                // Add
                if (currentEntryDataRound == nextEntryDataRound) {
                    // Delete entry
                    dataList.removeFirst()

                    val entryWithoutRound = currentEntry.second.take(currentEntry.second.size - 1)
                    currentEntry.first.addData(entryWithoutRound)

                } else {
                    break
                }
            }

            // Apply propagation
            var changed = true
            while (changed) {
                changed = false
                for (node in nodes) {
                    // 1. Filter new data with existing data
                    node.data = node.data.minus(node.oldData)
                    // 1.a and do not allow data that has not been created by back propagation
                    node.data = node.data.intersect(node.oldData2.map { it.take(node.minArity+2) }).toList()

                    // 2. If new data is not empty, then apply data forwarding
                    if (node.data.isNotEmpty()) {
                        //3. Propagate forward the new data in case it is not empty
                        val outEdges = dependencyGraph.outEdges[node].orEmpty()
                        for (outEdge in outEdges) {
                            if (nodes.contains(outEdge.to)) {
                                outEdge.forwardPropagateData()//forwardData(node.data)
                                if (outEdge is IntersectionEdge) {
                                    val intersectingNode = outEdge.to
                                    val intersectingEdges = dependencyGraph.inEdges[intersectingNode].orEmpty().map { it as IntersectionEdge }
                                    val edge1 = intersectingEdges[0]
                                    val edge2 = intersectingEdges[1]
                                    intersectingNode.addJoin(edge1, edge2)
                                }
                            }
                        }

                        // 4. Then copy data to oldData
                        node.oldData = node.oldData + node.data

                        // 5. And reset newly derived data to have a new working set
                        node.data = emptyList()

                        // 6. Last but not least, something has changed, hence set changed to true
                        changed = true
                    }

                }
            }
        }
    }


}