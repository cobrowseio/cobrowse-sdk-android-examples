package io.cobrowse.sample.compose.ui.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.cobrowse.cobrowseRedacted
import io.cobrowse.sample.compose.ui.CobrowseViewModelFactory
import io.cobrowse.sample.compose.ui.EndSessionAction
import io.cobrowse.sample.compose.ui.SampleTopAppBar
import io.cobrowse.sample.compose.ui.openPrivacyPolicy
import io.cobrowse.sample.compose.ui.rememberCobrowseSession
import io.cobrowse.sample.core.R

/**
 * Displays the user who is currently logged in, and lets them share a session code or start
 * the Agent Present mode.
 */
@Composable
fun AccountScreen(
    viewModelFactory: CobrowseViewModelFactory,
    onAgentPresent: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: AccountViewModel = viewModel(factory = viewModelFactory)
    val sessionCode by viewModel.sessionCode.collectAsStateWithLifecycle()
    val session by rememberCobrowseSession()
    val isSessionActive = session?.isActive == true
    val context = LocalContext.current

    // The session code is no longer needed once the session has started or ended
    LaunchedEffect(isSessionActive, session == null) {
        if (isSessionActive || session == null) viewModel.clearSessionCode()
    }

    Scaffold(
        topBar = {
            SampleTopAppBar(
                title = stringResource(R.string.fragment_account_title),
                onBack = onBack,
                actions = {
                    EndSessionAction()
                    IconButton(onClick = onSettings) {
                        Icon(
                            painter = painterResource(R.drawable.ic_action_settings),
                            contentDescription = stringResource(R.string.menu_item_settings)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.logo_account),
                contentDescription = null
            )

            Text(
                text = viewModel.user?.displayName.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                modifier = Modifier.cobrowseRedacted()
            )

            Text(
                text = viewModel.user?.email.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.cobrowseRedacted()
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = sessionCode,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                modifier = Modifier.alpha(if (sessionCode.isEmpty()) 0f else 1f)
            )

            // Keep the buttons' space while hidden, so the screen doesn't jump
            val sessionButtonsAlpha = if (isSessionActive) 0f else 1f
            Button(
                onClick = { viewModel.requestSessionCode() },
                enabled = !isSessionActive,
                modifier = Modifier
                    .width(BUTTON_WIDTH)
                    .alpha(sessionButtonsAlpha)
            ) {
                Text(stringResource(R.string.button_get_session_code))
            }

            FilledTonalButton(
                onClick = onAgentPresent,
                enabled = !isSessionActive,
                modifier = Modifier
                    .width(BUTTON_WIDTH)
                    .alpha(sessionButtonsAlpha)
            ) {
                Text(stringResource(R.string.button_agent_present))
            }

            TextButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.width(BUTTON_WIDTH)
            ) {
                Text(stringResource(R.string.log_out))
            }

            TextButton(
                onClick = { context.openPrivacyPolicy() },
                modifier = Modifier.width(BUTTON_WIDTH)
            ) {
                Text(stringResource(R.string.privacy_policy))
            }
        }
    }
}

private val BUTTON_WIDTH = 220.dp
