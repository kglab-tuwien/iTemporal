package at.ac.tuwien.dbai.kg.iTemporal.graphGenerator

import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.GraphGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.MutableDependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.NodeType
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericEdge
import kotlin.math.*
import kotlin.random.Random

class GraphGenerator : GraphGenerator {
    override fun getPriority(): Int = -1

    private var averageNumberOfSingleNodesPerPath: Double = 0.0
    private var remainingSingleEdgeNodes: Int = 0

    fun createPath(startNode: Node, endNode: Node): Pair<List<Node>, List<Edge>> {
        var nodes = RandomGenerator.getNextArityWith0(mean = averageNumberOfSingleNodesPerPath, variance = 2.0)
        nodes = max(0, nodes)
        nodes = min(remainingSingleEdgeNodes, nodes)
        //nodes = 0 // For debugging

        val nodeList = mutableListOf<Node>()
        val edgeList = mutableListOf<Edge>()

        for (i in 0 until nodes) {
            nodeList.add(Node())
        }

        val totalList = listOf(startNode) + nodeList + listOf(endNode)

        for (i in 0 until totalList.size - 1) {
            edgeList.add(GenericEdge(from = totalList[i], to = totalList[i + 1]))
        }

        // Subtract created edges
        remainingSingleEdgeNodes -= nodes

        return Pair(nodeList, edgeList)
    }


