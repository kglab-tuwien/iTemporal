package at.ac.tuwien.dbai.kg.iTemporal.ruleGenerators.questdbGenerator

import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.temporal.TriangleUnit

class SampleByEdge(override var from: Node,
                 override var to: Node,
                 var unit:TriangleUnit,
                 var unitLength:Int
) : BasicEdge(from, to) {

    override fun getLabel(): String {
        return "SampleByEdge"
    }
}