package org.bernshtam.weather.tools

import mt.edu.um.cf2.jgribx.GribFile
import java.util.*

object Grib2CSV {
    @JvmStatic
    fun main(args: Array<String>) {
        val name = "C:\\Users\\User\\Downloads\\C3_2019110300_CLCL.grb"
        val file = GribFile(name)
        // println(file)
        val record = file.getRecord(GregorianCalendar(2019, 10, 3, 17, 0, 0), "LCDC", "SFC")
        println("latitude,longtitude,cloud-cover")
        var lat = 29.0
        while(lat<=34.0) {
        var lon = 34.0
            while(lon<=36.0) {
                val message = record.getValue(lat, lon)
                if (message!=0.0)
                println(String.format("%.2f,%.2f,%.2f",lat,lon,message))
                lon+=0.1
            }
            lat+=0.1
        }

    }
}