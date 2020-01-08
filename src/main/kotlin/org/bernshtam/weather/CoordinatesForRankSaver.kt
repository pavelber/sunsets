package org.bernshtam.weather

import org.bernshtam.weather.dal.CoordinatesDAL
import org.bernshtam.weather.datasources.GeoNamesConnector

object CoordinatesForRankSaver {
    fun save() {

        if (CoordinatesDAL.getAll().isEmpty()) {
            val countries = mutableMapOf<String, Int>()
            var lat = LAT_START + CELL_SIZE
            while (lat < LAT_END) {
                var long = LONG_START + CELL_SIZE
                while (long < LONG_END) {

                    val country = GeoNamesConnector.getCountry(lat, long)
                    countries[country] = countries.getOrDefault(country, 0) + 1
                    if (country.startsWith("IL") || country.startsWith("PS") ||
                            (country.startsWith("SY") && long < 35.9 && lat > 32.7))
                        CoordinatesDAL.save(Coordinates(lat, long))

                    long += CELL_SIZE
                }
                lat += CELL_SIZE
            }

//            println(countries)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        save()
    }
}