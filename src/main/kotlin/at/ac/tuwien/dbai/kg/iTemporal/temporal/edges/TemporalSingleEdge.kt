package at.ac.tuwien.dbai.kg.iTemporal.temporal.edges

import at.ac.tuwien.dbai.kg.iTemporal.util.RandomGenerator
import at.ac.tuwien.dbai.kg.iTemporal.core.Registry
import at.ac.tuwien.dbai.kg.iTemporal.core.contracts.SingleEdge
import kotlin.random.Random

interface TemporalSingleEdge : SingleEdge, TemporalEdge {

    fun applyInterval(interval: Interval):Interval
    fun applyIntervalBackward(interval: Interval):Interval

    fun timeIntervalBackward(values:List<Double>):List<Double>
    fun timeIntervalForward(values:List<Double>):List<Double>

    override fun backwardPropagateData() {
        val newData = mutableListOf<List<Double>>()


        for(entry in to.data) {
            // Include edge?
            val isIncluded = Random.nextDouble()
            if(isCyclic && isIncluded > Registry.properties.temporalInclusionPercentage) {
                continue
            }

            val newTimePoints = this.timeIntervalBackward(entry)

            val newEntry = Array(from.minArity+2){index -> -100.0}
            for ((fromIndex, orderId) in this.termOrder.withIndex()) {
                if (orderId < 0) {
                    newEntry[fromIndex] = RandomGenerator.generateTerm(Registry.properties.cardinalityTermDomain)
                } else if (orderId >= 0 && orderId < to.minArity) {
                    newEntry[fromIndex] = entry[orderId]
                } else {
                    newEntry[fromIndex] = RandomGenerator.generateTerm(Registry.properties.cardinalityTermDomain)
                }
            }

            // Add time interval
            newEntry[from.minArity] = newTimePoints[0]
            newEntry[from.minArity+1] = newTimePoints[1]

            // Do not add if time interval is invalid
            if (newTimePoints[0] > newTimePoints[1]) {
                continue
            }
            newData.add(newEntry.toList())
        }


        this.from.data = newData + this.from.data
    }

    override fun forwardPropagateData() {
        val newData = mutableListOf<List<Double>>()

        for(entry in from.data) {
            val newEntry = Array(to.minArity+2){index -> -100.0}

            val newTimePoints = this.timeIntervalForward(entry)

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
            newEntry[to.minArity] = newTimePoints[0]
            newEntry[to.minArity+1] = newTimePoints[1]

            // Do not add if time interval is invalid
            if (newTimePoints[0] > newTimePoints[1]) {
                continue
            }

            newData.add(newEntry.toList())
        }

        this.to.data = this.to.data + newData
    }


}