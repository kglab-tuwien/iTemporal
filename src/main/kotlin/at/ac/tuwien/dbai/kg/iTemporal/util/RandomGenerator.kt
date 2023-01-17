package at.ac.tuwien.dbai.kg.iTemporal.util

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.dependencyGraph.Node
import at.ac.tuwien.dbai.kg.iTemporal.temporal.assignments.MetaIntervalAssigner
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.asJavaRandom

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return (this * multiplier).roundToLong() / multiplier
}

object RandomGenerator {

    var sharedRandom: Random = Random.Default
    var sharedRandomJava = sharedRandom.asJavaRandom()

    fun setSeed(seed: Int) {
        sharedRandom = Random(seed)
        sharedRandomJava = sharedRandom.asJavaRandom()
    }

    // Restrict arity to at least 1
    fun getNextArity(mean:Int, variance: Double=0.0) :Int {
        return max(1,(sharedRandomJava.nextGaussian() * sqrt(variance) + mean).roundToInt())
    }

    fun getNextArityWith0(mean:Int, variance: Double=0.0) :Int {
        return max(0,(sharedRandomJava.nextGaussian() * sqrt(variance) + mean).roundToInt())
    }

    fun  getNextArityWith0(mean:Double, variance: Double=0.0) :Int {
        return max(0,(sharedRandomJava.nextGaussian() * sqrt(variance) + mean).roundToInt())
    }

    fun getNextDoubleWithPrecision(mean: Double, variance: Double, maxPrecision: Int = 0): Double {
        return max(0.0,(sharedRandomJava.nextGaussian() * sqrt(variance) + mean)).round(maxPrecision)
    }

    fun getDoubleWithPrecisionBetween(low: Double = 0.0, high: Double = 1.0, maxPrecision: Int = 0): Double {
        return sharedRandom.nextDouble(low,high).round(maxPrecision)
    }

    fun generateTerm(numberUniqueValues:Long, from:Long=0) : Double {
        return sharedRandom.nextLong(from,numberUniqueValues).toDouble()
    }


    fun generateData(node: Node): List<List<Double>> {
        val minArity=node.minArity
        val entries = mutableListOf<List<Double>>()

        val minimumOutputMean = Registry.properties.averageAmountOfGeneratedOutputs
        val minimumOutputVariance = Registry.properties.varianceAmountOfGeneratedOutputs

        val dataAmount = max(0,(sharedRandomJava.nextGaussian() * sqrt(minimumOutputVariance) + minimumOutputMean).roundToInt())

        val minimumOutputMeanIntervals = Registry.properties.averageAmountOfGeneratedOutputIntervals
        val minimumOutputVarianceIntervals = Registry.properties.varianceAmountOfGeneratedOutputIntervals

        for (i in 0 until dataAmount) {
            val entry = mutableListOf<Double>()

            for (j in 0 until minArity) {
                entry.add(generateTerm(Registry.properties.cardinalityTermDomain))
            }

            val intervalAmount = max(0,(sharedRandomJava.nextGaussian() * sqrt(minimumOutputVarianceIntervals) + minimumOutputMeanIntervals).roundToInt())

            for (j in 0 until intervalAmount) {
                val intervalStart = sharedRandom.nextLong(
                    Registry.properties.outputTimestampStart,
                    Registry.properties.outputTimestampEnd
                )
                val minimalIntervalSize =
                    max(0.0, MetaIntervalAssigner.getIntervalInformations()[node]!!.intervalOffset.getDuration())
                val intervalEnd: Double = if (Registry.properties.generateTimePoints) {
                    // We work externally with dates
                    intervalStart + minimalIntervalSize + 1
                } else {
                    // At least the minimal interval size
                    max(
                        intervalStart + 1.0 + Registry.properties.temporalFactor * getNextDoubleWithPrecision(
                            Registry.properties.averageOutputIntervalDuration,
                            Registry.properties.varianceOutputIntervalDuration
                        ), intervalStart + minimalIntervalSize + 1
                    )
                }

                // Copy entry and add intervals
                val insertEntry = mutableListOf<Double>()
                insertEntry.addAll(entry)
                insertEntry.add(intervalStart.toDouble())
                insertEntry.add(intervalEnd)
                assert(intervalStart < intervalEnd)

                entries.add(insertEntry)
            }
        }

        return entries
    }


}