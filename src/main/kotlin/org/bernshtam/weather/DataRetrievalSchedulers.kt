package org.bernshtam.weather

import org.bernshtam.weather.datasources.IMSStreamProvider
import org.bernshtam.weather.utils.Scheduler
import java.util.concurrent.TimeUnit

object DataRetrievalSchedulers {

    fun runSchedulers() {
        val task = r { IMSStreamProvider.redownload() }
        Scheduler(task, 2 * 60, TimeUnit.MINUTES, 0).start()
           }
}

fun r(f: () -> Unit): Runnable = Runnable { f() }