package at.ac.tuwien.dbai.kg.iTemporal.aggregation

enum class AggregationType {
    Max,
    Min,
    Count,
    Sum,
    Unknown;

    fun getTypeString(): String {
        return when (this) {
            Max -> "max"
            Min -> "min"
            Count -> "mcount"
            Sum -> "msum"
            Unknown -> throw RuntimeException("Invalid selection. Error in code")
        }
    }
}