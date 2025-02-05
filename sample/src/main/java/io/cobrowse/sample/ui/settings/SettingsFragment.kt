package io.cobrowse.sample.ui.settings

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.cobrowse.CobrowseAccessibilityService
import io.cobrowse.sample.R
import io.cobrowse.sample.ui.CobrowseViewModelFactory

/**
 * Fragment that allows user to modify persistent preferences.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, CobrowseViewModelFactory())
            .get(SettingsViewModel::class.java)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<Preference?>("accessibilityService")?.let { button ->
            button.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                activity?.let {
                    if (CobrowseAccessibilityService.isRunning()) {
                        openAccessibilitySettings()
                        return@OnPreferenceClickListener true
                    }

                    with(AlertDialog.Builder(it).create()) {
                        this.setCancelable(false)
                        this.setTitle(getString(R.string.accessibility_warning_title))
                        this.setMessage(getString(R.string.accessibility_warning_message))
                        this.setCancelable(true)
                        this.setButton(
                            DialogInterface.BUTTON_NEGATIVE,
                            getString(R.string.accessibility_warning_button_cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        this.setButton(
                            DialogInterface.BUTTON_POSITIVE,
                            getString(R.string.accessibility_warning_button_ok)) { dialog, _ ->
                            dialog.dismiss()
                            openAccessibilitySettings()
                        }
                        this.show()
                    }
                }
                return@OnPreferenceClickListener true
            }
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}