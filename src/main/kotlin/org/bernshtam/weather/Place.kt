package org.bernshtam.weather

data class Place(val name: String, val lat: Double, val long: Double) {
    companion object {
        val PLACES = listOf(
                Place("Ashdod", 31.80, 34.65),
                Place("Herzlia", 32.16, 34.84),
                Place("Palmachim", 31.93, 34.70),
                Place("Tel Aviv", 32.08, 34.78),
                Place("Haifa", 32.79, 34.98),
                Place("Ashkelon", 31.66, 34.57),
                Place("Netania", 32.32, 34.85)
        ).map{it.name to it}.toMap()
    }
}