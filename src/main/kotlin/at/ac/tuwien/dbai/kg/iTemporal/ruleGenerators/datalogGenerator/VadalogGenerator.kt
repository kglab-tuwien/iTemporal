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

object VadalogGenerator:RuleGeneration {

    override fun getPriority(): Int = 666

    override fun getLanguage(): String {
        return "Vadalog"
    }

    override fun convert(dependencyGraph: DependencyGraph):DependencyGraph {
        val mutableDependencyGraph = dependencyGraph.toMutableDependencyGraph()
        val inEdges = mutableDependencyGraph.inEdges.values.flatten().filter { edge ->
            edge is ClosingEdge
        }

        for (closingEdge in inEdges) {
            // Check pattern (closing should only be in since operation, so we do some basic checking to ensure correct graph pattern)
            // but ignore term order
            val startNode = closingEdge.from
            val cNode = closingEdge.to
            val closingNodeOutEdges = mutableDependencyGraph.outEdges[cNode].orEmpty();

            if (closingNodeOutEdges.size != 2 || !(closingNodeOutEdges[0] is IntersectionEdge) || !(closingNodeOutEdges[1] is IntersectionEdge)) {
                continue;
            }

            val t1 = closingNodeOutEdges[0].to
            val t2= closingNodeOutEdges[1].to

            // Both t1 and t2 have two incoming intersection edges.
            val t1Inedges = mutableDependencyGraph.inEdges[t1].orEmpty();
            val t2Inedges = mutableDependencyGraph.inEdges[t2].orEmpty();

            if (t1Inedges.size != 2 || !(t1Inedges[0] is IntersectionEdge) || !(t1Inedges[1] is IntersectionEdge)) {
                continue;
            }

            if (t2Inedges.size != 2 || !(t2Inedges[0] is IntersectionEdge) || !(t2Inedges[1] is IntersectionEdge)) {
                continue;
            }

            // We have to find out the node between diamondMinus and Intersection edge
            val t1OtherInEdge = t1Inedges.filter { it.from != cNode }.first()
            val t2OtherInEdge = t2Inedges.filter { it.from != cNode }.first()

            val t1OtherInNode = t1OtherInEdge.from
            val t2OtherInNode = t2OtherInEdge.from

            val outt1 = mutableDependencyGraph.outEdges[t1].orEmpty().map { it.to }.firstOrNull()
            val outt2 = mutableDependencyGraph.outEdges[t2].orEmpty().map { it.to }.firstOrNull()

            if (outt1 != t2OtherInNode && outt2 != t1OtherInNode) {
                continue
            }

            val tempMiddleNode = if(outt1 == t2OtherInNode) t2OtherInNode else t1OtherInNode
            val otherStartNode = if(outt1 == t2OtherInNode) t1OtherInNode else t2OtherInNode
            val outNode = if(outt1 == t2OtherInNode) t2 else t1
            val innerIntersection = if(outt1 == t2OtherInNode) t1 else t2
            val innerIntersectionOtherInEdge = if(outt1 == t2OtherInNode) t1OtherInEdge else t2OtherInEdge
            val innerIntersectionInEdge = if(outt1 == t2OtherInNode) t1Inedges.filter { it.from == cNode }.first() else t2Inedges.filter { it.from == cNode }.first()

            if (mutableDependencyGraph.inEdges[tempMiddleNode].orEmpty().size != 1) {
                continue
            }
            if (mutableDependencyGraph.outEdges[tempMiddleNode].orEmpty().size != 1) {
                continue
            }

            val temporalEdge = mutableDependencyGraph.inEdges[tempMiddleNode].orEmpty()[0] as TemporalSingleEdge

            mutableDependencyGraph.nodes.remove(cNode)
            mutableDependencyGraph.nodes.remove(innerIntersection)
            mutableDependencyGraph.nodes.remove(otherStartNode)
            mutableDependencyGraph.nodes.remove(tempMiddleNode)

            mutableDependencyGraph.removeEdge(closingEdge)
            mutableDependencyGraph.removeEdge(closingNodeOutEdges[1])
            mutableDependencyGraph.removeEdge(closingNodeOutEdges[0])
            mutableDependencyGraph.removeEdge(temporalEdge)
            mutableDependencyGraph.removeEdge(mutableDependencyGraph.outEdges[tempMiddleNode].orEmpty()[0])

            if (temporalEdge is DiamondPlusEdge) {
                mutableDependencyGraph.addEdge(UntilMergeEdge(
                    from2 =otherStartNode,
                    from=startNode,
                    to=outNode,
                    termOrder2 = innerIntersectionOtherInEdge.termOrder,
                    termOrder = innerIntersectionInEdge.termOrder,
                    t1 = temporalEdge.t1,
                    t2 = temporalEdge.t2
                ));
            }

            if (temporalEdge is DiamondMinusEdge) {
                mutableDependencyGraph.addEdge(SinceMergeEdge(
                    from2 =otherStartNode,
                    from=startNode,
                    to=outNode,
                    termOrder2 = innerIntersectionOtherInEdge.termOrder,
                    termOrder = innerIntersectionInEdge.termOrder,
                    t1 = temporalEdge.t1,
                    t2 = temporalEdge.t2
                ));
            }

        }



        return mutableDependencyGraph
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

        inputContent.appendLine("@timeGranularity(\"no_date\").")

        val useHeaders = if(Registry.properties.outputCsvHeader) "true" else "false"
        for(node in dependencyGraph.nodes) {
            if (node.type == NodeType.Input) {
                inputContent.appendLine("@input(\"${node.name}\").")
                inputContent.appendLine("@bind(\"${node.name}\", \"csv useHeaders=$useHeaders\", \"${path.absolutePath}\",\"${node.name}_numeric.csv\").")
                for (i in 0 until node.minArity) {
                    inputContent.appendLine("@mapping(\"${node.name}\",${i},\"${i}\",\"double\").")
                }
                inputContent.appendLine("@mapping(\"${node.name}\",${node.minArity},\"${node.minArity}\",\"double\").")
                inputContent.appendLine("@mapping(\"${node.name}\",${node.minArity+1},\"${node.minArity+1}\",\"double\").")
                inputContent.appendLine("@timeMapping(\"${node.name}\",${node.minArity},${node.minArity+1},#T,#T).")
                inputContent.appendLine("@temporalType(\"${node.name}\",\"double\").")
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
                        is SinceMergeEdge -> this.renderRule(inEdge)
                        is UntilMergeEdge -> this.renderRule(inEdge)
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

    private fun renderRule(edge: SinceMergeEdge): String {
        return "${convertNode(edge.to)} :- ${
            convertNode(edge.from,edge.termOrder)
        } <S>[${edge.t1},${edge.t2}] ${convertNode(edge.from2,edge.termOrder2)}."
    }

    private fun renderRule(edge: UntilMergeEdge): String {
        return "${convertNode(edge.to)} :- ${
            convertNode(edge.from,edge.termOrder)
        } <U>[${edge.t1},${edge.t2}] ${convertNode(edge.from2, edge.termOrder2)}."
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
        return "${convertNode(edge.to)} :- <->[${edge.t1},${edge.t2}] ${convertNode(edge.from,edge.termOrder)}."
    }

    private fun renderRule(edge: DiamondPlusEdge): String {
        return "${convertNode(edge.to)} :- <+>[${edge.t1},${edge.t2}] ${convertNode(edge.from,edge.termOrder)}."
    }

    private fun renderRule(edge: BoxMinusEdge): String {
        return "${convertNode(edge.to)} :- [-][${edge.t1},${edge.t2}] ${convertNode(edge.from,edge.termOrder)}."
    }

    private fun renderRule(edge: BoxPlusEdge): String {
        return "${convertNode(edge.to)} :- [+][${edge.t1},${edge.t2}] ${convertNode(edge.from,edge.termOrder)}."
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
}