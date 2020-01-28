package org.bernshtam.weather.datasources

import java.io.File
import java.time.format.DateTimeFormatter

object IMSConstants {
    const val HIGH_CLOUDS_PARAM = "HCDC"
    const val HIGH_CLOUDS_FILE = "CLCH"
    const val MEDIUM_CLOUDS_PARAM = "MCDC"
    const val MEDIUM_CLOUDS_FILE = "CLCM"
    const val LOW_CLOUDS_PARAM = "LCDC"
    const val LOW_CLOUDS_FILE = "CLCL"
    const val TOTAL_CLOUDS_PARAM_FILE = "LCDC"
    const val TOTAL_CLOUDS_PARAM = "CLCT"
//    const val TEMP_PARAM_FILE = "LCDC"
//    const val TEMP_PARAM = "T_2M"
//    const val RAIN_PARAM_FILE = "APCP"
//    const val RAIN_PARAM = "TOT_PREC"
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val params = listOf(
            Pair(HIGH_CLOUDS_FILE, HIGH_CLOUDS_PARAM),
            Pair(MEDIUM_CLOUDS_FILE, MEDIUM_CLOUDS_PARAM),
            Pair(LOW_CLOUDS_FILE, LOW_CLOUDS_PARAM)
            //, Pair(RAIN_PARAM, RAIN_PARAM_FILE)
    )


    val downloadDir = File(System.getProperty("java.io.tmpdir"), "ims")
}