package at.ac.tuwien.dbai.kg.iTemporal.ruleGenerators.questdbGenerator

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node

class SelectEdge(override var from: Node,
               override var to: Node,
               var termName:String
) : BasicEdge(from, to) {

    override fun getLabel(): String {
        return "SELECT"
    }
}