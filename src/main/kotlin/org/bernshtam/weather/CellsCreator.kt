package org.bernshtam.weather

import mt.edu.um.cf2.jgribx.GribFile
import org.bernshtam.weather.dal.CellDAL.saveLocalCloudsCell
import org.bernshtam.weather.datasources.IMSConstants
import org.bernshtam.weather.utils.Utils
import org.bernshtam.weather.utils.Utils.getTodayOrTommorrowDependsOnTimeNow
import java.io.FileInputStream
import java.time.temporal.ChronoUnit
import java.util.*

object CellsCreator {

    private const val AVERAGE_WEST_FACTOR = 3


    fun recalclulate() {
        val gribFiles = openGribFiles()

        val startDay = getTodayOrTommorrowDependsOnTimeNow()
        (0L..2L).map { startDay.plus(it, ChronoUnit.DAYS) }
                .forEach { date ->
                    var lat = LAT_START + CELL_SIZE
                    while (lat < LAT_END) {
                        var long = LONG_START + CELL_SIZE
                        while (long < LONG_END) {
                            val moment = Utils.getSunsetMoment(lat, long, date)
                            val zonalDateTime = moment.toLocalTimestamp().inLocalView()
                            val calendar = GregorianCalendar.from(zonalDateTime.toTemporalAccessor())
                            val low = getAverageValue(gribFiles.getValue(IMSConstants.LOW_CLOUDS_PARAM), calendar, IMSConstants.LOW_CLOUDS_PARAM_FILE, lat, long)
                            val medium = getAverageValue(gribFiles.getValue(IMSConstants.MEDIUM_CLOUDS_PARAM), calendar, IMSConstants.MEDIUM_CLOUDS_PARAM_FILE, lat, long)
                            val high = getAverageValue(gribFiles.getValue(IMSConstants.HIGH_CLOUDS_PARAM), calendar, IMSConstants.HIGH_CLOUDS_PARAM_FILE, lat, long)
                            val rain = getAverageValue(gribFiles.getValue(IMSConstants.RAIN_PARAM), calendar, IMSConstants.RAIN_PARAM_FILE, lat, long)
                            val cell = Cell(date, CELL_SIZE, lat, long, low, medium, high, rain, null, null, null, null, null,null)
                            try {
                                saveLocalCloudsCell(cell)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            long += CELL_SIZE
                        }
                        lat += CELL_SIZE
                    }
                }
    }


    private fun getAverageValue(gribFile: GribFile, calendar: GregorianCalendar, param: String, lat: Double, long: Double): Double {
        val record = gribFile.getRecord(calendar, param, "SFC")
        val here = record.getValue(lat, long)
        val north = record.getValue(lat + CELL_SIZE / 2, long)
        val south = record.getValue(lat - CELL_SIZE / 2, long)
        val west = record.getValue(lat, long - CELL_SIZE / 2)
        return (here + north + south + AVERAGE_WEST_FACTOR * west) / (3 + AVERAGE_WEST_FACTOR)
    }

    fun openGribFiles(): Map<String, GribFile> {
        return IMSConstants.params.map { it.first to GribFile(FileInputStream(getFile(it.first))) }.toMap()

    }


    private fun getFile(s: String) =
            IMSConstants.downloadDir.listFiles { f -> f.name.endsWith("$s.grb") }.minBy { it.lastModified() }!!
}