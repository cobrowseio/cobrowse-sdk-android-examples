package io.cobrowse.sample.data

import android.net.Uri
import io.cobrowse.CobrowseIO
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.URLDecoder

/**
 * Applies `https://cbrws.io/...` deep links to the Cobrowse.io SDK. Values are URL-encoded.
 *
 * To open a link with adb:
 * ```
 * adb shell 'am start -a android.intent.action.VIEW -d "https://cbrws.io/license/trial" io.cobrowse.demo.compose'
 * ```
 */
object CobrowseDeepLinks {

    /**
     * Applies the deep link. Returns `false` if the link is not supported.
     */
    fun handle(uri: Uri): Boolean {
        return when (uri.pathSegments.firstOrNull()) {
            "api" -> updateApi(uri)
            "license" -> updateLicense(uri)
            "data" -> updateCustomData(uri)
            "s" -> startSession(uri)
            "code" -> startSession(uri)
            "demo" -> setupForDemo(uri)
            else -> false
        }
    }

    /**
     * Checks if the deep link sets up a demo, which expects the user to be logged in.
     */
    fun isDemo(uri: Uri): Boolean = uri.pathSegments.firstOrNull() == "demo"

    /**
     * Accepts URLs like `https://cbrws.io/api/https%3A%2F%2Fcobrowse.io`
     */
    private fun updateApi(uri: Uri): Boolean {
        uri.pathSegments.drop(1).firstOrNull()?.let {
            with(CobrowseIO.instance()) {
                stop()
                api(decode(it))
                start()
                return true
            }
        }
        return false
    }

    /**
     * Accepts URLs like `https://cbrws.io/license/trial`
     */
    private fun updateLicense(uri: Uri): Boolean {
        uri.pathSegments.drop(1).firstOrNull()?.let {
            with(CobrowseIO.instance()) {
                stop()
                license(decode(it))
                start()
                return true
            }
        }
        return false
    }

    /**
     * Accepts URLs like `https://cbrws.io/data?firstKey=firstValue&secondKey=secondValue`
     */
    private fun updateCustomData(uri: Uri): Boolean {
        uri.toString().toHttpUrlOrNull()?.let { url ->
            val data = url.queryParameterNames.associateWith { url.queryParameter(decode(it)) }
            with(CobrowseIO.instance()) {
                customData(data)
                return true
            }
        }
        return false
    }

    /**
     * Accepts URLs like `https://cbrws.io/s?id=abc` or `https://cbrws.io/code/000000`
     */
    private fun startSession(uri: Uri): Boolean {
        when (uri.pathSegments.firstOrNull()) {
            "s" -> {
                uri.toString().toHttpUrlOrNull()?.let { url ->
                    url.queryParameter("id")?.let { sessionId ->
                        return startSession(decode(sessionId))
                    }
                }
            }
            "code" -> {
                uri.pathSegments.drop(1).firstOrNull()?.let { code ->
                    return startSession(code)
                }
            }
            else -> {}
        }
        return false
    }

    /**
     * Accepts URLs like `https://cbrws.io/demo/someDemoId?api=https%3A%2F%2Fcobrowse.io&license=trial`
     */
    private fun setupForDemo(uri: Uri): Boolean {
        with(CobrowseIO.instance()) {
            stop()
            uri.toString().toHttpUrlOrNull()?.let { url ->
                url.queryParameter("api")?.let { api(decode(it)) }
                url.queryParameter("license")?.let { license(decode(it)) }
                url.queryParameterNames
                    .filter { it != "api" && it != "license" }
                    .associateWith { url.queryParameter(decode(it)) }
                    .toMutableMap()
                    .let {
                        uri.pathSegments.drop(1).firstOrNull()?.let { demoId ->
                            it["demo_id"] = decode(demoId)
                        }
                        customData(it.toMap())
                    }
            }
            start()
        }
        return true
    }

    private fun decode(it: String?): String = URLDecoder.decode(it, "UTF-8")

    private fun startSession(idOrCode: String): Boolean {
        with(CobrowseIO.instance()) {
            stop()
            start()
            getSession(idOrCode, null)
        }
        return true
    }
}
