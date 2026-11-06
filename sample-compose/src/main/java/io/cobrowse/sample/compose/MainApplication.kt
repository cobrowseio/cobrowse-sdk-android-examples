package io.cobrowse.sample.compose

import android.app.Application
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import io.cobrowse.CobrowseIO
import io.cobrowse.sample.compose.ui.ConsentPrompt
import io.cobrowse.sample.compose.ui.RedactionByDefault
import io.cobrowse.sample.data.CobrowseSessionDelegate
import io.cobrowse.sample.data.getAndroidLogTag

/**
 * Android application class.
 */
class MainApplication : Application() {

    @Suppress("PrivatePropertyName")
    private val Any.TAG: String
        get() = javaClass.getAndroidLogTag()

    override fun onCreate() {
        super.onCreate()

        CobrowseSessionDelegate.consentPrompt = { request, _ ->
            ConsentPrompt.request = request
        }
        // The root composable is redacted instead, so parts of it can be unredacted
        CobrowseSessionDelegate.redactWindowByDefault = false
        RedactionByDefault.load(this)

        with(CobrowseIO.instance()) {
            license("trial")
            customData(buildMap<String, String> {
                put(CobrowseIO.USER_EMAIL_KEY, "android@demo.com")
                put(CobrowseIO.DEVICE_NAME_KEY, "Android Compose Demo")
            })
            // Composables are matched by their Modifier.testTag(), which the Cobrowse.io
            // Gradle plugin passes to the SDK as the id of the composable
            redactedViews(arrayOf(
                "#username", "#password",
                "#account_name", "#account_email",
                "#transaction_title", "#transaction_subtitle", "#transaction_amount",
                "#balance", "#total_spent",
                // The root composable, only with redaction by default
                "#app_redacted"
            ))
            // Visible even with redaction by default
            unredactedViews(arrayOf(
                "#balance_header", "#spending_chart", "#total_spent_header", "#total_spent_footer",
                "#transaction_web_view"
            ))
            webviewRedactedViews(arrayOf(
                "#title",
                "#amount",
                "#subtitle",
                "#map"
            ))
            setDelegate(CobrowseSessionDelegate.getInstance())
            start()
        }

        // If using Firebase Messaging to start sessions please include your own `google-services.json`
        if (FirebaseApp.getApps(this).size > 0) {
            // The Firebase installation ID is delivered to MainFirebaseMessagingService.onRegistered()
            FirebaseMessaging.getInstance().register().addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "FCM registration failed", task.exception)
                }
            })
        } else {
            Log.w(TAG, "Firebase app is not initialized. Did you copy your `google-services.json` file?")
        }
    }
}
