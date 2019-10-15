package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import com.grum.geocalc.Coordinate
import com.grum.geocalc.EarthCalc
import com.grum.geocalc.Point
import net.time4j.Moment
import net.time4j.PlainDate
import net.time4j.SI
import net.time4j.calendar.astro.GeoLocation
import net.time4j.calendar.astro.SolarTime
import net.time4j.calendar.astro.SunPosition
import org.bernshtam.weather.Utils.EARTH_RADIUS
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*


object MainTest {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    private const val R = 6731.0 // km
    private const val deltaPosition = 10.0 // km


    //Rehovot
    val lat = 31.897852
    val long = 34.8089183
    val zone = "Asia/Jerusalem" //darkSkyForZone["timezone"] as String
    val zoneId = ZoneId.of(zone)
    val place = SolarTime.ofLocation(lat, long)
    val sunset = place.sunset()
    @JvmStatic
    fun main(args: Array<String>) {



        //val dates = listOf("2019-10-11", "2019-10-12", "2019-10-13").sorted()
         val dates = listOf("2019-06-22", "2017-12-12", "2018-02-15", "2019-01-12", "2017-12-27", "2016-02-11", "2019-10-05", "2018-12-16", "2018-03-18", "2019-10-10", "2019-10-11", "2019-10-12", "2019-10-13",
                 "2019-10-14","2019-10-15","2019-10-16","2019-10-17","2019-10-18","2019-10-19").sorted()
        //val dates = listOf("2019-10-08","2019-10-09","2019-10-10","2019-10-11","2019-10-12","2019-10-13","2019-10-14").sorted()
        println("Time\tClouds 0\tClouds 5 \tClouds 10\t pressure 0\tpressure 5\tpressure 10")
        dates.forEach { date ->
            processDate(date, lat, long)
        }
    }

    private fun processDate(date: String, lat: Double, long: Double) {
        val sunsetMoment = getSunSetMoment(date)
        // val pos = SunPosition.at(sunsetMoment, GeoLocation.of(lat, long))
        val (dateOfSunset, darkSkyJsonOfSunSet) = getDarkSkyJson(sunsetMoment, lat, long)
        val cloudCover = current(darkSkyJsonOfSunSet, "cloudCover") // in 0..1
        val pressureCover = current(darkSkyJsonOfSunSet, "pressure") // in 0..1
        //val dewPoint = Utils.getDoubleFromJson(current?.get("dewPoint")) // Fahrenheit
        //val temperature = Utils.getDoubleFromJson(current?.get("temperature")) // Fahrenheit
        //val h = if (dewPoint != null && temperature != null) 1000.0 * (temperature - dewPoint) / 4.4 / 3.281 else 0.0 //meters
        //val h2 = if (dewPoint != null && temperature != null) 122.0 * (F2C(temperature)!! - F2C(dewPoint)!!) else 0.0 //meters
       // val arcOfSunFromSunsetToNoVisibleToClouds = Math.acos(EARTH_RADIUS / (EARTH_RADIUS + h / 1000.0)) * 180.0 / Math.PI // in degrees
        val darkSkyAtCloudsPoint = getDarkSkyAfterMinutes(sunsetMoment, 5L, lat, long)
        val cloudsAtCloudsPoint = current(darkSkyAtCloudsPoint, "cloudCover")
        val pressureAtCloudsPoint = current(darkSkyAtCloudsPoint, "pressure")

        val darkSkyAtCloudsPoint2 = getDarkSkyAfterMinutes(sunsetMoment, 10L, lat, long)
        val cloudsAtCloudsPoint2 = current(darkSkyAtCloudsPoint2, "cloudCover")
        val pressureAtCloudsPoint2 = current(darkSkyAtCloudsPoint2, "pressure")

        println("$dateOfSunset\t$cloudCover\t$cloudsAtCloudsPoint\t$cloudsAtCloudsPoint2\t$pressureCover\t$pressureAtCloudsPoint\t$pressureAtCloudsPoint2")
    }

    private fun getDarkSkyAfterMinutes(sunsetMoment: Moment, minutesAfterSunset: Long, lat: Double, long: Double): JsonObject {
        val minsAfterSunsetMoment = sunsetMoment.plus(minutesAfterSunset * 60, SI.SECONDS)
        val minsAfterSunsetPosition = SunPosition.at(minsAfterSunsetMoment, GeoLocation.of(lat, long))
        val minsAfterZonalDateTime = minsAfterSunsetMoment.toLocalTimestamp().inLocalView()
        val minsAfterDateOfSunset = Date.from(minsAfterZonalDateTime.toTimestamp().toTemporalAccessor().atZone(zoneId).toInstant())
        //val heightToSeeAfterFeefteenMins = EARTH_RADIUS / (Math.cos(-minsAfterSunsetPosition.elevation * Math.PI / 180.0)) - EARTH_RADIUS
        val angelAfterNMinutes = minutesAfterSunset * 360.0 / (60 * 24.0)
        val distanceOfAngle = 2 * Math.PI * EARTH_RADIUS * angelAfterNMinutes / 360.0 * 1000.0
        val distanceToClouds = 2 * distanceOfAngle
        val pointOfClouds = EarthCalc.pointAt(Point.at(Coordinate.fromDegrees(lat), Coordinate.fromDegrees(long)),
                minsAfterSunsetPosition.azimuth, distanceToClouds)
        val pointAtTimeOfClouds = PointAtTime.at(pointOfClouds.latitude, pointOfClouds.longitude, minsAfterDateOfSunset)
        val darkSkyAtCloudsPoint = DataSkyWrapper.get(pointAtTimeOfClouds)
        return darkSkyAtCloudsPoint
    }

    private fun getDarkSkyJson(sunsetMoment: Moment, lat: Double, long: Double): Pair<Date, JsonObject> {
        val zonalDateTime = sunsetMoment.toLocalTimestamp().inLocalView()
        val dateOfSunset = Date.from(zonalDateTime.toTimestamp().toTemporalAccessor().atZone(zoneId).toInstant())
        val pointAtTime = PointAtTime.at(lat, long, dateOfSunset)
        val darkSkyJsonOfSunSet = DataSkyWrapper.get(pointAtTime)
        return Pair(dateOfSunset, darkSkyJsonOfSunSet)
    }

    private fun getSunSetMoment(date: String): Moment {
        val startOfDay = dateFormat.parse(date)
        //val darkSkyForZone = DataSkyWrapper.get(p)
        val toLocalDate = startOfDay.toInstant()
                .atZone(zoneId)
                .toLocalDate()
        val sunsetMoment = PlainDate.from(toLocalDate).get(sunset).get()
        return sunsetMoment
    }

    private fun current(darkSkyJsonOfSunSet: JsonObject, key: String): Double? {
        val current = darkSkyJsonOfSunSet["currently"] as JsonObject?
        val cloudCover = current?.get(key) // in 0..1
        return Utils.getDoubleFromJson(cloudCover)
    }

    fun r(s: Double): String {
        return String.format("%.2f", s)
    }
}