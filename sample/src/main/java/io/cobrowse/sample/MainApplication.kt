package io.cobrowse.sample

import android.app.Application
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import io.cobrowse.CobrowseIO
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

        with(CobrowseIO.instance()) {
            license("trial")
            customData(buildMap<String, String> {
                put(CobrowseIO.USER_EMAIL_KEY, "android@demo.com")
                put(CobrowseIO.DEVICE_NAME_KEY, "Android Demo")
            })
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