package org.bernshtam.weather

import mt.edu.um.cf2.jgribx.GribFile
import org.bernshtam.weather.dal.CellDAL.saveLocalCloudsCell
import org.bernshtam.weather.datasources.IMSConstants
import org.bernshtam.weather.utils.Utils
import org.bernshtam.weather.utils.Utils.getTodayOrTommorrowDependsOnTimeNow
import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

object CellsCreator {


    fun recalclulate() {
        val gribFiles = openGribFiles()
        IMSConstants.params.forEach { p ->
            val file = p.first
            val param = p.second
            val grib = gribFiles.getValue(file)
            val records = grib.records
            records.forEach { r ->
                val time = r.forecastTime
                var lat = LAT_START + CELL_SIZE
                while (lat < LAT_END) {
                    var long = LONG_START + CELL_SIZE
                    while (long < LONG_END) {
                        val value = r.getValue(lat, long)
                        long += CELL_SIZE
                    }
                    lat += CELL_SIZE
                }

            }
        }

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
                            val low = getValue(gribFiles.getValue(IMSConstants.LOW_CLOUDS_FILE), calendar, IMSConstants.LOW_CLOUDS_PARAM, lat, long)
                            val medium = getValue(gribFiles.getValue(IMSConstants.MEDIUM_CLOUDS_FILE), calendar, IMSConstants.MEDIUM_CLOUDS_PARAM, lat, long)
                            val high = getValue(gribFiles.getValue(IMSConstants.HIGH_CLOUDS_FILE), calendar, IMSConstants.HIGH_CLOUDS_PARAM, lat, long)
                            //val rain = getAverageValue(gribFiles.getValue(IMSConstants.RAIN_PARAM), calendar, IMSConstants.RAIN_PARAM_FILE, lat, long)
                            val cell = Cell(LocalDateTime.from(moment.toTemporalAccessor()), lat, long, low, medium, high, null, null, null)
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


    private fun getValue(gribFile: GribFile, time: GregorianCalendar, param: String, lat: Double, long: Double): Double {
        val record = gribFile.getRecord(time, param, "SFC")
        return record.getValue(lat, long)
    }

    fun openGribFiles(): Map<String, GribFile> {
        return IMSConstants.params.map { it.first to GribFile(FileInputStream(getFile(it.first))) }.toMap()

    }


    private fun getFile(s: String) =
            IMSConstants.downloadDir.listFiles { f -> f.name.endsWith("$s.grb") }.minBy { it.lastModified() }!!
}