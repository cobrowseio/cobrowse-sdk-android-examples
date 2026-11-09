package io.cobrowse.sample.compose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.cobrowse.sample.compose.CobrowseSessionDelegate
import io.cobrowse.sample.core.R
import io.cobrowse.sample.data.ConsentRequest

/**
 * Asks the user to consent to the agent's request, if there is one.
 */
@Composable
fun ConsentDialog() {
    val request = CobrowseSessionDelegate.consentRequest ?: return
    val session = CobrowseSessionDelegate.current

    // The agent withdrew the request, or the session ended
    if (session == null || !request.isPending(session)) {
        SideEffect { CobrowseSessionDelegate.consentRequest = null }
        return
    }

    fun answer(allowed: Boolean) {
        if (allowed) request.allow(session) else request.deny(session)
        CobrowseSessionDelegate.consentRequest = null
    }

    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(request.heading)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(request.body))
                Text(stringResource(request.terms))
            }
        },
        confirmButton = {
            TextButton(onClick = { answer(allowed = true) }) {
                Text(stringResource(R.string.consent_allow))
            }
        },
        dismissButton = {
            TextButton(onClick = { answer(allowed = false) }) {
                Text(stringResource(R.string.consent_deny))
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}
