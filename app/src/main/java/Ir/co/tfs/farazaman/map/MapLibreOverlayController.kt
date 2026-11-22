package Ir.co.tfs.farazaman.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.SymbolLayer
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
import org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.iconIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.iconOffset
import org.maplibre.android.style.layers.PropertyFactory.iconPitchAlignment
import org.maplibre.android.style.layers.PropertyFactory.iconRotate
import org.maplibre.android.style.layers.PropertyFactory.iconRotationAlignment
import org.maplibre.android.style.layers.PropertyFactory.visibility
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

        addEmptySource(TRACK_POINT_SOURCE)
        style.addLayer(
            CircleLayer(TRACK_POINT_LAYER, TRACK_POINT_SOURCE).withProperties(
                circleColor(Color.rgb(28, 104, 220)),
                circleRadius(3f),
                circleOpacity(0.8f),
                circleStrokeColor(Color.WHITE),
                circleStrokeWidth(1f),
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

        addEmptySource(VIOLATION_WAYPOINT_SOURCE)
        style.addLayer(
            CircleLayer(VIOLATION_WAYPOINT_LAYER, VIOLATION_WAYPOINT_SOURCE).withProperties(
                circleColor(Color.rgb(211, 47, 47)),
                circleRadius(6f),
                circleStrokeColor(Color.WHITE),
                circleStrokeWidth(2f),
            ),
        )

        addEmptySource(LAYER_WAYPOINT_SOURCE)
        style.addLayer(
            CircleLayer(LAYER_WAYPOINT_LAYER, LAYER_WAYPOINT_SOURCE).withProperties(
                circleColor(Color.rgb(0, 137, 123)),
                circleRadius(6f),
                circleStrokeColor(Color.WHITE),
                circleStrokeWidth(2f),
            ),
        )

        addEmptySource(LOCATION_SOURCE)
        style.addImage(LOCATION_HEADING_IMAGE, createHeadingArrow())
        style.addLayer(
            CircleLayer(LOCATION_HALO_LAYER, LOCATION_SOURCE).withProperties(
                circleColor(LOCATION_COLOR),
                circleRadius(13f),
                circleOpacity(0.2f),
            ),
        )
        style.addLayer(
            SymbolLayer(LOCATION_HEADING_LAYER, LOCATION_SOURCE).withProperties(
                iconImage(LOCATION_HEADING_IMAGE),
                iconOffset(arrayOf(0f, -23f)),
                iconRotate(0f),
                iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP),
                iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                visibility(Property.NONE),
            ),
        )
        style.addLayer(
            CircleLayer(LOCATION_LAYER, LOCATION_SOURCE).withProperties(
                circleColor(LOCATION_COLOR),
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

    fun updateTrackPoints(points: List<LatLng>) {
        val features = points.map {
            Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude))
        }
        updateSource(TRACK_POINT_SOURCE, FeatureCollection.fromFeatures(features))
    }

    fun updateCurrentLocation(point: LatLng?, headingDegrees: Float? = null) {
        val feature = point?.let {
            Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude))
        }
        updateSource(LOCATION_SOURCE, feature?.let(FeatureCollection::fromFeature) ?: emptyFeatures())
        style.getLayerAs<SymbolLayer>(LOCATION_HEADING_LAYER)?.setProperties(
            iconRotate(headingDegrees ?: 0f),
            visibility(if (point != null && headingDegrees != null) Property.VISIBLE else Property.NONE),
        )
    }

    fun setCurrentLocationError(showError: Boolean) {
        val color = if (showError) LOCATION_ERROR_COLOR else LOCATION_COLOR
        style.getLayerAs<CircleLayer>(LOCATION_LAYER)?.setProperties(circleColor(color))
        style.getLayerAs<CircleLayer>(LOCATION_HALO_LAYER)?.setProperties(
            circleColor(color),
            circleOpacity(if (showError) 0.34f else 0.2f),
        )
    }

    fun updateWaypoints(
        violationPoints: List<LatLng>,
        layerPoints: List<LatLng>,
    ) {
        val violationFeatures = violationPoints.map {
            Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude))
        }
        updateSource(VIOLATION_WAYPOINT_SOURCE, FeatureCollection.fromFeatures(violationFeatures))
        val layerFeatures = layerPoints.map {
            Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude))
        }
        updateSource(LAYER_WAYPOINT_SOURCE, FeatureCollection.fromFeatures(layerFeatures))
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

    /** Moves with the live location without resetting a zoom level chosen by the user. */
    fun follow(point: LatLng) {
        map.moveCamera(CameraUpdateFactory.newLatLng(point))
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

    private fun createHeadingArrow(): Bitmap {
        val bitmap = Bitmap.createBitmap(56, 68, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val path = Path().apply {
            moveTo(28f, 3f)
            lineTo(51f, 53f)
            quadTo(52f, 59f, 46f, 56f)
            lineTo(28f, 47f)
            lineTo(10f, 56f)
            quadTo(4f, 59f, 5f, 53f)
            close()
        }
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 7f
            strokeJoin = Paint.Join.ROUND
        })
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = LOCATION_COLOR
            style = Paint.Style.FILL
        })
        return bitmap
    }

    private companion object {
        val LOCATION_COLOR: Int = Color.rgb(0, 121, 107)
        val LOCATION_ERROR_COLOR: Int = Color.rgb(211, 47, 47)
        const val ROUTE_SOURCE = "farazaman-route-source"
        const val ROUTE_LAYER = "farazaman-route-layer"
        const val TRACK_POINT_SOURCE = "farazaman-track-point-source"
        const val TRACK_POINT_LAYER = "farazaman-track-point-layer"
        const val FENCE_SOURCE = "farazaman-fence-source"
        const val FENCE_FILL_LAYER = "farazaman-fence-fill"
        const val FENCE_LINE_LAYER = "farazaman-fence-line"
        const val VIOLATION_WAYPOINT_SOURCE = "farazaman-violation-waypoint-source"
        const val VIOLATION_WAYPOINT_LAYER = "farazaman-violation-waypoint-layer"
        const val LAYER_WAYPOINT_SOURCE = "farazaman-layer-waypoint-source"
        const val LAYER_WAYPOINT_LAYER = "farazaman-layer-waypoint-layer"
        const val LOCATION_SOURCE = "farazaman-location-source"
        const val LOCATION_HEADING_IMAGE = "farazaman-location-heading-image"
        const val LOCATION_HEADING_LAYER = "farazaman-location-heading-layer"
        const val LOCATION_HALO_LAYER = "farazaman-location-halo"
        const val LOCATION_LAYER = "farazaman-location-layer"
    }
}
