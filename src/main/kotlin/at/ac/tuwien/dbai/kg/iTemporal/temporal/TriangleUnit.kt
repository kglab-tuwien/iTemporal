package at.ac.tuwien.dbai.kg.iTemporal.temporal

enum class TriangleUnit {
    Year,
    Month,
    Week,
    Day,
    Unknown;

    fun getTypeString(): String {
        return when (this) {
            Year -> "year"
            Month -> "month"
            Week -> "week"
            Day -> "day"
            Unknown -> throw RuntimeException("Invalid selection. Error in code")
        }
    }
}