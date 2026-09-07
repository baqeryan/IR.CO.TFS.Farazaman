package Ir.co.tfs.farazaman.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BusinessActorRoleTest {

    @Test
    fun `all service actor roles are retained with Persian display names`() {
        val expected = mapOf(
            "MunicipalContractSupervisor" to "ناظر قرارداد",
            "MunicipalContractInspector" to "بازرس قرارداد",
            "AssetDriver" to "راننده",
            "Contractor" to "پیمانکار",
            "WorkStationOperator" to "اپراتور ایستگاه",
            "Citizen" to "شهروند",
            "ContractorManagingDirector" to "مدیرعامل",
            "PropertyOwner" to "مالک ملک",
            "AssetVehicleOwner" to "مالک خودرو",
        )

        assertEquals(
            expected,
            BusinessActorRole.entries.associate { it.serviceKey to it.displayNameFa },
        )
    }

    @Test
    fun `response resolves known actor role keys case insensitively`() {
        val response = UserRolesResponse(
            actorRoles = mapOf(
                "contractor" to "پیمانکار",
                "MunicipalContractInspector" to "بازرس قرارداد",
                "UnknownRole" to "نامشخص",
            ),
        )

        assertEquals(
            setOf(BusinessActorRole.Contractor, BusinessActorRole.MunicipalContractInspector),
            response.businessActorRoles(),
        )
    }
}
