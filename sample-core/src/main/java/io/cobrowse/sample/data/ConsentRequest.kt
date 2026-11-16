package io.cobrowse.sample.data

import androidx.annotation.StringRes
import io.cobrowse.Session
import io.cobrowse.sample.core.R

/**
 * A request from the agent that requires the user's consent.
 */
enum class ConsentRequest(
    @StringRes val heading: Int,
    @StringRes val body: Int,
    @StringRes val terms: Int
) {
    Screenshare(
        R.string.consent_session_heading,
        R.string.consent_session_body,
        R.string.consent_session_terms
    ),
    RemoteControl(
        R.string.consent_remote_control_heading,
        R.string.consent_remote_control_body,
        R.string.consent_remote_control_terms
    ),
    FullDevice(
        R.string.consent_full_device_heading,
        R.string.consent_full_device_body,
        R.string.consent_full_device_terms
    );

    fun isPending(session: Session) = when (this) {
        // The SDK asks for consent while the session is authorizing, not pending
        Screenshare -> session.isAuthorizing
        RemoteControl -> session.remoteControl() == Session.RemoteControlState.Requested
        FullDevice -> session.fullDevice() == Session.FullDeviceState.Requested
    }

    fun allow(session: Session) = when (this) {
        Screenshare -> session.activate(null)
        RemoteControl -> session.setRemoteControl(Session.RemoteControlState.On, null)
        FullDevice -> session.setFullDevice(Session.FullDeviceState.On, null)
    }

    fun deny(session: Session) = when (this) {
        Screenshare -> session.end(null)
        RemoteControl -> session.setRemoteControl(Session.RemoteControlState.Rejected, null)
        FullDevice -> session.setFullDevice(Session.FullDeviceState.Rejected, null)
    }
}
