package Ir.co.tfs.farazaman.presentation.compose.track

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackScreensContractTest {
    @Test
    fun `track list events carry only semantic identifiers`() {
        val event = TrackListEvent.Open(42L)
        assertEquals(42L, (event as TrackListEvent.Open).id)
    }

    @Test
    fun `track detail state keeps operation flags independent`() {
        val state = TrackDetailUiState(1L, "مسیر", "امروز", 10, 2, exporting = true)
        assertEquals(true, state.exporting)
        assertEquals(false, state.uploading)
    }
}
