package Ir.co.tfs.farazaman.domain.road

import org.junit.Assert.assertEquals
import org.junit.Test

class RoadWidthCategoryTest {
    @Test
    fun narrowRoadClasses_areUpTo15Meters() {
        val classes = listOf(
            "residential", "living_street", "service", "unclassified", "track",
            "track_grade1", "track_grade2", "track_grade3", "track_grade4", "track_grade5",
            "pedestrian", "footway", "cycleway", "path", "bridleway", "steps", "unknown",
        )
        classes.forEach { fclass ->
            assertEquals(RoadWidthCategory.UP_TO_15_METERS, RoadWidthCategory.fromFclass(fclass))
        }
    }

    @Test
    fun mediumRoadClasses_areOver15To25Meters() {
        listOf("tertiary", "secondary", "secondary_link").forEach { fclass ->
            assertEquals(RoadWidthCategory.OVER_15_TO_25_METERS, RoadWidthCategory.fromFclass(fclass))
        }
    }

    @Test
    fun largeRoadClasses_areOver25To45Meters() {
        listOf("primary", "primary_link", "trunk_link").forEach { fclass ->
            assertEquals(RoadWidthCategory.OVER_25_TO_45_METERS, RoadWidthCategory.fromFclass(fclass))
        }
    }

    @Test
    fun widestRoadClasses_areOver45Meters() {
        listOf("trunk", "motorway", "motorway_link").forEach { fclass ->
            assertEquals(RoadWidthCategory.OVER_45_METERS, RoadWidthCategory.fromFclass(fclass))
        }
    }

    @Test
    fun valuesAreNormalized_andUnexpectedValuesUseNarrowFallback() {
        assertEquals(
            RoadWidthCategory.OVER_45_METERS,
            RoadWidthCategory.fromFclass(" Motorway-Link "),
        )
        assertEquals(RoadWidthCategory.UP_TO_15_METERS, RoadWidthCategory.fromFclass(null))
        assertEquals(RoadWidthCategory.UP_TO_15_METERS, RoadWidthCategory.fromFclass("custom"))
    }
}
