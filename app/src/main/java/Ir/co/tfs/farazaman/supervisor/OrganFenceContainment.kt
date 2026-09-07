package Ir.co.tfs.farazaman.supervisor

import kotlin.math.abs

object OrganFenceContainment {

    /** Returns null when the WKT does not contain a usable POLYGON. */
    fun contains(wkt: String, latitude: Double, longitude: Double): Boolean? {
        if (!latitude.isFinite() || !longitude.isFinite()) return null
        val normalized = wkt.trim()
        if (!normalized.startsWith("POLYGON", ignoreCase = true)) return null

        val body = normalized.substringAfter('(', "").substringBeforeLast(')', "")
            .trim().removePrefix("(").removeSuffix(")")
        if (body.isBlank()) return null

        val rings = body.split(Regex("\\)\\s*,\\s*\\("))
            .mapNotNull(::parseRing)
        val outer = rings.firstOrNull() ?: return null

        if (!isInsideOrOnBoundary(longitude, latitude, outer)) return false
        return rings.drop(1).none { isInsideOrOnBoundary(longitude, latitude, it) }
    }

    private fun parseRing(raw: String): List<Point>? {
        val points = raw.split(',').mapNotNull { coordinate ->
            val parts = coordinate.trim().split(Regex("\\s+"))
            if (parts.size < 2) return@mapNotNull null
            val longitude = parts[0].toDoubleOrNull() ?: return@mapNotNull null
            val latitude = parts[1].toDoubleOrNull() ?: return@mapNotNull null
            Point(longitude, latitude)
        }
        return points.takeIf { it.size >= 3 }
    }

    private fun isInsideOrOnBoundary(x: Double, y: Double, ring: List<Point>): Boolean {
        var inside = false
        var previous = ring.last()
        for (current in ring) {
            if (isOnSegment(x, y, previous, current)) return true
            if ((current.y > y) != (previous.y > y)) {
                val intersectionX = (previous.x - current.x) * (y - current.y) /
                    (previous.y - current.y) + current.x
                if (x < intersectionX) inside = !inside
            }
            previous = current
        }
        return inside
    }

    private fun isOnSegment(x: Double, y: Double, a: Point, b: Point): Boolean {
        val cross = (x - a.x) * (b.y - a.y) - (y - a.y) * (b.x - a.x)
        if (abs(cross) > EPSILON) return false
        return x in minOf(a.x, b.x) - EPSILON..maxOf(a.x, b.x) + EPSILON &&
            y in minOf(a.y, b.y) - EPSILON..maxOf(a.y, b.y) + EPSILON
    }

    private data class Point(val x: Double, val y: Double)

    private const val EPSILON = 1e-9
}
