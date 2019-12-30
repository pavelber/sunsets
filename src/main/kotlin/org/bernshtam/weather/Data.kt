package org.bernshtam.weather

import org.bernshtam.weather.utils.Utils
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

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

data class DataAtPoint(val imsClouds: IMSClouds, val pressure: Double?, val visibility: Double?)
data class DarkSkyDataAtPoint(val cloudCover: Double?, val pressure: Double?, val visibility: Double?)
data class MarkAndDescription(val date: String, val mark: Int, val maxMark: Int, val description: String)
data class IMSClouds(val high: Double, val medium: Double, val low: Double)

data class Cell(val date: LocalDate,
                val square_size: Double,
                val latitude: Double,
                val longitude: Double,
                val low: Double,
                val medium: Double,
                val high: Double,
                val sunset_near: Double,
                val sunset_far: Double,
                val sun_blocking_5: Double,
                val sun_blocking_10: Double
)