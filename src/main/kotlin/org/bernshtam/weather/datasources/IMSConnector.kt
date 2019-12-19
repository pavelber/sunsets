package org.bernshtam.weather.datasources

import com.beust.klaxon.JsonObject
import mt.edu.um.cf2.jgribx.GribFile
import org.bernshtam.weather.IMSClouds
import org.bernshtam.weather.PointAtTime
import org.bernshtam.weather.datasources.IMSConstants.downloadDir
import org.bernshtam.weather.datasources.IMSConstants.params
import java.io.FileInputStream
import java.util.*

object IMSConnector {


    private var gribFiles = openGribFiles()

    fun getCloudsParams(pointAtTime: PointAtTime): IMSClouds {
        val paramMap = params.map { p ->
            val value: Double = getValue(pointAtTime, p)
            p.second to value
        }.toMap()

        val json = JsonObject(paramMap)
        return IMSClouds(
                json.double(IMSConstants.HIGH_CLOUDS_PARAM)!! / 100.0,
                json.double(IMSConstants.MEDIUM_CLOUDS_PARAM)!! / 100.0,
                json.double(IMSConstants.LOW_CLOUDS_PARAM)!! / 100.0
        )
    }


    fun openGribFiles(): Map<String, GribFile> {
        return params.map { it.first to GribFile(FileInputStream(getFile(it.first))) }.toMap()

    }

    fun reopenGribFiles() {
        val oldFiles = gribFiles;
        gribFiles = openGribFiles()
    }

    private fun getFile(s: String) =
            downloadDir.listFiles { f -> f.name.endsWith("$s.grb") }.minBy { it.lastModified() }!!

    private fun getValue(pointAtTime: PointAtTime, param: Pair<String, String>): Double {
        val paramName = param.first
        val paramIMSName = param.second
        val record = gribFiles.getValue(paramName).getRecord(GregorianCalendar.from(pointAtTime.time), paramIMSName, "SFC")
        return record.getValue(pointAtTime.lat, pointAtTime.long)
    }

}