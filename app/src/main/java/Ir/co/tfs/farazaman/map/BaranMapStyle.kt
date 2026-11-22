package Ir.co.tfs.farazaman.map

import android.content.Context
import android.content.res.Configuration
import android.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Loads Baran's hosted MapLibre style, with cached and generated fallbacks. */
object BaranMapStyle {
    const val STYLE_URL = "https://baran.tfs.co.ir/tiles/style.json"
    const val MANIFEST_URL = "https://baran.tfs.co.ir/tiles/current.json"

    private const val PREF_CACHED_STYLE_JSON = "baran_map_style_json"
    private const val PREF_CACHED_PMTILES_URL = "baran_map_pmtiles_url"
    private const val GLYPHS_URL =
        "https://baran.tfs.co.ir/map-assets/glyphs/{fontstack}/{range}.pbf"
    private const val FONT_STACK = "IRANYekan Regular"
    private const val FALLBACK_PMTILES_URL =
        "https://baran.tfs.co.ir/tiles/iran-20260827-1857.pmtiles"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun load(context: Context): String = withContext(Dispatchers.IO) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val dark = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        val hostedStyle = runCatching { fetchHostedStyle() }
            .onSuccess { preferences.edit().putString(PREF_CACHED_STYLE_JSON, it).apply() }
            .getOrNull()
        if (hostedStyle != null) return@withContext hostedStyle

        preferences.getString(PREF_CACHED_STYLE_JSON, null)
            ?.takeIf { runCatching { validateHostedStyle(it) }.isSuccess }
            ?.let { return@withContext it }

