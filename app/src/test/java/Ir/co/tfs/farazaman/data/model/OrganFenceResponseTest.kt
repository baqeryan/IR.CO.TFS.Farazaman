package Ir.co.tfs.farazaman.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class OrganFenceResponseTest {

    @Test
    fun `parses fence WKT from GisLayersDataFenceOrgan details response`() {
        val json = """
            {
              "dataModel": {
                "gisLayersDataId": 2,
                "name": "منطقه 1 ناحیه 1",
                "organId": 8002,
                "gisLayersData": {
                  "gisLayersDataId": 2,
                  "gisLayersId": 1080001,
                  "name": "محدوده منطقه 1 ناحیه 1",
                  "isDeleted": false,
                  "gisLayersDataWellKnownText": {
                    "gisLayersDataId": 2,
                    "wellKnownText": "POLYGON ((52.674116 36.578894, 52.674245 36.57902, 52.674116 36.578894))"
                  }
                }
              }
            }
        """.trimIndent()

        val response = Gson().fromJson(json, OrganFenceResponse::class.java)
        val dataModel = assertNotNull(response.dataModel).let { response.dataModel!! }
        val layer = assertNotNull(dataModel.gisLayersData).let { dataModel.gisLayersData!! }

        assertEquals(8002, dataModel.organId)
        assertEquals("منطقه 1 ناحیه 1", dataModel.name)
        assertEquals("محدوده منطقه 1 ناحیه 1", layer.name)
        assertEquals(
            "POLYGON ((52.674116 36.578894, 52.674245 36.57902, 52.674116 36.578894))",
            layer.wellKnownTextData?.wellKnownText,
        )
    }
}
