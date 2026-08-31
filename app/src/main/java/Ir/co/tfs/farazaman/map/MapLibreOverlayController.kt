package Ir.co.tfs.farazaman.map

import android.graphics.Color
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.layers.PropertyFactory.lineOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

class MapLibreOverlayController(
    private val map: MapLibreMap,
    private val style: Style,
) {
    private val fenceFeatures = mutableListOf<Feature>()

    init {
        addEmptySource(ROUTE_SOURCE)
        style.addLayer(
            LineLayer(ROUTE_LAYER, ROUTE_SOURCE).withProperties(
                lineColor(Color.rgb(28, 104, 220)),
                lineWidth(5f),
                lineOpacity(0.95f),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
            ),
        )

        addEmptySource(FENCE_SOURCE)
        style.addLayer(
            FillLayer(FENCE_FILL_LAYER, FENCE_SOURCE).withProperties(
                fillColor(Color.rgb(0, 121, 107)),
                fillOpacity(0.20f),
            ),
        )
        style.addLayer(
            LineLayer(FENCE_LINE_LAYER, FENCE_SOURCE).withProperties(
                lineColor(Color.rgb(0, 105, 92)),
                lineWidth(2f),
                lineOpacity(0.7f),
            ),
        )

        addEmptySource(WAYPOINT_SOURCE)
        style.addLayer(
            CircleLayer(WAYPOINT_LAYER, WAYPOINT_SOURCE).withProperties(
                circleColor(Color.rgb(255, 193, 7)),
                circleRadius(6f),
                circleStrokeColor(Color.WHITE),
                circleStrokeWidth(2f),
            ),
        )

        addEmptySource(LOCATION_SOURCE)
        style.addLayer(
            CircleLayer(LOCATION_HALO_LAYER, LOCATION_SOURCE).withProperties(
                circleColor(Color.rgb(0, 121, 107)),
                circleRadius(13f),
                circleOpacity(0.2f),
            ),
        )
        style.addLayer(
            CircleLayer(LOCATION_LAYER, LOCATION_SOURCE).withProperties(
                circleColor(Color.rgb(0, 121, 107)),
                circleRadius(7f),
                circleStrokeColor(Color.WHITE),
                circleStrokeWidth(2.5f),
            ),
        )
    }

    fun updateRoute(points: List<LatLng>) {
        val feature = points.takeIf { it.size >= 2 }
            ?.map { Point.fromLngLat(it.longitude, it.latitude) }
            ?.let { Feature.fromGeometry(LineString.fromLngLats(it)) }
        updateSource(ROUTE_SOURCE, feature?.let(FeatureCollection::fromFeature) ?: emptyFeatures())
    }

    fun updateCurrentLocation(point: LatLng?) {
        val feature = point?.let {
            Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude))
        }
        updateSource(LOCATION_SOURCE, feature?.let(FeatureCollection::fromFeature) ?: emptyFeatures())
    }

    fun updateWaypoints(points: List<LatLng>) {
        val features = points.map {
            Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude))
        }
        updateSource(WAYPOINT_SOURCE, FeatureCollection.fromFeatures(features))
    }

    fun addFence(points: List<LatLng>) {
        if (points.size < 3) return
        val ring = points.map { Point.fromLngLat(it.longitude, it.latitude) }.toMutableList()
        if (ring.first() != ring.last()) ring += ring.first()
        fenceFeatures += Feature.fromGeometry(Polygon.fromLngLats(listOf(ring)))
        updateSource(FENCE_SOURCE, FeatureCollection.fromFeatures(fenceFeatures))
    }

    fun center(point: LatLng, zoom: Double = 18.0, durationMs: Int = 700) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, zoom), durationMs)
    }

    fun fit(points: List<LatLng>, paddingPx: Int = 100, durationMs: Int = 700) {
        if (points.isEmpty()) return
        if (points.size == 1) {
            center(points.first())
            return
        }
        val bounds = LatLngBounds.Builder().includes(points).build()
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx), durationMs)
    }

    fun zoomIn() = map.animateCamera(CameraUpdateFactory.zoomIn())

    fun zoomOut() = map.animateCamera(CameraUpdateFactory.zoomOut())

    private fun addEmptySource(id: String) {
        style.addSource(GeoJsonSource(id, emptyFeatures()))
    }

    private fun updateSource(id: String, features: FeatureCollection) {
        style.getSourceAs<GeoJsonSource>(id)?.setGeoJson(features)
    }

    private fun emptyFeatures(): FeatureCollection = FeatureCollection.fromFeatures(emptyArray<Feature>())

    private companion object {
        const val ROUTE_SOURCE = "farazaman-route-source"
        const val ROUTE_LAYER = "farazaman-route-layer"
        const val FENCE_SOURCE = "farazaman-fence-source"
        const val FENCE_FILL_LAYER = "farazaman-fence-fill"
        const val FENCE_LINE_LAYER = "farazaman-fence-line"
        const val WAYPOINT_SOURCE = "farazaman-waypoint-source"
        const val WAYPOINT_LAYER = "farazaman-waypoint-layer"
        const val LOCATION_SOURCE = "farazaman-location-source"
        const val LOCATION_HALO_LAYER = "farazaman-location-halo"
        const val LOCATION_LAYER = "farazaman-location-layer"
    }
}
