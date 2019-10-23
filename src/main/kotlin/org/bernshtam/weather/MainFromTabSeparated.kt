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
import org.bernshtam.weather.Utils.DEGREES
import org.bernshtam.weather.Utils.EARTH_RADIUS
import org.bernshtam.weather.Utils.METERS_IN_KM
import org.bernshtam.weather.Utils.MINUTES_PER_DAY
import org.bernshtam.weather.Utils.SECONDS_PER_MIN
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

        val points = mutableListOf<MarkAndDescription>()
        points.add(getCloudsNearHorizon(sunsetMoment,lat,long))
        points.add(getCloudsNearMe(sunsetMoment,lat,long))

        println("$category\t$date\t\t")

        println()
    }
    private fun getCloudsNearMe(sunsetMoment: Moment, lat: Double, long: Double): MarkAndDescription {
        var points = 0
        val pointAtSunset = getPoint(sunsetMoment, lat, long)
        val (cloudCover, pressure, visibility, alt) = getDataAtPoint(pointAtSunset)

        val pointAtSunset10South = getPoint(sunsetMoment, lat-0.1, long)
        val (cloudCover10South, pressure10South, visibility10South, alt10South) = getDataAtPoint(pointAtSunset10South)

        val pointAtSunset10North = getPoint(sunsetMoment, lat+0.1, long)
        val (cloudCover10North, pressure10North, visibility10North, alt10North) = getDataAtPoint(pointAtSunset10North)

        val pointAtSunset10West = getPoint(sunsetMoment, lat, long-0.1)
        val (cloudCover10West, pressure10West, visibility10West, alt10West) = getDataAtPoint(pointAtSunset10West)

        if (cloudCover != null && cloudCover >= 0.2) points++
        if (cloudCover10South != null && cloudCover10South >= 0.2) points++
        if (cloudCover10North != null && cloudCover10North >= 0.2) points++
        if (cloudCover10West != null && cloudCover10West >= 0.2) points+=3

        val description = if (points>0) "Clouds above you." else ""

        return MarkAndDescription(points,description)
    }
    private fun getCloudsNearHorizon(sunsetMoment: Moment, lat: Double, long: Double): MarkAndDescription {
        var points = 0
        val pointAtSunsetHorizonClouds = getPointAfterKm(sunsetMoment, lat, long, 50.0)
        val (cloudsAtHorizonClouds, pressureAtHorizonClouds, visibilityAtHorizonClouds) = getDarkSkyDataAtPoint(pointAtSunsetHorizonClouds)

        if (cloudsAtHorizonClouds != null && cloudsAtHorizonClouds >= 0.2 && cloudsAtHorizonClouds <= 0.7) points++

        val pointAtSunsetHorizonClouds2 = getPointAfterKm(sunsetMoment, lat, long, 100.0)
        val (cloudsAtHorizonClouds2, pressureAtHorizonClouds2, visibilityAtHorizonClouds2) = getDarkSkyDataAtPoint(pointAtSunsetHorizonClouds2)

        if (cloudsAtHorizonClouds2 != null && cloudsAtHorizonClouds2 >= 0.2 && cloudsAtHorizonClouds2 <= 0.7) points++

        val description = if (points>0) "Nice clouds near horizon." else ""

        return MarkAndDescription(points,description)
    }

    private fun getDataAtPoint(pointAtSunset: PointAtTime): DataAtPoint {
        val (cloudCover, pressure, visibility) = getDarkSkyDataAtPoint(pointAtSunset)

        val alt = getCloudsAlt(pointAtSunset)
        return DataAtPoint(cloudCover, pressure, visibility, alt)
    }


    private fun getDarkSkyDataAtPoint(pointAtSunset: PointAtTime): DarkSkyDataAtPoint {
        val darkSkyJsonOfSunSet = DataSkyWrapper.get(pointAtSunset)
        val cloudCover = current(darkSkyJsonOfSunSet, "cloudCover") // in 0..1
        val pressureCover = current(darkSkyJsonOfSunSet, "pressure") // in 0..1
        val visibility = current(darkSkyJsonOfSunSet, "visibility") // in 0..1
        return DarkSkyDataAtPoint(cloudCover, pressureCover, visibility)
    }


    private fun getPointAfterKm(sunsetMoment: Moment, lat: Double, long: Double, km: Double): PointAtTime {
        val zonalDateTime = sunsetMoment.toLocalTimestamp().inLocalView()
        val pointOfClouds = EarthCalc.pointAt(Point.at(Coordinate.fromDegrees(lat), Coordinate.fromDegrees(long)),
                SunPosition.at(sunsetMoment, GeoLocation.of(lat, long)).azimuth, km * METERS_IN_KM)
        val pointAtTimeOfClouds = PointAtTime.at(pointOfClouds.latitude, pointOfClouds.longitude, zonalDateTime.toTemporalAccessor())
        return pointAtTimeOfClouds
    }


    private fun getPointAfterMinutes(sunsetMoment: Moment, minutesAfterSunset: Long, lat: Double, long: Double): PointAtTime {
        val minsAfterSunsetMoment = sunsetMoment.plus(minutesAfterSunset * SECONDS_PER_MIN, SI.SECONDS)
        val minsAfterSunsetPosition = SunPosition.at(minsAfterSunsetMoment, GeoLocation.of(lat, long))
        val minsAfterZonalDateTime = minsAfterSunsetMoment.toLocalTimestamp().inLocalView()
        //val heightToSeeAfterFeefteenMins = EARTH_RADIUS / (Math.cos(-minsAfterSunsetPosition.elevation * Math.PI / 180.0)) - EARTH_RADIUS
        val angelAfterNMinutes = minutesAfterSunset * DEGREES / (MINUTES_PER_DAY)
        val distanceOfAngle = 2 * Math.PI * EARTH_RADIUS * angelAfterNMinutes / DEGREES * METERS_IN_KM
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