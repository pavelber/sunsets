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
import org.bernshtam.weather.datasources.DataSkyWrapper
import org.bernshtam.weather.datasources.IMSConnector
import org.bernshtam.weather.utils.Utils
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.roundToInt

class SunSetService() {

    companion object {
        private const val zone = "Asia/Jerusalem" //darkSkyForZone["timezone"] as String
        private val zoneId = ZoneId.of(zone)
        private const val PATTERN = "dd/MM/yyy"

        private val dateFormat = SimpleDateFormat(PATTERN)
        private val dateTimeFormatter = DateTimeFormatter.ofPattern(PATTERN)
    }

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
        val normalization = 100.0 / result.maxMark
        val normalizedMark = (result.mark * normalization).roundToInt()
        return MarkAndDescription(result.date, normalizedMark, 100, result.description)
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

        val pointAtSunset = getPoint(sunsetMoment, lat, long)
        val (cloudCover, pressure, visibility) = getDataAtPoint(pointAtSunset)

        val pointAtSunset10South = getPoint(sunsetMoment, lat - 0.1, long)
        val (cloudCover10South, pressure10South, visibility10South) = getDataAtPoint(pointAtSunset10South)

        val pointAtSunset10North = getPoint(sunsetMoment, lat + 0.1, long)
        val (cloudCover10North, pressure10North, visibility10North) = getDataAtPoint(pointAtSunset10North)

        val pointAtSunset10West = getPoint(sunsetMoment, lat, long - 0.1)
        val (cloudCover10West, pressure10West, visibility10West) = getDataAtPoint(pointAtSunset10West)

        val point5Min = getPointAfterMinutes(sunsetMoment, 5L, lat, long)
        val point10Min = getPointAfterMinutes(sunsetMoment, 10L, lat, long)
        val (cloudCover5Min, pressure5Min, visibility5Min) = getDarkSkyDataAtPoint(point5Min)
        val (cloudCover10Min, pressure10Min, visibility10Min) = getDarkSkyDataAtPoint(point10Min)

        val low = (cloudCover.low + cloudCover10South.low + cloudCover10North.low + cloudCover10West.low * 3) / 5.0
        val medium = (cloudCover.medium + cloudCover10South.medium + cloudCover10North.medium + cloudCover10West.medium * 3) / 5.0
        val high = (cloudCover.high + cloudCover10South.high + cloudCover10North.high + cloudCover10West.high * 3) / 5.0

        val coefFrom5MinsLighting = if (cloudCover5Min == null || cloudCover5Min > 0.6) 0 else if (cloudCover5Min > 0.3) 1 else 2
        val coefFrom10MinsLighting = if (cloudCover10Min == null || cloudCover10Min > 0.6) 0 else if (cloudCover10Min > 0.3) 1 else 2
        val coefFromLighting = coefFrom5MinsLighting + coefFrom10MinsLighting

        val highClouds = low < 0.5 && medium < 0.5 && high > 0.2
        val notLowClouds = low < 0.5 && (medium > 0.0 || high > 0.0)
        val lightDescription = when {
            coefFromLighting > 2 && highClouds -> "An excellent chance for good light on clouds after sunset."
            coefFromLighting > 0 && notLowClouds -> "A chance for good light on clouds after sunset."
            else -> ""

        }
        val cloudsDescriptions = "Low clouds: ${(100 * low).toInt()}. Medium clouds: ${(100 * medium).toInt()}. High clouds: ${(100 * high).toInt()}."
        val description = "$cloudsDescriptions $lightDescription"

        val max = 16
        if (low > 0.5) return MarkAndDescription("", 0, max, description)
        else {

            if (medium > 0.5) return MarkAndDescription("", 2 * coefFromLighting, max, description)
            else {

                if (high > 0.5) return MarkAndDescription("", 4 * coefFromLighting, max, description)
                else {
                    if (high > 0.2) return MarkAndDescription("", 3 * coefFromLighting, max, description)
                    else if (high == 0.0) return MarkAndDescription("", (if (medium > 0.2) 1 else 0) * coefFromLighting, max, description)
                    else {
                        return MarkAndDescription("", 2 * coefFromLighting, max, description)
                    }
                }
            }
        }
    }


    private fun getCloudsNearHorizon(sunsetMoment: Moment, lat: Double, long: Double): MarkAndDescription {
        var points = 0
        val pointAtSunsetHorizonClouds = getPointAfterKm(sunsetMoment, lat, long, 50.0)
        val (cloudsAtHorizonClouds, pressureAtHorizonClouds, visibilityAtHorizonClouds) = getDarkSkyDataAtPoint(pointAtSunsetHorizonClouds)
        var description = ""
        if (cloudsAtHorizonClouds != null)
            when {
                cloudsAtHorizonClouds < 0.2 -> description += " A little or no clouds on the west."
                cloudsAtHorizonClouds in 0.2..0.7 -> {
                    points += 4;description += " Clouds on the west."
                }
                else -> {
                    points += 2; description += " Heavy clouds on the west."
                }
            }

        val pointAtSunsetHorizonClouds2 = getPointAfterKm(sunsetMoment, lat, long, 100.0)
        val (cloudsAtHorizonClouds2, pressureAtHorizonClouds2, visibilityAtHorizonClouds2) = getDarkSkyDataAtPoint(pointAtSunsetHorizonClouds2)

        if (cloudsAtHorizonClouds2 != null) when {
            cloudsAtHorizonClouds2 == 0.0 -> {
                points += 6; description += " Clear sky on the sunset point."
            }
            cloudsAtHorizonClouds2 in 0.2..0.7 -> {
                points += 2; description += " Clouds on the sunset point."
            }
            else -> {
                description += " Heavy clouds on the sunset point."
            }

        }
        val FACTOR = 1

        return MarkAndDescription("", FACTOR * points, FACTOR * 8, description)
    }

    private fun getDataAtPoint(pointAtTime: PointAtTime): DataAtPoint {
        val (cloudCover, pressure, visibility) = getDarkSkyDataAtPoint(pointAtTime)

        val clouds = IMSConnector.getCloudsParams(pointAtTime)
        return DataAtPoint(clouds, pressure, visibility)
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