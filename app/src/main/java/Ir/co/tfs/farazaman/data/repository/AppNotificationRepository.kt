package Ir.co.tfs.farazaman.data.repository

import Ir.co.tfs.farazaman.data.model.AppNotificationDto
import Ir.co.tfs.farazaman.data.model.NotificationIndexRequest
import Ir.co.tfs.farazaman.data.model.NotificationPageRequest
import Ir.co.tfs.farazaman.data.model.NotificationPaginationRequest
import Ir.co.tfs.farazaman.service.remote.AppNotificationApi
import Ir.co.tfs.farazaman.AppConstants
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationRepository @Inject constructor(
    private val preferences: SharedPreferences,
    private val okHttpClient: OkHttpClient,
) {
    suspend fun loadPage(page: Int, pageSize: Int = PAGE_SIZE): NotificationPage {
        val result = api().getNotifications(
            NotificationIndexRequest(
                pagination = NotificationPaginationRequest(
                    NotificationPageRequest(currentPage = page, pageSize = pageSize),
                ),
            ),
        )
        val pageInfo = result.pagination?.page
        return NotificationPage(
            items = result.data,
            currentPage = pageInfo?.currentPage ?: page,
            hasMore = pageInfo?.let { it.currentPage < it.pageCount } ?: (result.data.size >= pageSize),
            unreadCount = result.response?.unreadCount,
        )
    }

    suspend fun unreadCount(): Int = api().getUnreadCount().unreadCount.coerceAtLeast(0)
    suspend fun details(id: Int): AppNotificationDto = api().getDetails(id)
    suspend fun visit(id: Int): AppNotificationDto = api().markAsRead(id)

    suspend fun delete(id: Int) {
        val response = api().delete(listOf(id))
        if (!response.isSuccessful) throw retrofit2.HttpException(response)
    }

    private fun api(): AppNotificationApi {
        val baseUrl = preferences.getString("BASE_URL", AppConstants.BASE_URL)
            ?.trim().orEmpty().ifBlank { AppConstants.BASE_URL }
            .let { if (it.endsWith('/')) it else "$it/" }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AppNotificationApi::class.java)
    }

    data class NotificationPage(
        val items: List<AppNotificationDto>,
        val currentPage: Int,
        val hasMore: Boolean,
        val unreadCount: Int?,
    )

    companion object { const val PAGE_SIZE = 20 }
}
