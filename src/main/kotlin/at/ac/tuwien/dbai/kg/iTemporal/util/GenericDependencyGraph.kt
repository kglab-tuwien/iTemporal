package at.ac.tuwien.dbai.kg.iTemporal.util

import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.Factory
import java.io.File


open class GenericDependencyGraph<NodeType: INode, EdgeType: IEdge<NodeType>>(
        open val nodes: Set<NodeType> = setOf(),
        open val inEdges: Map<NodeType, List<EdgeType>> = mapOf(),
        open val outEdges: Map<NodeType, List<EdgeType>> = mapOf()
    ) {

    fun toMutableDependencyGraph(): GenericMutableDependencyGraph<NodeType, EdgeType> {
        return GenericMutableDependencyGraph(this.nodes.toMutableSet(), this.inEdges.mapValues { it.value.toMutableList() }.toMutableMap(), this.outEdges.mapValues { it.value.toMutableList() }.toMutableMap())
    }

    override fun toString(): String {
        return "GenericDependencyGraph(nodes=$nodes, inEdges=$inEdges, outEdges=$outEdges)"
    }

    fun getReachableNodes(startNode: NodeType, ignoreEdges: Set<EdgeType> = emptySet()): Set<NodeType> {
        val visited = mutableSetOf<NodeType>()
        val queue = mutableListOf<NodeType>(startNode)

        while(queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (visited.contains(node)) continue
            visited.add(node)

            queue.addAll(this.outEdges[node]?.filter { !ignoreEdges.contains(it) }?.map { it.to } ?: listOf())
        }

        return visited
    }

    fun isCyclic(startNode: NodeType): Boolean {
        val visited = mutableSetOf<NodeType>()
        val queue = mutableListOf<NodeType>(startNode)

        while(queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (visited.contains(node)) continue
            visited.add(node)

            if (this.outEdges[node].orEmpty().any {it.to == startNode}) {
                return true
            }

            queue.addAll(this.outEdges[node].orEmpty().map { it.to })
        }

        return false
    }

    fun draw(name:String) {
        var g = Factory.graph().directed().named(name)

        for (node in this.nodes) {
            var graphNode = node.getStyle(Factory.node(node.getLabel()))
            if(this.outEdges[node] != null) {
                for (outgoingEdge in this.outEdges[node]!!) {
                    graphNode = graphNode.link(outgoingEdge.getStyle(Factory.to(Factory.node(outgoingEdge.to.getLabel())).with(Label.of((outgoingEdge.getLabel())))))
                }
            }
            g = g.with(graphNode)
        }

        val graphViz = Graphviz.fromGraph(g)
        graphViz.render(Format.PNG).toFile(File("out/$name.png"))
    }
}