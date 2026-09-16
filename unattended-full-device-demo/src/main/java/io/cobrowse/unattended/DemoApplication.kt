package io.cobrowse.unattended

import android.app.Activity
import android.app.Application
import io.cobrowse.CobrowseIO
import io.cobrowse.Session

/**
 * Auto-approves every Cobrowse consent step so a session runs unattended.
 * The only prompt left is Android's own screen capture dialog, which the
 * OS shows each time full-device capture starts and which cannot be skipped.
 */
class DemoApplication : Application(),
    CobrowseIO.SessionLoadDelegate,
    CobrowseIO.SessionRequestDelegate,
    CobrowseIO.FullDeviceRequestDelegate,
    CobrowseIO.RemoteControlRequestDelegate {

    // Lets the foreground activity refresh when session state changes.
    var onSessionChanged: (() -> Unit)? = null

    override fun onCreate() {
        super.onCreate()
        with(CobrowseIO.instance()) {
            license("trial") // insert your license here
            customData(mapOf(CobrowseIO.DEVICE_NAME_KEY to "Unattended Full Device Demo"))
            this.setDelegate(this@DemoApplication)
            start()
        }
    }

    override fun sessionDidLoad(session: Session) {
        session.setFullDevice(Session.FullDeviceState.Requested, null)
        onSessionChanged?.invoke()
    }

    override fun handleSessionRequest(activity: Activity?, session: Session) {
        // Automatically accept the session request - Could be replaced with a countdown UI
        session.activate(null)
    }

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
        // Keep remote control on for the whole session so the agent never has to request it.
        if (session.isActive) session.setRemoteControl(Session.RemoteControlState.On, null)
        onSessionChanged?.invoke()
    }

    override fun sessionDidEnd(session: Session) {
        onSessionChanged?.invoke()
    }
}
