package io.cobrowse.sample.core

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

/**
 * Opens the privacy policy in a Custom Tab.
 */
fun Context.openPrivacyPolicy() {
    CustomTabsIntent.Builder()
        .build()
        .launchUrl(this, getString(R.string.privacy_policy_url).toUri())
}
