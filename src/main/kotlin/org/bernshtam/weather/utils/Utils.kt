package org.bernshtam.weather.utils

import com.grum.geocalc.Coordinate
import com.grum.geocalc.EarthCalc
import com.grum.geocalc.Point
import com.sun.org.apache.xalan.internal.lib.ExsltMath
import net.time4j.Moment
import net.time4j.PlainDate
import net.time4j.SI
import net.time4j.ZonalDateTime
import net.time4j.calendar.astro.GeoLocation
import net.time4j.calendar.astro.SolarTime
import net.time4j.calendar.astro.SunPosition
import net.time4j.engine.CalendarDate
import net.time4j.engine.ChronoFunction
import org.bernshtam.weather.LAT_START
import org.bernshtam.weather.LONG_START
import org.bernshtam.weather.PointAtTime
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

object Utils {

    const val EARTH_RADIUS = 6731.0 // km
    private const val PRECISION = 0.01

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

    fun getTodayOrTommorrowDependsOnTimeNow():LocalDate {
        val sunsetMomentToday = Utils.getSunsetMoment(LAT_START, LONG_START,  LocalDate.now())
        val sunsetMomentZonalDateTime = sunsetMomentToday.toLocalTimestamp().inLocalView()
        val now = ZonalDateTime.from(ZonedDateTime.now())
        val day = if (sunsetMomentZonalDateTime.compareByLocalTimestamp(now)>1) LocalDate.now() else LocalDate.now().plusDays(1)
        return day
    }

    fun getSunsetMoment(lat: Double, long: Double, date: LocalDate): Moment {
        val place = SolarTime.ofLocation(lat, long)
        val sunset = place.sunset()

        val sunsetMoment = getSunSetMoment(date, sunset)
        return sunsetMoment
    }

    private fun getSunSetMoment(date: LocalDate, sunset: ChronoFunction<CalendarDate, Optional<Moment>>): Moment {
        val sunsetMoment = PlainDate.from(date).get(sunset).get()
        return sunsetMoment
    }

    fun getPointAfterKm(sunsetMoment: Moment, lat: Double, long: Double, km: Double): PointAtTime {
        val zonalDateTime = sunsetMoment.toLocalTimestamp().inLocalView()
        val pointOfClouds = EarthCalc.pointAt(Point.at(Coordinate.fromDegrees(lat), Coordinate.fromDegrees(long)),
                SunPosition.at(sunsetMoment, GeoLocation.of(lat, long)).azimuth, km * Utils.METERS_IN_KM)
        val pointAtTimeOfClouds = PointAtTime.at(pointOfClouds.latitude, pointOfClouds.longitude, zonalDateTime.toTemporalAccessor())
        return pointAtTimeOfClouds
    }


    fun getPointAfterMinutes(sunsetMoment: Moment, minutesAfterSunset: Long, lat: Double, long: Double): PointAtTime {
        val minsAfterSunsetMoment = sunsetMoment.plus(minutesAfterSunset * Utils.SECONDS_PER_MIN, SI.SECONDS)
        val minsAfterSunsetPosition = SunPosition.at(minsAfterSunsetMoment, GeoLocation.of(lat, long))
        val minsAfterZonalDateTime = minsAfterSunsetMoment.toLocalTimestamp().inLocalView()
        //val heightToSeeAfterFeefteenMins = EARTH_RADIUS / (Math.cos(-minsAfterSunsetPosition.elevation * Math.PI / 180.0)) - EARTH_RADIUS
        val angelAfterNMinutes = minutesAfterSunset * Utils.DEGREES / (Utils.MINUTES_PER_DAY)
        val distanceOfAngle = 2 * Math.PI * Utils.EARTH_RADIUS * angelAfterNMinutes / Utils.DEGREES * Utils.METERS_IN_KM
        val distanceToClouds = 2 * distanceOfAngle
        //println(minutesAfterSunset.toString()+" "+distanceToClouds)
        val pointOfClouds = EarthCalc.pointAt(Point.at(Coordinate.fromDegrees(lat), Coordinate.fromDegrees(long)),
                minsAfterSunsetPosition.azimuth, distanceToClouds)
        val pointAtTimeOfClouds = PointAtTime.at(pointOfClouds.latitude, pointOfClouds.longitude, minsAfterZonalDateTime.toTemporalAccessor())
        return pointAtTimeOfClouds
    }


    fun getPoint(sunsetMoment: Moment, lat: Double, long: Double): PointAtTime {
        val zonalDateTime = sunsetMoment.toLocalTimestamp().inLocalView()
        val pointAtTime = PointAtTime.at(lat, long, zonalDateTime.toTemporalAccessor())
        return pointAtTime
    }

    fun Double.eq(a: Double) = ExsltMath.abs(this - a) < PRECISION
}