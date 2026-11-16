package io.cobrowse.sample.compose.ui

import android.content.Context
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.cobrowse.sample.compose.CobrowseSessionDelegate
import io.cobrowse.sample.core.R

/**
 * Whether the whole UI is hidden from the agent, except for the unredacted composables.
 */
object RedactionByDefault {
    var isEnabled by mutableStateOf(false)
        private set

    fun load(context: Context) {
        isEnabled = context.preferences.getBoolean(KEY, false)
    }

    fun set(context: Context, enabled: Boolean) {
        context.preferences.edit().putBoolean(KEY, enabled).apply()
        isEnabled = enabled
    }

    private const val KEY = "isRedactionByDefaultEnabled"

    /**
     * The default shared preferences, where the `sample` app keeps the same setting.
     */
    private val Context.preferences
        get() = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
}

/**
 * A toolbar action that ends the active Cobrowse.io session. It is shown only during a session.
 */
@Composable
fun EndSessionAction() {
    val session = CobrowseSessionDelegate.current
    if (session?.isActive == true) {
        IconButton(onClick = { session.end(null) }) {
            Icon(
                painter = painterResource(R.drawable.ic_action_cancel_presentation),
                contentDescription = stringResource(R.string.end_cobrowse_session)
            )
        }
    }
}
