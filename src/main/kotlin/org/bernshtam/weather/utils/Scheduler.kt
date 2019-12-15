package org.bernshtam.weather.utils

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Scheduler(private val task: Runnable, private val interval: Long, private val timeUnit: TimeUnit, private val initialDelay: Long = 0) {

    private val scheduler = Executors.newScheduledThreadPool(3)


    fun stop() {
        scheduler.shutdown()
    }

    fun start() {
        try {
            println("Scheduked")
            scheduler.scheduleWithFixedDelay(task, initialDelay, interval, timeUnit)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}