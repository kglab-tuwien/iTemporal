package at.ac.tuwien.dbai.kg.iTemporal.util

object NameGenerator {
    private var id = 0

    fun getUniqueName():String {
        return "g" + (++id)
    }

    fun reset() {
        id = 0
    }


}