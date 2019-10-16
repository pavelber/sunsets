package org.bernshtam.weather

import java.util.*

object TokenManager {
    private val props = Properties()

    init {
        props.load(TokenManager::class.java.classLoader.getResourceAsStream("application.properties"))
    }

    fun get(name:String):String {
        return props.getProperty(name)
    }
}