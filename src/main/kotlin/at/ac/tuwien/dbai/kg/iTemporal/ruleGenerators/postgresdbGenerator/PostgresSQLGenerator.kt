package at.ac.tuwien.dbai.kg.iTemporal.ruleGenerators.postgresdbGenerator

import at.ac.tuwien.dbai.kg.iTemporal.aggregation.AggregationType
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.ITAEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.RuleGeneration
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraphHelper
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.NodeType
import at.ac.tuwien.dbai.kg.iTemporal.core.edges.*
import at.ac.tuwien.dbai.kg.iTemporal.temporal.TriangleUnit
import at.ac.tuwien.dbai.kg.iTemporal.temporal.edges.*
import java.io.File

object PostgresSQLGenerator: RuleGeneration {

    override fun getPriority(): Int = 667

    override fun getLanguage(): String {
        return "PostgresSQL"
    }

    override fun convert(dependencyGraph: DependencyGraph): DependencyGraph {
        return dependencyGraph
    }

    override fun run(dependencyGraph: DependencyGraph, storeFile:Boolean):String {
        val path: File = Registry.properties.path

        if(storeFile) {
            if (!path.isDirectory) {
                throw RuntimeException("Illegal argument, no folder provided")
            }
        }

        val queryContent = StringBuilder()
        val fileContent = StringBuilder()

        if (dependencyGraph.nodes.filter { it.type == NodeType.Output }.size != 1) {
            fileContent.append("Only graphs with a single output are supported.")
            return fileContent.toString()
        }

        val sCCs = DependencyGraphHelper.calculateSCC(dependencyGraph)

        if (sCCs.size != dependencyGraph.nodes.size) {
            fileContent.append("Only graphs without recursion are supported.")
            return fileContent.toString()
        }

        val sccOrder = DependencyGraphHelper.createSCCOrder(dependencyGraph, sCCs).reversed()

        var isFirst = true;

        for (sccId in sccOrder) {
            val nodes: List<Node> = sCCs[sccId]

            for (node in nodes) {
                if (node.type == NodeType.Input) {
                    fileContent.appendLine("DROP TABLE IF EXISTS ${node.name};")
                    fileContent.appendLine("CREATE TABLE ${node.name} (")

                    val variables = mutableListOf<String>()
                    for (i in 0 until node.minArity) {
                        fileContent.appendLine("i${i} real,")
                        variables.add("i${i}")
                    }
                    fileContent.appendLine("startdate timestamp,")
                    fileContent.appendLine("enddate timestamp")
                    fileContent.appendLine(");")

                    val variablesString = variables.joinToString(",")
                    fileContent.appendLine("COPY ${node.name} (${if(variablesString.isEmpty())"" else variablesString + ", "} startdate, enddate) FROM '${path.absolutePath}/${node.name}_date.csv' DELIMITER ',' CSV HEADER;")
                }

                val inEdges = dependencyGraph.inEdges[node].orEmpty()

                // Is not a rule head
                if (inEdges.isEmpty()) {
                    continue
                }

                if (isFirst) {
                    isFirst = false;
                    queryContent.appendLine("WITH ")
                }

                if(inEdges.any { it is IntersectionEdge }) {
                    if (inEdges.size != 2) {
                        throw RuntimeException("invalid number of intersection edges")
                    }
                    val edge1 = inEdges[0]
                    val edge2 = inEdges[1]


                    val variables = Array(edge1.to.minArity){""}
                    val joinList = mutableListOf<String>()

                    edge1.termOrder.filter { it != -1 }.forEachIndexed { index, value -> variables[index]="${edge1.from.name}.i$value as i$index"  }
                    edge2.termOrder.filter { it != -1 }.forEachIndexed { index, value ->
                        if (variables[index] != "") {
                            // We have a join pair
                            joinList.add("${variables[index].substring(0, variables[index].indexOf(" "))}=${edge2.from.name}.i$value")
                        } else {
                            variables[index] = "${edge2.from.name}.i$value as i$index"
                        }
                    }

                    var joinListString = joinList.joinToString(" AND ")
                    if (joinListString.isEmpty()) {
                        joinListString = "true"
                    }

                    queryContent.appendLine("${edge1.to.name} AS (SELECT ${variables.joinToString(",")}, GREATEST(${edge1.from.name}.startdate, ${edge2.from.name}.startdate) as startdate, LEAST(${edge1.from.name}.enddate,${edge2.from.name}.enddate) as enddate FROM ${edge1.from.name} JOIN ${edge2.from.name} on (${joinListString}) WHERE GREATEST(${edge1.from.name}.startdate, ${edge2.from.name}.startdate) <= LEAST(${edge1.from.name}.enddate,${edge2.from.name}.enddate)), ")

                } else if(inEdges.any { it is UnionEdge }) {
                    if (inEdges.size != 2) {
                        throw RuntimeException("invalid number of union edges")
                    }
                    val edge1 = inEdges[0]
                    val edge2 = inEdges[1]

                    queryContent.appendLine("${edge1.to.name} AS (SELECT ${getVariableOrder(edge1.from.name, edge1.termOrder)} startdate, enddate FROM ${edge1.from.name} UNION SELECT ${getVariableOrder(edge2.from.name, edge2.termOrder)}, startdate, enddate FROM ${edge2.from.name}), ")

                } else {
                    for (inEdge in inEdges) {
                        val result = when (inEdge) {
                            is LinearEdge -> this.renderRule(inEdge)
                            is DiamondMinusEdge -> this.renderRule(inEdge)
                            is DiamondPlusEdge -> this.renderRule(inEdge)
                            is BoxMinusEdge -> this.renderRule(inEdge)
                            is BoxPlusEdge -> this.renderRule(inEdge)
                            is ClosingEdge -> this.renderRule(inEdge)
                            is TriangleUpEdge -> this.renderRule(inEdge)
                            is ConditionalEdge -> this.renderRule(inEdge)
                            is ITAEdge -> this.renderRule(inEdge)
                            else -> ""
                        }
                        queryContent.appendLine(result)
                    }
                }

                if  (node.type == NodeType.Output) {
                    queryContent.appendLine(createMergeRules(node.name, "output", node.minArity));
                    queryContent.setLength(queryContent.lastIndexOf(","));
                    queryContent.appendLine()
                    queryContent.appendLine("SELECT * FROM output_merged;")
                }

            }
        }

        fileContent.appendLine()
        fileContent.append(queryContent)


        if (storeFile) {
            path.resolve("temporalVadalog.txt").writeText(fileContent.toString())
        }

        return fileContent.toString()
    }

