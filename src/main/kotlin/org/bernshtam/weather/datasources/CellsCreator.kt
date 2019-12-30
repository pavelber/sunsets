package org.bernshtam.weather.datasources

import mt.edu.um.cf2.jgribx.GribFile
import org.bernshtam.weather.Cell
import org.bernshtam.weather.DB
import org.bernshtam.weather.utils.Utils
import java.io.FileInputStream
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

object CellsCreator {

    private const val latStart = 29.0
    private const val latEnd = 34.0
    private const val longStart = 34.0
    private const val longEnd = 36.0
    private const val cellSize = 0.1
    private const val AVERAGE_WEST_FACTOR = 3
    private const val AVERAGE_RADIUS = cellSize

    fun recalclulate() {
        val gribFiles = openGribFiles()
        val today = LocalDate.now()
        (0L..2L).map { today.plus(it, ChronoUnit.DAYS) }
                .forEach { date ->
                    var lat = latStart+ AVERAGE_RADIUS
                    while (lat < latEnd) {
                        var long = longStart + AVERAGE_RADIUS
                        while (long < longEnd) {
                            val moment = Utils.getSunsetMoment(lat, long, date)
                            val zonalDateTime = moment.toLocalTimestamp().inLocalView()
                            val calendar = GregorianCalendar.from(zonalDateTime.toTemporalAccessor())
                            val low = getAverageValue(gribFiles.getValue(IMSConstants.LOW_CLOUDS_PARAM), calendar, IMSConstants.LOW_CLOUDS_PARAM_FILE, lat, long)
                            val medium = getAverageValue(gribFiles.getValue(IMSConstants.MEDIUM_CLOUDS_PARAM), calendar, IMSConstants.MEDIUM_CLOUDS_PARAM_FILE, lat, long)
                            val high = getAverageValue(gribFiles.getValue(IMSConstants.HIGH_CLOUDS_PARAM), calendar, IMSConstants.HIGH_CLOUDS_PARAM_FILE, lat, long)
                            val cell = Cell(date, cellSize, lat, long, low, medium, high, 0.0, 0.0, 0.0, 0.0)
                            try {
                                DB.saveCell(cell)
                                println("Saved $lat $long")
                            } catch (e:Exception){
                                e.printStackTrace()
                            }
                            long += AVERAGE_RADIUS
                        }
                        lat += AVERAGE_RADIUS
                    }

                }
    }


    private fun getAverageValue(gribFile: GribFile, calendar: GregorianCalendar, param: String, lat: Double, long: Double): Double {
        val record = gribFile.getRecord(calendar, param, "SFC")
        val here = record.getValue(lat, long)
        val north = record.getValue(lat + AVERAGE_RADIUS/2, long)
        val south = record.getValue(lat - AVERAGE_RADIUS/2, long)
        val west = record.getValue(lat, long - AVERAGE_RADIUS/2)
        return (here + north + south + AVERAGE_WEST_FACTOR * west) / (3 + AVERAGE_WEST_FACTOR)
    }

    fun openGribFiles(): Map<String, GribFile> {
        return IMSConstants.params.map { it.first to GribFile(FileInputStream(getFile(it.first))) }.toMap()

    }


    private fun getFile(s: String) =
            IMSConstants.downloadDir.listFiles { f -> f.name.endsWith("$s.grb") }.minBy { it.lastModified() }!!
}