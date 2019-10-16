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
import net.time4j.engine.CalendarDate
import net.time4j.engine.ChronoFunction
import org.bernshtam.weather.Utils.EARTH_RADIUS
import java.io.File
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*


object MainFromTabSeparated {
    private val dateFormat = SimpleDateFormat("dd/MM/yyy")
    private const val R = 6731.0 // km
    private const val deltaPosition = 10.0 // km


    //Rehovot
    //val lat = 31.897852
    //val long = 34.8089183
    val zone = "Asia/Jerusalem" //darkSkyForZone["timezone"] as String
    val zoneId = ZoneId.of(zone)
    @JvmStatic
    fun main(args: Array<String>) {

        val file = File("c:\\temp\\sunsets2.txt")
        val lines = file.readLines()
        val splited = lines.map { it.split("\t") }.sortedBy { it[2] }
        splited.forEach { line ->
            val date = line[0]
            val latlon = line[1].split(",")
            val lat = latlon[0].toDouble()
            val long = latlon[1].toDouble()
            val category = line[2]
            processDate(date, lat, long, category)
        }
    }

    private fun processDate(date: String, lat: Double, long: Double, category: String) {
        val place = SolarTime.ofLocation(lat, long)
        val sunset = place.sunset()

        val sunsetMoment = getSunSetMoment(date, sunset)
        // val pos = SunPosition.at(sunsetMoment, GeoLocation.of(lat, long))
        val darkSkyJsonOfSunSet = getDarkSkyJson(sunsetMoment, lat, long)
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

        val darkSkyAtSunsetHorizonClouds = getDarkSkyAfterKm(sunsetMoment, 50.0, lat, long)
        val cloudsAtHorizonClouds = current(darkSkyAtSunsetHorizonClouds, "cloudCover")
        val darkSkyAtSunsetHorizonClouds2 = getDarkSkyAfterKm(sunsetMoment, 100.0, lat, long)
        val cloudsAtHorizonClouds2 = current(darkSkyAtSunsetHorizonClouds2, "cloudCover")

        var sunsetPoints = 0
        if (cloudCover != null && cloudCover >= 0.2) sunsetPoints++
        if (cloudsAtHorizonClouds != null && cloudsAtHorizonClouds >= 0.2 && cloudsAtHorizonClouds <= 0.7) sunsetPoints++
        if (cloudsAtHorizonClouds2 != null && cloudsAtHorizonClouds2 >= 0.2 && cloudsAtHorizonClouds2 <= 0.7) sunsetPoints++
        if (cloudsAtHorizonClouds != null && cloudsAtHorizonClouds >= 0.7 &&
                cloudsAtHorizonClouds2 != null && cloudsAtHorizonClouds2 <= 0.3) sunsetPoints++

        var aftersunsetPoints = 0
        if (cloudCover != null && cloudCover >= 0.2) aftersunsetPoints++
        if (cloudsAtCloudsPoint != null && cloudsAtCloudsPoint <= 0.3) aftersunsetPoints++
        if (cloudsAtCloudsPoint2 != null && cloudsAtCloudsPoint2 <= 0.3) aftersunsetPoints++

        print("$category\t$date\t$cloudCover\t$cloudsAtHorizonClouds\t$cloudsAtHorizonClouds2\t$cloudsAtCloudsPoint\t$cloudsAtCloudsPoint2\t")
        // print("${diff(pressureCover,pressureAtCloudsPoint)}\t${diff(pressureAtCloudsPoint,pressureAtCloudsPoint2)}\t${diff(pressureCover,pressureAtCloudsPoint2)}")
        print("$sunsetPoints\t$aftersunsetPoints")
        println()
    }

    private fun diff(d1: Double?, d2: Double?): Double? {
        if (d1 == null || d2 == null) return null
        return Math.floor(d2 - d1)
    }

    private fun getDarkSkyAfterKm(sunsetMoment: Moment, km: Double, lat: Double, long: Double): JsonObject {
        val zonalDateTime = sunsetMoment.toLocalTimestamp().inLocalView()
        val pointOfClouds = EarthCalc.pointAt(Point.at(Coordinate.fromDegrees(lat), Coordinate.fromDegrees(long)),
                SunPosition.at(sunsetMoment, GeoLocation.of(lat, long)).azimuth, km * 1000.0)
        val pointAtTimeOfClouds = PointAtTime.at(pointOfClouds.latitude, pointOfClouds.longitude, zonalDateTime.toTemporalAccessor())
        val darkSkyAtCloudsPoint = DataSkyWrapper.get(pointAtTimeOfClouds)
        return darkSkyAtCloudsPoint
    }

    private fun getDarkSkyAfterMinutes(sunsetMoment: Moment, minutesAfterSunset: Long, lat: Double, long: Double): JsonObject {
        val minsAfterSunsetMoment = sunsetMoment.plus(minutesAfterSunset * 60, SI.SECONDS)
        val minsAfterSunsetPosition = SunPosition.at(minsAfterSunsetMoment, GeoLocation.of(lat, long))
        val minsAfterZonalDateTime = minsAfterSunsetMoment.toLocalTimestamp().inLocalView()
        //val heightToSeeAfterFeefteenMins = EARTH_RADIUS / (Math.cos(-minsAfterSunsetPosition.elevation * Math.PI / 180.0)) - EARTH_RADIUS
        val angelAfterNMinutes = minutesAfterSunset * 360.0 / (60 * 24.0)
        val distanceOfAngle = 2 * Math.PI * EARTH_RADIUS * angelAfterNMinutes / 360.0 * 1000.0
        val distanceToClouds = 2 * distanceOfAngle
        //println(minutesAfterSunset.toString()+" "+distanceToClouds)
        val pointOfClouds = EarthCalc.pointAt(Point.at(Coordinate.fromDegrees(lat), Coordinate.fromDegrees(long)),
                minsAfterSunsetPosition.azimuth, distanceToClouds)
        val pointAtTimeOfClouds = PointAtTime.at(pointOfClouds.latitude, pointOfClouds.longitude, minsAfterZonalDateTime.toTemporalAccessor())
        val darkSkyAtCloudsPoint = DataSkyWrapper.get(pointAtTimeOfClouds)
        return darkSkyAtCloudsPoint
    }

    private fun getDarkSkyJson(sunsetMoment: Moment, lat: Double, long: Double): JsonObject {
        val zonalDateTime = sunsetMoment.toLocalTimestamp().inLocalView()
        val pointAtTime = PointAtTime.at(lat, long, zonalDateTime.toTemporalAccessor())
        val darkSkyJsonOfSunSet = DataSkyWrapper.get(pointAtTime)
        return darkSkyJsonOfSunSet
    }

    private fun getSunSetMoment(date: String, sunset: ChronoFunction<CalendarDate, Optional<Moment>>): Moment {
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