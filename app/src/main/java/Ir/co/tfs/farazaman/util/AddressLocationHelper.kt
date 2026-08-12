package Ir.co.tfs.farazaman.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object AddressLocationHelper {

    private const val MIN_ADDRESS_UPDATE_DISTANCE_METERS = 5f

    fun shouldUpdateAddress(
        previous: Pair<Double, Double>?,
        current: Pair<Double, Double>,
    ): Boolean {
        if (previous == null) return true

        return distanceMeters(previous, current) >= MIN_ADDRESS_UPDATE_DISTANCE_METERS
    }

    internal fun distanceMeters(
        first: Pair<Double, Double>,
        second: Pair<Double, Double>,
    ): Double {
        val latitudeDelta = Math.toRadians(second.first - first.first)
        val longitudeDelta = Math.toRadians(second.second - first.second)
        val firstLatitude = Math.toRadians(first.first)
        val secondLatitude = Math.toRadians(second.first)
        val haversine = sin(latitudeDelta / 2).let { it * it } +
            cos(firstLatitude) * cos(secondLatitude) *
            sin(longitudeDelta / 2).let { it * it }
        return EARTH_RADIUS_METERS * 2 * atan2(sqrt(haversine), sqrt(1 - haversine))
    }

    private const val EARTH_RADIUS_METERS = 6_371_000.0
}
