package at.ac.tuwien.dbai.kg.iTemporal.util

import at.ac.tuwien.dbai.kg.iTemporal.core.Registry

object NameGenerator {
    fun getUniqueName():String {
        return "g" + (++Registry.properties.lastNameID)
    }

    fun reset() {
        Registry.properties.lastNameID = 0
    }


}