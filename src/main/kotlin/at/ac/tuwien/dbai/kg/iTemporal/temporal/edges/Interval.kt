package at.ac.tuwien.dbai.kg.iTemporal.temporal.edges

data class Interval(val t1:Double, val t2:Double) {

    fun normalize():Interval {
        return Interval(0.0,t2-t1)
    }
}