        val cachedUrl = preferences.getString(PREF_CACHED_PMTILES_URL, null)
        val pmtilesUrl = runCatching { fetchCurrentPmtilesUrl() }
            .onSuccess { preferences.edit().putString(PREF_CACHED_PMTILES_URL, it).apply() }
            .getOrElse { cachedUrl ?: FALLBACK_PMTILES_URL }
        createStyleJson(
            pmtilesUrl = pmtilesUrl,
            dark = dark,
        )
    }

    private fun fetchHostedStyle(): String {
        val request = Request.Builder().url(STYLE_URL).get().build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Baran style HTTP ${response.code}" }
            val body = response.body?.string()?.trimStart('\uFEFF')
                ?: error("Baran style response is empty")
            validateHostedStyle(body)
            return body
        }
    }

    private fun validateHostedStyle(styleJson: String) {
        val style = JSONObject(styleJson)
        require(style.optInt("version") == 8) { "Unsupported Baran style version" }
        require(style.optJSONArray("layers")?.length()?.let { it > 0 } == true) {
            "Baran style has no layers"
        }
        val sourceUrl = style.optJSONObject("sources")
            ?.optJSONObject("baran")
            ?.optString("url")
            .orEmpty()
        require(sourceUrl.startsWith("pmtiles://https://baran.tfs.co.ir/tiles/")) {
            "Unexpected Baran style source URL"
        }
    }

    private fun fetchCurrentPmtilesUrl(): String {
        val request = Request.Builder().url(MANIFEST_URL).get().build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Baran manifest HTTP ${response.code}" }
            val body = response.body?.string()?.trimStart('\uFEFF')
                ?: error("Baran manifest response is empty")
            return JSONObject(body).getString("pmtilesUrl").also { url ->
                require(url.startsWith("https://baran.tfs.co.ir/tiles/")) {
                    "Unexpected Baran PMTiles URL"
                }
            }
        }
    }

    internal fun createStyleJson(pmtilesUrl: String, dark: Boolean): String {
        val background = if (dark) "#17211f" else "#f4f1e8"
        val landcover = if (dark) "#26382f" else "#dce8cf"
        val landuse = if (dark) "#293631" else "#e7eadf"
        val park = if (dark) "#234432" else "#cde8c7"
        val water = if (dark) "#183a4b" else "#b7dff2"
        val building = if (dark) "#3d4542" else "#d9d1c7"
        val roadCasing = if (dark) "#111817" else "#c8c2b7"
        val road = if (dark) "#66716d" else "#ffffff"
        val boundary = if (dark) "#8b7777" else "#9a7777"
        val label = if (dark) "#d5e2de" else "#40504c"
        val labelHalo = if (dark) "#17211f" else "#ffffff"
        val sourceUrl = JSONObject.quote("pmtiles://$pmtilesUrl")

        return """
            {
              "version": 8,
              "name": "FaraZaman Baran",
              "glyphs": "$GLYPHS_URL",
              "sources": {
                "baran": { "type": "vector", "url": $sourceUrl, "minzoom": 0, "maxzoom": 15 }
              },
              "layers": [
                { "id": "background", "type": "background", "paint": { "background-color": "$background" } },
                { "id": "landcover", "type": "fill", "source": "baran", "source-layer": "landcover", "paint": { "fill-color": "$landcover", "fill-opacity": 0.72 } },
                { "id": "landuse", "type": "fill", "source": "baran", "source-layer": "landuse", "paint": { "fill-color": "$landuse", "fill-opacity": 0.66 } },
                { "id": "park", "type": "fill", "source": "baran", "source-layer": "park", "paint": { "fill-color": "$park", "fill-opacity": 0.78 } },
                { "id": "water", "type": "fill", "source": "baran", "source-layer": "water", "paint": { "fill-color": "$water" } },
                { "id": "waterway", "type": "line", "source": "baran", "source-layer": "waterway", "paint": { "line-color": "$water", "line-width": ["interpolate", ["linear"], ["zoom"], 6, 0.5, 15, 3.0] } },
                { "id": "building", "type": "fill", "source": "baran", "source-layer": "building", "minzoom": 13, "paint": { "fill-color": "$building", "fill-opacity": 0.82, "fill-outline-color": "$roadCasing" } },
                { "id": "boundary", "type": "line", "source": "baran", "source-layer": "boundary", "paint": { "line-color": "$boundary", "line-width": ["interpolate", ["linear"], ["zoom"], 4, 0.5, 12, 1.5], "line-dasharray": [3, 2] } },
                { "id": "road-casing", "type": "line", "source": "baran", "source-layer": "transportation", "minzoom": 5, "layout": { "line-cap": "round", "line-join": "round" }, "paint": { "line-color": "$roadCasing", "line-width": ["interpolate", ["linear"], ["zoom"], 5, 1.0, 12, 3.5, 15, 8.0] } },
                { "id": "road", "type": "line", "source": "baran", "source-layer": "transportation", "minzoom": 5, "layout": { "line-cap": "round", "line-join": "round" }, "paint": { "line-color": "$road", "line-width": ["interpolate", ["linear"], ["zoom"], 5, 0.5, 12, 2.0, 15, 6.0] } },
                { "id": "water-label", "type": "symbol", "source": "baran", "source-layer": "water_name", "minzoom": 7, "layout": { "text-field": ["coalesce", ["get", "name:fa"], ["get", "name:nonlatin"], ["get", "name"], ["get", "name:latin"]], "text-font": ["$FONT_STACK"], "text-size": ["interpolate", ["linear"], ["zoom"], 7, 10, 14, 14], "symbol-placement": "point", "text-padding": 3 }, "paint": { "text-color": "#397d9b", "text-halo-color": "$labelHalo", "text-halo-width": 1.5 } },
                { "id": "waterway-label", "type": "symbol", "source": "baran", "source-layer": "waterway", "minzoom": 11, "layout": { "symbol-placement": "line", "symbol-spacing": 220, "text-field": ["coalesce", ["get", "name:fa"], ["get", "name:nonlatin"], ["get", "name"], ["get", "name:latin"]], "text-font": ["$FONT_STACK"], "text-size": 11, "text-keep-upright": true }, "paint": { "text-color": "#397d9b", "text-halo-color": "$labelHalo", "text-halo-width": 1.5 } },
                { "id": "landuse-label", "type": "symbol", "source": "baran", "source-layer": "landuse", "minzoom": 11, "layout": { "text-field": ["coalesce", ["get", "name:fa"], ["get", "name:nonlatin"], ["get", "name"], ["get", "name:latin"]], "text-font": ["$FONT_STACK"], "text-size": 11, "text-max-width": 9, "text-padding": 4 }, "paint": { "text-color": "$label", "text-halo-color": "$labelHalo", "text-halo-width": 1.5 } },
                { "id": "park-label", "type": "symbol", "source": "baran", "source-layer": "park", "minzoom": 10, "layout": { "text-field": ["coalesce", ["get", "name:fa"], ["get", "name:nonlatin"], ["get", "name"], ["get", "name:latin"]], "text-font": ["$FONT_STACK"], "text-size": 11, "text-max-width": 9, "text-padding": 4 }, "paint": { "text-color": "#287052", "text-halo-color": "$labelHalo", "text-halo-width": 1.5 } },
                { "id": "mountain-peak-label", "type": "symbol", "source": "baran", "source-layer": "mountain_peak", "minzoom": 8, "layout": { "text-field": ["coalesce", ["get", "name:fa"], ["get", "name:nonlatin"], ["get", "name"], ["get", "name:latin"]], "text-font": ["$FONT_STACK"], "text-size": 11, "text-offset": [0, 0.6], "text-anchor": "top" }, "paint": { "text-color": "$label", "text-halo-color": "$labelHalo", "text-halo-width": 1.5 } },
                { "id": "aerodrome-label", "type": "symbol", "source": "baran", "source-layer": "aerodrome_label", "minzoom": 8, "layout": { "text-field": ["coalesce", ["get", "name:fa"], ["get", "name:nonlatin"], ["get", "name"], ["get", "name:latin"], ["get", "iata"]], "text-font": ["$FONT_STACK"], "text-size": 11, "text-max-width": 9 }, "paint": { "text-color": "$label", "text-halo-color": "$labelHalo", "text-halo-width": 1.5 } },
                { "id": "place-label", "type": "symbol", "source": "baran", "source-layer": "place", "minzoom": 3, "layout": { "text-field": ["coalesce", ["get", "name:fa"], ["get", "name:nonlatin"], ["get", "name"], ["get", "name:latin"]], "text-font": ["$FONT_STACK"], "text-size": ["interpolate", ["linear"], ["zoom"], 3, 11, 8, 13, 14, 16], "text-max-width": 10, "text-padding": 4, "text-allow-overlap": false }, "paint": { "text-color": "$label", "text-halo-color": "$labelHalo", "text-halo-width": 1.8, "text-halo-blur": 0.5 } },
                { "id": "road-label", "type": "symbol", "source": "baran", "source-layer": "transportation_name", "minzoom": 8, "layout": { "symbol-placement": "line", "symbol-spacing": 120, "text-field": ["coalesce", ["get", "name:fa"], ["get", "name:nonlatin"], ["get", "name"], ["get", "name:latin"], ["get", "ref"]], "text-font": ["$FONT_STACK"], "text-size": ["interpolate", ["linear"], ["zoom"], 8, 11, 12, 13, 15, 15, 18, 17], "text-max-angle": 45, "text-padding": 1, "text-keep-upright": true, "text-allow-overlap": true, "text-ignore-placement": false }, "paint": { "text-color": "$label", "text-halo-color": "$labelHalo", "text-halo-width": 2.4, "text-halo-blur": 0.5 } },
                { "id": "road-label-fallback", "type": "symbol", "source": "baran", "source-layer": "transportation", "minzoom": 10, "layout": { "symbol-placement": "line", "symbol-spacing": 180, "text-field": ["coalesce", ["get", "name:fa"], ["get", "name:nonlatin"], ["get", "name"], ["get", "name:latin"], ["get", "ref"]], "text-font": ["$FONT_STACK"], "text-size": ["interpolate", ["linear"], ["zoom"], 10, 11, 15, 15, 18, 17], "text-max-angle": 45, "text-padding": 2, "text-keep-upright": true, "text-allow-overlap": false }, "paint": { "text-color": "$label", "text-halo-color": "$labelHalo", "text-halo-width": 2.4, "text-halo-blur": 0.5 } },
                { "id": "poi-label", "type": "symbol", "source": "baran", "source-layer": "poi", "minzoom": 12, "layout": { "text-field": ["coalesce", ["get", "name:fa"], ["get", "name:nonlatin"], ["get", "name"], ["get", "name:latin"]], "text-font": ["$FONT_STACK"], "text-size": ["interpolate", ["linear"], ["zoom"], 12, 10, 16, 12], "text-max-width": 9, "text-offset": [0, 0.8], "text-anchor": "top", "text-padding": 3, "text-allow-overlap": false }, "paint": { "text-color": "$label", "text-halo-color": "$labelHalo", "text-halo-width": 1.5, "text-halo-blur": 0.5 } },
                { "id": "building-label", "type": "symbol", "source": "baran", "source-layer": "building", "minzoom": 16, "layout": { "text-field": ["coalesce", ["get", "name:fa"], ["get", "name:nonlatin"], ["get", "name"], ["get", "name:latin"]], "text-font": ["$FONT_STACK"], "text-size": 10, "text-max-width": 8, "text-padding": 2 }, "paint": { "text-color": "$label", "text-halo-color": "$labelHalo", "text-halo-width": 1.2 } },
                { "id": "house-number", "type": "symbol", "source": "baran", "source-layer": "housenumber", "minzoom": 16, "layout": { "text-field": ["coalesce", ["get", "housenumber"], ["get", "house_number"], ["get", "name"]], "text-font": ["$FONT_STACK"], "text-size": 9, "text-padding": 1 }, "paint": { "text-color": "$label", "text-halo-color": "$labelHalo", "text-halo-width": 1 } }
              ]
            }
        """.trimIndent()
    }
}
