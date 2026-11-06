package io.cobrowse.sample.compose.ui.account

import androidx.lifecycle.ViewModel
import io.cobrowse.CobrowseIO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Starts the Agent Present mode: a "reverse" screensharing session where an agent uses this app
 * to share their screen.
 */
class AgentPresentViewModel : ViewModel() {

    private val _codeRejected = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /**
     * Emits when the present code is not accepted.
     */
    val codeRejected: SharedFlow<Unit> = _codeRejected.asSharedFlow()

    fun initiateSession(code: String) {
        CobrowseIO.instance().getSession(code) { error, session ->
            if (error == null) {
                // Presenting mode has been activated successfully
                session?.setCapabilities(arrayOfNulls(0), null)
            } else {
                _codeRejected.tryEmit(Unit)
            }
        }
    }
}
