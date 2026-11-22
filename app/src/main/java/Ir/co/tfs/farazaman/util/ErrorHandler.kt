package Ir.co.tfs.farazaman.util

import android.content.Context
import android.widget.Toast
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response as RetrofitResponse

object ErrorHandler {
    fun handleHttpError(response: Response, context: Context, defaultErrorMessage: String) {
        val message = when (response.code) {
            400 -> messageFromBody(response.code, response.body?.string(), defaultErrorMessage)
            401 -> "خطای 401: نشست کاربری معتبر نیست؛ دوباره وارد شوید"
            403 -> "خطای 403: دسترسی مجاز نیست"
            404 -> "خطای 404: منبع موردنظر یافت نشد"
            500 -> "خطای 500: خطای سرور"
            502 -> "خطای 502: سرور در دسترس نیست"
            503 -> "خطای 503: سرویس موقتاً در دسترس نیست"
            else -> defaultErrorMessage
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun handleRetrofitHttpError(
        response: RetrofitResponse<*>,
        context: Context,
        defaultErrorMessage: String,
    ) {
        val message = when (response.code()) {
            400 -> retrofitErrorMessage(response, defaultErrorMessage)
            401 -> "خطای 401: نشست کاربری معتبر نیست؛ دوباره وارد شوید"
            403 -> "خطای 403: دسترسی مجاز نیست"
            404 -> "خطای 404: منبع موردنظر یافت نشد"
            500 -> "خطای 500: خطای سرور"
            502 -> "خطای 502: سرور در دسترس نیست"
            503 -> "خطای 503: سرویس موقتاً در دسترس نیست"
            else -> defaultErrorMessage
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun retrofitErrorMessage(response: RetrofitResponse<*>, defaultErrorMessage: String): String =
        messageFromBody(
            response.code(),
            runCatching { response.errorBody()?.string() }.getOrNull(),
            defaultErrorMessage,
        )

    fun messageFromBody(code: Int, body: String?, defaultErrorMessage: String): String {
        if (code != 400) return defaultErrorMessage
        val fallbackDetail = "درخواست نامعتبر"
        val content = body?.trim().orEmpty()
        val detail = if (content.isBlank()) {
            fallbackDetail
        } else if (!content.startsWith("{") && !content.startsWith("[")) {
            sanitize(content).ifBlank { fallbackDetail }
        } else {
            runCatching {
                val messages = linkedSetOf<String>()
                if (content.startsWith("{")) collectMessages(JSONObject(content), messages)
                else collectMessages(JSONArray(content), messages)
                messages.joinToString("؛ ").takeIf(String::isNotBlank)
            }.getOrNull() ?: sanitize(content).ifBlank { fallbackDetail }
        }
        return "خطای $code: $detail"
    }

    private fun collectMessages(value: Any?, output: MutableSet<String>, traverseAllKeys: Boolean = false) {
        when (value) {
            is JSONObject -> {
                val messageKeys = listOf("message", "error_description", "errorMessage", "detail", "title", "error")
                messageKeys
                    .forEach { key -> if (value.has(key)) collectMessages(value.opt(key), output) }
                value.opt("errors")?.let { collectMessages(it, output, traverseAllKeys = true) }
                if (traverseAllKeys) {
                    value.keys().forEach { key ->
                        if (key !in messageKeys && key != "errors") collectMessages(value.opt(key), output, true)
                    }
                }
            }
            is JSONArray -> repeat(value.length()) { collectMessages(value.opt(it), output, traverseAllKeys) }
            is String -> sanitize(value).takeIf(String::isNotBlank)?.let(output::add)
        }
    }

    private fun sanitize(value: String): String = value
        .replace(Regex("<[^>]+>"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(MAX_USER_ERROR_LENGTH)

    fun handleNetworkError(exception: Exception, context: Context, defaultErrorMessage: String) {
        val message = when (exception) {
            is java.net.UnknownHostException -> "خطا در اتصال به سرور؛ اتصال اینترنت را بررسی کنید"
            is java.net.SocketTimeoutException -> "زمان انتظار برای پاسخ سرور به پایان رسید"
            is java.net.ConnectException -> "خطا در اتصال به سرور"
            else -> defaultErrorMessage
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private const val MAX_USER_ERROR_LENGTH = 800
}
