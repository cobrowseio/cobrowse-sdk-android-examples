package io.cobrowse.sample.compose.ui.login

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.cobrowse.sample.compose.R
import io.cobrowse.sample.compose.ui.CobrowseViewModelFactory
import io.cobrowse.sample.compose.ui.EndSessionAction
import io.cobrowse.sample.compose.ui.SampleTopAppBar
import io.cobrowse.sample.core.openPrivacyPolicy

/**
 * A screen with the login form.
 */
@Composable
fun LoginScreen(
    viewModelFactory: CobrowseViewModelFactory,
    onLoginSuccess: () -> Unit
) {
    val viewModel: LoginViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Scaffold(
        topBar = {
            SampleTopAppBar(
                title = stringResource(R.string.app_name),
                actions = { EndSessionAction() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(R.string.app_name)
                )

                Text(
                    text = stringResource(io.cobrowse.sample.core.R.string.login_welcome),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = { viewModel.onUsernameChange(it) },
                    label = { Text(stringResource(io.cobrowse.sample.core.R.string.prompt_email)) },
                    singleLine = true,
                    isError = uiState.usernameError != null,
                    supportingText = uiState.usernameError?.let { { Text(stringResource(it)) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .fillMaxWidth()
                        .testTag("username")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = { Text(stringResource(io.cobrowse.sample.core.R.string.prompt_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = uiState.passwordError != null,
                    supportingText = uiState.passwordError?.let { { Text(stringResource(it)) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { viewModel.login() }),
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .fillMaxWidth()
                        .testTag("password")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.login() },
                    enabled = uiState.username.isNotBlank() && uiState.password.isNotEmpty()
                ) {
                    Text(stringResource(io.cobrowse.sample.core.R.string.action_sign_in))
                }

                uiState.errorMessage?.let {
                    Text(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Text(
                    text = annotatedStringResource(io.cobrowse.sample.core.R.string.login_welcome_secondary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = { context.openPrivacyPolicy() }) {
                    Text(stringResource(io.cobrowse.sample.core.R.string.privacy_policy))
                }
            }
        }
    }
}

/**
 * Loads a string resource with its styling, like `<b>` and `<small>`.
 */
@Composable
private fun annotatedStringResource(@StringRes id: Int): AnnotatedString {
    val text = LocalContext.current.resources.getText(id)
    return remember(text) {
        val spanned = text as? Spanned ?: return@remember AnnotatedString(text.toString())
        buildAnnotatedString {
            append(spanned.toString())
            spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
                val start = spanned.getSpanStart(span)
                val end = spanned.getSpanEnd(span)
                when (span) {
                    is StyleSpan -> if (span.style == Typeface.BOLD) {
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                    }
                    is RelativeSizeSpan -> addStyle(SpanStyle(fontSize = span.sizeChange.em), start, end)
                }
            }
        }
    }
}

@Preview(widthDp = 1280, heightDp = 720)
@Composable
fun LoginScreenPreview() {
    LoginScreen(CobrowseViewModelFactory()) {
    }
}
