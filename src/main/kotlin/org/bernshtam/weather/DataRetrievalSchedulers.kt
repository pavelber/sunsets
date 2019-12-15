package org.bernshtam.weather

import org.bernshtam.weather.utils.Scheduler
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object DataRetrievalSchedulers {
    fun runSchedulers() {
        (0 .. 2L).forEach { day ->
            Place.PLACES.values.forEach { p ->
                val task = r {
                    try {
                        SunSetService.getMarkAndDescription(p.lat, p.long, LocalDate.now().plusDays(day))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }


                Scheduler(task,
                        2, TimeUnit.HOURS).start()
            }
        }
    }
}

fun r(f: () -> Unit): Runnable = Runnable { f() }