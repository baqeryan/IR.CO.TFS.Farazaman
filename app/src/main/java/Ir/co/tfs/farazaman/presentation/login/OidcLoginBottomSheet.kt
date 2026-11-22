package Ir.co.tfs.farazaman.presentation.login

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.auth.OidcAuthManager
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse

class OidcLoginBottomSheet : BottomSheetDialogFragment() {

    var onAuthSuccess: ((AuthorizationResponse) -> Unit)? = null
    var onAuthCancelled: (() -> Unit)? = null
    var onAuthError: ((AuthorizationException) -> Unit)? = null

    private var authRequest: AuthorizationRequest? = null
    private var authHandled = false
    private var cancelNotified = false
    private var loadingProgress by mutableIntStateOf(0)
    private var sheetBehavior: BottomSheetBehavior<View>? = null

    companion object {
        private const val TAG = "OidcLoginBottomSheet"
        private const val ARG_AUTH_URL = "auth_url"
        private const val ARG_AUTH_REQUEST_JSON = "auth_request_json"

        fun newInstance(
            authUrl: String,
            authRequestJson: String,
        ): OidcLoginBottomSheet {
            return OidcLoginBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_AUTH_URL, authUrl)
                    putString(ARG_AUTH_REQUEST_JSON, authRequestJson)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val authUrl = arguments?.getString(ARG_AUTH_URL).orEmpty()
        authRequest = readAuthRequest()
        if (authUrl.isBlank() || authRequest == null) {
            Log.e(TAG, "Missing auth URL or request")
            dismissAllowingStateLoss()
        }
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FaraZamanTheme {
                    OidcLoginScreen(
                        title = getString(R.string.login_oidc_title),
                        progress = loadingProgress,
                        onHandleTouch = { pressed ->
                            sheetBehavior?.isDraggable = pressed
                        },
                        createWebView = { context ->
                            WebView(context).also { webView ->
                                setupWebView(webView)
                                clearWebSession {
                                    if (!isAdded) return@clearWebSession
                                    Log.d(TAG, "Loading OIDC login page")
                                    webView.loadUrl(authUrl)
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog ?: return
        dialog.setCanceledOnTouchOutside(false)
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?: return
        bottomSheet.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        val sheetHeight = (resources.displayMetrics.heightPixels * 0.88f).toInt()
        bottomSheet.layoutParams = bottomSheet.layoutParams.apply {
            height = sheetHeight
        }
        sheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isHideable = true
            isDraggable = false
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState != BottomSheetBehavior.STATE_DRAGGING) {
                        isDraggable = false
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
            })
        }
    }

    override fun onDestroyView() {
        sheetBehavior = null
        super.onDestroyView()
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        notifyCancelled()
    }

    override fun onCancel(dialog: android.content.DialogInterface) {
        super.onCancel(dialog)
        notifyCancelled()
    }

    private fun notifyCancelled() {
        if (!authHandled && !cancelNotified) {
            cancelNotified = true
            onAuthCancelled?.invoke()
        }
    }

    private fun readAuthRequest(): AuthorizationRequest? {
        val json = arguments?.getString(ARG_AUTH_REQUEST_JSON).orEmpty()
        if (json.isBlank()) return null
        return try {
            AuthorizationRequest.jsonDeserialize(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize authorization request", e)
            null
        }
    }

    private fun clearWebSession(onComplete: () -> Unit) {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            removeAllCookies { _ ->
                onComplete()
            }
        }
    }

    private fun setupWebView(webView: WebView) {
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            userAgentString = buildBrowserUserAgent(userAgentString)
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                loadingProgress = if (newProgress in 1..99) newProgress else 0
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return handleRedirect(request.url)
            }

            @Deprecated("Deprecated in API 24")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return handleRedirect(Uri.parse(url))
            }
        }
    }

    private fun buildBrowserUserAgent(defaultUserAgent: String): String {
        return defaultUserAgent
            .replace("; wv", "")
            .replace(Regex("Version/\\d+\\.\\d+ "), "")
    }

    private fun handleRedirect(uri: Uri): Boolean {
        if (!isOidcRedirectUri(uri)) {
            return false
        }

        authHandled = true
        val request = authRequest
        if (request == null) {
            Log.e(TAG, "Redirect received but authorization request is missing")
            onAuthError?.invoke(
                AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW,
            )
            dismissAllowingStateLoss()
            return true
        }

        Log.d(TAG, "OIDC redirect received: $uri")

        if (uri.getQueryParameter(AuthorizationException.PARAM_ERROR) != null) {
            val exception = AuthorizationException.fromOAuthRedirect(uri)
            Log.e(TAG, "OIDC redirect error: ${exception.error} - ${exception.errorDescription}")
            onAuthError?.invoke(exception)
            dismissAllowingStateLoss()
            return true
        }

        val response = AuthorizationResponse.Builder(request)
            .fromUri(uri)
            .build()
        onAuthSuccess?.invoke(response)
        dismissAllowingStateLoss()
        return true
    }

    private fun isOidcRedirectUri(uri: Uri): Boolean {
        val redirect = OidcAuthManager.REDIRECT_URI
        return uri.scheme.equals(redirect.scheme, ignoreCase = true) &&
            uri.authority == redirect.authority
    }
}
