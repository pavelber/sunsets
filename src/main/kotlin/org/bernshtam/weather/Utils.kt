package org.bernshtam.weather

object Utils {

     const val EARTH_RADIUS = 6731.0 // km

    /**
     * Hours per day.
     */
    const val HOURS_PER_DAY = 24
    /**
     * Minutes per hour.
     */
    const val MINUTES_PER_HOUR = 60

    const val MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY

    const val DEGREES = 360.0
    const val METERS_IN_KM = 1000.0
    const val SECONDS_PER_MIN = 60

    fun distanceToDegree(d: Double): Double {
        return d / 111.139
    }

    fun egreeToDistance(d: Double): Double {
        return d * 111.139
    }

    fun roundToHalf(d: Double): Double {
        return Math.round(d * 2) / 2.0
    }

    fun F2C(v: Any?): Double? {
        val f = getDoubleFromJson(v)
        return if (f == null) null else (f - 32) / 1.8
    }

    fun getDoubleFromJson(cStr: Any?) =
            when (cStr) {
                is Double -> cStr
                is Int -> cStr.toDouble()
                null -> null
                else -> cStr.toString().toDouble()
            }
}