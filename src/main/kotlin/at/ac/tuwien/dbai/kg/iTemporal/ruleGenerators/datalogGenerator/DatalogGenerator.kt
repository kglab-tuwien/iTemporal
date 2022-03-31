package at.ac.tuwien.dbai.kg.iTemporal.ruleGenerators.datalogGenerator

import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.ITAEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleGeneration
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.NodeType
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.*
import at.ac.tuwien.dbai.kg.iTemporal.temporal.TriangleUnit
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.*
import java.io.File

object DatalogGenerator:RuleGeneration {

    override fun getPriority(): Int = 666

    override fun getLanguage(): String {
        return "Datalog"
    }

    override fun convert(dependencyGraph: DependencyGraph):DependencyGraph {
        return dependencyGraph
    }

    override fun run(dependencyGraph: DependencyGraph, storeFile:Boolean):String {
        val path:File = Registry.properties.path

        if(storeFile) {
            if (!path.isDirectory) {
                throw RuntimeException("Illegal argument, no folder provided")
            }
        }

        val fileContent = StringBuilder()

        val inputContent = StringBuilder()
        val outputContent = StringBuilder()
        val ruleContent = StringBuilder()

        val useHeaders = if(Registry.properties.outputCsvHeader) "true" else "false"
        for(node in dependencyGraph.nodes) {
            if (node.type == NodeType.Input) {
                inputContent.appendLine("@input(\"${node.name}\").")
                inputContent.appendLine("@bind(\"${node.name}\", \"csv useHeaders=$useHeaders\", \"${path.absolutePath}\",\"${node.name}_date.csv\").")
                for (i in 0 until node.minArity) {
                    inputContent.appendLine("@mapping(\"${node.name}\",${i},\"${i}\",\"double\").")
                }
                inputContent.appendLine("@mapping(\"${node.name}\",${node.minArity},\"${node.minArity}\",\"date\").")
                inputContent.appendLine("@mapping(\"${node.name}\",${node.minArity+1},\"${node.minArity+1}\",\"date\").")
                inputContent.appendLine("@timeMapping(\"${node.name}\",${node.minArity},${node.minArity+1},#T,#T).")
            } else if (node.type == NodeType.Output) {
                outputContent.appendLine("@output(\"${node.name}\").")
                //outputContent.appendLine("@bind(\"${node.name}\", \"csv useHeaders=false\", \"${path.absolutePath}\",\"${node.name}.csv\").")
            }

            val inEdges = dependencyGraph.inEdges[node].orEmpty()

            // Is not a rule head
            if (inEdges.isEmpty()) {
                continue
            }

            if(inEdges.any { it is IntersectionEdge }) {
                if (inEdges.size != 2) {
                    throw RuntimeException("invalid number of intersection edges")
                }
                val edge1 = inEdges[0]
                val edge2 = inEdges[1]
                val toPart = convertNode(node)
                val fromEdge1 = convertNode(edge1.from,edge1.termOrder)
                val fromEdge2 = convertNode(edge2.from,edge2.termOrder)

                ruleContent.appendLine("$toPart :- ${fromEdge1}, ${fromEdge2}.")
            } else {
                for (inEdge in inEdges) {
                    val result = when (inEdge) {
                        is LinearEdge -> this.renderRule(inEdge)
                        is UnionEdge -> this.renderRule(inEdge)
                        is DiamondMinusEdge -> this.renderRule(inEdge)
                        is DiamondPlusEdge -> this.renderRule(inEdge)
                        is BoxMinusEdge -> this.renderRule(inEdge)
                        is BoxPlusEdge -> this.renderRule(inEdge)
                        is ITAEdge -> this.renderRule(inEdge)
                        is TriangleUpEdge -> this.renderRule(inEdge)
                        is ClosingEdge -> this.renderRule(inEdge)
                        is ConditionalEdge -> this.renderRule(inEdge)
                        is ExistentialEdge -> this.renderRule(inEdge)
                        else -> ""
                    }
                    ruleContent.appendLine(result)
                }
            }
        }

        fileContent
            .append(inputContent)
            .append(outputContent)
            .append(ruleContent)

        if (storeFile) {
            path.resolve("temporalVadalog.txt").writeText(fileContent.toString())
        }

        return fileContent.toString()
    }

