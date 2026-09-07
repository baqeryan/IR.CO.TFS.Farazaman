package Ir.co.tfs.farazaman.data.model

import Ir.co.tfs.farazaman.data.db.model.TrackPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object ViolationGeoLocationMapper {

    fun hasValidCoordinates(point: TrackPoint?): Boolean {
        if (point == null) return false
        return point.latitude.isFinite() &&
            point.longitude.isFinite() &&
            point.latitude in -90.0..90.0 &&
            point.longitude in -180.0..180.0 &&
            !(point.latitude == 0.0 && point.longitude == 0.0)
    }

    fun create(visitDate: String, point: TrackPoint): BillOriginViEventGeoLocation {
        require(hasValidCoordinates(point)) { "A valid GPS point is required" }

        val latitude = point.latitude
        val longitude = point.longitude
        val recordedAt = point.pointTimestamp
            .takeIf { it > 0L }
            ?.let(::formatTimestamp)
            ?: visitDate

        return BillOriginViEventGeoLocation(
            geoJson = "{\"type\":\"Point\",\"coordinates\":[$longitude,$latitude]}",
            wellKnownText = "POINT($longitude $latitude)",
            gisGeolocationTrack = GisGeolocationTrack(
                latitude = latitude,
                longitude = longitude,
                elevation = point.elevation.finiteOrNull(),
                time = recordedAt,
                magneticVariant = null,
                cBaro = point.atmosphericPressure.positiveFiniteOrNull(),
                accuracy = point.accuracy.positiveFiniteOrNull(),
                altitudeAccuracy = null,
                heading = point.compassHeading.finiteOrNull(),
                speed = point.speed.nonNegativeFiniteOrNull(),
                type = "ViEvent",
                satelliteNumber = null,
            ),
        )
    }

    private fun formatTimestamp(timestampMillis: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestampMillis))

    private fun Double?.finiteOrNull(): Double? = this?.takeIf(Double::isFinite)

    private fun Double?.positiveFiniteOrNull(): Double? =
        finiteOrNull()?.takeIf { it > 0.0 }

    private fun Double?.nonNegativeFiniteOrNull(): Double? =
        finiteOrNull()?.takeIf { it >= 0.0 }
}
