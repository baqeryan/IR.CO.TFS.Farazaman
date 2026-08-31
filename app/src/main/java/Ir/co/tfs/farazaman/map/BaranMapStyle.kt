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
            dark = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES,
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

    private fun createStyleJson(pmtilesUrl: String, dark: Boolean): String {
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
                { "id": "place-label", "type": "symbol", "source": "baran", "source-layer": "place", "minzoom": 3, "layout": { "text-field": ["coalesce", ["get", "name"], ["get", "name:nonlatin"], ["get", "name:latin"]], "text-font": ["$FONT_STACK"], "text-size": ["interpolate", ["linear"], ["zoom"], 3, 11, 8, 13, 14, 16], "text-max-width": 10, "text-allow-overlap": false }, "paint": { "text-color": "$label", "text-halo-color": "$labelHalo", "text-halo-width": 1.5, "text-halo-blur": 0.5 } },
                { "id": "road-label", "type": "symbol", "source": "baran", "source-layer": "transportation_name", "minzoom": 11, "layout": { "symbol-placement": "line", "symbol-spacing": 300, "text-field": ["coalesce", ["get", "name"], ["get", "name:nonlatin"], ["get", "name:latin"], ["get", "ref"]], "text-font": ["$FONT_STACK"], "text-size": ["interpolate", ["linear"], ["zoom"], 11, 10, 15, 13], "text-max-angle": 30, "text-keep-upright": true, "text-allow-overlap": false }, "paint": { "text-color": "$label", "text-halo-color": "$labelHalo", "text-halo-width": 2, "text-halo-blur": 0.5 } },
                { "id": "poi-label", "type": "symbol", "source": "baran", "source-layer": "poi", "minzoom": 14, "layout": { "text-field": ["coalesce", ["get", "name"], ["get", "name:nonlatin"], ["get", "name:latin"]], "text-font": ["$FONT_STACK"], "text-size": 11, "text-max-width": 9, "text-offset": [0, 0.8], "text-anchor": "top", "text-allow-overlap": false }, "paint": { "text-color": "$label", "text-halo-color": "$labelHalo", "text-halo-width": 1.5, "text-halo-blur": 0.5 } }
              ]
            }
        """.trimIndent()
    }
}
