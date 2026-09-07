package Ir.co.tfs.farazaman.service.remote

import Ir.co.tfs.farazaman.data.model.AppNotificationDto
import Ir.co.tfs.farazaman.data.model.NotificationIndexRequest
import Ir.co.tfs.farazaman.data.model.NotificationIndexResponse
import Ir.co.tfs.farazaman.data.model.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AppNotificationApi {
    @POST("api/AppNotification/Index")
    suspend fun getNotifications(@Body request: NotificationIndexRequest): NotificationIndexResponse

    @GET("api/AppNotification/UnreadCount")
    suspend fun getUnreadCount(): UnreadCountResponse

    @GET("api/AppNotification/Details/{id}")
    suspend fun getDetails(@Path("id") id: Int): AppNotificationDto

    @POST("api/AppNotification/Visit/{id}")
    suspend fun markAsRead(@Path("id") id: Int): AppNotificationDto

    @POST("api/AppNotification/Delete")
    suspend fun delete(@Body ids: List<Int>): Response<Unit>
}
