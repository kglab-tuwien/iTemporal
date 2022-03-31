package at.ac.tuwien.dbai.kg.iTemporal.aggregation.jobs

import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.AggregationType
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.AggregationEdge
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges.ITAEdge
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.ArityPropertyAssignment
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.DependencyGraph
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import kotlin.math.max
import kotlin.math.min

/**
 * Sets the arity dependent property information for a random aggregation edge
 */
object AggregationPropertyAssigner : ArityPropertyAssignment {
    override fun getPriority(): Int = 57

    override fun run(nodes: List<Node>, dependencyGraph: DependencyGraph): Boolean {

        // Unhandled Aggregation Nodes
        val aggregationNodes = nodes.filter { dependencyGraph.inEdges[it].orEmpty().any { edge -> edge is ITAEdge && (edge.numberOfGroupingTerms == -1 || edge.numberOfContributors == -1) } }.shuffled()

        if (aggregationNodes.isEmpty()) {
            return false
        }

        // We get some random aggregation node
        val aggregationNode = aggregationNodes.first()

        val e1 = dependencyGraph.inEdges[aggregationNode]!![0] as AggregationEdge

        // Calculate number of group by terms
        var groupBys = 0
        if(e1.numberOfGroupingTerms < 0) {
            groupBys = RandomGenerator.getNextArityWith0(
                Registry.properties.averageNumberOfGroupByTerms,
                Registry.properties.varianceNumberOfGroupByTerms
            )
            groupBys = min(aggregationNode.maxArity - 1, groupBys)
            groupBys = max(aggregationNode.minArity - 1, groupBys)
        } else {
            if (!((groupBys >= (aggregationNode.minArity - 1)) && (groupBys <= (aggregationNode.maxArity - 1)))) {
                throw RuntimeException("Invalid number of group by terms pre-assigned")
            }
        }

        // Calculate number of contributors
        var contributors = 0

        if (e1.numberOfContributors < 0 && e1.aggregationType != AggregationType.Max && e1.aggregationType != AggregationType.Min) {
            contributors = RandomGenerator.getNextArityWith0(
                Registry.properties.averageNumberOfContributorTerms,
                Registry.properties.varianceNumberOfContributorTerms
            )
            // Restrict maximum amount
            contributors = min(e1.from.maxArity - 1 - groupBys, contributors)
            // We do not have to restrict minimum amount as the remaining can be filled up by other nodes.
            //contributors = max(e1.from.minArity - 1 - groupBys, contributors)
        }

        // optionally add additional terms to increase arity, but we follow minimum arity principle
        var others = 0
        others = max(others,e1.from.minArity - 1 - groupBys - contributors)
        others = min(others,e1.from.maxArity - 1 - groupBys - contributors)

        // Update nodes and edges
        aggregationNode.minArity = groupBys+1
        aggregationNode.maxArity = groupBys+1

        e1.numberOfGroupingTerms = groupBys
        e1.numberOfContributors = contributors

        e1.from.minArity = groupBys+contributors+1+others
        e1.from.maxArity = e1.from.minArity

        return true

    }

}