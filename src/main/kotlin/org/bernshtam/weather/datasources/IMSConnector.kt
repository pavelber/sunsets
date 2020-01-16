package org.bernshtam.weather.datasources

import mt.edu.um.cf2.jgribx.GribFile
import org.bernshtam.weather.IMSParams
import org.bernshtam.weather.PointAtTime
import org.bernshtam.weather.datasources.IMSConstants.downloadDir
import org.bernshtam.weather.datasources.IMSConstants.params
import java.io.FileInputStream
import java.util.*

object IMSConnector {


    private var gribFiles = openGribFiles()

    fun getIMSParams(pointAtTime: PointAtTime): IMSParams {
        val paramMap = params.map { p ->
            val value: Double = getValue(pointAtTime, p)
            p.second to value
        }.toMap()


        return IMSParams(
                paramMap.getValue(IMSConstants.HIGH_CLOUDS_PARAM_FILE) / 100.0,
                paramMap.getValue(IMSConstants.MEDIUM_CLOUDS_PARAM_FILE) / 100.0,
                paramMap.getValue(IMSConstants.LOW_CLOUDS_PARAM_FILE) / 100.0,
                paramMap.getValue(IMSConstants.RAIN_PARAM_FILE) / 100.0
        )
    }


    fun openGribFiles(): Map<String, GribFile> {
        try {
            return params.map { it.first to GribFile(FileInputStream(getFile(it.first))) }.toMap()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

    }

    fun reopenGribFiles() {
        gribFiles = openGribFiles()
    }

    private fun getFile(s: String) =
            downloadDir.listFiles { f -> f.name.endsWith("$s.grb") }.maxBy { it.name }!!

    private fun getValue(pointAtTime: PointAtTime, param: Pair<String, String>): Double {
        val paramName = param.first
        val paramIMSName = param.second
        val record = gribFiles.getValue(paramName).getRecord(GregorianCalendar.from(pointAtTime.time), paramIMSName, "SFC")
        return record.getValue(pointAtTime.lat, pointAtTime.long)
    }

}