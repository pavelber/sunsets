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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

object SunSetService {

    private const val zone = "Asia/Jerusalem" //darkSkyForZone["timezone"] as String
    private val zoneId = ZoneId.of(zone)
    private const val PATTERN = "dd/MM/yyy"

    private val dateFormat = SimpleDateFormat(PATTERN)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern(PATTERN)

    fun getMarkAndDescription(lat: Double, long: Double): List<MarkAndDescription> {
        val today = LocalDate.now()
        return (0L..2L)
                .map { today.plus(it, ChronoUnit.DAYS) }
                .map { getMarkAndDescription(lat, long, it) }
    }

    fun getMarkAndDescription(lat: Double, long: Double, date: LocalDate): MarkAndDescription {
        val place = SolarTime.ofLocation(lat, long)
        val sunset = place.sunset()

        val sunsetMoment = getSunSetMoment(date, sunset)

        val points = mutableListOf<MarkAndDescription>()
        points.add(getCloudsNearHorizon(sunsetMoment, lat, long))
        points.add(getCloudsNearMe(sunsetMoment, lat, long))

        val result = points.reduce { acc,
                                     markAndDescription ->
            MarkAndDescription(date.format(dateTimeFormatter), acc.mark + markAndDescription.mark, acc.maxMark + markAndDescription.maxMark, reduceDescription(acc, markAndDescription))
        }
        return result
    }

    private fun reduceDescription(acc: MarkAndDescription, markAndDescription: MarkAndDescription) =
            reduceDescription(acc.description, markAndDescription.description)

    private fun reduceDescription(acc: String, markAndDescription: String) =
            if (acc.isBlank())
                if (markAndDescription.isBlank())
                    ""
                else markAndDescription
            else
                if (markAndDescription.isBlank())
                    acc
                else
                    "$acc $markAndDescription"

    private fun getCloudsNearMe(sunsetMoment: Moment, lat: Double, long: Double): MarkAndDescription {
        var points = 0
        val pointAtSunset = getPoint(sunsetMoment, lat, long)
        val (cloudCover, pressure, visibility, alt) = getDataAtPoint(pointAtSunset)

        val pointAtSunset10South = getPoint(sunsetMoment, lat - 0.1, long)
        val (cloudCover10South, pressure10South, visibility10South) = getDarkSkyDataAtPoint(pointAtSunset10South)

        val pointAtSunset10North = getPoint(sunsetMoment, lat + 0.1, long)
        val (cloudCover10North, pressure10North, visibility10North) = getDarkSkyDataAtPoint(pointAtSunset10North)

        val pointAtSunset10West = getPoint(sunsetMoment, lat, long - 0.1)
        val (cloudCover10West, pressure10West, visibility10West) = getDarkSkyDataAtPoint(pointAtSunset10West)

        if (cloudCover != null && cloudCover >= 0.2) points++
        if (cloudCover10South != null && cloudCover10South >= 0.2) points++
        if (cloudCover10North != null && cloudCover10North >= 0.2) points++
        if (cloudCover10West != null && cloudCover10West >= 0.2) points += 3

        var description = if (points > 0) "Clouds above you." else "Clear sky above you."

        if (points > 0) {// we have clouds nearby
            val point5Min = getPointAfterMinutes(sunsetMoment, 5L, lat, long)
            val point10Min = getPointAfterMinutes(sunsetMoment, 10L, lat, long)
            val (cloudCover5Min, pressure5Min, visibility5Min) = getDarkSkyDataAtPoint(point5Min)
            val (cloudCover10Min, pressure10Min, visibility10Min) = getDarkSkyDataAtPoint(point10Min)
            var cloudPoints = 0
            if (cloudCover5Min != null && cloudCover5Min <= 0.2) cloudPoints++
             if (cloudCover10Min != null && cloudCover10Min <= 0.2) cloudPoints++

            if (alt != null && alt > 5000 && cloudPoints > 0) {
                points += cloudPoints
                description = reduceDescription(description, "A chance for good light on clouds after sunset.")
            }
        }

        return MarkAndDescription("", points, 8, description)
    }

    private fun getCloudsNearHorizon(sunsetMoment: Moment, lat: Double, long: Double): MarkAndDescription {
        var points = 0
        val pointAtSunsetHorizonClouds = getPointAfterKm(sunsetMoment, lat, long, 50.0)
        val (cloudsAtHorizonClouds, pressureAtHorizonClouds, visibilityAtHorizonClouds) = getDarkSkyDataAtPoint(pointAtSunsetHorizonClouds)

        if (cloudsAtHorizonClouds != null && cloudsAtHorizonClouds >= 0.2 && cloudsAtHorizonClouds <= 0.7) points += 2

        val pointAtSunsetHorizonClouds2 = getPointAfterKm(sunsetMoment, lat, long, 100.0)
        val (cloudsAtHorizonClouds2, pressureAtHorizonClouds2, visibilityAtHorizonClouds2) = getDarkSkyDataAtPoint(pointAtSunsetHorizonClouds2)

        if (cloudsAtHorizonClouds2 != null && cloudsAtHorizonClouds2 >= 0.2 && cloudsAtHorizonClouds2 <= 0.7) points += 2

        val description = if (points > 0) "Nice clouds near horizon." else ""

        return MarkAndDescription("", points, 4, description)
    }

    private fun getDataAtPoint(pointAtSunset: PointAtTime): DataAtPoint {
        val (cloudCover, pressure, visibility) = getDarkSkyDataAtPoint(pointAtSunset)

        val alt = TAFWrapper.getCloudsAlt(pointAtSunset)
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
                SunPosition.at(sunsetMoment, GeoLocation.of(lat, long)).azimuth, km * Utils.METERS_IN_KM)
        val pointAtTimeOfClouds = PointAtTime.at(pointOfClouds.latitude, pointOfClouds.longitude, zonalDateTime.toTemporalAccessor())
        return pointAtTimeOfClouds
    }


    private fun getPointAfterMinutes(sunsetMoment: Moment, minutesAfterSunset: Long, lat: Double, long: Double): PointAtTime {
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


    private fun getPoint(sunsetMoment: Moment, lat: Double, long: Double): PointAtTime {
        val zonalDateTime = sunsetMoment.toLocalTimestamp().inLocalView()
        val pointAtTime = PointAtTime.at(lat, long, zonalDateTime.toTemporalAccessor())
        return pointAtTime
    }

    private fun getSunSetMoment(date: LocalDate, sunset: ChronoFunction<CalendarDate, Optional<Moment>>): Moment {
        val sunsetMoment = PlainDate.from(date).get(sunset).get()
        return sunsetMoment
    }

    private fun current(darkSkyJsonOfSunSet: JsonObject, key: String): Double? {
        val current = darkSkyJsonOfSunSet["currently"] as JsonObject?
        val cloudCover = current?.get(key) // in 0..1
        return Utils.getDoubleFromJson(cloudCover)
    }

}