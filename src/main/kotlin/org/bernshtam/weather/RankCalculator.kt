package org.bernshtam.weather

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object RankCalculator {
    fun recalculate() {
        val today = LocalDate.now()
        (0L..2L).map { today.plus(it, ChronoUnit.DAYS) }
                .forEach { date ->
                    val cells = DB.getCells(date)
                    cells.forEach {c->
                        val mnD = CellsSunSetService.getMarkAndDescription(c)
                        c.rank = mnD.mark.toDouble()
                        c.description = mnD.description
                        DB.updateCell(c)

                    }
                }
    }


}
