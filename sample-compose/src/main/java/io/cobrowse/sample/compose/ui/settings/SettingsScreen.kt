package io.cobrowse.sample.compose.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.cobrowse.CobrowseAccessibilityService
import io.cobrowse.sample.compose.ui.RedactionByDefault
import io.cobrowse.sample.compose.ui.SampleTopAppBar
import io.cobrowse.sample.core.R

/**
 * Lets the user change the Cobrowse.io SDK configuration of the app.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var showAccessibilityWarning by remember { mutableStateOf(false) }

    fun openAccessibilitySettings() {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    Scaffold(
        topBar = {
            SampleTopAppBar(
                title = stringResource(R.string.settings_category_cobrowse),
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            val redactionByDefault = RedactionByDefault.isEnabled
            ListItem(
                leadingContent = {
                    Icon(painterResource(R.drawable.ic_eye), contentDescription = null)
                },
                headlineContent = { Text(stringResource(R.string.settings_item_unredaction)) },
                supportingContent = {
                    Text(stringResource(
                        if (redactionByDefault) R.string.settings_item_unredaction_summary_on
                        else R.string.settings_item_unredaction_summary_off
                    ))
                },
                trailingContent = {
                    Switch(
                        checked = redactionByDefault,
                        onCheckedChange = { RedactionByDefault.set(context, it) }
                    )
                },
                modifier = Modifier.clickable {
                    RedactionByDefault.set(context, !redactionByDefault)
                }
            )
            ListItem(
                leadingContent = {
                    Icon(painterResource(R.drawable.ic_settings_remote), contentDescription = null)
                },
                headlineContent = { Text(stringResource(R.string.button_activate_accessibility_service)) },
                supportingContent = {
                    Text(stringResource(R.string.button_activate_accessibility_service_description))
                },
                modifier = Modifier.clickable {
                    if (CobrowseAccessibilityService.isRunning()) {
                        openAccessibilitySettings()
                    } else {
                        showAccessibilityWarning = true
                    }
                }
            )
        }
    }

    if (showAccessibilityWarning) {
        AlertDialog(
            onDismissRequest = { showAccessibilityWarning = false },
            title = { Text(stringResource(R.string.accessibility_warning_title)) },
            text = {
                Text(
                    text = stringResource(R.string.accessibility_warning_message),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showAccessibilityWarning = false
                    openAccessibilitySettings()
                }) {
                    Text(stringResource(R.string.accessibility_warning_button_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibilityWarning = false }) {
                    Text(stringResource(R.string.accessibility_warning_button_cancel))
                }
            }
        )
    }
}
