package at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph

import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge

class MutableDependencyGraph(
    override val nodes: MutableSet<Node> = mutableSetOf(),
    override val inEdges: MutableMap<Node, MutableList<Edge>> = hashMapOf(),
    override val outEdges: MutableMap<Node, MutableList<Edge>> = hashMapOf()
): DependencyGraph(nodes, inEdges, outEdges) {

    fun addEdge(edge: Edge) {
        if (!this.inEdges.containsKey(edge.to)) {
            this.inEdges[edge.to] = mutableListOf()
        }
        this.inEdges[edge.to]!!.add(edge)

        if (!this.outEdges.containsKey(edge.from)) {
            this.outEdges[edge.from] = mutableListOf()
        }
        this.outEdges[edge.from]!!.add(edge)
    }

    fun removeEdge(edge: Edge) {
        if (!this.inEdges.containsKey(edge.to)) {
            this.inEdges[edge.to] = mutableListOf()
        }
        this.inEdges[edge.to]!!.remove(edge)

        if (!this.outEdges.containsKey(edge.from)) {
            this.outEdges[edge.from] = mutableListOf()
        }
        this.outEdges[edge.from]!!.remove(edge)
    }

    override fun toString(): String {
        return "MutableDependencyGraph(nodes=$nodes, inEdges=$inEdges, outEdges=$outEdges)"
    }


}