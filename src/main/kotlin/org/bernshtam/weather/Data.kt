package org.bernshtam.weather

import org.bernshtam.weather.utils.Utils
import java.math.BigDecimal
import java.time.LocalDateTime
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
data class IMSParams(val high: Double, val medium: Double, val low: Double, val rain: Double)

const val CELL_SIZE = 0.1

const val LAT_START = 29.0
const val LAT_END = 34.0
const val LONG_START = 34.0
const val LONG_END = 36.0


data class Cell(val id: Long?,
                val time: LocalDateTime,
                val latitude: Double,
                val longitude: Double,
                val low: Double,
                val medium: Double,
                val high: Double,
                val clouds: BigDecimal?,
                val temp: BigDecimal?,
                val rain: BigDecimal?
) {
    constructor(time: LocalDateTime,
                latitude: Double,
                longitude: Double,
                low: Double,
                medium: Double,
                high: Double,
                clouds: BigDecimal?,
                temp: BigDecimal?,
                rain: BigDecimal?) :
            this(null, time, latitude, longitude, low, medium, high, clouds, temp, rain)

}

data class Event(val id: Long?, val cellId: Long, val event: String)

data class EventData(val id: Long?, val cellId: Long, val param: String, val value: BigDecimal?)

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
        ).map { it.name to it }.toMap()
    }
}