    private fun getVariableOrder(name: String, order: List<Int>): String {
        if (order.isEmpty()) {
            return ""
        }

        return order.withIndex().sortedBy { it.value }.filter { it.value != -1 }.map {
            "${name}.i${it.index} as i${it.value}"
        }.joinToString(", ") + ","
    }

    fun renderRule(edge: Edge): String {
        throw RuntimeException("Not implemented yet")
    }

    private fun createMergeRules(fromName: String, toName: String, terms: Int):String {
        val variablesString = Array(terms) {"i${it}"}.joinToString(",")

        var variablesStringPartition = ""
        var variablesStringSelect = ""
        var variablesStringGroup = ""
        if (variablesString.isNotEmpty()) {
            variablesStringPartition = "PARTITION BY $variablesString"
            variablesStringSelect = "$variablesString,"
            variablesStringGroup = ", $variablesString"
        }

        return "${toName}_step_marker AS (SELECT *, lag(enddate) OVER ($variablesStringPartition ORDER BY startdate) < startdate OR NULL AS step FROM ${fromName}),\n" +
                "${toName}_group_marker AS (SELECT *, count(step) OVER ($variablesStringPartition ORDER BY startdate) AS grp FROM ${toName}_step_marker),\n" +
                "${toName}_merged AS (SELECT $variablesStringSelect min(startdate) as startdate, max(enddate) as enddate FROM ${toName}_group_marker GROUP BY grp $variablesStringGroup),\n"
    }

    private fun renderRule(edge: LinearEdge): String {
        return "${edge.to.name} AS (SELECT ${getVariableOrder(edge.from.name, edge.termOrder)} startdate, enddate FROM ${edge.from.name}), "
    }

    private fun renderRule(edge: DiamondMinusEdge): String {
        return "${edge.to.name} AS (SELECT ${getVariableOrder(edge.from.name, edge.termOrder)} startdate + interval '${edge.t1} seconds' as startdate, enddate + interval '${edge.t2} seconds' as enddate FROM ${edge.from.name}), "
    }

    private fun renderRule(edge: DiamondPlusEdge): String {
        return "${edge.to.name} AS (SELECT ${getVariableOrder(edge.from.name, edge.termOrder)} startdate - interval '${edge.t2} seconds' as startdate, enddate - interval '${edge.t1} seconds' as enddate FROM ${edge.from.name}), "
    }

    private fun renderRule(edge: BoxMinusEdge): String {
        return createMergeRules(edge.from.name, edge.to.name, edge.termOrder.size)+"${edge.to.name} AS (SELECT ${getVariableOrder(edge.to.name+"_merged", edge.termOrder)} startdate + interval '${edge.t2} seconds' as startdate, enddate + interval '${edge.t1} seconds' as enddate FROM ${edge.to.name}_merged WHERE startdate + interval '${edge.t2} seconds' < enddate + interval '${edge.t1} seconds') , "
    }

    private fun renderRule(edge: BoxPlusEdge): String {
        return createMergeRules(edge.from.name, edge.to.name, edge.termOrder.size)+"${edge.to.name} AS (SELECT ${getVariableOrder(edge.to.name+"_merged", edge.termOrder)} startdate - interval '${edge.t1} seconds' as startdate, enddate - interval '${edge.t2} seconds' as enddate FROM ${edge.to.name}_merged WHERE startdate - interval '${edge.t1} seconds' < enddate - interval '${edge.t2} seconds'), "
    }

    private fun renderRule(edge: ClosingEdge): String {
        return "${edge.to.name} AS (SELECT ${getVariableOrder(edge.from.name, edge.termOrder)} startdate, enddate FROM ${edge.from.name}), "
    }

