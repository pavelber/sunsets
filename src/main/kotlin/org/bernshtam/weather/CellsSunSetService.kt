package org.bernshtam.weather

import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

object CellsSunSetService {

    private const val PATTERN = "dd/MM/yyy"

    private val dateTimeFormatter = DateTimeFormatter.ofPattern(PATTERN)

    private const val HIGH_CLOUDS_FACTOR = 2;

    fun getMarkAndDescription(cell: Cell, cS: Cell, cN: Cell, cW: Cell): MarkAndDescription {
        val date = cell.date

        val points = mutableListOf<MarkAndDescription>()
        points.add(getCloudsNearHorizon(cell))
        val highCloudsLight = getCloudsNearMe(cell, cS, cN, cW)
        points.add(highCloudsLight.let { it.copy(mark = HIGH_CLOUDS_FACTOR * it.mark, maxMark = HIGH_CLOUDS_FACTOR * it.maxMark) })

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

        val coefFrom5MinsLighting = if (c.sun_blocking_near ?: 0.0 > 60.0) 0 else if (c.sun_blocking_near ?: 0.0 > 30.0) 1 else 2
        val coefFrom10MinsLighting = if (c.sun_blocking_far ?: 0.0 > 60.0) 0 else if (c.sun_blocking_far ?: 0.0 > 30.0) 1 else 2
        val coefFromLighting = coefFrom5MinsLighting + coefFrom10MinsLighting

        val highClouds = low < 50.0 && medium < 50.0 && high > 20.0
        val notLowClouds = low < 50.0 && (medium > 0.0 || high > 0.0)
        val lightDescription = when {
            coefFromLighting > 2 && highClouds -> "An excellent chance for good light on clouds after sunset."
            coefFromLighting > 0 && notLowClouds -> "A chance for good light on clouds after sunset."
            else -> ""

        }
        val cloudsDescriptions = "Low clouds: ${low.toInt()}. Medium clouds: ${medium.toInt()}. High clouds: ${high.toInt()}."
        val description = "$cloudsDescriptions $lightDescription"

        val max = 24
        if (low > 20.0) return MarkAndDescription("", 0, max, cloudsDescriptions)
        else {
            if (medium > 20.0) return MarkAndDescription("", 1 * coefFromLighting, max, description)
            else {
                if (high > 50.0) return MarkAndDescription("", 6 * coefFromLighting, max, description)
                else {
                    if (high > 20.0) return MarkAndDescription("", 4 * coefFromLighting, max, description)
                    else if (high == 0.0) return MarkAndDescription("", (if (medium > 20.0) 1 else 0) * coefFromLighting, max, cloudsDescriptions)
                    else {
                        return MarkAndDescription("", 2 * coefFromLighting, max, cloudsDescriptions)
                    }
                }
            }
        }
    }


    private fun getCloudsNearHorizon(c: Cell): MarkAndDescription {
        var points = 0
        var description = ""

        when {
            c.sunset_near ?: 0.0 < 20.0 -> description += " A little or no clouds on the west."
            c.sunset_near ?: 0.0 in 20.0..70.0 -> {
                points += 4;description += " Clouds on the west."
            }
            else -> {
                points += 2; description += " Heavy clouds on the west."
            }
        }


        when {
            c.sunset_far ?: 0.0 < 20.0 -> {
                points += 6; description += " Clear sky on the sunset point."
            }
            c.sunset_far ?: 0.0 in 20.0..70.0 -> {
                points += 2; description += " Clouds on the sunset point."
            }
            else -> {
                description += " Heavy clouds on the sunset point."
            }
        }

        return MarkAndDescription("", points, 10, description)
    }


}