package Ir.co.tfs.farazaman.map

import android.graphics.RectF
import android.graphics.PointF
import org.maplibre.geojson.Geometry
import org.maplibre.geojson.LineString
import org.maplibre.geojson.MultiLineString
import org.maplibre.geojson.Point
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import kotlin.math.sqrt

data class MapRoadInfo(
    val name: String,
    val roadClass: String = "",
)

/** Reads the closest named road from vector features already rendered by MapLibre. */
object MapRoadNameResolver {
    private const val QUERY_RADIUS_PX = 192f
    private val preferredLayerIds = listOf(
        "road-label",
        "road-label-fallback",
        "road",
        "road-casing",
    )
    private val nameProperties = listOf("name:fa", "name:nonlatin", "name", "name:latin", "ref")
    private val classProperties = listOf("fclass", "class", "subclass")

    fun resolve(map: MapLibreMap, location: LatLng): MapRoadInfo? {
        val style = map.style ?: return null
        val availableLayers = style.layers.map { it.id }
        val roadLayers = buildList {
            preferredLayerIds.filterTo(this) { it in availableLayers }
            availableLayers.filterTo(this) { id ->
                val normalized = id.lowercase()
                (normalized.contains("road") || normalized.contains("street") ||
                    normalized.contains("transportation")) && id !in this
            }
        }
        if (roadLayers.isEmpty()) return null

        val point = map.projection.toScreenLocation(location)
        val area = RectF(
            point.x - QUERY_RADIUS_PX,
            point.y - QUERY_RADIUS_PX,
            point.x + QUERY_RADIUS_PX,
            point.y + QUERY_RADIUS_PX,
        )
        return runCatching {
            map.queryRenderedFeatures(area, *roadLayers.toTypedArray())
                .asSequence()
                .mapNotNull { feature ->
                    val name = nameProperties.firstNotNullOfOrNull { key ->
                        if (feature.hasProperty(key)) feature.getStringProperty(key)
                            ?.trim()?.takeIf(String::isNotBlank) else null
                    } ?: return@mapNotNull null
                    val roadClass = classProperties.firstNotNullOfOrNull { key ->
                        if (feature.hasProperty(key)) feature.getStringProperty(key)
                            ?.trim()?.takeIf(String::isNotBlank) else null
                    }.orEmpty()
                    val distance = feature.geometry()?.let { geometry ->
                        distanceToGeometry(map, point, geometry)
                    } ?: Float.MAX_VALUE
                    RoadCandidate(MapRoadInfo(name = name, roadClass = roadClass), distance)
                }
                .minByOrNull(RoadCandidate::distancePixels)
                ?.road
        }.getOrNull()
    }

    private fun distanceToGeometry(map: MapLibreMap, target: PointF, geometry: Geometry): Float =
        when (geometry) {
            is Point -> distance(target, map.toScreenPoint(geometry))
            is LineString -> distanceToLine(map, target, geometry.coordinates())
            is MultiLineString -> geometry.coordinates()
                .minOfOrNull { line -> distanceToLine(map, target, line) }
                ?: Float.MAX_VALUE
            else -> Float.MAX_VALUE
        }

    private fun distanceToLine(map: MapLibreMap, target: PointF, coordinates: List<Point>): Float {
        if (coordinates.isEmpty()) return Float.MAX_VALUE
        if (coordinates.size == 1) return distance(target, map.toScreenPoint(coordinates.first()))

        return coordinates.zipWithNext().minOf { (start, end) ->
            distanceToSegment(
                target,
                map.toScreenPoint(start),
                map.toScreenPoint(end),
            )
        }
    }

    private fun MapLibreMap.toScreenPoint(point: Point): PointF =
        projection.toScreenLocation(LatLng(point.latitude(), point.longitude()))

    private fun distanceToSegment(target: PointF, start: PointF, end: PointF): Float {
        val deltaX = end.x - start.x
        val deltaY = end.y - start.y
        val lengthSquared = deltaX * deltaX + deltaY * deltaY
        if (lengthSquared == 0f) return distance(target, start)

        val projection = (((target.x - start.x) * deltaX + (target.y - start.y) * deltaY) /
            lengthSquared).coerceIn(0f, 1f)
        return distance(
            target,
            PointF(start.x + projection * deltaX, start.y + projection * deltaY),
        )
    }

    private fun distance(first: PointF, second: PointF): Float {
        val deltaX = first.x - second.x
        val deltaY = first.y - second.y
        return sqrt(deltaX * deltaX + deltaY * deltaY)
    }

    private data class RoadCandidate(
        val road: MapRoadInfo,
        val distancePixels: Float,
    )
}
