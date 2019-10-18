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
import org.bernshtam.weather.TAFWrapper.getCloudsAlt
import org.bernshtam.weather.Utils.EARTH_RADIUS
import java.io.File
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*


object MainFromTabSeparated {
    private val dateFormat = SimpleDateFormat("dd/MM/yyy")

    val zone = "America/Anchorage" //darkSkyForZone["timezone"] as String
    val zoneId = ZoneId.of(zone)
    @JvmStatic
    fun main(args: Array<String>) {
        DB.migrate()
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

        DB.shutdown()
    }

    private fun processDate(date: String, lat: Double, long: Double, category: String) {
        val place = SolarTime.ofLocation(lat, long)
        val sunset = place.sunset()

        val sunsetMoment = getSunSetMoment(date, sunset)

        val pointAtSunset = getPoint(sunsetMoment, lat, long)
        val (cloudCover, pressure, visibility, alt) = getDataAtPoint(pointAtSunset)

        val pointAtCloudsPoint = getPointAfterMinutes(sunsetMoment, 5L, lat, long)
        val (cloudsAtCloudsPoint, pressureAtCloudsPoint, visibilityAtCloudsPoint,altAtCloudsPoint) = getDataAtPoint(pointAtCloudsPoint)

        val pointAtCloudsPoint2 = getPointAfterMinutes(sunsetMoment, 10L, lat, long)
        val (cloudsAtCloudsPoint2, pressureAtCloudsPoin2t, visibilityAtCloudsPoint2, altAtCloudsPoint2) = getDataAtPoint(pointAtCloudsPoint2)


        val pointAtSunsetHorizonClouds = getPointAfterKm(sunsetMoment,  lat, long,50.0)
        val (cloudsAtHorizonClouds, pressureAtHorizonClouds, visibilityAtHorizonClouds, altAtHorizonClouds) = getDataAtPoint(pointAtSunsetHorizonClouds)

        val pointAtSunsetHorizonClouds2 = getPointAfterKm(sunsetMoment,  lat, long, 100.0)
        val (cloudsAtHorizonClouds2, pressureAtHorizonClouds2, visibilityAtHorizonClouds2, altAtHorizonClouds2) = getDataAtPoint(pointAtSunsetHorizonClouds2)


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

        print("$category\t$date\t$cloudCover($alt)\t$cloudsAtHorizonClouds($altAtCloudsPoint)\t$cloudsAtHorizonClouds2($altAtCloudsPoint2)\t$cloudsAtCloudsPoint($altAtHorizonClouds)\t$cloudsAtCloudsPoint2($altAtHorizonClouds2)\t")
        // print("${diff(pressureCover,pressureAtCloudsPoint)}\t${diff(pressureAtCloudsPoint,pressureAtCloudsPoint2)}\t${diff(pressureCover,pressureAtCloudsPoint2)}")
        print("$sunsetPoints\t$aftersunsetPoints")
        println()
    }

    private fun getDataAtPoint(pointAtSunset: PointAtTime): DataAtPoint {
        val darkSkyJsonOfSunSet = DataSkyWrapper.get(pointAtSunset)
        val cloudCover = current(darkSkyJsonOfSunSet, "cloudCover") // in 0..1
        val pressureCover = current(darkSkyJsonOfSunSet, "pressure") // in 0..1
        val visibility = current(darkSkyJsonOfSunSet, "visibility") // in 0..1
        val alt = getCloudsAlt(pointAtSunset)
        return DataAtPoint(cloudCover,pressureCover,visibility,alt)
    }




    private fun getPointAfterKm(sunsetMoment: Moment, lat: Double, long: Double, km: Double): PointAtTime {
        val zonalDateTime = sunsetMoment.toLocalTimestamp().inLocalView()
        val pointOfClouds = EarthCalc.pointAt(Point.at(Coordinate.fromDegrees(lat), Coordinate.fromDegrees(long)),
                SunPosition.at(sunsetMoment, GeoLocation.of(lat, long)).azimuth, km * 1000.0)
        val pointAtTimeOfClouds = PointAtTime.at(pointOfClouds.latitude, pointOfClouds.longitude, zonalDateTime.toTemporalAccessor())
        return pointAtTimeOfClouds
    }


    private fun getPointAfterMinutes(sunsetMoment: Moment, minutesAfterSunset: Long, lat: Double, long: Double): PointAtTime {
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
        return pointAtTimeOfClouds
    }


    private fun getPoint(sunsetMoment: Moment, lat: Double, long: Double): PointAtTime {
        val zonalDateTime = sunsetMoment.toLocalTimestamp().inLocalView()
        val pointAtTime = PointAtTime.at(lat, long, zonalDateTime.toTemporalAccessor())
        return pointAtTime
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