    private fun convertNode(node: Node, order: List<Int>? = null): String {
        var orderNew = order
        if (orderNew == null) {
            orderNew = Array(node.minArity) { index -> index }.toList()
        }
        if (orderNew.isEmpty()) {
            return node.name
        }
        return node.name + "(" + (orderNew.joinToString(",") { if (it != -1) "N$it" else "_" }) + ")"
    }

    fun renderRule(edge: Edge): String {
        throw RuntimeException("Not implemented yet")
    }

    private fun renderRule(edge: LinearEdge): String {
        return "${convertNode(edge.to)} :- ${convertNode(edge.from,edge.termOrder)}."
    }

    private fun renderRule(edge: UnionEdge): String {
        return "${convertNode(edge.to)} :- ${convertNode(edge.from,edge.termOrder)}."
    }

    private fun renderRule(edge: TriangleUpEdge): String {
        val unitType = when(edge.unit) {
            TriangleUnit.Year -> "years"
            TriangleUnit.Month -> "months"
            TriangleUnit.Week -> "weeks"
            TriangleUnit.Day -> "days"
            TriangleUnit.Unknown -> throw RuntimeException("invalid unit type")
        }
        val unitLength = when(edge.unit) {
            //TriangleUnit.Week -> 7
            else -> 1
        }
        return "${convertNode(edge.to)} :- /\\$unitLength $unitType ${convertNode(edge.from,edge.termOrder)}."
    }

    // to=N0,N1,N2, order=[1,0,4,3], group by 2, contributor 1 => from(N1,N0,N4,N3), N2=aggregationType(N3,<N4>)
    private fun renderRule(edge: ITAEdge): String {
        val fromString = convertNode(edge.from,edge.termOrder)
        val toString = convertNode(edge.to)
        var contributors = ""
        if (edge.numberOfContributors > 0) {
            contributors =
                ", <" + (Array(edge.numberOfContributors) { index -> "N" + (edge.to.minArity + index) }.joinToString(",")) + ">"
        }
        return "$toString :- ${fromString}, N${edge.to.minArity - 1} = ${edge.aggregationType.getTypeString()}(N${edge.to.minArity + edge.numberOfContributors}${contributors})."
    }

    private fun renderRule(edge: DiamondMinusEdge): String {
        return "${convertNode(edge.to)} :- <->[${edge.t1/1000},${edge.t2/1000}] ${convertNode(edge.from,edge.termOrder)}."
    }

    private fun renderRule(edge: DiamondPlusEdge): String {
        return "${convertNode(edge.to)} :- <+>[${edge.t1/1000},${edge.t2/1000}] ${convertNode(edge.from,edge.termOrder)}."
    }

    private fun renderRule(edge: BoxMinusEdge): String {
        return "${convertNode(edge.to)} :- [-][${edge.t1/1000},${edge.t2/1000}] ${convertNode(edge.from,edge.termOrder)}."
    }

    private fun renderRule(edge: BoxPlusEdge): String {
        return "${convertNode(edge.to)} :- [+][${edge.t1/1000},${edge.t2/1000}] ${convertNode(edge.from,edge.termOrder)}."
    }

    private fun renderRule(edge: ClosingEdge): String {
        return "${convertNode(edge.to)} :- © ${convertNode(edge.from,edge.termOrder)}."
    }

    private fun renderRule(edge: ConditionalEdge): String {
        var conditions = edge.conditions.map { condition ->
            "N${condition.variable} ${condition.type.getOperator()} " +
            if (condition is ValueCondition) {
                "${condition.value}"
            } else if (condition is VarCondition) {
                "N${condition.variable2}"
            } else {
                throw RuntimeException("Invalid condition type")
            }
        }.joinToString(", ")

        if (conditions.isNotEmpty()) {
            conditions = ", " + conditions
        }

        return "${convertNode(edge.to)} :- ${convertNode(edge.from,edge.termOrder)}${conditions}."
    }

    private fun renderRule(edge: ExistentialEdge): String {
        return "${convertNode(edge.to)} :- ${convertNode(edge.from,edge.termOrder)}."
    }
}