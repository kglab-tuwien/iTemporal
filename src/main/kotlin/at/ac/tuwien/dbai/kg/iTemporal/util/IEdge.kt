package at.ac.tuwien.dbai.kg.iTemporal.util

import guru.nidi.graphviz.model.Link


interface IEdge<NodeType> {
    val from: NodeType
    val to: NodeType

    /**
     * Returns the label of the edge
     * Used in DG Drawer for labeling the drawn edge
     */
    fun getLabel(): String = ""

    fun getStyle(edge: Link): Link
}