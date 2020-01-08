package org.bernshtam.weather

import org.bernshtam.weather.utils.Utils
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

data class Coordinates(val lat: Double, val long: Double)

@Suppress("DataClassPrivateConstructor")
data class PointAtTime private constructor(val lat: Double, val long: Double, val time: ZonedDateTime) {

    fun getLocalTime(): Long {
        return (Date.from(time.toInstant()).time / 100000) * 100
    }

    companion object {
        fun at(lat: Double, long: Double, time: ZonedDateTime): PointAtTime {
            val latRounded = Utils.roundToHalf(lat * 10.0) / 10.0
            val longRounded = Utils.roundToHalf(long * 10.0) / 10.0
            return PointAtTime(latRounded, longRounded, time)
        }
    }
}

data class MarkAndDescription(val date: String, val mark: Int, val maxMark: Int, val description: String)
data class IMSClouds(val high: Double, val medium: Double, val low: Double)

const val CELL_SIZE = 0.1

const val LAT_START = 29.0
const val LAT_END = 34.0
const val LONG_START = 34.0
const val LONG_END = 36.0


data class Cell(val date: LocalDate,
                val square_size: Double,
                val latitude: Double,
                val longitude: Double,
                val low: Double,
                val medium: Double,
                val high: Double,
                var rank: Double?,
                var sunset_near: Double?,
                var sunset_far: Double?,
                var sun_blocking_near: Double?,
                var sun_blocking_far: Double?,
                var description: String?
)


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