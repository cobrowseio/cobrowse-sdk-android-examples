package io.cobrowse.sample.compose.ui.transactions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.cobrowse.cobrowseUnredacted

private const val TAG = "TransactionWebView"

/**
 * Displays the details of a transaction, a web page. Links to other pages are passed to
 * [onOpenUrl] instead of being opened in place.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TransactionWebView(
    url: String,
    onOpenUrl: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoading by remember(url) { mutableStateOf(true) }
    val currentOnOpenUrl by rememberUpdatedState(onOpenUrl)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        key(url) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                request: WebResourceRequest
                            ): Boolean {
                                when (request.url.scheme) {
                                    "tel", "sms", "mailto" -> {
                                        view.context.invokeViewIntent(request.url)
                                        return true
                                    }
                                }
                                if (request.isForMainFrame && !request.isRedirect) {
                                    currentOnOpenUrl(request.url)
                                    return true
                                }
                                return false
                            }

                            override fun onPageFinished(view: WebView, url: String?) {
                                isLoading = false
                            }
                        }
                        loadUrl(url)
                    }
                },
                onRelease = { it.destroy() },
                modifier = Modifier
                    .fillMaxSize()
                    // The web page is visible even with redaction by default. Sensitive
                    // elements are hidden by `CobrowseIO.webviewRedactedViews()`.
                    .cobrowseUnredacted()
            )
        }
        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}

private fun Context.invokeViewIntent(uri: Uri) {
    try {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, uri), null))
    } catch (e: Exception) {
        Log.e(TAG, "Cannot open a URI of the scheme ${uri.scheme}", e)
    }
}
