package org.bernshtam.weather

import mt.edu.um.cf2.jgribx.GribFile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object GribX {
    @JvmStatic
    fun main(args: Array<String>) {
        val date = LocalDate.now()
        val yesterday = LocalDate.now().minusDays(1)
        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val yesterdayStr = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val params = listOf(Pair("CLCH", "HCDC"), Pair("CLCM", "MCDC"), Pair("CLCL", "LCDC"))

        params.forEach { p ->
            val patterns = listOf(
                "C3_${dateStr}12_${p.first}.grb",
                "C3_${dateStr}00_${p.first}.grb",
                "C3_${yesterdayStr}12_${p.first}.grb",
                "C3_${yesterdayStr}00_${p.first}.grb"
            )
            for (pattern in patterns) {
                try {
                    val stream = NTLM.get(pattern)
                    //val name = "C:\\Users\\User\\Downloads\\C3_2019111812_CLCH.grb"
                    try {
                        val file = GribFile(stream)
                        val record = file.getRecord(GregorianCalendar(2019, 10, 19, 17, 0, 0), p.second, "SFC")
                        println(record.forecastTime.toInstant())
                        println(record.getValue(31.93, 34.7))
                        break
                    } finally {
                        stream.close()
                    }

                } catch (e: Exception) {
                    println(e.toString())
                    continue;
                }
            }
        }

    }
}