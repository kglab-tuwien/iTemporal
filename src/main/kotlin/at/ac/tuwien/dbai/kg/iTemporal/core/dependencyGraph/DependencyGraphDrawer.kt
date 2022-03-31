package at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.Factory
import java.io.File

object DependencyGraphDrawer {


    fun draw(graph: DependencyGraph, name:String) {
        var g = Factory.graph().directed().named(name)

        for (node in graph.nodes) {
            var graphNode = Factory.node(node.getLabel())
            when (node.type) {
                NodeType.General -> {
                    //graphNode=graphNode.with(Color.BLACK)
                }
                NodeType.Input -> {
                    graphNode=graphNode.with(Color.BLUE)
                }
                NodeType.Output -> {
                    graphNode=graphNode.with(Color.BROWN)
                }
            }
            if(graph.outEdges[node] != null) {
                for (outgoingEdge in graph.outEdges[node]!!) {
                    graphNode = graphNode.link(Factory.to(Factory.node(outgoingEdge.to.getLabel())).with(Label.of((outgoingEdge.getLabel()))))
                }
            }
            g = g.with(graphNode)
        }

        val graphViz = Graphviz.fromGraph(g)
        graphViz.render(Format.PNG).toFile(File("out/$name.png"))
    }
}