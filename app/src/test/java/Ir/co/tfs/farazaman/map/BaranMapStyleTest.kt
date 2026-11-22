package Ir.co.tfs.farazaman.map

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BaranMapStyleTest {
    @Test
    fun completeStyleIncludesPersianRoadAndContextLabels() {
        val style = JSONObject(
            BaranMapStyle.createStyleJson(
                pmtilesUrl = "https://baran.tfs.co.ir/tiles/test.pmtiles",
                dark = false,
            ),
        )
        val layers = style.getJSONArray("layers")
        val layersById = (0 until layers.length())
            .associate { index ->
                layers.getJSONObject(index).let { it.getString("id") to it }
            }

        assertEquals(
            "https://baran.tfs.co.ir/map-assets/glyphs/{fontstack}/{range}.pbf",
            style.getString("glyphs"),
        )
        assertEquals(8.0, layersById.getValue("road-label").getDouble("minzoom"), 0.0)
        assertEquals(
            "transportation",
            layersById.getValue("road-label-fallback").getString("source-layer"),
        )
        assertEquals(12.0, layersById.getValue("poi-label").getDouble("minzoom"), 0.0)
        assertTrue(layersById.getValue("road-label").toString().contains("name:fa"))
        assertTrue(layersById.getValue("road-label").toString().contains("IRANYekan Regular"))
        assertTrue(
            setOf(
                "water-label",
                "waterway-label",
                "landuse-label",
                "park-label",
                "mountain-peak-label",
                "aerodrome-label",
                "building-label",
                "house-number",
            ).all(layersById::containsKey),
        )
    }

    @Test
    fun completeStyleUsesDarkPaletteWhenRequested() {
        val style = JSONObject(
            BaranMapStyle.createStyleJson(
                pmtilesUrl = "https://baran.tfs.co.ir/tiles/test.pmtiles",
                dark = true,
            ),
        )
        val background = style.getJSONArray("layers").getJSONObject(0)
        assertEquals("#17211f", background.getJSONObject("paint").getString("background-color"))
    }
}
