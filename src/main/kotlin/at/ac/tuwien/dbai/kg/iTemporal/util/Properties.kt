package at.ac.tuwien.dbai.kg.iTemporal.util

import java.io.File
import java.text.SimpleDateFormat

/**
 * Represents the properties of the generator.
 * @property nodes number of nodes in the graph
 * @property inputNodes number of input nodes in the graph
 * @property outputNodes number of output nodes in the graph
 * @property multiEdgeRules number of nodes with multiple incoming edges. May be increased if not compatible with setting.
 * @property recursiveRules percentage that an inserted edge creates a SCC by closing a cycle or adds a chordal edge.
 * @property recursiveComplexity level between 0 and 1, where 0 are single simple cycles and 1 is a single strongly connected component
 *
 *
 * Rule Category Distribution:
 * @property coreMultiEdgeRules percentage of join rules (union, intersection).
 * @property coreSingleEdgeRules percentage of join rules (linear).
 * @property aggregationRules percentage of aggregation rules.
 * @property singleEdgeTemporalRules percentage of temporal rules (diamond, box rules).
 * @property multiEdgeTemporalRules percentage of temporal rules (diamond, box rules).
 *
 * Rule Type Distribution:
 * @property linearRules percentage of linear rule.
 *
 * @property intersectionRules percentage of intersection rule.
 * @property unionRules percentage of union rule.
 *
 * @property diamondMinusRules percentage of diamond minus rules.
 * @property diamondPlusRules percentage of diamond plus rules.
 * @property boxMinusRules percentage of box minus rules.
 * @property boxPlusRules percentage of box plus rules.
 *
 * @property sinceRules percentage of since rules.
 * @property untilRules percentage of until rules.
 *
 * @property spanningTemporalAggregationRules percentage of spanning temporal aggregation rules.
 * @property movingWindowTemporalAggregationRules percentage of moving window temporal aggregation rules.
 * @property instantaneousTemporalAggregationRules percentage of instantaneous temporal aggregation rules.
 *
 * Property Assignments:
 *
 * Intersection properties:
 * @property averageNumberOfOverlappingJoinTerms
 * @property varianceNumberOfOverlappingJoinTerms
 *
 * Temporal properties:
 * @property temporalFactor multiplier for temporal values (e.g., to convert provided numbers to higher durations, e.g., seconds, days, etc.)
 * @property averageNumberOfTemporalUnitsT1
 * @property varianceNumberOfTemporalUnitsT1
 * @property averageNumberOfTemporalUnitsT2
 * @property varianceNumberOfTemporalUnitsT2
 * @property temporalMaxPrecision to how many units the temporal intervals should be rounded (e.g., 2.34256 with precision 2 => 2.34)
 *
 * Aggregation properties:
 * @property averageNumberOfContributorTerms
 * @property varianceNumberOfContributorTerms
 * @property averageNumberOfGroupByTerms
 * @property varianceNumberOfGroupByTerms
 *
 * Data:
 *
 * Data (Output Nodes):
 * @property averageOutputArity
 * @property varianceOutputArity
 * @property outputTimestampStart range of intervals where seeding data is produced
 * @property outputTimestampEnd range of intervals where seeding data is produced
 * @property averageOutputIntervalDuration duration of the interval length
 * @property varianceOutputIntervalDuration
 * @property averageAmountOfGeneratedOutputs
 * @property varianceAmountOfGeneratedOutputs
 *
 * Data (Aggregation nodes):
 * @property averageAggregationSelectivity
 * @property varianceAggregationSelectivity
 * @property averageAggregationBucket
 * @property varianceAggregationBucket
 * @property percentageViaContributor
 *
 * Data (Union)
 * @property unionInclusionPercentage how likely an entry is included at each incoming edge
 *
 * Data (Temporal)
 * @property temporalInclusionPercentage how likely an entry is forwarded to the "previous round" in cycles
 *
 * Data
 * @property maxInnerNodeDataFactor maximum amount of data for an inner node given by averageOutputArity * maxInnerNodeDataFactor
 *
 * Data (General):
 * @property cardinalityTermDomain number of different terms that are produced (1000 -> numbers between 0 and 999)
 * @property generateTimePoints if true, then we only produce punctual intervals as input data
 * @property outputCsvHeader if true, then the csv contains a header row
 * @property outputQuestDB if true, then an additional csv for questBD is produced that follow its timestamp convention
 *
 * Other:
 * @property path
 */
