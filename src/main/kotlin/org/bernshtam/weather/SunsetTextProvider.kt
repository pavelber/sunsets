package org.bernshtam.weather

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

object SunsetTextProvider {
    private val service = DBSunSetService()

    suspend fun handle(call: ApplicationCall) {
        val request = call.request
        val lat = request.queryParameters["lat"]?.toDouble()
        val long = request.queryParameters["long"]?.toDouble()
        if (lat == null || long == null) call.respond(HttpStatusCode.BadRequest, "no latitude and longitude")
        else if (long < 34.0 || long > 36.0) call.respond(HttpStatusCode.BadRequest, "working in israel only")
        else if (lat < 29.5 || lat > 33.5) call.respond(HttpStatusCode.BadRequest, "working in israel only")
        else call.respond(service.getMarkAndDescription(lat, long))
    }
}