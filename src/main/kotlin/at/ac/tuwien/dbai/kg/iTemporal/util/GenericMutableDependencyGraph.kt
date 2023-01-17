package at.ac.tuwien.dbai.kg.iTemporal.util

open class GenericMutableDependencyGraph<NodeType: INode, EdgeType: IEdge<NodeType>> (
    override val nodes: MutableSet<NodeType> = mutableSetOf(),
    override val inEdges: MutableMap<NodeType, MutableList<EdgeType>> = hashMapOf(),
    override val outEdges: MutableMap<NodeType, MutableList<EdgeType>> = hashMapOf()
) : GenericDependencyGraph<NodeType, EdgeType>(nodes, inEdges, outEdges) {

    fun addEdge(edge: EdgeType):GenericDependencyGraph<NodeType,EdgeType> {
        if (!this.inEdges.containsKey(edge.to)) {
            this.inEdges[edge.to] = mutableListOf()
        }
        this.inEdges[edge.to]!!.add(edge)

        if (!this.outEdges.containsKey(edge.from)) {
            this.outEdges[edge.from] = mutableListOf()
        }
        this.outEdges[edge.from]!!.add(edge)

        return this
    }

    fun removeEdge(edge: EdgeType):GenericDependencyGraph<NodeType,EdgeType> {
        if (!this.inEdges.containsKey(edge.to)) {
            this.inEdges[edge.to] = mutableListOf()
        }
        this.inEdges[edge.to]!!.remove(edge)

        if (!this.outEdges.containsKey(edge.from)) {
            this.outEdges[edge.from] = mutableListOf()
        }
        this.outEdges[edge.from]!!.remove(edge)

        return this
    }

    fun addNode(termNode: NodeType):GenericDependencyGraph<NodeType,EdgeType> {
        this.nodes.add(termNode)
        return this
    }

    override fun toString(): String {
        return "GenericMutableDependencyGraph(nodes=$nodes, inEdges=$inEdges, outEdges=$outEdges)"
    }
}