package io.cobrowse.sample.compose.ui.account

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.cobrowse.sample.compose.ui.CobrowseViewModelFactory
import io.cobrowse.sample.compose.ui.EndSessionAction
import io.cobrowse.sample.compose.ui.SampleTopAppBar
import io.cobrowse.sample.compose.ui.rememberCobrowseSession
import io.cobrowse.sample.core.R

private const val CODE_LENGTH = 6

/**
 * Implements the Cobrowse.io Agent Present mode: an agent enters their present code and uses
 * this app to share their screen within a "reverse" screensharing session.
 */
@Composable
fun AgentPresentScreen(
    viewModelFactory: CobrowseViewModelFactory,
    onBack: () -> Unit
) {
    val viewModel: AgentPresentViewModel = viewModel(factory = viewModelFactory)
    val session by rememberCobrowseSession()
    val isPresenting = session?.isActive == true

    var digits by remember { mutableStateOf(List(CODE_LENGTH) { "" }) }
    var isSubmitting by remember { mutableStateOf(false) }
    val focusRequesters = remember { List(CODE_LENGTH) { FocusRequester() } }
    val shake = remember { Animatable(0f) }

    fun reset() {
        digits = List(CODE_LENGTH) { "" }
        isSubmitting = false
    }

    LaunchedEffect(Unit) {
        viewModel.codeRejected.collect {
            repeat(3) {
                shake.animateTo(-10f, tween(70, easing = LinearEasing))
                shake.animateTo(10f, tween(70, easing = LinearEasing))
            }
            shake.animateTo(0f, tween(35, easing = LinearEasing))
            reset()
            focusRequesters.first().requestFocus()
        }
    }

    LaunchedEffect(session == null, isPresenting) {
        if (session == null || isPresenting) reset()
        if (!isPresenting) focusRequesters.first().requestFocus()
    }

    fun onDigitChange(index: Int, value: String) {
        val digit = value.filter(Char::isDigit).takeLast(1)
        digits = digits.toMutableList().also { it[index] = digit }
        when {
            digit.isEmpty() -> if (index > 0) focusRequesters[index - 1].requestFocus()
            index < CODE_LENGTH - 1 -> focusRequesters[index + 1].requestFocus()
            digits.all { it.isNotEmpty() } -> {
                isSubmitting = true
                viewModel.initiateSession(digits.joinToString(""))
            }
        }
    }

    Scaffold(
        topBar = {
            SampleTopAppBar(
                title = stringResource(R.string.fragment_agent_present_title),
                onBack = onBack,
                actions = { EndSessionAction() }
            )
        }
    ) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            if (isPresenting) {
                Image(
                    painter = painterResource(R.drawable.logo_presentation),
                    contentDescription = null
                )
            }

            Text(
                text = stringResource(
                    if (isPresenting) R.string.agent_present_welcome_you_are_presenting
                    else R.string.agent_present_welcome
                ),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            if (!isPresenting) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .offset { IntOffset(shake.value.dp.roundToPx(), 0) }
                ) {
                    digits.forEachIndexed { index, digit ->
                        OutlinedTextField(
                            value = digit,
                            onValueChange = { onDigitChange(index, it) },
                            // Not disabled, so the boxes keep the focus while the code is checked
                            readOnly = isSubmitting,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                            placeholder = {
                                Text(
                                    text = "${index + 1}",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier
                                .width(48.dp)
                                .focusRequester(focusRequesters[index])
                                .onPreviewKeyEvent { event ->
                                    // Move back to the previous box if this one is already empty
                                    if (event.type == KeyEventType.KeyDown
                                        && event.key == Key.Backspace
                                        && digit.isEmpty()
                                        && index > 0) {
                                        focusRequesters[index - 1].requestFocus()
                                        true
                                    } else false
                                }
                        )
                    }
                }
            }
        }
    }
}
