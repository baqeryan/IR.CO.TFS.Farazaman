package Ir.co.tfs.farazaman.domain.road

enum class RoadWidthCategory {
    UP_TO_15_METERS,
    OVER_15_TO_25_METERS,
    OVER_25_TO_45_METERS,
    OVER_45_METERS;

    companion object {
        fun fromFclass(fclass: String?): RoadWidthCategory {
            val normalized = fclass.orEmpty().trim().lowercase().replace('-', '_')
            return when {
                normalized in OVER_45_CLASSES -> OVER_45_METERS
                normalized in OVER_25_TO_45_CLASSES -> OVER_25_TO_45_METERS
                normalized in OVER_15_TO_25_CLASSES -> OVER_15_TO_25_METERS
                else -> UP_TO_15_METERS
            }
        }

        private val OVER_15_TO_25_CLASSES = setOf(
            "tertiary",
            "secondary",
            "secondary_link",
        )
        private val OVER_25_TO_45_CLASSES = setOf(
            "primary",
            "primary_link",
            "trunk_link",
        )
        private val OVER_45_CLASSES = setOf(
            "trunk",
            "motorway",
            "motorway_link",
        )
    }
}
