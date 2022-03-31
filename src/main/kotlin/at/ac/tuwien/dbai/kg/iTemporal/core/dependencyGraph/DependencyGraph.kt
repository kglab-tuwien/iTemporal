package at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


open class DependencyGraph(
    open val nodes: Set<Node> = setOf(),
    open val inEdges: Map<Node, List<Edge>> = mapOf(),
    open val outEdges: Map<Node, List<Edge>> = mapOf()
) {

    fun toJson():String {
        val gson = GsonBuilder().registerTypeAdapter(MutableDependencyGraph::class.java, DGSerializer()).registerTypeAdapter(DependencyGraph::class.java, DGSerializer()).create()
        val data = gson.toJson(this)
        return data
    }

    fun store(path: File) {
        path.writeText(this.toJson())
    }

    companion object {
        fun parseFromJson(json: String): DependencyGraph {
            val gson = GsonBuilder().registerTypeAdapter(DependencyGraph::class.java, DGSerializer()).create()
            return gson.fromJson(json, DependencyGraph::class.java)
        }
        fun parseFromJson(file: File): DependencyGraph {
            val bufferedReader: BufferedReader = file.bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            return parseFromJson(inputString)
        }
    }

    fun draw(name: String) {
        DependencyGraphDrawer.draw(this, name)
    }

    fun toMutableDependencyGraph():MutableDependencyGraph {
        return MutableDependencyGraph(this.nodes.toMutableSet(), this.inEdges.mapValues { it.value.toMutableList() }.toMutableMap(), this.outEdges.mapValues { it.value.toMutableList() }.toMutableMap())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DependencyGraph) return false

        if (nodes != other.nodes) return false
        if (inEdges != other.inEdges) return false
        if (outEdges != other.outEdges) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nodes.hashCode()
        result = 31 * result + inEdges.hashCode()
        result = 31 * result + outEdges.hashCode()
        return result
    }

    fun getData():Map<String, String> {
        val returnData = mutableMapOf<String, String>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val inputNodes = nodes.filter { it.type == NodeType.Input }
        for (node in inputNodes) {
            val key = node.name

            val headerText = if (Registry.properties.outputCsvHeader) {
                val terms = Array(node.minArity +2) { index -> "i"+index }.toList()
                terms.joinToString(",")+"\n"
            } else ""

            var data = node.oldData.filter { !Registry.properties.generateTimePoints || it[node.minArity] == it[node.minArity+1]-1 }.distinct()

            if (Registry.properties.outputQuestDB) {
                data = data.sortedBy { it[node.minArity] }
            }

            val outputData = headerText + data.joinToString("\n") { it ->
                val newList =
                    it.take(node.minArity) + it.drop(node.minArity)
                        .map { "\"${dateFormat.format(Date(it.toLong()))}\"" }
                newList.joinToString(",")
            }
            returnData[key] = outputData
        }

        return returnData
    }

    fun writeData() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dateFormatQuestDB = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'000'Z")

        val inputNodes = nodes.filter { it.type == NodeType.Input }

        val minDate = inputNodes.map { node-> node.oldData.map{ data -> data[node.minArity]}}.flatten().minOrNull()

        for (node in inputNodes) {
            val file: File = Registry.properties.path.resolve(node.name+"_date.csv")
            val file2: File = Registry.properties.path.resolve(node.name+"_numeric.csv")
            val file3: File = Registry.properties.path.resolve(node.name+"_questdb.csv")

            val headerText = if (Registry.properties.outputCsvHeader) {
                val terms = Array(node.minArity +2) { index -> "i"+index }.toList()
                terms.joinToString(",")+"\n"
            } else ""

            var data = node.oldData.filter { !Registry.properties.generateTimePoints || it[node.minArity] == it[node.minArity+1]-1 }.distinct()

            if (Registry.properties.outputQuestDB) {
                data = data.sortedBy { it[node.minArity] }
            }

            file.writeText(headerText + data.joinToString("\n") { it ->
                val newList =
                    it.take(node.minArity) + it.drop(node.minArity)
                        .map { "\"${dateFormat.format(Date(it.toLong()))}\"" }
                newList.joinToString(",")
            })


            file2.writeText(headerText + data.joinToString("\n") { it ->
                val newList =
                    it.take(node.minArity) + it.drop(node.minArity)
                        .map { (it-minDate!!)}
                newList.joinToString(",")
            })

            if(Registry.properties.outputQuestDB) {
                file3.writeText(headerText + data.joinToString("\n") { it ->
                    val newList =
                        it.take(node.minArity) + it.drop(node.minArity)
                            .map { "\"${dateFormatQuestDB.format(Date(it.toLong()))}\"" }
                    newList.joinToString(",")
                })
            }
        }
    }


}