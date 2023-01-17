package at.ac.tuwien.dbai.kg.iTemporal.util

import guru.nidi.graphviz.model.Node


interface INode {

    /**
     * Returns the label of the edge
     * Used in DG Drawer for labeling the drawn edge
     */
    fun getLabel(): String

    fun getStyle(node: Node): Node

}