package org.bernshtam.weather

import com.beust.klaxon.JsonObject
import mt.edu.um.cf2.jgribx.GribFile
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object IMSConnector {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    val HIGH_CLOUDS_PARAM = "HCDC"
    val MEDIUM_CLOUDS_PARAM = "MCDC"
    val LOW_CLOUDS_PARAM = "LCDC"

    private val params = listOf(Pair("CLCH", HIGH_CLOUDS_PARAM), Pair("CLCM", MEDIUM_CLOUDS_PARAM), Pair("CLCL", LOW_CLOUDS_PARAM))

    @JvmStatic
    fun main(args: Array<String>) {


        val time = ZonedDateTime.of(2019, 11, 27, 16, 30, 0, 0, ZoneId.systemDefault())
        val ta = PointAtTime.at(32.0853, 34.78, time)
        val haifa = PointAtTime.at(32.79, 34.98, time)
        println(getCloudsParams(haifa).toJsonString(prettyPrint = true))
    }

    fun getCloudsParams(pointAtTime: PointAtTime):JsonObject {
        val paramMap = params.map { p ->
            val value: Double = getValue(pointAtTime, p)
            p.second to value
        }.toMap()

        return JsonObject(paramMap)
    }

    private fun getValue(pointAtTime: PointAtTime, param: Pair<String, String>): Double {
        val fileDate = LocalDate.now()
        val yesterday = fileDate.minusDays(1)
        val dateStr = fileDate.format(dateTimeFormatter)
        val yesterdayStr = yesterday.format(dateTimeFormatter)
        val paramFileName = param.first
        val paramName = param.second
        val patterns = listOf(
                "C3_${dateStr}12_$paramFileName.grb",
                "C3_${dateStr}00_$paramFileName.grb",
                "C3_${yesterdayStr}12_$paramFileName.grb",
                "C3_${yesterdayStr}00_$paramFileName.grb"
        )
        for (pattern in patterns) {
            try {
                val stream = IMSStreamProvider.get(pattern)
                stream.use {
                    val file = GribFile(it)
                    val record = file.getRecord(GregorianCalendar.from(pointAtTime.time), paramName, "SFC")
                    return record.getValue(pointAtTime.lat, pointAtTime.long)
                }

            } catch (e: Exception) {
                println(e.toString())
                continue;
            }
        }
        throw IllegalArgumentException("Can't find a file")
    }
}