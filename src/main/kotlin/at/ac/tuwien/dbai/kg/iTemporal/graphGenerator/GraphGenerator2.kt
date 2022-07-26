package at.ac.tuwien.dbai.kg.iTemporal.graphGenerator

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.GraphGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.MutableDependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.NodeType
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.GenericEdge
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class GraphGenerator2 : GraphGenerator {
    override fun getPriority(): Int = -2

    private val pathLengths = ArrayList<Int>()

    private val inputNodes = ArrayList<Node>()
    private val multiNodes = ArrayList<Node>()
    private val outputNodes = ArrayList<Node>()
    private val dependencyGraph = MutableDependencyGraph()

    // Variances in percent
    private val variancePathLength = 0.25
    private val varianceSCCNodes = 0.25
    private val percentageInsertInSCC = 0.5

    private var recursiveSCCs: List<List<Node>> = mutableListOf()
    private var nonRecursiveSCCs: List<Node> = listOf()

    private var metaGraph: DependencyGraph = DependencyGraph()
    private var recursiveSCCMap: Map<Node, List<Node>> = mapOf()
    private var nonRecursiveSCCMap: Map<Node, Node> = mapOf()

    private var recCount = 0

    /**
     * Stores in pathLengths the number of nodes for the next created path
     * paths the number of paths that are generated
     * nodes the total number of nodes available for the graph
     */
    private fun pregeneratePathLengths(paths: Int, nodes: Int) {
        val averagePathAmount = nodes.toDouble() / paths.toDouble()

        var usedNodes = 0

        // The last path takes the remaining ones (hence we start with 1)
        for (i in 1 until paths) {
            var nextPathLength =
                RandomGenerator.getNextArityWith0(
                    mean = averagePathAmount,
                    variance = averagePathAmount * variancePathLength
                )
            nextPathLength = max(0, nextPathLength)
            nextPathLength = min(nodes - usedNodes, nextPathLength)
            usedNodes += nextPathLength
            pathLengths.add(nextPathLength)
        }

        pathLengths.add(nodes - usedNodes)

        pathLengths.shuffle(RandomGenerator.sharedRandom)
    }

    /**
     * Adds a path to the dependency graph between two nodes
     * Returns the inserted inner nodes
     */
    private fun createPath(startNode: Node, endNode: Node): List<Node> {
        val length = pathLengths.removeFirst()
        val nodeList = mutableListOf<Node>()

        for (i in 0 until length) {
            nodeList.add(Node())
        }
        dependencyGraph.nodes.addAll(nodeList)

        val totalList = listOf(startNode) + nodeList + listOf(endNode)

        for (i in 0 until totalList.size - 1) {
            dependencyGraph.addEdge(GenericEdge(from = totalList[i], to = totalList[i + 1]))
        }

        return nodeList
    }

    /**
     * Connects the input nodes to arbitrary multi nodes, or in case no multi node exist, then to the output node
     */
    private fun connectInputNodes() {
        if (multiNodes.isEmpty()) {
            if (inputNodes.size > 1) {
                throw RuntimeException("MultiNodes and InputNodes do not match")
            }
            createPath(inputNodes.first(), outputNodes.removeFirst())
            return
        }

        for (iNode in inputNodes) {
            val cNode = multiNodes.filter { dependencyGraph.inEdges[it].orEmpty().size < 2 }
                .random(RandomGenerator.sharedRandom)
            createPath(iNode, cNode)
        }
    }

    /**
     * Connects full multiNodes, i.e., nodes that are not allowed to get an additional input to the next multiNode
     */
    private fun connectFullMultiNodes() {
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
                    this.createPath(fullMultiNode, outputNodes.removeFirst())
                    break
                }
                val cNode = nextMultiNodes.random(RandomGenerator.sharedRandom)
                this.createPath(fullMultiNode, cNode)
                changed = true
            }
        }
    }

    /**
     * This function groups the number of multi nodes into SCCs
     */
    private fun computeSCCDistribution(recursiveRules: Int, recursiveComplexity: Double) {
        /**
         * One can create the following components:
         *
         * a->b->c and b->b, so always adding a self-loop
         * there could be even something like b->d->b and b->d so twice an edge from b to d
         * In case the recursive complexity is 0, we only create such cycles mentioned above
         * Such a cycle count as 1 recursive edge
         *
         * In case the recursive complexity is one
         * there is a single SCC except those of input and output nodes
         * i->a->b->c->o and some edge c->a would create such component
         *
         */

        // We determine the number of groups

        // Only nodes without an incoming edge can act as a recursive node, why?
        // if it has an incoming edge, then this is from an input (prev scc)
        // When we connect that node with some previous SCC, then the second input is also blocked, but is there an exception?
        // Yes, in case there is no incoming additional SCC, then the second edge can form a cycle, and then the node connects to some other SCC
        // How many of such SCC types are allowed?
        // In order that this works out, for n such nodes, n-1 nodes with empty incoming edges are required so that they also then form a single cycle
        val maxNumberOfEmptyNodes = multiNodes.filter { dependencyGraph.inEdges[it].orEmpty().isEmpty() }.size
        val maxNumberOfOneNodes = multiNodes.filter { dependencyGraph.inEdges[it].orEmpty().size == 1 }.size

        val maxNumberOfNodes =
            maxNumberOfEmptyNodes + maxNumberOfOneNodes - max(0, maxNumberOfOneNodes - (maxNumberOfEmptyNodes - 1))

        val maxNumberRecursiveRules = min(recursiveRules, maxNumberOfNodes)


        var amountNumberRecursiveComponents = (maxNumberRecursiveRules * (1.0 - recursiveComplexity)).roundToInt()

        // Set minimum, in case there is a recursive rule
        if (amountNumberRecursiveComponents == 0 && maxNumberRecursiveRules > 0) {
            amountNumberRecursiveComponents = 1
        }


        // There are multi nodes, which do not require a recursive edge.
        // We can add them to some recursive SCC or use them for some intermediary step.
        // How we use them depends on the randomness of the average number of nodes


        val recursiveSCCs = mutableListOf<MutableList<Node>>()
        val possibleNodes =
            (multiNodes.filter { dependencyGraph.inEdges[it].orEmpty().isEmpty() }
                    + multiNodes.filter { dependencyGraph.inEdges[it].orEmpty().size == 1 }
                .shuffled(RandomGenerator.sharedRandom)
                .take(maxNumberOfOneNodes))
                .shuffled(RandomGenerator.sharedRandom).toMutableList()

        // Used to count the number of recRules
        var recRuleCount = 0

        // Each SCC has at least one node
        for (i in 0 until amountNumberRecursiveComponents) {
            recursiveSCCs.add(mutableListOf())
            val nodeToAdd = possibleNodes.removeFirst()
            recursiveSCCs[i].add(nodeToAdd)
            recRuleCount++
        }

        // Now we add the remaining nodes as long as we have nodes

        // We can only add nodes that have zero incoming edges, as all other nodes do not contribute to recursive edges.
        val oneEdgeNodes = possibleNodes.filter { dependencyGraph.inEdges[it].orEmpty().isNotEmpty() }.toMutableList()
        possibleNodes.removeIf { dependencyGraph.inEdges[it].orEmpty().isNotEmpty() }

        // Average number of nodes per component
        val averageNumberOfRecMultiNodes =
            if (amountNumberRecursiveComponents > 0) maxNumberRecursiveRules / amountNumberRecursiveComponents else 0

        for (i in 0 until amountNumberRecursiveComponents) {
            if (possibleNodes.isEmpty()) {
                break
            }

            val numberOfNodesInComponent =
                RandomGenerator.getNextArityWith0(mean = averageNumberOfRecMultiNodes, variance = varianceSCCNodes)

            while (recursiveSCCs[i].size < numberOfNodesInComponent) {
                val nodeToAdd = possibleNodes.removeFirst()
                recursiveSCCs[i].add(nodeToAdd)
                if (recursiveSCCs[i].size > 2 || dependencyGraph.inEdges[recursiveSCCs[i][0]].orEmpty().isEmpty()) {
                    recRuleCount++
                }
                if (possibleNodes.isEmpty()) {
                    break
                }
            }
        }

        // Fill up with possible nodes as long as recRuleCount < maxNumberRecursiveRules by adding a node to a random component
        // Note that there can be less available nodes, as the single recursions (single edge) influences the number of elements, but if combined reduces the number of possible recursions.
        // Usually we recommend that the number of recursive edges is smaller than the number of multi edges so that this has no impact.
        while (possibleNodes.isNotEmpty() && recRuleCount < maxNumberRecursiveRules) {
            val selectedSCC = RandomGenerator.sharedRandom.nextInt(amountNumberRecursiveComponents)
            val nodeToAdd = possibleNodes.removeFirst()
            recursiveSCCs[selectedSCC].add(nodeToAdd)
            recRuleCount++
        }

        // What we have now:
        // Nodes grouped into n recursive SCCs
        // Nodes that are not grouped into SCCs that have
        // (a) no ingoing edge (i.e. current possible nodes)
        // (b) one ingoing edge (i.e. oneEdgeNodes)

        // For b, they can be a separate SCC or be inserted in one of the recursive SCCs that contain a node with empty ingoing edges
        val recRelevant = recursiveSCCs.filter { recSCC ->
            recSCC.any { dependencyGraph.inEdges[it].orEmpty().isEmpty() }
        }

        if (recRelevant.isNotEmpty()) {
            val removalList = mutableListOf<Node>()
            for (oneEdgeNode in oneEdgeNodes) {
                if (RandomGenerator.sharedRandom.nextDouble() < percentageInsertInSCC) {
                    recRelevant.random(RandomGenerator.sharedRandom).add(oneEdgeNode)
                    removalList.add(oneEdgeNode)
                }
            }
            oneEdgeNodes.removeAll(removalList)
        }


        if (recRelevant.isNotEmpty()) {
            val removalList = mutableListOf<Node>()
            for (possibleNode in possibleNodes) {
                if (RandomGenerator.sharedRandom.nextDouble() < percentageInsertInSCC) {
                    recRelevant.random(RandomGenerator.sharedRandom).add(possibleNode)
                    removalList.add(possibleNode)
                }
            }
            possibleNodes.removeAll(removalList)
        }

        // These component have its own SCC
        val nonRecSCCs = mutableListOf<Node>()
        nonRecSCCs.addAll(possibleNodes)
        nonRecSCCs.addAll(oneEdgeNodes)

        this.recursiveSCCs = recursiveSCCs.shuffled(RandomGenerator.sharedRandom)
        this.nonRecursiveSCCs = nonRecSCCs.shuffled(RandomGenerator.sharedRandom)
    }


    private fun propagateEdgeAdd(
        graphNodes: Set<Node>,
        edge: Edge,
        ingoingNodes: MutableMap<Node, MutableSet<Node>>,
        outgoingNodes: MutableMap<Node, MutableSet<Node>>
    ) {
        outgoingNodes[edge.from]!!.add(edge.to)
        outgoingNodes[edge.from]!!.addAll(outgoingNodes[edge.to]!!)

        ingoingNodes[edge.to]!!.add(edge.from)
        ingoingNodes[edge.to]!!.addAll(ingoingNodes[edge.from]!!)


        // Update other nodes
        for (y in graphNodes) {
            if (outgoingNodes[y]!!.contains(edge.from)) {
                outgoingNodes[y]!!.addAll(outgoingNodes[edge.from]!!)
            }
            if (ingoingNodes[y]!!.contains(edge.to)) {
                ingoingNodes[y]!!.addAll(ingoingNodes[edge.to]!!)
            }
        }
    }

    private fun propagateRemoveAdd(
        graph: DependencyGraph,
        ingoingNodes: MutableMap<Node, MutableSet<Node>>,
        outgoingNodes: MutableMap<Node, MutableSet<Node>>
    ) {
        for (y in graph.nodes) {
            ingoingNodes[y]!!.clear()
            outgoingNodes[y]!!.clear()
            ingoingNodes[y]!!.addAll(graph.inEdges[y].orEmpty().map { it.from })
            outgoingNodes[y]!!.addAll(graph.outEdges[y].orEmpty().map { it.to })
        }

        var changed = true
        while (changed) {
            changed = false
            for (y in graph.nodes) {
                val added1 =
                    ingoingNodes[y]!!.addAll(graph.inEdges[y].orEmpty().flatMap { e -> ingoingNodes[e.from]!! })
                val added2 =
                    outgoingNodes[y]!!.addAll(graph.outEdges[y].orEmpty().flatMap { e -> outgoingNodes[e.to]!! })
                if (added1 || added2) {
                    changed = true
                }
            }
        }
    }

    /**
     * Connects the nodes randomly in meta graph
     */
    private fun computeSccGraphInitial(
        metaGraph: MutableDependencyGraph,
        ingoingNodes: MutableMap<Node, MutableSet<Node>>,
        outgoingNodes: MutableMap<Node, MutableSet<Node>>
    ): MutableList<Node> {
        val isConnectedToInput = metaGraph.nodes.associateWith { false }.toMutableMap()
        val allowedConnections = metaGraph.nodes.associateWith { 0 }.toMutableMap()

        // There some nodes which have to be connected first, i.e., those nodes which are recursive but have to connect to some other node and are not allowed to have an input
        val rec1 = this.recursiveSCCMap.keys.filter { metaNode ->
            this.recursiveSCCMap[metaNode].orEmpty().all { dependencyGraph.inEdges[it].orEmpty().size == 1 }
        }
        val rec2 = this.recursiveSCCMap.keys.filter { key1 -> rec1.none { key1 == it } }

        val nonRec1 = this.nonRecursiveSCCMap.keys.filter { metaNode ->
            dependencyGraph.inEdges[this.nonRecursiveSCCMap[metaNode]].orEmpty().isNotEmpty()
        }
        val nonRec2 = this.nonRecursiveSCCMap.keys.filter { key1 -> nonRec1.none { key1 == it } }

        for (el in rec1) {
            isConnectedToInput[el] = true
            allowedConnections[el] = 0
        }
        for (el in rec2) {
            isConnectedToInput[el] =
                this.recursiveSCCMap[el].orEmpty().any { dependencyGraph.inEdges[it].orEmpty().isNotEmpty() }
            allowedConnections[el] =
                this.recursiveSCCMap[el].orEmpty().filter { dependencyGraph.inEdges[it].orEmpty().isEmpty() }.size
        }
        for (el in nonRec1) {
            isConnectedToInput[el] = true
            allowedConnections[el] = 1
        }
        for (el in nonRec2) {
            isConnectedToInput[el] = false
            allowedConnections[el] = 2
        }

        val metaNodesToProcess = metaGraph.nodes.toMutableList()

        val outputComponents = mutableListOf<Node>()

        loop@ while (metaNodesToProcess.isNotEmpty()) {
            val nextNode =
                metaNodesToProcess.filter { isConnectedToInput[it] == true }.random(RandomGenerator.sharedRandom)
            metaNodesToProcess.remove(nextNode)

            // Add possible splits along the path
            val numberOfNextComponents = RandomGenerator.sharedRandom.nextInt(1, 3)

            for (x in 0 until numberOfNextComponents) {
                // No self-loop allowed
                // No cycles are allowed
                // The node is allowed to have an additional incoming edge
                val connectingNodes = metaGraph.nodes.filter {
                    it != nextNode && allowedConnections[it]!! > 0 && outgoingNodes[it].orEmpty()
                        .none { oNode -> oNode == nextNode }
                }

                if (connectingNodes.isEmpty()) {
                    // Only add, if there is no connection yet
                    if (x == 0) {
                        outputComponents.add(nextNode)
                    }
                    continue@loop
                }
                val connectingNode = connectingNodes.random(RandomGenerator.sharedRandom)

                val edge = GenericEdge(from = nextNode, to = connectingNode)
                metaGraph.addEdge(edge)
                allowedConnections[connectingNode] = allowedConnections[connectingNode]!! - 1
                isConnectedToInput[connectingNode] = true


                this.propagateEdgeAdd(metaGraph.nodes, edge, ingoingNodes, outgoingNodes)
            }
        }


        if (outputComponents.isEmpty()) {
            assert(this.recursiveSCCs.isEmpty())
            assert(this.nonRecursiveSCCs.isEmpty())
        }

        return outputComponents
    }

    /**
     * We have different groups of DAGs that are not connected and hence create independent queries, so we have to merge them into a single group
     */
    private fun computeSccGraphMergeDAGs(
        metaGraph: MutableDependencyGraph,
        ingoingNodes: MutableMap<Node, MutableSet<Node>>,
        outgoingNodes: MutableMap<Node, MutableSet<Node>>,
        outputComponents: MutableList<Node>
    ) {
        // We have to ensure that all components are connected
        // We check for overlapping ingoing nodes for each outputComponent

        val connectedOutputComponents = mutableListOf<MutableList<Node>>()

        for (i in 0 until outputComponents.size) {
            val node = outputComponents[i]

            val components = connectedOutputComponents.filter { connectedOutputComponent ->
                connectedOutputComponent.any {
                    ingoingNodes[it].orEmpty().intersect(ingoingNodes[node].orEmpty()).isNotEmpty()
                }
            }


            if (components.isEmpty()) {
                connectedOutputComponents.add(mutableListOf(node))
            } else {
                val x = components.flatten().toMutableList()
                x.add(node)
                connectedOutputComponents.removeAll(components)
                connectedOutputComponents.add(x)
                components.first().add(node)
            }
        }


        // By definition there is no node which allows an additional connection as otherwise it would be connected
        // Hence we select a node that is splittable, i.e. a node where one edge can be removed
        // This is, when one following node (outgoingNodes) of the edge target contains a different ingoing node
        // or there is a point where the graph again merges with the same input node
        while (connectedOutputComponents.size > 1) {
            val nodesWithMoreOutgoingEdges = metaGraph.nodes
                .filter { metaGraph.outEdges[it].orEmpty().size > 1 }
                .filter { targetNode ->
                    val targetList =
                        metaGraph.outEdges[targetNode]!!.map { e -> outgoingNodes[e.to].orEmpty() + e.to }.withIndex()
                    val reachableNodes = metaGraph.getConnectedNodes(targetNode)

                    // Check whether the target node has an edge which removal is not splitting the connected component.
                    metaGraph.outEdges[targetNode]!!.any {
                        metaGraph.getConnectedNodes(targetNode, setOf(it)).size == reachableNodes.size
                    }
                }


            if (nodesWithMoreOutgoingEdges.isEmpty()) {
                metaGraph.draw("metaGraph")
                throw RuntimeException("Cannot combine components, lets start debugging ...")
            }

            val targetNode = nodesWithMoreOutgoingEdges.random(RandomGenerator.sharedRandom)


            if (connectedOutputComponents.none { outputNodes ->
                    outputNodes.any { ingoingNodes[it].orEmpty().contains(targetNode) }
                }) {
                metaGraph.draw("metaGraph")
                throw RuntimeException("Something is wrong here, lets start debugging ...")
            }
            val thisComponent = connectedOutputComponents.first { outputNodes ->
                outputNodes.any { ingoingNodes[it].orEmpty().contains(targetNode) }
            }
            val otherComponent = connectedOutputComponents.filter {
                it != thisComponent
            }.random(RandomGenerator.sharedRandom)
            // Add otherComponent to this component
            thisComponent.addAll(otherComponent)
            // Remove other component
            connectedOutputComponents.remove(otherComponent)

            // Remove edge
            val allEdges =
                metaGraph.outEdges[targetNode].orEmpty().map { e -> Pair(e, outgoingNodes[e.to].orEmpty() + e.to) }

            val reachableNodes = metaGraph.getConnectedNodes(targetNode)

            /*Check whether the component is connected without the edge*/
            val possibleEdges = allEdges.filter { possibleEdgeForRemoval ->
                val reachableNodesAfterRemoval =
                    metaGraph.getConnectedNodes(targetNode, setOf(possibleEdgeForRemoval.first))
                reachableNodesAfterRemoval.size == reachableNodes.size
            }

            assert(possibleEdges.isNotEmpty())

            val edge = possibleEdges.random(RandomGenerator.sharedRandom).first
            // Update removal

            val nodeForConnection = otherComponent.random(RandomGenerator.sharedRandom)
            outputComponents.remove(nodeForConnection)
            val newEdge = GenericEdge(from = nodeForConnection, to = edge.to)

            metaGraph.removeEdge(edge)
            metaGraph.addEdge(newEdge)
            this.propagateRemoveAdd(metaGraph, ingoingNodes, outgoingNodes)

        }

    }

    /**
     * We have a single DAG, but it may contain to many nodes which require an output.
     * Here we reduce the number of output nodes to the amount of required outputs.
     */
    private fun computeSccGraphReduceOutputs(
        metaGraph: MutableDependencyGraph,
        ingoingNodes: MutableMap<Node, MutableSet<Node>>,
        outgoingNodes: MutableMap<Node, MutableSet<Node>>,
        outputComponents: MutableList<Node>
    ) {
        //
        // By the construction, we cannot find a node that is connectable with the output component.
        // We are allowed to have at most n outputs
        // The other outputs have to be restructured.
        // (1) we remove edges from nodes with multiple outgoing edges and connect an output, in case no recursion is created
        // thereby we are only allowed to remove an edge such that an existing component is not decoupled.
        // This includes the case where after the connection the decoupling is resolved.


        while (outputComponents.size > outputNodes.size) {
            val nodesWithMoreOutgoingEdges = metaGraph.nodes
                .filter { metaGraph.outEdges[it].orEmpty().size > 1 }

            if (nodesWithMoreOutgoingEdges.isEmpty()) {
                metaGraph.draw("metaGraph")
                throw RuntimeException("Cannot combine outputs, no multi edges, lets start debugging ...")
            }

            val potentialEdges = nodesWithMoreOutgoingEdges.flatMap { node ->
                metaGraph.outEdges[node].orEmpty().map { Pair(it, metaGraph.getReachableNodes(it.to, setOf(it))) }
            }

            if (potentialEdges.isEmpty()) {
                metaGraph.draw("metaGraph")
                throw RuntimeException("no potential edge found to remove...")
            }

            val outputNodeOrder = outputComponents.shuffled(RandomGenerator.sharedRandom)

            var selectedOutput: Node? = null
            var selectedEdge: Edge? = null

            for (possibleOutputNode in outputNodeOrder) {
                // Select possible edges for the node
                // 1) After the edge is replaced, the output node has to reach a different node
                // 2) The new graph has to be fully connected
                // 3) The new graph has to be a DAG

                val otherOutputNodeReachableEdgeList = potentialEdges.filter { edgeReachableNodes ->
                    // Check for DAG, check for reaching a different output node
                    edgeReachableNodes.second.none { it == possibleOutputNode } && edgeReachableNodes.second.any { it in outputComponents }
                }.filter {
                    // Check for fully connected, either the edge removal has no impact on the connectedness or the target is
                    // not connected to the output node anymore
                    val connectedNodesTarget = metaGraph.getConnectedNodes(it.first.to, setOf(it.first))
                    connectedNodesTarget.size == metaGraph.nodes.size || !connectedNodesTarget.contains(
                        possibleOutputNode
                    )
                }.map { it.first }


                if (otherOutputNodeReachableEdgeList.isEmpty()) continue


                selectedOutput = possibleOutputNode
                selectedEdge = otherOutputNodeReachableEdgeList.random(RandomGenerator.sharedRandom)

                break
            }

            if (selectedOutput == null || selectedEdge == null) {
                metaGraph.draw("metaGraph")
                throw RuntimeException("No possible edge found for reducing outputs")
            }


            // Update removal
            val newEdge = GenericEdge(from = selectedOutput, to = selectedEdge.to)

            metaGraph.removeEdge(selectedEdge)
            metaGraph.addEdge(newEdge)

            this.propagateRemoveAdd(metaGraph, ingoingNodes, outgoingNodes)

            outputComponents.remove(selectedOutput)
        }
    }

    private fun computeSccGraph() {
        val recursiveSCCsMap = this.recursiveSCCs.associateBy { Node() }
        val nonRecursiveSCCsMap = this.nonRecursiveSCCs.associateBy { Node() }.toMutableMap()

        val metaNodes = recursiveSCCsMap.keys + nonRecursiveSCCsMap.keys
        val metaGraph = MutableDependencyGraph()
        metaGraph.nodes.addAll(metaNodes)

        this.metaGraph = metaGraph
        this.recursiveSCCMap = recursiveSCCsMap
        this.nonRecursiveSCCMap = nonRecursiveSCCsMap

        val ingoingNodes = metaNodes.associateWith { mutableSetOf<Node>() }.toMutableMap()
        val outgoingNodes = metaNodes.associateWith { mutableSetOf<Node>() }.toMutableMap()


        val outputComponents = this.computeSccGraphInitial(metaGraph, ingoingNodes, outgoingNodes)

        ingoingNodes.forEach { x ->
            assert(!x.value.contains(x.key))
        }
        outgoingNodes.forEach { x ->
            assert(!x.value.contains(x.key))
        }

        this.computeSccGraphMergeDAGs(metaGraph, ingoingNodes, outgoingNodes, outputComponents)

        ingoingNodes.forEach { x ->
            assert(!x.value.contains(x.key))
        }
        outgoingNodes.forEach { x ->
            assert(!x.value.contains(x.key))
        }

        assert(metaGraph.isFullyConnected())

        this.computeSccGraphReduceOutputs(metaGraph, ingoingNodes, outgoingNodes, outputComponents)

        assert(metaGraph.isFullyConnected())

        ingoingNodes.forEach { x ->
            assert(!x.value.contains(x.key))
        }
        outgoingNodes.forEach { x ->
            assert(!x.value.contains(x.key))
        }


        // Connect outputs
        val outputComps = metaGraph.nodes.filter { metaGraph.outEdges[it].orEmpty().isEmpty() }
        for (outputComponent in outputComps) {
            val outMap = Node()
            metaGraph.addEdge(GenericEdge(from = outputComponent, to = outMap))
            nonRecursiveSCCsMap[outMap] = outputNodes.removeFirst()
        }

        while (outputNodes.isNotEmpty() && metaGraph.nodes.isNotEmpty()) {
            val outMap = Node()
            metaGraph.addEdge(GenericEdge(from = metaGraph.nodes.random(RandomGenerator.sharedRandom), to = outMap))
            nonRecursiveSCCsMap[outMap] = outputNodes.removeFirst()
        }

    }

    /**
     * Helper function to connect the nodes in a single component
     */
    private fun connectSingleComponent(multiNodes: Set<Node>, allNodes: MutableSet<Node>, numberOfRecursiveRules: Int) {
        /**
         * For each node we create an arbitrary path to a different/same node
         * Following paths are allowed:
         * (a) to any node that has no ingoing edge yet
         * (b) to a node that contains a single ingoing edge and is not the same node
         *     (1) is not in a cycle
         *          (i)   and the current node does not create a cycle when connecting
         *          (ii)  creates a cycle and there is a different node in the cycle that has at most one ingoing edge
         *          (iii) creates a cycle and there is a node where the the ingoingEdges differ to the outgoingEdges after closing the cycle
         *     (2) is in a cycle, but
         *          (i)   the current node is not connected to the SCC
         *          (ii)  the current node is part of the cycle and there is a different node in the cycle that has at most one ingoing edge
         *          (iii) the current node is part of the cycle and there is a node where the ingoingEdges differ to the outgoingEdges
         * In short, we do not allow to create a sub-scc which cannot be connected to a different sub-scc to merge them together
         * (c) if a and b fails, then there exists a node that contains a single ingoing edge
         *
         * Afterwards we connect the current sub-components.
         */

        val processNodes = multiNodes.toMutableList()

        val ingoingNodes: MutableMap<Node, MutableSet<Node>> =
            multiNodes.associateWith { mutableSetOf<Node>() }.toMutableMap()
        val outgoingNodes: MutableMap<Node, MutableSet<Node>> =
            multiNodes.associateWith { mutableSetOf<Node>() }.toMutableMap()

        while (processNodes.isNotEmpty()) {
            val currentNode = processNodes.removeFirst()

            val listA = multiNodes.filter { targetNode -> dependencyGraph.inEdges[targetNode].orEmpty().isEmpty() }
            val listB =
                multiNodes.filter { targetNode -> targetNode != currentNode && dependencyGraph.inEdges[targetNode].orEmpty().size == 1 }
            val list1 = listB.filter { targetNode -> !outgoingNodes[targetNode]!!.contains(targetNode) }
            val list1I = list1.filter { targetNode -> !ingoingNodes[currentNode]!!.contains(targetNode) }
            val list1II = list1
                .filter { targetNode -> ingoingNodes[currentNode]!!.contains(targetNode) }
                .filter { targetNode ->
                    val cycleNodes =
                        (ingoingNodes[currentNode]!! + currentNode).intersect((outgoingNodes[targetNode]!! + targetNode))
                    cycleNodes.any { cycleNode -> cycleNode != targetNode && dependencyGraph.inEdges[cycleNode].orEmpty().size < 2 }
                }
            val list1III = list1
                .filter { targetNode -> ingoingNodes[currentNode]!!.contains(targetNode) }
                .filter { targetNode ->
                    val cycleNodes =
                        (ingoingNodes[currentNode]!! + currentNode).intersect((outgoingNodes[targetNode]!! + targetNode))
                    cycleNodes.any { cycleNode -> ingoingNodes[cycleNode]!!.minus(cycleNodes).isNotEmpty() }
                }
            val list2 = listB.filter { targetNode -> outgoingNodes[targetNode]!!.contains(targetNode) }
            val list2I = list2.filter { targetNode -> !outgoingNodes[targetNode]!!.contains(currentNode) }
            val list2II = list2.filter { targetNode ->
                outgoingNodes[targetNode]!!.intersect(ingoingNodes[targetNode]!!)
                    .any { cycleNode -> cycleNode != targetNode && (dependencyGraph.inEdges[cycleNode].orEmpty().size < 2) }
            }
            val list2III = list2.filter { targetNode ->
                outgoingNodes[targetNode]!!.contains(currentNode) && ingoingNodes[targetNode]!!.minus(outgoingNodes[targetNode])
                    .isNotEmpty()
            }
            var possibleNodes = (listA + list1I + list1II + list1III + list2I + list2II + list2III).toSet()

            if (possibleNodes.isEmpty()) {
                val otherNodes =
                    multiNodes.filter { targetNode -> dependencyGraph.inEdges[targetNode].orEmpty().size == 1 }
                if (otherNodes.size > 1) {
                    dependencyGraph.draw("dgDebug")
                    throw RuntimeException("too many nodes ... lets debug")
                }
                possibleNodes = otherNodes.toSet()
            }
            val targetNode = possibleNodes.random(RandomGenerator.sharedRandom)

            val addedNodes = this.createPath(currentNode, targetNode)
            allNodes.addAll(addedNodes)
            this.propagateEdgeAdd(
                multiNodes,
                GenericEdge(from = currentNode, to = targetNode),
                ingoingNodes,
                outgoingNodes
            )

            // If we reach the currentNode, we created a recursive edge
            if (outgoingNodes[currentNode]!!.contains(currentNode)) {
                this.recCount++
            }
        }

        /**
         * Connection step
         * 1. Step: Connect some node from a different SCC path to an open node, if no such SCC exists, then from the current SCC in the path
         * 2. Step: + Create a DAG of the seperated SCCs
         * 3. Step: + Close the DAG
         */

        //Remove cycles
        var openNodes = outgoingNodes.filter { !it.value.contains(it.key) }.map { it.key }
        // Remove paths
        openNodes = openNodes.filter { node ->
            outgoingNodes.filter { it.key != node && it.value.contains(node) }.isEmpty()
        }

        // We create a group for each open node (i.e. a node that has no ingoing edge)
        // And then we add groups for the remaining SCC
        val groups = mutableListOf<List<Node>>()

        for (openNode in openNodes) {
            val z = listOf(openNode) + outgoingNodes[openNode].orEmpty().toList()
            groups.add(z)
        }

        //val groupingNodes = outgoingNodes.keys.toMutableList()
        // Restrict to SCC nodes (i.e. nodes that are not outgoing edges of an SCC)
        val groupingNodes = outgoingNodes.filter { it.value.contains(it.key) }.keys.toMutableList()


        // Remove all SCC covered by openNodes
        for (x in groups.flatten()) {
            groupingNodes.remove(x)
        }


        // Now only SCC remain
        while (groupingNodes.isNotEmpty()) {
            val x = groupingNodes.removeFirst()
            groups.add(outgoingNodes[x].orEmpty().toList())
            groupingNodes.removeAll(outgoingNodes[x].orEmpty())

            assert(outgoingNodes[x].orEmpty().any { it == x })
        }

        // Create a random order to connect groups
        groups.shuffled(RandomGenerator.sharedRandom)
        //For each open node, map to unique group index
        val groupToOpenNode = openNodes.associateBy { openNode ->
            groups.withIndex().first { it.value.contains(openNode) }.index
        }

        // Either there is more than one group, or the group has an open node which has to be connected
        if (groups.size > 1 || openNodes.isNotEmpty()) {
            for (groupId in 0 until groups.size) {
                val targetGroupId = if (groupId + 1 == groups.size) 0 else groupId + 1


                // We are only allowed to select a node that ends in an SCC for connecting to next group
                val randomSourceNodes = groups[groupId].filter { sourceNode ->
                    outgoingNodes[sourceNode].orEmpty().contains(sourceNode)
                }

                val randomSourceNode = randomSourceNodes.random(RandomGenerator.sharedRandom)

                // Select target node in next group, this is either the open node
                // And if it is an SCC, then a node that allows to add an edge
                val randomTargetNode = if (groupToOpenNode.contains(targetGroupId)) {
                    groupToOpenNode[targetGroupId]!!
                } else {
                    val possibleTargetNodes = groups[targetGroupId]
                        .filter { targetNode -> dependencyGraph.inEdges[targetNode].orEmpty().size < 2 }
                    if (possibleTargetNodes.isEmpty()) {
                        dependencyGraph.draw("dgDebug")
                    }
                    possibleTargetNodes.random(RandomGenerator.sharedRandom)
                }

                // Add path to dependency graph
                val addedNodes = this.createPath(randomSourceNode, randomTargetNode)
                allNodes.addAll(addedNodes)

            }

            // This creates exactly one recursive edge
            recCount++
        }

        assert(multiNodes.none { targetNode -> dependencyGraph.inEdges[targetNode].orEmpty().isEmpty() })
        assert(multiNodes.none { targetNode -> dependencyGraph.inEdges[targetNode].orEmpty().size > 2 })
        val missingInputs = multiNodes.filter { targetNode -> dependencyGraph.inEdges[targetNode].orEmpty().size < 2 }

        for (targetNode in missingInputs) {
            val sourceNode = allNodes.random(RandomGenerator.sharedRandom)
            val addedNodes = this.createPath(sourceNode, targetNode)
            allNodes.addAll(addedNodes)
            recCount++
        }

    }

    /**
     * Creates the final dependency graph
     */
    private fun createGraph(numberOfRecursiveRules: Int) {
        // Stores whether node of meta graph has been computed
        val nodeHandledInfo = this.metaGraph.nodes.associateWith { false }.toMutableMap()

        for (i in 0 until this.metaGraph.nodes.size) {
            val possibleComponents = this.metaGraph.nodes.filter { node ->
                nodeHandledInfo[node] == false && this.metaGraph.inEdges[node].orEmpty()
                    .all { edge -> nodeHandledInfo[edge.from]!! }
            }
            if (possibleComponents.isEmpty()) {
                metaGraph.draw("metaGraph")
                throw RuntimeException("some unexpected error, lets start debugging ...")
            }
            val component = possibleComponents.first()

            val isRecursive = this.recursiveSCCMap.contains(component)

            val innerNodes =
                (if (isRecursive) this.recursiveSCCMap[component]!! else listOf(this.nonRecursiveSCCMap[component]!!)).toSet()

            val allNodes = mutableSetOf<Node>()
            allNodes.addAll(innerNodes)

            if (isRecursive) {
                this.connectSingleComponent(innerNodes, allNodes, numberOfRecursiveRules)
            }

            // Connect all outgoing edges to next node and set handled to true
            for (outEdge in this.metaGraph.outEdges[component].orEmpty()) {
                val fromNode = allNodes.random(RandomGenerator.sharedRandom)
                val isTargetRecursive = this.recursiveSCCMap.contains(outEdge.to)
                val targetNodes =
                    if (isTargetRecursive) this.recursiveSCCMap[outEdge.to]!! else listOf(this.nonRecursiveSCCMap[outEdge.to]!!)
                val selectableNodes = if (isTargetRecursive) {
                    targetNodes.filter { this.dependencyGraph.inEdges[it].orEmpty().isEmpty() }
                } else {
                    targetNodes
                }

                if (selectableNodes.isEmpty()) {
                    metaGraph.draw("metaGraph")
                    dependencyGraph.draw("dgDebug")
                    throw RuntimeException("some unexpected error, lets start debugging ...")
                }

                val toNode = selectableNodes.random(RandomGenerator.sharedRandom)
                this.createPath(startNode = fromNode, endNode = toNode)
            }

            nodeHandledInfo[component] = true

        }

        // Connect remaining output nodes in case no meta node existed for linking in previous component
        while (outputNodes.isNotEmpty()) {
            val node = dependencyGraph.nodes.filter { dependencyGraph.outEdges[it].orEmpty().isNotEmpty() }
                .random(RandomGenerator.sharedRandom)
            this.createPath(node, outputNodes.removeFirst())
        }

    }

    override fun generate(): DependencyGraph {

        val totalNodes = Registry.properties.nodes
        val numberOfInputNodes = Registry.properties.inputNodes
        val numberOfOutputNodes = Registry.properties.outputNodes
        val recursiveComplexity = Registry.properties.recursiveComplexity
        val innerNodes = totalNodes - numberOfInputNodes - numberOfOutputNodes
        val numberOfMultiEdgeRules = (innerNodes * Registry.properties.multiEdgeRules).roundToInt()
        val numberOfRecursiveRules = (innerNodes * Registry.properties.recursiveRules).roundToInt()

        // We require at least input nodes-1 + numberOfRecursiveRules multi edges
        val multiEdgeInnerNodes = max(numberOfMultiEdgeRules, numberOfInputNodes - 1 + numberOfRecursiveRules)
        val singleEdgeInnerNodes = max(0, innerNodes - multiEdgeInnerNodes)

        if (multiEdgeInnerNodes > innerNodes) {
            throw RuntimeException("invalid configuration, more multi nodes required then possible inner nodes")
        }
        val paths = multiEdgeInnerNodes * 2 + numberOfOutputNodes

        // Step 0: Create nodes
        for (i in 0 until numberOfInputNodes) {
            inputNodes.add(Node(type = NodeType.Input))
        }

        for (i in 0 until numberOfOutputNodes) {
            outputNodes.add(Node(type = NodeType.Output))
        }

        for (i in 0 until multiEdgeInnerNodes) {
            multiNodes.add(Node(type = NodeType.General))
        }

        dependencyGraph.nodes.addAll(inputNodes)
        dependencyGraph.nodes.addAll(outputNodes)
        dependencyGraph.nodes.addAll(multiNodes)

        // Step 1: Path lengths
        this.pregeneratePathLengths(paths, singleEdgeInnerNodes)

        // Step 2: Connect full nodes (i.e. no remaining incoming edge) to next node
        this.connectInputNodes()
        this.connectFullMultiNodes()

        // Step 2: SCC Distribution
        this.computeSCCDistribution(numberOfRecursiveRules, recursiveComplexity)

        // Step 3: Compute how the SCC are connected
        this.computeSccGraph()

        // Step 4: Iterate over Meta Graph to generate each SCC connection and then connect to next node
        this.createGraph(numberOfRecursiveRules)

        return dependencyGraph
    }

}