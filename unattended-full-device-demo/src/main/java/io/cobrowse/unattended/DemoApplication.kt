package io.cobrowse.unattended

import android.app.Activity
import android.app.Application
import io.cobrowse.CobrowseIO
import io.cobrowse.Session

private const val CONSENT_COUNTDOWN_SECONDS = 5

/**
 * Runs a Cobrowse session unattended. Session consent is a countdown shown over
 * whatever is on screen, and every other consent step is approved automatically.
 * The only prompt left is Android's own screen capture dialog, which the SDK
 * accepts itself while the accessibility service is running.
 */
class DemoApplication : Application(),
    CobrowseIO.SessionLoadDelegate,
    CobrowseIO.SessionRequestDelegate,
    CobrowseIO.FullDeviceRequestDelegate,
    CobrowseIO.RemoteControlRequestDelegate {

    // Lets the foreground activity refresh when session state changes.
    var onSessionChanged: (() -> Unit)? = null

    private lateinit var overlay: SessionOverlay

    override fun onCreate() {
        super.onCreate()
        overlay = SessionOverlay(this)
        with(CobrowseIO.instance()) {
            license("trial")
            customData(mapOf(CobrowseIO.DEVICE_NAME_KEY to "Unattended Full Device Demo"))
            this.setDelegate(this@DemoApplication)
            start()
        }
    }

    override fun sessionDidLoad(session: Session) {
        session.setFullDevice(Session.FullDeviceState.Requested, null)
        onSessionChanged?.invoke()
    }

    // Session consent is handled from sessionDidUpdate so it works while the app is in
    // the background. The SDK only calls this delegate when an Activity is in the
    // foreground; implementing it stops the SDK showing its own dialog or launching the app.
    override fun handleSessionRequest(activity: Activity?, session: Session) = Unit

    override fun handleFullDeviceRequest(activity: Activity?, session: Session) {
        // Automatically accept the full-device consent prompt.
        // The Android OS's capture dialog still follows but will be auto-accepted by the
        // SDK if the accessibility service is running.
        session.setFullDevice(Session.FullDeviceState.On, null)
    }

    override fun handleRemoteControlRequest(activity: Activity?, session: Session) {
        // Automatically accept the remote control consent prompt.
        session.setRemoteControl(Session.RemoteControlState.On, null)
    }

    override fun sessionDidUpdate(session: Session) {
        when {
            session.isAuthorizing -> overlay.showConsent(
                seconds = CONSENT_COUNTDOWN_SECONDS,
                onAccept = { session.activate(null) },
                onDecline = { session.end(null) })
            session.isActive -> {
                // Keep remote control on for the whole session so the agent never has to request it.
                session.setRemoteControl(Session.RemoteControlState.On, null)
                overlay.showActive { session.end(null) }
            }
            else -> overlay.hide()
        }
        onSessionChanged?.invoke()
    }

    override fun sessionDidEnd(session: Session) {
        overlay.hide()
        onSessionChanged?.invoke()
    }
}