    override fun generate(): DependencyGraph {
        val dependencyGraph = MutableDependencyGraph()

        val totalNodes = Registry.properties.nodes
        val numberOfInputNodes = Registry.properties.inputNodes
        val numberOfOutputNodes = Registry.properties.outputNodes
        val innerNodes = totalNodes - numberOfInputNodes - numberOfOutputNodes

        val numberOfMultiEdgeRules = (innerNodes * Registry.properties.multiEdgeRules).roundToInt()
        var numberOfRecursiveRules = (innerNodes * Registry.properties.recursiveRules).roundToInt()
        val recursiveComplexity = Registry.properties.recursiveComplexity

        // We require at least input nodes-1 + numberOfRecursiveRules multi edges
        val multiEdgeInnerNodes = max(numberOfInputNodes - 1 + numberOfRecursiveRules, numberOfMultiEdgeRules)
        val singleEdgeInnerNodes = innerNodes - multiEdgeInnerNodes


        val totalPaths = multiEdgeInnerNodes * 2 + numberOfOutputNodes

        this.remainingSingleEdgeNodes = singleEdgeInnerNodes
        averageNumberOfSingleNodesPerPath = singleEdgeInnerNodes.toDouble() / totalPaths.toDouble()

        /**
         * How it works:
         * Total number of simple paths:
         * - multi-edge * 2 (two inputs per multi-edge, either to merge two inputs or a recursive path)
         * - outputNodes (one path per output node)
         *
         * 1. We init all multi-nodes
         * 2. We connect each input by a path to some multi node which have at most one inputs (so that we have in total at most two inputs)
         * 3. We determine the number of recursive components (SCCs) and assign the multi-nodes into the SCC
         * 4. For each SCC
         * 4a. We connect the nodes in the SCC
         *      - We first connect each multi edge to some other multi edge and track which nodes are connected
         *      - Thereby we are only allowed to connect to a cycle, if it is ok to have an additional recursive edge
         * 4b. We add a path to the following SCC/to the output
         * 5. We add paths from some random node to an output

         */


        val multiNodes = mutableListOf<Node>()
        val inputNodes = mutableListOf<Node>()
        val outputNodes = mutableListOf<Node>()

        for (i in 0 until numberOfOutputNodes) {
            outputNodes.add(Node(type = NodeType.Output))
        }
        dependencyGraph.nodes.addAll(outputNodes)

        // It is a path with no joins, i.e., there is only a single input
        if (multiEdgeInnerNodes == 0) {
            val iNode = Node(type = NodeType.Input)
            inputNodes.add(iNode)
            val pathInfo = this.createPath(iNode, outputNodes.removeFirst())
            dependencyGraph.nodes.addAll(pathInfo.first)
            pathInfo.second.forEach { dependencyGraph.addEdge(it) }
            dependencyGraph.nodes.addAll(inputNodes)
        } else {
            // Step 1: MultiNode init
            for (i in 0 until multiEdgeInnerNodes) {
                multiNodes.add(Node())
            }
            dependencyGraph.nodes.addAll(multiNodes)

            // Step 2: Input Node to MultiNode
            for (i in 0 until numberOfInputNodes) {
                val iNode = Node(type = NodeType.Input)
                inputNodes.add(iNode)
                val cNode = multiNodes.filter { dependencyGraph.inEdges[it].orEmpty().size < 2 }.random()
                val pathInfo = this.createPath(iNode, cNode)
                dependencyGraph.nodes.addAll(pathInfo.first)
                pathInfo.second.forEach { dependencyGraph.addEdge(it) }
            }
            dependencyGraph.nodes.addAll(inputNodes)

            // Step 2a. Each multi node with two input nodes connect to other multi node
            var changed = true
            while (changed) {
                changed = false
                val fullMultiNodes = multiNodes.filter {
                    dependencyGraph.inEdges[it].orEmpty().size == 2 && dependencyGraph.outEdges[it].orEmpty().isEmpty()
                }

                for (fullMultiNode in fullMultiNodes) {
                    val nextMultiNodes = multiNodes.filter { dependencyGraph.inEdges[it].orEmpty().size < 2 }
                    if (nextMultiNodes.isEmpty() && fullMultiNodes.size == 1) {
                        //The last multi node only required to connect to output
                        val pathInfo = this.createPath(fullMultiNode, outputNodes.removeFirst())
                        dependencyGraph.nodes.addAll(pathInfo.first)
                        pathInfo.second.forEach { dependencyGraph.addEdge(it) }
                        break
                    }
                    val cNode = nextMultiNodes.random()
                    val pathInfo = this.createPath(fullMultiNode, cNode)
                    dependencyGraph.nodes.addAll(pathInfo.first)
                    pathInfo.second.forEach { dependencyGraph.addEdge(it) }
                    changed = true
                }
            }

            // Step 3: Strongly connected components

            // Each component can be connected to itself, if there is at least one input free creating a simple recursion
            val possibleMultiNodes =
                multiNodes.filter { dependencyGraph.inEdges[it].orEmpty().size < 2 }.shuffled().toMutableList()

            if (possibleMultiNodes.isNotEmpty()) {


                val supportiveRecursionMultiNode =
                    multiNodes.filter { dependencyGraph.inEdges[it].orEmpty().isEmpty() }.shuffled().toMutableList()

                val maxRecursiveComponents = min(
                    numberOfRecursiveRules,
                    supportiveRecursionMultiNode.size
                ) // lowest complexity (each multiNode is in its own SCC)

                // At least one recursive component is required
                val recursiveComponents = max(1, (maxRecursiveComponents * (1 - recursiveComplexity)).toInt())

                val averageNumberOfMultiNodes = possibleMultiNodes.size.toDouble() / recursiveComponents.toDouble()

                val sccs: MutableList<List<Node>> = mutableListOf()

                while (possibleMultiNodes.isNotEmpty()) {
                    var nodes = RandomGenerator.getNextArityWith0(mean = averageNumberOfMultiNodes, variance = 2.0)
                    nodes = max(1, nodes)
                    nodes = min(possibleMultiNodes.size, nodes)

                    val sccList = mutableListOf<Node>()

                    for (i in 0 until nodes) {
                        sccList.add(possibleMultiNodes.removeFirst())
                    }

                    sccs.add(sccList)
                }

                // Step 4
                // We sort the components in such a way, that the first component has at least one input so that the component makes sense (i.e., there is a node with an incoming edge)
                while (true) {
                    if (sccs.first().any { dependencyGraph.inEdges[it].orEmpty().size > 0 }) {
                        break
                    }
                    // Shift to the end
                    sccs.add(sccs.removeFirst())
                }

                // We now compute how many recursive edges follow minimum and maximum
                val followingRecursionOptions: Array<Pair<Int, Int>> = Array(sccs.size + 1) { index -> Pair(0, 0) }

                for ((i, scc) in sccs.withIndex().reversed()) {
                    // Following components will return 1 less due to an added edge
                    val additionalEdge = if (i == 0) 0 else 1
                    var maximumRecursiveEdges =
                        max(0, scc.filter { dependencyGraph.inEdges[it].orEmpty().isEmpty() }.size - additionalEdge)
                    var minimumRecursiveEdges = if (maximumRecursiveEdges > 0) 1 else 0

                    maximumRecursiveEdges += followingRecursionOptions[i + 1].second
                    minimumRecursiveEdges += followingRecursionOptions[i + 1].first
                    followingRecursionOptions[i] = Pair(minimumRecursiveEdges, maximumRecursiveEdges)
                }

                var totalRecursiveEdges = 0

                // Update total number by new minimum based on calculation
                numberOfRecursiveRules = min(numberOfRecursiveRules, followingRecursionOptions[0].second)

                for ((i, scc) in sccs.withIndex()) {
                    val minimumRecursiveEdges =
                        (numberOfRecursiveRules - totalRecursiveEdges) - followingRecursionOptions[i + 1].second // total - maximum = minimum required
                    var maximumRecursiveEdges =
                        (numberOfRecursiveRules - totalRecursiveEdges) - followingRecursionOptions[i + 1].first  // total - minimum = maximum required

                    val possibleRecursiveEdges = scc.filter { dependencyGraph.inEdges[it].orEmpty().isEmpty() }.size
                    maximumRecursiveEdges = min(maximumRecursiveEdges, possibleRecursiveEdges)

                    // This is the case, if only a self-loop is possible
                    if (maximumRecursiveEdges == 0) {
                        maximumRecursiveEdges = minimumRecursiveEdges
                    }

                    val recursiveEdges = Random.nextInt(minimumRecursiveEdges, maximumRecursiveEdges + 1)
                    totalRecursiveEdges += recursiveEdges

                    // Here comes the logic for the creation of the SCC
                    // 1. Each multi node requires at least one outgoing edge to a different multi-node
                    var paths = 0

                    val reachingNodes = scc.associateWith { mutableSetOf<Node>() }

                    val allSCCNodes = mutableListOf<Node>()
                    allSCCNodes.addAll(scc)

                    val nodes = scc.shuffled().toMutableList()

                    // We start a path and continue the path until it reaches an existing node
                    var currentNode = nodes.removeFirst()
                    val startNode = currentNode

                    // Add a simple cycle here
                    if (nodes.isEmpty() && totalRecursiveEdges > 0) {
                        val pathInfo = this.createPath(currentNode, currentNode)
                        dependencyGraph.nodes.addAll(pathInfo.first)
                        allSCCNodes.addAll(pathInfo.first)
                        pathInfo.second.forEach { dependencyGraph.addEdge(it) }
                        reachingNodes[currentNode]!!.add(currentNode)
                    }
                    // We connect each multi edge with a different multi-edge
                    // This may create multiple sub SCC nodes
                    while (nodes.isNotEmpty()) {
                        // We open a new path if the node has no incoming edge
                        if (dependencyGraph.inEdges[currentNode].orEmpty().isEmpty()) {
                            paths++
                        }
                        // We select a target node from the SCC
                        // This node can be random, except it is restricted
                        val targetRestricted = paths >= recursiveEdges

                        // We first try to connect to some node that is not connected at all in case it is restricted, otherwise we choose a node that is possible to get an additional edge
                        var targetNodes =
                            if (targetRestricted) nodes else scc.filter { dependencyGraph.inEdges[it].orEmpty().size < 2 }

                        // We connect the path to a node that has no incoming edge, and is (a) not on the same path
                        if (targetNodes.isEmpty()) {
                            targetNodes = scc.filter {
                                dependencyGraph.inEdges[it].orEmpty()
                                    .isEmpty() && !reachingNodes[it]!!.contains(currentNode)
                            }
                        }
                        // or (ii) all are on the same path then the same node is the target
                        if (targetNodes.isEmpty() && paths == 1) {
                            targetNodes = listOf(startNode)
                        }

                        // If no targetNode found, we can connect the node to some node, that is not on the current path
                        val targetNode = if (targetNodes.size > 1) targetNodes.random() else scc.filter {
                            !reachingNodes[it]!!.contains(currentNode)
                        }.random()

                        // This node reaches now every node reached by the target node plus the target node itself
                        reachingNodes[currentNode]!!.add(targetNode)
                        reachingNodes[currentNode]!!.addAll(reachingNodes[targetNode]!!)

                        // Update nodes that each the current middle node also with that nodes
                        for (x in scc) {
                            if (reachingNodes[x]!!.contains(currentNode)) {
                                reachingNodes[x]!!.addAll(reachingNodes[currentNode]!!)
                            }
                        }

                        // Add path to dependency graph
                        val pathInfo = this.createPath(currentNode, targetNode)
                        dependencyGraph.nodes.addAll(pathInfo.first)
                        allSCCNodes.addAll(pathInfo.first)
                        pathInfo.second.forEach { dependencyGraph.addEdge(it) }


                        if (nodes.contains(targetNode)) {
                            nodes.remove(targetNode)
                            currentNode = targetNode
                        } else {
                            currentNode = nodes.removeFirst()
                        }

                    }

                    // Connect nodes

                    // 1. Step: Connect some node from a different SCC path to an open node, if no such SCC exists, then from the current SCC in the path
                    // 2. Step: + Create a DAG of the seperated SCCs
                    // 3. Step: + Close the DAG
                    var openNodes = reachingNodes.filter { !it.value.contains(it.key) }.map { it.key }
                    // We check if the open node is in some other list, then it is not an open node
                    openNodes = openNodes.filter { node ->
                        reachingNodes.filter { it.key != node && it.value.contains(node) }.isEmpty()
                    }

                    val groups = mutableListOf<List<Node>>()

                    // We create a group for each open node
                    // And then we add groups for the remaining SCC
                    for (openNode in openNodes) {
                        val z = listOf(openNode) + reachingNodes[openNode].orEmpty().toList()
                        groups.add(z)
                    }

                    val groupingNodes = reachingNodes.keys.toMutableList()

                    // Remove all SCC covered by openNodes
                    for (x in groups.flatten()) {
                        groupingNodes.remove(x)
                    }

                    // Now only SCC remain
                    while (groupingNodes.isNotEmpty()) {
                        val x = groupingNodes.removeFirst()
                        groups.add(reachingNodes[x].orEmpty().toList())
                        groupingNodes.removeAll(reachingNodes[x].orEmpty())

                        assert(reachingNodes[x].orEmpty().any { it == x })
                    }

                    // Create a random order to connect groups
                    groups.shuffled()
                    val groupToOpenNode = openNodes.associateBy { openNode ->
                        groups.withIndex().first { it.value.contains(openNode) }.index
                    }


                    for (groupId in 0 until groups.size) {
                        // There could be a single non-recursive, then it is also just the node as it will get embedded in the recursion
                        val randomSourceNodes =
                            groups[groupId].filter { groupNode ->
                                reachingNodes[groupNode].orEmpty().contains(groupNode)
                            }

                        val randomSourceNode = if (randomSourceNodes.isNotEmpty()) randomSourceNodes.random() else {
                            groups[groupId].first { groupNode -> reachingNodes[groupNode].orEmpty().isEmpty() }
                        }

                        val targetGroupId = if (groupId + 1 == groups.size) 0 else groupId + 1
                        val randomTargetNode = if (groupToOpenNode.contains(targetGroupId)) {
                            groupToOpenNode[targetGroupId]!!
                        } else {
                            groups[targetGroupId].filter { groupNode ->
                                reachingNodes[groupNode].orEmpty().contains(groupNode)
                            }.random()
                        }

                        // Add path to dependency graph
                        val pathInfo = this.createPath(randomSourceNode, randomTargetNode)
                        dependencyGraph.nodes.addAll(pathInfo.first)
                        allSCCNodes.addAll(pathInfo.first)
                        pathInfo.second.forEach { dependencyGraph.addEdge(it) }
                    }

                    // Step 4. Fill open multi edges with additional paths inside SCC,
                    // Every multi edge node has at least one path
                    assert(scc.none { dependencyGraph.inEdges[it].orEmpty().isEmpty() })

                    val remainingNodesToConnect = scc.filter { dependencyGraph.inEdges[it].orEmpty().size == 1 }

                    // The DAG->cycle connection has increased the paths by 1
                    paths++

                    for (node in remainingNodesToConnect) {
                        val nodesInlcudingLastMultiEdge = mutableListOf<Node>()

                        var currentPathNode = node
                        while (true) {
                            currentPathNode = dependencyGraph.inEdges[currentPathNode]!!.first().from
                            nodesInlcudingLastMultiEdge.add(currentPathNode)
                            if (dependencyGraph.inEdges[currentPathNode].orEmpty().size > 1) {
                                break
                            }
                        }

                        val fromNode = if (paths < recursiveEdges) {
                            // In such a case we require a source node from an edge that is not one before the multi edges
                            paths++
                            allSCCNodes.filter { !nodesInlcudingLastMultiEdge.contains(it) }.random()
                        } else {
                            nodesInlcudingLastMultiEdge.random()
                            // We add a path just from some edge before the previous multi edge
                        }

                        val pathInfo = this.createPath(fromNode, node)
                        dependencyGraph.nodes.addAll(pathInfo.first)
                        allSCCNodes.addAll(pathInfo.first)
                        pathInfo.second.forEach { dependencyGraph.addEdge(it) }
                    }


                    // In certain cases there could be 3 edges, i.e., when there is an input to the component
                    //assert(scc.all { dependencyGraph.inEdges[it].orEmpty().size == 2 })


                    // Connect to next SCC
                    if (i + 1 < sccs.size) {
                        val nextNode = if (sccs[i + 1].size == 1) {
                            sccs[i + 1].first()
                        } else {
                            var sccNextScc = sccs[i + 1].filter { dependencyGraph.inEdges[it].orEmpty().isEmpty() }
                            if (sccNextScc.isEmpty()) {
                                sccNextScc = sccs[i + 1].filter { dependencyGraph.inEdges[it].orEmpty().size == 1 }
                            }
                            sccNextScc.random()
                        }
                        val pathInfo = this.createPath(allSCCNodes.random(), nextNode)
                        dependencyGraph.nodes.addAll(pathInfo.first)
                        pathInfo.second.forEach { dependencyGraph.addEdge(it) }
                    } else {
                        val pathInfo = this.createPath(allSCCNodes.random(), outputNodes.removeFirst())
                        dependencyGraph.nodes.addAll(pathInfo.first)
                        pathInfo.second.forEach { dependencyGraph.addEdge(it) }
                    }

                }
            }
        }

        // connect remaining output nodes by getting a path from a random node in the dependency graph
        while (outputNodes.isNotEmpty()) {
            val pathInfo = this.createPath(
                dependencyGraph.nodes.filter { it.type != NodeType.Output }.random(),
                outputNodes.removeFirst()
            )
            dependencyGraph.nodes.addAll(pathInfo.first)
            pathInfo.second.forEach { dependencyGraph.addEdge(it) }
        }

        /*
        val moreThan2 = dependencyGraph.nodes.filter { dependencyGraph.inEdges[it].orEmpty().size > 2 }
        if (moreThan2.isNotEmpty()) {
            dependencyGraph.draw("debug")
        }*/

        return dependencyGraph
    }


}