package Ir.co.tfs.farazaman.data.model

import com.google.gson.annotations.SerializedName

data class NotificationIndexRequest(
    @SerializedName("Request") val request: NotificationFilter = NotificationFilter(),
    @SerializedName("Pagination") val pagination: NotificationPaginationRequest = NotificationPaginationRequest(),
)

data class NotificationFilter(
    @SerializedName("Ids") val ids: List<Int>? = null,
    @SerializedName("NotificationTypeIds") val notificationTypeIds: List<Short>? = null,
    @SerializedName("IsRead") val isRead: Boolean? = null,
    @SerializedName("MinCreateDate") val minCreateDate: String? = null,
    @SerializedName("MaxCreateDate") val maxCreateDate: String? = null,
    @SerializedName("Term") val term: String? = null,
)

data class NotificationPaginationRequest(
    @SerializedName("Page") val page: NotificationPageRequest = NotificationPageRequest(),
)

data class NotificationPageRequest(
    @SerializedName("CurrentPage") val currentPage: Int = 1,
    @SerializedName("PageSize") val pageSize: Int = 20,
    @SerializedName("SortExpression") val sortExpression: String = "AppNotificationID desc",
)

data class NotificationIndexResponse(
    @SerializedName("Data") val data: List<AppNotificationDto> = emptyList(),
    @SerializedName("Pagination") val pagination: NotificationPaginationResponse? = null,
    @SerializedName("Response") val response: UnreadCountResponse? = null,
)

data class UnreadCountResponse(
    @SerializedName("UnreadCount") val unreadCount: Int = 0,
)

data class NotificationPaginationResponse(
    @SerializedName("Page") val page: NotificationPageResponse? = null,
)

data class NotificationPageResponse(
    @SerializedName("CurrentPage") val currentPage: Int = 1,
    @SerializedName("PageCount") val pageCount: Int = 1,
    @SerializedName("TotalCount") val totalCount: Int = 0,
    @SerializedName("PageSize") val pageSize: Int = 20,
    @SerializedName("Skip") val skip: Int = 0,
    @SerializedName("Take") val take: Int = 20,
    @SerializedName("SortExpression") val sortExpression: String? = null,
)

data class AppNotificationDto(
    @SerializedName("AppNotificationID") val id: Int,
    @SerializedName("Title") val title: String,
    @SerializedName("Link") val link: String? = null,
    @SerializedName("CreateDate") val createDate: String,
    @SerializedName("ExpireDate") val expireDate: String? = null,
    @SerializedName("Type") val type: String? = null,
    @SerializedName("CssClass") val cssClass: String? = null,
    @SerializedName("Role") val role: Int? = null,
    @SerializedName("UserName") val userName: String? = null,
    @SerializedName("AppDbWorkItemId") val appDbWorkItemId: Long? = null,
    @SerializedName("AppNotificationTypeId") val notificationTypeId: Short? = null,
    @SerializedName("LastModifiedTime") val lastModifiedTime: String? = null,
    @SerializedName("AppNotificationTargetTypeId") val targetTypeId: Short? = null,
    @SerializedName("TargetTypeId") val targetId: String? = null,
) {
    val isRead: Boolean get() = lastModifiedTime != null
}
