package io.cobrowse.sample.compose

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import io.cobrowse.CobrowseIO
import io.cobrowse.Session
import io.cobrowse.sample.data.ConsentRequest

/**
 * Global delegate of Cobrowse.io sessions. Exposes the current session and the agent's requests
 * as Compose state.
 */
object CobrowseSessionDelegate : CobrowseIO.Delegate, CobrowseIO.SessionRequestDelegate,
    CobrowseIO.RemoteControlRequestDelegate, CobrowseIO.FullDeviceRequestDelegate {

    /**
     * The current session, if any. The SDK updates the same object in place, so every update
     * is treated as a new value.
     */
    var current: Session? by mutableStateOf(null, neverEqualPolicy())
        private set

    /**
     * The agent's request waiting for the user's consent, if any.
     */
    var consentRequest: ConsentRequest? by mutableStateOf(null)

    override fun sessionDidUpdate(session: Session) {
        current = session
    }

    override fun sessionDidEnd(session: Session) {
        current = null
    }

    override fun handleSessionRequest(activity: Activity?, session: Session) {
        current = session
        ask(ConsentRequest.Screenshare)
    }

    override fun handleRemoteControlRequest(activity: Activity?, session: Session) {
        ask(ConsentRequest.RemoteControl)
    }

    override fun handleFullDeviceRequest(activity: Activity?, session: Session) {
        ask(ConsentRequest.FullDevice)
    }

    /**
     * Like the dialog of the View-based sample, ignores a new request while another one is
     * waiting for the user's answer, so that one is not lost. A request the agent has withdrawn
     * is replaced, though: [io.cobrowse.sample.compose.ui.ConsentDialog] clears it only when it
     * is composed, which doesn't happen while the activity is stopped.
     */
    private fun ask(request: ConsentRequest) {
        val waiting = consentRequest
        val session = current
        if (waiting == null || session == null || !waiting.isPending(session)) {
            consentRequest = request
        }
    }

    /**
     * Helper function to get the delegate object in a more obvious way.
     */
    fun getInstance(): CobrowseSessionDelegate {
        return this
    }
}
