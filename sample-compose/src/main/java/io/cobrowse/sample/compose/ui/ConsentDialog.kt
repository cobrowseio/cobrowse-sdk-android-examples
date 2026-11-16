package io.cobrowse.sample.compose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.cobrowse.sample.compose.CobrowseSessionDelegate
import io.cobrowse.sample.core.R
import io.cobrowse.sample.data.ConsentRequest

/**
 * Asks the user to consent to the agent's request, if there is one. Looks like the consent
 * card of the View-based sample.
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

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        ConsentCard(
            request = request,
            onDeny = { answer(allowed = false) },
            onAllow = { answer(allowed = true) },
            modifier = Modifier
                .padding(horizontal = dimensionResource(R.dimen.consent_card_margin))
                .widthIn(max = dimensionResource(R.dimen.consent_card_width))
                .fillMaxWidth()
        )
    }
}

@Composable
private fun ConsentCard(
    request: ConsentRequest,
    onDeny: () -> Unit,
    onAllow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = colorResource(R.color.consentTextColor)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colorResource(R.color.consentCardBackground),
        shadowElevation = 8.dp
    ) {
        Column {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(request.heading),
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 21.sp,
                    letterSpacing = 0.sp
                )
                Text(
                    text = stringResource(request.body),
                    modifier = Modifier.padding(top = 7.dp),
                    color = textColor,
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    letterSpacing = 0.sp
                )
                Text(
                    text = stringResource(request.terms),
                    modifier = Modifier.padding(top = 8.dp),
                    color = colorResource(R.color.consentTermsTextColor),
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    letterSpacing = 0.sp
                )
            }

            HorizontalDivider(color = colorResource(R.color.consentDivider))

            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val buttonModifier = Modifier
                    .weight(1f)
                    .height(44.dp)

                TextButton(
                    onClick = onDeny,
                    modifier = buttonModifier,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(R.color.consentDenyColor)
                    )
                ) {
                    ConsentButtonText(stringResource(R.string.consent_deny))
                }
                Button(
                    onClick = onAllow,
                    modifier = buttonModifier,
                    shape = RoundedCornerShape(9.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.consentAllowColor),
                        contentColor = colorResource(R.color.consentAllowTextColor)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    ConsentButtonText(stringResource(R.string.consent_allow))
                }
            }
        }
    }
}

// Like the text of the Material Components buttons in the View-based sample
@Composable
private fun ConsentButtonText(text: String) {
    Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.0892857.em)
}
