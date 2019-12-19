package org.bernshtam.weather.datasources

import java.io.File
import java.time.format.DateTimeFormatter

object IMSConstants {
    val HIGH_CLOUDS_PARAM = "HCDC"
    val MEDIUM_CLOUDS_PARAM = "MCDC"
    val LOW_CLOUDS_PARAM = "LCDC"
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val params = listOf(Pair("CLCH", HIGH_CLOUDS_PARAM), Pair("CLCM", MEDIUM_CLOUDS_PARAM), Pair("CLCL", LOW_CLOUDS_PARAM))


    val downloadDir = File(System.getProperty("java.io.tmpdir"), "ims")
}