package org.bernshtam.weather

import com.sun.org.apache.xalan.internal.lib.ExsltMath.abs
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object RankCalculator {
    private const val STEP = CELL_SIZE
    private const val PRECISION = 0.01

    fun recalculate() {
        val today = LocalDate.now()
        (0L..2L).map { today.plus(it, ChronoUnit.DAYS) }
                .forEach { date ->
                    val cells = DB.getCells(date)
                    cells.forEach { c ->
                        val cS = find(cells, c, -STEP, 0.0)
                        val cN = find(cells, c, STEP, 0.0)
                        val cW = find(cells, c, 0.0, STEP)
                        if (cS!=null && cN!=null && cW!=null) {
                            val mnD = CellsSunSetService.getMarkAndDescription(c, cS, cN, cW)
                            c.rank = mnD.mark.toDouble()
                            c.description = mnD.description
                            DB.updateCell(c)
                        }
                    }
                }
    }

    private fun find(cells: List<Cell>, c: Cell, dLat: Double, dLong: Double) =
            cells.firstOrNull { it.latitude.eq(c.latitude + dLat) && c.longitude.eq(it.longitude + dLong) }


    private fun Double.eq(a: Double) = abs(this - a) < PRECISION


}
