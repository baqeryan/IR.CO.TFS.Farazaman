package Ir.co.tfs.farazaman.service.remote

import Ir.co.tfs.farazaman.data.model.UserRolesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface RolesService {
    @GET("api/AccountUser/Roles")
    fun getUserRoles(
        @Header("Authorization") token: String
    ): Call<UserRolesResponse>
}

