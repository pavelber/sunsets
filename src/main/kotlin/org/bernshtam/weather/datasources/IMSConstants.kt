package org.bernshtam.weather.datasources

import java.io.File
import java.time.format.DateTimeFormatter

object IMSConstants {
    const val HIGH_CLOUDS_PARAM_FILE = "HCDC"
    const val HIGH_CLOUDS_PARAM = "CLCH"
    const val MEDIUM_CLOUDS_PARAM_FILE = "MCDC"
    const val MEDIUM_CLOUDS_PARAM = "CLCM"
    const val LOW_CLOUDS_PARAM_FILE = "LCDC"
    const val LOW_CLOUDS_PARAM = "CLCL"
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val params = listOf(Pair(HIGH_CLOUDS_PARAM, HIGH_CLOUDS_PARAM_FILE), Pair(MEDIUM_CLOUDS_PARAM, MEDIUM_CLOUDS_PARAM_FILE), Pair(LOW_CLOUDS_PARAM, LOW_CLOUDS_PARAM_FILE))


    val downloadDir = File(System.getProperty("java.io.tmpdir"), "ims")
}