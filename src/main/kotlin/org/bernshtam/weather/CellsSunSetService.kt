package org.bernshtam.weather

import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

object CellsSunSetService {

    private const val PATTERN = "dd/MM/yyy"

    private val dateTimeFormatter = DateTimeFormatter.ofPattern(PATTERN)


    fun getMarkAndDescription(cell: Cell, cS: Cell, cN: Cell, cW: Cell): MarkAndDescription {
        val date = cell.date

        val points = mutableListOf<MarkAndDescription>()
        points.add(getCloudsNearHorizon(cell))
        points.add(getCloudsNearMe(cell, cS, cN, cW))

        val result = points.reduce { acc,
                                     markAndDescription ->
            MarkAndDescription(date.format(dateTimeFormatter), acc.mark + markAndDescription.mark, acc.maxMark + markAndDescription.maxMark, reduceDescription(acc, markAndDescription))
        }
        val normalization = 100.0 / result.maxMark
        val normalizedMark = (result.mark * normalization).roundToInt()
        return MarkAndDescription(result.date, normalizedMark, 100, result.description)
    }

    private fun reduceDescription(acc: MarkAndDescription, markAndDescription: MarkAndDescription) =
            reduceDescription(acc.description, markAndDescription.description)

    private fun reduceDescription(acc: String, markAndDescription: String) =
            if (acc.isBlank())
                if (markAndDescription.isBlank())
                    ""
                else markAndDescription
            else
                if (markAndDescription.isBlank())
                    acc
                else
                    "$acc $markAndDescription"

    private fun getCloudsNearMe(c: Cell, cS: Cell, cN: Cell, cW: Cell): MarkAndDescription {

        val low = (c.low + cS.low + cN.low + cW.low * 3) / 6.0
        val medium = (c.medium + cS.medium + cN.medium + cW.medium * 3) / 6.0
        val high = (c.high + cS.high + cN.high + cW.high * 3) / 6.0

        val coefFrom5MinsLighting = if (c.sun_blocking_near > 0.6) 0 else if (c.sun_blocking_near > 0.3) 1 else 2
        val coefFrom10MinsLighting = if (c.sun_blocking_far > 0.6) 0 else if (c.sun_blocking_far > 0.3) 1 else 2
        val coefFromLighting = coefFrom5MinsLighting + coefFrom10MinsLighting

        val highClouds = low < 0.5 && medium < 0.5 && high > 0.2
        val notLowClouds = low < 0.5 && (medium > 0.0 || high > 0.0)
        val lightDescription = when {
            coefFromLighting > 2 && highClouds -> "An excellent chance for good light on clouds after sunset."
            coefFromLighting > 0 && notLowClouds -> "A chance for good light on clouds after sunset."
            else -> ""

        }
        val cloudsDescriptions = "Low clouds: ${(100 * low).toInt()}. Medium clouds: ${(100 * medium).toInt()}. High clouds: ${(100 * high).toInt()}."
        val description = "$cloudsDescriptions $lightDescription"

        val max = 16
        if (low > 0.2) return MarkAndDescription("", 0, max, description)
        else {
            if (medium > 0.2) return MarkAndDescription("", 1 * coefFromLighting, max, description)
            else {

                if (high > 0.5) return MarkAndDescription("", 4 * coefFromLighting, max, description)
                else {
                    if (high > 0.2) return MarkAndDescription("", 3 * coefFromLighting, max, description)
                    else if (high == 0.0) return MarkAndDescription("", (if (medium > 0.2) 1 else 0) * coefFromLighting, max, description)
                    else {
                        return MarkAndDescription("", 2 * coefFromLighting, max, description)
                    }
                }
            }
        }
    }


    private fun getCloudsNearHorizon(c: Cell): MarkAndDescription {
        var points = 0
        var description = ""

        when {
            c.sunset_near < 0.2 -> description += " A little or no clouds on the west."
            c.sunset_near in 0.2..0.7 -> {
                points += 4;description += " Clouds on the west."
            }
            else -> {
                points += 2; description += " Heavy clouds on the west."
            }
        }


        when {
            c.sunset_far == 0.0 -> {
                points += 6; description += " Clear sky on the sunset point."
            }
            c.sunset_far in 0.2..0.7 -> {
                points += 2; description += " Clouds on the sunset point."
            }
            else -> {
                description += " Heavy clouds on the sunset point."
            }
        }

        val FACTOR = 1

        return MarkAndDescription("", FACTOR * points, FACTOR * 8, description)
    }


}