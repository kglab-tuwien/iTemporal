package at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import java.util.*
import kotlin.math.min


object DependencyGraphHelper {

    private var Time: Int = 0

    private lateinit var sCCs: MutableList<List<Node>>
    private lateinit var nodes: List<Node>
    private lateinit var adj: HashMap<Int, List<Int>>
    private lateinit var low: Array<Int>
    private lateinit var disc: Array<Int>
    private lateinit var stackMember: Array<Boolean>
    private lateinit var stack: Stack<Int>
    private var dependencyGraph: DependencyGraph? = null
    private var sccOrder: List<Int> = emptyList()


    fun createSCCOrder(dependencyGraph: DependencyGraph, sCCs: List<List<Node>>): List<Int> {
       if (sccOrder.isNotEmpty() && dependencyGraph == DependencyGraphHelper.dependencyGraph) {
            return sccOrder
       }

        val nodeToSccId: Map<Node, Int> = sCCs.flatMapIndexed { index, list -> list.map { Pair(it, index) } }.toMap()

        val dependsOn: MutableMap<Int, MutableList<Int>> = sCCs.mapIndexed { index, list ->
            Pair(index, list
                .flatMap { it ->
                    dependencyGraph.outEdges[it].orEmpty().map { it.to }
                } // Map nodes of SCC to outgoing nodes
                .map { nodeToSccId[it]!! }    // Map each node to SCC id
                .filter { it != index }     // Remove nodes that are part of the same SCC
                .distinct()                 // Reduce list to contain only a single entry per SCC
                .toMutableList()
            )
        }.toMap().toMutableMap()


        val sccOrder = mutableListOf<Int>()

        while (sccOrder.size != sCCs.size) {
            val nextNodes = dependsOn.filterValues { it.isEmpty() }
            sccOrder.addAll(nextNodes.keys)
            nextNodes.keys.forEach { sccId ->
                dependsOn.remove(sccId)
                dependsOn.forEach {
                    it.value.remove(sccId)
                }
            }
        }

        DependencyGraphHelper.sccOrder = sccOrder.toList()
        return sccOrder
    }

    fun reset(dependencyGraph: DependencyGraph) {
        // Reset
        Time = 0
        sCCs = mutableListOf()
        sccOrder = emptyList()
        nodes = dependencyGraph.nodes.toList()
        disc = Array(dependencyGraph.nodes.size) { i -> -1 }
        low = Array(dependencyGraph.nodes.size) { i -> -1 }
        stackMember = Array(dependencyGraph.nodes.size) { i -> false }
        stack = Stack<Int>()
        val nodeToIntMapping = nodes.mapIndexed { index, node -> Pair(node, index) }.toMap()
        adj = hashMapOf()
        for (edge in dependencyGraph.inEdges.values.flatten()) {
            val sourceIndex = nodeToIntMapping[edge.from]!!
            val targetIndex = nodeToIntMapping[edge.to]!!
            adj[sourceIndex] = adj[sourceIndex].orEmpty() + listOf(targetIndex)
        }
        DependencyGraphHelper.dependencyGraph = dependencyGraph

    }

    fun calculateSCC(dependencyGraph: DependencyGraph, force:Boolean = false): List<List<Node>> {
        // Return cached SCCs
        if (!force && Time > 0 && dependencyGraph == DependencyGraphHelper.dependencyGraph) {
            return sCCs
        }

        if(Registry.debug) {
            println("Recalculating SCC")
        }


        // Reset
        reset(dependencyGraph)


        for (i in 0 until dependencyGraph.nodes.size) {
            if (disc[i] == -1) {
                dfs(i)
            }
        }

        createSCCOrder(dependencyGraph, sCCs)

        return sCCs
    }


    private fun dfs(u: Int) {
        disc[u] = Time
        low[u] = Time
        Time += 1

        stackMember[u] = true
        stack.push(u)

        // get the list of edges from the node.
        val temp: List<Int> = adj[u].orEmpty()

        for (n in temp) {
            //If v is not visited
            if (disc[n] == -1) {
                dfs(n)
                low[u] = min(low[u], low[n])
            } else if (stackMember[n]) {
                low[u] = min(low[u], disc[n])
            }
        }

        if (low[u] == disc[u]) {
            var w = -1
            val sccData = mutableListOf<Node>()
            while (w != u) {
                w = stack.pop()
                sccData.add(nodes[w])
                stackMember[w] = false
            }
            sCCs.add(sccData)
        }
    }
}