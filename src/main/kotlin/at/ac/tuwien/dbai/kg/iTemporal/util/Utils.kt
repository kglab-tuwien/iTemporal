package at.ac.tuwien.dbai.kg.iTemporal.util

import kotlin.math.min

object Utils {

    fun cleanIntervals(dataList: List<List<Double>>) : MutableList<MutableList<Double>> {
        if (dataList.isEmpty()) {
            return mutableListOf()
        }

        val arity = dataList.first().size
        val timeStartIndex = arity-2
        val timeEndIndex = arity-1

        val sortedData = dataList.sortedWith(Comparator { a, b ->
            val x = a[timeStartIndex].compareTo(b[timeStartIndex])
            if (x == 0) {
                return@Comparator a[timeEndIndex].compareTo(b[timeEndIndex])
            }
            return@Comparator x
        }).map{it.toMutableList()}.toMutableList()

        val markAsRemoval = mutableListOf<Int>()
        for(i in 1 until sortedData.size) {
            if(sortedData[i][timeStartIndex] < sortedData[i-1][timeEndIndex]) {
                sortedData[i][timeStartIndex] = sortedData[i-1][timeEndIndex]
            }
            if(sortedData[i][timeStartIndex] >= sortedData[i][timeEndIndex]) {
                sortedData[i][timeEndIndex] = sortedData[i][timeStartIndex]
                markAsRemoval.add(i)
            }
            // Two entries starting at same time, keep only last as it may range further
            if(sortedData[i][timeStartIndex] == sortedData[i-1][timeStartIndex]) {
                markAsRemoval.add(i-1)
            }
        }




        markAsRemoval.sortDescending()
        markAsRemoval.distinct().forEach { sortedData.removeAt(it) }

        return sortedData
    }

    /**
     * Requires cleaned intervals
     */
    fun mergeIntervals(dataList: List<List<Double>>) : List<List<Double>> {
        if (dataList.isEmpty()) {
            return dataList
        }

        val arity = dataList.first().size
        val timeStartIndex = arity-2
        val timeEndIndex = arity-1

        val markAsRemoval = mutableListOf<Int>()

        val mergedData = dataList.map { it.toMutableList() }.toMutableList()

        for(i in 1 until mergedData.size) {
            if(mergedData[i][timeStartIndex] <= mergedData[i-1][timeEndIndex]) {
                mergedData[i][timeStartIndex] = mergedData[i-1][timeStartIndex]
                markAsRemoval.add(i-1)
            }
        }

        markAsRemoval.sortDescending()
        markAsRemoval.distinct().forEach { mergedData.removeAt(it) }

        return mergedData
    }

    fun inverseIntervals(dataList: List<List<Double>>, entry:List<Double>) : List<List<Double>> {
        val arity = entry.size
        val timeStartIndex = arity-2
        val timeEndIndex = arity-1

        val newList = mutableListOf<List<Double>>()
        var startTime = entry[timeStartIndex]
        for (dataEntry in dataList) {
            if (startTime < dataEntry[timeStartIndex]) {
                newList.add(entry.take(arity-2) + listOf(startTime, dataEntry[timeStartIndex]))
            }
            startTime = dataEntry[timeEndIndex]
        }
        if (startTime < entry[timeEndIndex]) {
            newList.add(entry.take(arity-2) + listOf(startTime, entry[timeEndIndex]))
        }
        return newList
    }

    /**
     * Both input intervals have to be sorted
     */
    fun filterIntervals(intervals: List<List<Double>>, filterIntervals:List<List<Double>>) : List<List<Double>> {
        if (intervals.isEmpty()) {
            return intervals
        }
        val arity = intervals.first().size
        val timeStartIndex = arity-2
        val timeEndIndex = arity-1

        val newList = mutableListOf<List<Double>>()
        var currentFilterEntry = 0

        outer@ for (interval in intervals) {
            // Add all entries after the current filter entry
            if (currentFilterEntry >= filterIntervals.size) {
                newList.add(interval)
                continue
            }
            // Skip filterIntervals before interval
            while(interval[timeStartIndex] > filterIntervals[currentFilterEntry][timeEndIndex]) {
                currentFilterEntry++
                if (currentFilterEntry >= filterIntervals.size) {
                    newList.add(interval)
                    continue@outer
                }
            }

            val currentInterval = interval.toMutableList()
            while (true) {
                var startPoint = currentInterval[timeStartIndex]
                var endPoint = currentInterval[timeEndIndex]

                // Update start point
                startPoint = min(filterIntervals[currentFilterEntry][timeStartIndex], startPoint)

                // Interval matches start point, then update start point to end of overlapping interval
                if (startPoint == filterIntervals[currentFilterEntry][timeStartIndex]) {
                    currentInterval[timeStartIndex] = filterIntervals[currentFilterEntry][timeEndIndex]
                    startPoint = currentInterval[timeStartIndex]
                    currentFilterEntry++
                    // Last entry reached for this interval
                    if (currentFilterEntry >= filterIntervals.size) {
                        break
                    }
                }

                // Check if start point and end point is valid, otherwise it was overlapping all over the interval
                if (startPoint >= endPoint) {
                    break
                }

                // Now we focus on the end point, and add the end point to the new intervals
                endPoint = min(filterIntervals[currentFilterEntry][timeStartIndex], endPoint)

                newList.add(currentInterval.take(arity-2) + listOf(startPoint,endPoint))

                if (endPoint < filterIntervals[currentFilterEntry][timeStartIndex]) {
                    break
                }

                // We do not update the end point but after the shortened interval, we have to update the start point for the next loop
                currentInterval[timeStartIndex] = filterIntervals[currentFilterEntry][timeEndIndex]
            }

        }

        return newList
    }
}