data class Properties(
    var nodes: Int = 6,
    var inputNodes: Int = 1,
    var outputNodes: Int = 1,
    var multiEdgeRules: Double = 0.3,
    var recursiveRules: Double = 0.1,
    var recursiveComplexity: Double = 0.1,

    var coreMultiEdgeRules: Double = 1.0,
    var multiEdgeTemporalRules: Double = 0.0,
    var coreSingleEdgeRules: Double = 0.45,
    var singleEdgeTemporalRules: Double = 0.25,
    var aggregationRules: Double = 0.3,

    // Fine-grained properties for single edge rules.
    var linearRules: Double = 1.0,

    // Fine-grained properties for multi edge rules.
    var intersectionRules: Double = 0.5,
    var unionRules: Double = 0.5,

    // Fine-grained properties for temporal rules.
    var diamondMinusRules: Double = 0.4,
    var diamondPlusRules: Double = 0.4,
    var boxMinusRules: Double = 0.1,
    var boxPlusRules: Double = 0.1,

    // Fine-grained properties for temporal multi-edge rules.
    var sinceRules: Double = 0.5,
    var untilRules: Double = 0.5,

    // Fine-grained properties for Aggregation.
    var spanningTemporalAggregationRules: Double = 0.3,
    var movingWindowTemporalAggregationRules: Double = 0.3,
    var instantaneousTemporalAggregationRules: Double = 0.4,


    // Rule arity generator (Gaussian distribution, capped at least by arity of 1)
    var averageOutputArity: Int = 3,
    var varianceOutputArity: Double = 1.0,

    // Aggregation settings
    var averageNumberOfContributorTerms: Int = 1,
    var varianceNumberOfContributorTerms: Double = 1.0,
    var averageNumberOfGroupByTerms: Int = 2,
    var varianceNumberOfGroupByTerms: Double = 1.0,

    // Intersection settings
    var averageNumberOfOverlappingJoinTerms: Int = 1,
    var varianceNumberOfOverlappingJoinTerms: Double = 1.0,

    // Temporal settings
    var temporalFactor: Double = 1000.0, // seconds = 1000
    var averageNumberOfTemporalUnitsT1: Double = 1.0,
    var varianceNumberOfTemporalUnitsT1: Double = 1.0,
    var averageNumberOfTemporalUnitsT2: Double = 3.0,
    var varianceNumberOfTemporalUnitsT2: Double = 1.0,
    var temporalMaxPrecision: Int = 0,

    // Data Generation Settings
    var cardinalityTermDomain:Long=1000,
    var averageAmountOfGeneratedOutputs: Int = 10,
    var varianceAmountOfGeneratedOutputs: Double = 3.0,
    var maxInnerNodeDataFactor: Int = 10,
    var outputTimestampStart:Long = (SimpleDateFormat("yyyy-mm-dd")).parse("2020-01-01").time,
    var outputTimestampEnd:Long = (SimpleDateFormat("yyyy-mm-dd")).parse("2022-01-01").time,
    var averageOutputIntervalDuration:Double = 10.0,
    var varianceOutputIntervalDuration:Double = 1.0,
    var averageAggregationSelectivity: Double = 0.2,
    var varianceAggregationSelectivity: Double = 0.02,
    var unionInclusionPercentage: Double = 0.6,
    var temporalInclusionPercentage: Double = 0.6,


    // Storage location
    var path:File = File("xxxx"),

    var generateTimePoints:Boolean = true,

    var averageAggregationBucket:Double = 1.0,
    var varianceAggregationBucket:Double = 0.0,
    var percentageViaContributor:Double = 0.7,

    var outputCsvHeader:Boolean = false,
    var outputQuestDB: Boolean = false,

)