package io.cobrowse.sample.compose.ui.account

import androidx.lifecycle.ViewModel
import io.cobrowse.CobrowseIO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Starts the Agent Present mode: a "reverse" screensharing session where an agent uses this app
 * to share their screen.
 */
class AgentPresentViewModel : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    /**
     * Whether a present code is being checked.
     */
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _codeResults = Channel<Boolean>(Channel.BUFFERED)
    /**
     * Whether each submitted code was accepted. The channel keeps a result until the screen
     * collects it, even if the screen is recreated in the meantime.
     */
    val codeResults: Flow<Boolean> = _codeResults.receiveAsFlow()

    fun initiateSession(code: String) {
        // Only one code is checked at a time
        if (!_isSubmitting.compareAndSet(expect = false, update = true)) return

        CobrowseIO.instance().getSession(code) { error, session ->
            if (error == null) {
                // Presenting mode has been activated successfully
                session?.setCapabilities(arrayOfNulls(0), null)
            }
            _isSubmitting.value = false
            _codeResults.trySend(error == null)
        }
    }
}