    private fun renderRule(edge: TriangleUpEdge): String {
        val unitType = when(edge.unit) {
            TriangleUnit.Year -> "year"
            TriangleUnit.Month -> "month"
            TriangleUnit.Week -> "week"
            TriangleUnit.Day -> "day"
            TriangleUnit.Unknown -> throw RuntimeException("invalid unit type")
        }

        return "${edge.to.name} AS (SELECT ${getVariableOrder(edge.from.name, edge.termOrder)} date_trunc('$unitType', startdate) as startdate, date_trunc('$unitType', enddate) + interval '1 $unitType' as enddate  FROM ${edge.from.name}), "
    }

    private fun renderRule(edge: ConditionalEdge): String {
        var conditions = edge.conditions.map { condition ->
            "i${condition.variable} ${condition.type.getOperatorSQL()} " +
                    if (condition is ValueCondition) {
                        "${condition.value}"
                    } else if (condition is VarCondition) {
                        "i${condition.variable2}"
                    } else {
                        throw RuntimeException("Invalid condition type")
                    }
        }.joinToString(", ")

        if (conditions.isNotEmpty()) {
            conditions = " WHERE " + conditions
        }

        return "${edge.to.name} AS (SELECT ${getVariableOrder(edge.from.name, edge.termOrder)} startdate, enddate FROM ${edge.from.name} $conditions), "
    }

    private fun renderRule(edge: ITAEdge): String {
        val itaBuilder = StringBuilder()

        // Direct Mapping required for first operation
        val variablesString = Array(edge.termOrder.size) {"i${it}"}.joinToString(",")

        val aggrVariableIdx = edge.termOrder.indexOf(edge.to.minArity + edge.numberOfContributors)

        val aggrType = when(edge.aggregationType) {
            AggregationType.Unknown -> throw RuntimeException("Invalid aggregation type")
            AggregationType.Max -> "max"
            AggregationType.Min -> "min"
            AggregationType.Sum -> "sum"
            AggregationType.Count -> "count"
        }

        // 1. Split into individual components
        itaBuilder.appendLine("${edge.to.name}_timeseries AS (SELECT $variablesString, generate_series(${edge.from.name}.startdate, ${edge.from.name}.enddate, '1 seconds') as timeunit FROM ${edge.from.name} ),")


        // 2. Add contributor aggregation
        var tableName = "timeseries"
        if (edge.numberOfContributors > 0) {
            // We compute the maximum per contributor

            var contributorGroupByString = edge.termOrder.flatMapIndexed { index, value ->
                if (value ==  edge.to.minArity + edge.numberOfContributors) {
                    listOf()
                } else if (value == -1) {
                    listOf()
                } else {
                    listOf("${edge.to.name}_timeseries.i$index")
                }
            }.joinToString(", ")

            if (contributorGroupByString != "") {
                contributorGroupByString = ", $contributorGroupByString"
            }

            val variablesContributorAggrString = edge.termOrder.flatMapIndexed { index, value ->
                if (value ==  edge.to.minArity + edge.numberOfContributors) {
                    listOf("max(${edge.to.name}_timeseries.i${index}) OVER(partition by timeunit$contributorGroupByString) as i${index}")
                } else if (value == -1) {
                    listOf()
                } else {
                    listOf("${edge.to.name}_timeseries.i$index as i$index")
                }
            }.joinToString(", ")

            itaBuilder.appendLine("${edge.to.name}_contributor_aggregation AS (SELECT DISTINCT $variablesContributorAggrString, timeunit FROM ${edge.to.name}_timeseries),")
            tableName = "contributor_aggregation"
        }

        // 3. Add aggregation
        var groupByString = edge.termOrder.flatMapIndexed { index, value ->
            if (value >=  edge.to.minArity) {
                listOf()
            } else if (value == -1) {
                listOf()
            } else {
                listOf("${edge.to.name}_${tableName}.i$index")
            }
        }.joinToString(", ")

        if (groupByString != "") {
            groupByString = ", $groupByString"
        }

        val variablesAggrString = edge.termOrder.withIndex().sortedBy { it.value }.filter { it.value != -1 && (it.value < edge.to.minArity || it.value ==  edge.to.minArity + edge.numberOfContributors) }.map {
            if (it.value ==  edge.to.minArity + edge.numberOfContributors) {
                "$aggrType(${edge.to.name}_${tableName}.i${aggrVariableIdx}) OVER(partition by timeunit$groupByString  ) as i${edge.to.minArity-1}"
            } else {
                "${edge.to.name}_${tableName}.i${it.index} as i${it.value}"
            }
        }.joinToString(", ")

        itaBuilder.appendLine("${edge.to.name}_aggregation AS (SELECT DISTINCT $variablesAggrString, timeunit as startdate, timeunit + INTERVAL '1 seconds' as enddate FROM ${edge.to.name}_${tableName}),")

        // Merge result
        itaBuilder.append(createMergeRules("${edge.to.name}_aggregation", edge.to.name, edge.to.minArity)
                + "${edge.to.name} AS (SELECT * FROM ${edge.to.name}_merged), "
        )

        return itaBuilder.toString()
    }
}