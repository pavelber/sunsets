package org.bernshtam.weather

import java.util.*

@Suppress("DataClassPrivateConstructor")
data class PointAtTime private constructor(val lat: Double, val long: Double, val time: Long) {
    companion object {
        fun at(lat: Double, long: Double, time: Long): PointAtTime {
            val latRounded = Utils.roundToHalf(lat * 10.0) / 10.0
            val longRounded = Utils.roundToHalf(long * 10.0) / 10.0
            val timeRounded = (time / 100) * 100
            return PointAtTime(latRounded, longRounded, timeRounded)
        }

        fun at(lat: Double, long: Double, d: Date): PointAtTime {
            val time = d.time / 1000
            return at(lat, long, time)
        }
    }
}