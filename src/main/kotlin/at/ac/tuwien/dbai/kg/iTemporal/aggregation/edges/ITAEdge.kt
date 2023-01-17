package at.ac.tuwien.dbai.kg.iTemporal.aggregation.edges

import at.ac.tuwien.dbai.kg.iTemporal.util.NameGenerator
import at.ac.tuwien.dbai.kg.iTemporal.util.Utils
import at.ac.tuwien.dbai.kg.iTemporal.aggregation.AggregationType
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.Edge
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator.sharedRandom
import java.lang.RuntimeException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class ITAEdge(
    override var from: Node,
    override var to: Node,
    override var isCyclic: Boolean = false,
    override var termOrder: List<Int> = emptyList(),
    override var numberOfGroupingTerms: Int = -1,
    override var numberOfContributors: Int = -1,
    override var aggregationType: AggregationType = AggregationType.Unknown,
    override var uniqueId: String = NameGenerator.getUniqueName(),
    override var termOrderShuffleAllowed: Boolean = true,
    override var termOrderReference: String? = null,
    override var existentialCount: Int = 0,
) : AggregationEdge {

    fun getAggregationTermFromIndex():Int {
        return to.minArity + numberOfContributors
    }

    fun getContributorIndices():List<Int> {
        val data = mutableListOf<Int>()
        for (id in to.minArity until to.minArity + numberOfContributors) {
            data.add(id)
        }
        return data
    }

    override fun backwardPropagateData() {
        /**
         * Step 1: Group Data per group by clause
         * Step 2: Output data may be overlapping for some terms. Choose only one output value per time and group by clause.
         * Step 3: Generate multiple values to be aggregated to retrieve output value. Consider:
         * - Different contributor values (contributor terms not given by output, random generated)
         * - Multiple values per group by term (group by terms given by output)
         * - Logic: First create fitting aggregation. Then create per entry multiple data entries for contributor values.
         */

        val aggregationIndex = to.minArity - 1
        val timeStartIndex = to.minArity
        val timeEndIndex = to.minArity + 1
        val aggregationFromIndex = this.termOrder.indexOf(to.minArity + numberOfContributors)

        val newData = mutableListOf<List<Double>>()

        // Step 1: create groups
        val groupings = to.data.groupBy { it.take(this.numberOfGroupingTerms) }
        val oldGroupings = this.to.oldData.groupBy { it.take(this.numberOfGroupingTerms) }

        for (group in groupings) {
            // Step 2: normalize data in group

            // If intervals are overlapping it takes one of the aggregation values
            val sortedData = Utils.mergeIntervals(Utils.cleanIntervals(group.value))

            val oldGroup = Utils.mergeIntervals(Utils.cleanIntervals(oldGroupings[group.key].orEmpty()))
            val relevantOnes = Utils.filterIntervals(sortedData, oldGroup)

            // Step 3: Generate input data
            for (entry in relevantOnes) {
                /**
                 *
                 * We have three axis to consider:
                 * - Splitting the values along the time axis
                 *   basket process
                 * - Splitting the values along the contributors
                 *   we generate different values for the same contributor and only count the maximum one
                 * - Splitting the values between remaining arity
                 *   we can ignore the remaining arity when we choose the aggregation value (for count, we use this value as differentiation) carefully (distinct)
                 *
                 * Process:
                 * 0. Check generation interval, i.e., generate aggregation only for new group by keys or not covered time intervals
                 *    This is, we ignore recursive aggregation generation, as always smaller values are generated for the same time
                 *    causing, in general, e.g., count, wrong aggregation tuples.
                 * 1. Determine amount of baskets B
                 * 2. Generate B-1 dividers of baskets
                 * 3. For each basket, starting with the first one:
                 *  a. compute number of elements (n) in the basket (for fulfilling selectivity)
                 *  b. randomly move x% of previous basket (if exists) to current basket (i.e., extend those intervals)
                 *  c. fill up the remaining basket with elements (i.e., add remaining elements for count,min,max, or for sum add values to reach the new result value, i.e., aggregation value - added values)
                 *  c.1. When filling up, generate also randomly different values for contributor terms
                 *  c.2. For count, it is important to solve the selectivity constraint via contributors. In case there are not any. Then one cannot fulfill selectivity constraint, as this one is limited by the count
                 *  d. continue the process with the next basket
                 * 4. merge overlapping values between baskets to single output
                 *
                 * Filling up the basket:
                 * sum: Generate n values, calculate the sum, divide each value by the sum and multiply by the desired result value
                 * count: Generate n different values, where n here is the number of remaining elements
                 * min: Generate n different values, all at least aggregationResult, set one random value to min
                 * max: Generate n different values, all at most aggregationResult, set one random value to max
                 *
                 */

                // Stop recalculation
                /*val isHandled = this.to.oldData.any { oldDataEntry ->
                    entry.withIndex().all { it.value == oldDataEntry[it.index] || it.index == aggregationIndex }
                }
                if (isHandled) {
                    continue
                }*/


                // Determine amount of baskets
                val offset = entry[to.minArity + 1] - entry[to.minArity]

                val tempNumberOfBaskets = RandomGenerator.getNextArityWith0(Registry.properties.averageAggregationBucket,Registry.properties.varianceAggregationBucket)

                val numberOfBaskets =
                    max(1, min((offset).toLong(), tempNumberOfBaskets.toLong())).toInt()

                // Generate dividers
                val basketBoundaries = mutableListOf<Double>()
                while (basketBoundaries.size < numberOfBaskets - 1) {
                    val value = entry[to.minArity] + RandomGenerator.getDoubleWithPrecisionBetween(
                        high = offset,
                        maxPrecision = Registry.properties.temporalMaxPrecision
                    )
                    if (!basketBoundaries.contains(value)) {
                        basketBoundaries.add(value)
                    }
                }
                basketBoundaries.sort()

                val baskets: List<Pair<Double, Double>> = listOf(
                    Pair(
                        entry[to.minArity],
                        if (basketBoundaries.isEmpty()) entry[to.minArity + 1] else basketBoundaries.first()
                    )
                ) + basketBoundaries.mapIndexed { index, d ->
                    Pair(
                        basketBoundaries[index],
                        if (index + 1 == basketBoundaries.size) entry[to.minArity + 1] else basketBoundaries[index + 1]
                    )
                }

                // This is the case, if we have an arity = 0
                if (aggregationIndex < 0) {
                    System.err.println("Warning: Aggregation index is negative. This is the case if arity 0 is used in combination with aggregation")
                } else {

                    val aggregationResult = entry[aggregationIndex]

                    // We only consider positive values (monotonicity)
                    if (aggregationResult == 0.0) {
                        continue
                    }

                    if (aggregationResult < 0.0) {
                        throw RuntimeException("Invalid Aggregation Result produced, this should not happen")
                    }
                }

                val aggregationResult: Double = if (aggregationIndex < 0) 1.0 else entry[aggregationIndex]

                var previousBasket = listOf<MutableList<Double>>()

                for ((index, basket) in baskets.withIndex()) {
                    // compute number of elements (n) in the basket
                    val selectivity = max(
                        0.001, RandomGenerator.getNextDoubleWithPrecision(
                            Registry.properties.averageAggregationSelectivity,
                            Registry.properties.varianceAggregationSelectivity,
                            maxPrecision = 4
                        )
                    )
                    var numberOfInputTuples = (1.0 / selectivity).toInt()

                    val basketData = mutableSetOf<MutableList<Double>>()


                    // move values from previous basket
                    if (index > 0) {
                        // implement copy operation (not necessary in first step, we just generate n different values per basket)
                        // we limit the copy to 50\% at most
                        val copyAmount = RandomGenerator.sharedRandom.nextInt((previousBasket.size * 0.5).toInt())
                        previousBasket = previousBasket.shuffled(RandomGenerator.sharedRandom).take(copyAmount).map {
                            // Copy data for not modifying basket
                            val data = ArrayList(it)
                            data[from.minArity] = basket.first
                            data[from.minArity + 1] = basket.second
                            data
                        }
                        // Update required amount of tuples to be generated
                        numberOfInputTuples -= - previousBasket.size
                    }

                    val newAggregationResult = aggregationResult

                    var numberOfRelevantInput = numberOfInputTuples
                    // We can play with contributors here
                    if (this.numberOfContributors > 0) {
                        numberOfRelevantInput =
                            max(1, (numberOfRelevantInput * (1-Registry.properties.percentageViaContributor)).toInt())
                    }
                    if (this.aggregationType == AggregationType.Max) {
                        numberOfRelevantInput = min(numberOfRelevantInput, aggregationResult.toInt()+1)
                    }
                    if (this.aggregationType == AggregationType.Count) {
                        numberOfRelevantInput = min(numberOfRelevantInput, aggregationResult.toInt())
                    }
                    if (this.numberOfContributors == 0) {
                        if (this.aggregationType == AggregationType.Count && this.from.minArity == this.numberOfGroupingTerms + 1) {
                            numberOfRelevantInput = newAggregationResult.toInt()
                        }
                        numberOfInputTuples = numberOfRelevantInput
                    }

                    val contributorFromIndexes = this.termOrder.withIndex()
                        .filter { it.value >= to.minArity && it.value < to.minArity + numberOfContributors }
                        .map { it.index }


                    var randomTermCountList = mutableSetOf<Double>()
                    if (this.aggregationType == AggregationType.Count) {
                        if (aggregationResult/Registry.properties.cardinalityTermDomain.toDouble() > 0.75 || isCyclic) {
                            randomTermCountList = MutableList(Registry.properties.cardinalityTermDomain.toInt()){it.toDouble()}.toMutableSet()
                        } else {
                            while(randomTermCountList.size < aggregationResult.toInt() ) {
                                randomTermCountList.add(RandomGenerator.generateTerm(Registry.properties.cardinalityTermDomain))
                            }
                        }
                    }
                    val countList:MutableList<Double>
                    if (!isCyclic) {
                        countList = randomTermCountList.shuffled(RandomGenerator.sharedRandom).toMutableList()
                    } else {
                        countList = randomTermCountList.take(aggregationResult.toInt()).shuffled(RandomGenerator.sharedRandom).toMutableList()
                    }

                    for (i in 0 until numberOfRelevantInput) {
                        val inputData = Array(this.from.minArity + 2) { -1.0 }
                        for ((fromIndex, orderId) in this.termOrder.withIndex()) {
                            if (orderId < 0) {
                                inputData[fromIndex] = RandomGenerator.generateTerm(Registry.properties.cardinalityTermDomain)
                            } else if (orderId in 0 until numberOfGroupingTerms) {
                                inputData[fromIndex] = entry[orderId]
                            } else if (orderId >= to.minArity && orderId < to.minArity + numberOfContributors) {
                                // We generate some random contributor variable
                                inputData[fromIndex] = RandomGenerator.generateTerm(Registry.properties.cardinalityTermDomain)
                            } else if (orderId == this.getAggregationTermFromIndex()) {
                                when (this.aggregationType) {
                                    AggregationType.Count -> inputData[fromIndex] = countList.removeFirst()
                                    AggregationType.Sum -> inputData[fromIndex] = RandomGenerator.generateTerm(Registry.properties.cardinalityTermDomain)
                                    AggregationType.Min -> inputData[fromIndex] = RandomGenerator.generateTerm(Registry.properties.cardinalityTermDomain, from=aggregationResult.toLong())
                                    AggregationType.Max -> inputData[fromIndex] = RandomGenerator.generateTerm(aggregationResult.toLong()+1)
                                    AggregationType.Unknown -> {}
                                }

                                //inputData[fromIndex] = entry[to.minArity - 1]
                            } else {
                                inputData[fromIndex] = RandomGenerator.generateTerm(Registry.properties.cardinalityTermDomain)
                            }
                        }
                        inputData[from.minArity] = basket.first
                        inputData[from.minArity + 1] = basket.second
                        basketData.add(inputData.toMutableList())
                    }

                    // Set random entry to max
                    if (this.aggregationType == AggregationType.Max) {
                        val randomEntry = basketData.random(sharedRandom)
                        basketData.remove(randomEntry)
                        randomEntry[aggregationFromIndex] = aggregationResult
                        basketData.add(randomEntry)
                    }
                    // Set random entry to min
                    if (this.aggregationType == AggregationType.Min) {
                        val randomEntry = basketData.random(sharedRandom)
                        basketData.remove(randomEntry)
                        randomEntry[aggregationFromIndex] = aggregationResult
                        basketData.add(randomEntry)
                    }

                    val basketDataList = basketData.toList()

                    // Create contributor elements
                    for (i in 0 until (numberOfInputTuples - numberOfRelevantInput)) {
                        // Select one entry
                        val baseEntry = basketDataList[RandomGenerator.sharedRandom.nextInt(0, basketDataList.size)]
                        // Copy and modify
                        val copyEntry = baseEntry.mapIndexed { fI, d ->
                            if (fI == aggregationFromIndex) {
                                when (this.aggregationType) {
                                    AggregationType.Count, AggregationType.Sum -> RandomGenerator.generateTerm(
                                        Registry.properties.cardinalityTermDomain
                                    )
                                    AggregationType.Min -> RandomGenerator.generateTerm(
                                        Registry.properties.cardinalityTermDomain,
                                        from = aggregationResult.toLong()
                                    )
                                    AggregationType.Max -> RandomGenerator.generateTerm(aggregationResult.toLong()+1)
                                    AggregationType.Unknown -> -72.0 //inputData[fromIndex] = RandomGenerator.generateTerm(properties.cardinalityTermDomain)
                                }
                            } else d
                        }
                        // Insert
                        basketData.add(copyEntry.toMutableList())
                    }

                    // Normalize sum
                    if (this.aggregationType == AggregationType.Sum) {
                        var targetSum = newAggregationResult

                        // Reduce target sum by copied elements
                        val previousBasketSum = previousBasket.groupBy { basketEntry -> contributorFromIndexes.map { basketEntry[it] }}
                            .map { basketEntry ->
                                basketEntry.value.maxOf { it[aggregationFromIndex] }
                            }.sum()
                        targetSum -= previousBasketSum

                        val basketSum =
                            basketData.groupBy { basketEntry -> contributorFromIndexes.map { basketEntry[it] } }
                                .map { basketEntry ->
                                    basketEntry.value.maxOf { it[aggregationFromIndex] }
                                }.sum()

                        val newBasketData = mutableSetOf<MutableList<Double>>()

                        for (basketEntry in basketData) {
                            basketEntry[aggregationFromIndex] =
                                basketEntry[aggregationFromIndex] / basketSum * targetSum
                            // Normalization Trick, adapt values to integers
                            if (numberOfRelevantInput < aggregationResult) {
                                basketEntry[aggregationFromIndex] = round(basketEntry[aggregationFromIndex])
                            }
                            newBasketData.add(basketEntry)
                        }

                        if (numberOfRelevantInput < aggregationResult) {
                            val basketGroup =
                                newBasketData.groupBy { basketEntry -> contributorFromIndexes.map { basketEntry[it] } }
                            val basketSum2 =
                                basketGroup.map { basketEntry ->
                                    basketEntry.value.maxOf { it[aggregationFromIndex] }
                                }.sum()

                            var difference = aggregationResult - basketSum2

                            if (difference > 0.0) {
                                val randomGroup = basketGroup.values.random(sharedRandom)
                                randomGroup.map { it[aggregationFromIndex] = it[aggregationFromIndex] + difference }
                                difference = 0.0
                            }
                            if (difference < 0.0) {
                                while (difference < 0.0) {
                                    val randomGroup = basketGroup.values.filter {g -> g.any{it[aggregationFromIndex] > 0}}.random(sharedRandom)
                                    val subtractValue = min(-difference, randomGroup.maxOf { it[aggregationFromIndex] })
                                    difference += subtractValue
                                    randomGroup.map { it[aggregationFromIndex] = it[aggregationFromIndex] - subtractValue }
                                }
                                // Remove negative contributors due to rounding
                                newBasketData.removeIf {it[aggregationFromIndex] < 0}
                            }
                        }
                        newBasketData.addAll(previousBasket)
                        previousBasket = newBasketData.toList()
                        newData.addAll(newBasketData)
                    } else {
                        basketData.addAll(previousBasket)
                        previousBasket = basketData.toList()
                        newData.addAll(basketData)
                    }
                }

                // merge (not necessary in first step), we continue with the smallest intervals
                // merge can also be done at some other location were required


            }
        }

        if (aggregationFromIndex >= 0) {
            assert(newData.none { it[aggregationFromIndex] < 0 })
        }

        this.from.data = newData + this.from.data
    }

    override fun forwardPropagateData() {
        // We use a trick and look up result in to node by checking group-by terms and time-frame of data
        val newData = mutableListOf<List<Double>>()

        for (entry in from.data) {
            val newEntry = Array(to.minArity + 2) { index -> -100.0 }

            for ((fromIndex, orderId) in this.termOrder.withIndex()) {
                // Not relevant term
                if (orderId < 0 || orderId >= to.minArity) {
                    continue
                }
                if (orderId < to.minArity) {
                    newEntry[orderId] = entry[fromIndex]
                }
            }

            // Add time interval
            newEntry[to.minArity] = entry[from.minArity]
            newEntry[to.minArity + 1] = entry[from.minArity + 1]


            newData.add(newEntry.toList())
        }


        val groupings = newData.groupBy { it.take(this.numberOfGroupingTerms) }

        val resultGroupings = this.to.oldData2.groupBy { it.take(this.numberOfGroupingTerms) }

        val resultData = mutableListOf<List<Double>>()
        for (group in groupings) {
            val resultGroup = resultGroupings[group.key].orEmpty()

            for (entry in group.value) {
                val resultEntries =
                    resultGroup.filter { it[it.size - 3] <= entry[it.size - 2] && it[it.size - 2] >= entry[it.size - 3] }
                        .map { it.take(it.size - 3) + listOf(entry[it.size - 3], entry[it.size - 2]) }.distinct()
                // We just add all for the same time as something can be derived
                resultData.addAll(resultEntries)
            }
        }

        this.to.data = this.to.data + resultData
    }

    override fun getLabel(): String {
        return "ITA"
    }

    override fun copy(): Edge {
        return ITAEdge(from=from, to=to, isCyclic=isCyclic, termOrder=termOrder, numberOfGroupingTerms=numberOfGroupingTerms, numberOfContributors=numberOfContributors,aggregationType=aggregationType, termOrderShuffleAllowed = termOrderShuffleAllowed, termOrderReference = termOrderReference)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ITAEdge

        if (from != other.from) return false
        if (to != other.to) return false
        if (isCyclic != other.isCyclic) return false
        if (termOrder != other.termOrder) return false
        if (numberOfGroupingTerms != other.numberOfGroupingTerms) return false
        if (numberOfContributors != other.numberOfContributors) return false
        if (aggregationType != other.aggregationType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + isCyclic.hashCode()
        result = 31 * result + termOrder.hashCode()
        result = 31 * result + numberOfGroupingTerms
        result = 31 * result + numberOfContributors
        result = 31 * result + aggregationType.hashCode()
        return result
    }

    override fun toString(): String {
        return "ITAEdge(from=$from, to=$to, isCyclic=$isCyclic, termOrder=$termOrder, numberOfGroupingTerms=$numberOfGroupingTerms, numberOfContributors=$numberOfContributors, aggregationType=$aggregationType, uniqueId='$uniqueId')"
    }


}