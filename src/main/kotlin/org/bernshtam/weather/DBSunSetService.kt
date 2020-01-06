package org.bernshtam.weather

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DBSunSetService {

    companion object {
        private const val PATTERN = "dd/MM/yyy"

        private val dateTimeFormatter = DateTimeFormatter.ofPattern(PATTERN)
    }

    fun getMarkAndDescription(lat: Double, long: Double): List<MarkAndDescription> {
        val today = LocalDate.now()
        return (0L..2L)
                .map { today.plus(it, ChronoUnit.DAYS) }
                .map { getMarkAndDescription(lat, long, it) }
    }

    fun getMarkAndDescription(lat: Double, long: Double, date: LocalDate): MarkAndDescription {
        val cell = DB.findNearestCell(lat, long, date)
        return MarkAndDescription(dateTimeFormatter.format(date),
                (cell?.rank ?: 0.0).toInt(), 100, cell?.description ?: "")
    }

}