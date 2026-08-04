package io.cobrowse.sample.compose.ui.account

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.cobrowse.sample.compose.ui.CobrowseViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModelFactory: CobrowseViewModelFactory,
    onLogout: () -> Unit,
    onBack: () -> Unit = {}
) {
    val viewModel: AccountViewModel = viewModel(factory = viewModelFactory)
    val context = LocalContext.current
    var sessionCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(io.cobrowse.sample.core.R.string.fragment_account_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack,
                            contentDescription = stringResource(
                                io.cobrowse.sample.compose.R.string.icon_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_myplaces),
                    contentDescription = "Account",
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = viewModel.user?.displayName ?: "User",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = viewModel.user?.email ?: "user@example.com",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (sessionCode.isNotEmpty()) {
                    Text(
                        text = sessionCode,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        /* TODO: session code functionality */
                        sessionCode = viewModel.getSessionCode()
                    },
                    modifier = Modifier.width(220.dp)
                ) {
                    Text(stringResource(io.cobrowse.sample.core.R.string.button_get_session_code))
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { /* TODO: Agent present mode functionality */ },
                    modifier = Modifier.width(220.dp)
                ) {
                    Text(stringResource(io.cobrowse.sample.core.R.string.button_agent_present))
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.width(220.dp)
                ) {
                    Text(stringResource( io.cobrowse.sample.core.R.string.log_out))
                }

                Spacer(modifier = Modifier.height(16.dp))

                val policy = stringResource(io.cobrowse.sample.core.R.string.privacy_policy_url)
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(policy))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.width(220.dp)
                ) {
                    Text(stringResource(io.cobrowse.sample.core.R.string.privacy_policy))
                }
            }
        }
    }
}

