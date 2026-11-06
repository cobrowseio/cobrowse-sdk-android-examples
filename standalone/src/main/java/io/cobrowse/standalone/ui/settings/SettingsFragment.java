package io.cobrowse.standalone.ui.settings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import io.cobrowse.standalone.CobrowsePreferences;
import io.cobrowse.standalone.LocalNetwork;
import io.cobrowse.standalone.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_PERMISSION_ACCESS_LOCAL_NETWORK = 49;

    private SettingsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        Preference api = findPreference(CobrowsePreferences.KEY_API);
        if (api != null) {
            api.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue instanceof String && LocalNetwork.isLocalUrl((String) newValue)) {
                    requestLocalNetworkPermission();
                }
                return true;
            });
        }
    }

    private void requestLocalNetworkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.CINNAMON_BUN) {
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_LOCAL_NETWORK)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[] {Manifest.permission.ACCESS_LOCAL_NETWORK},
                    REQUEST_PERMISSION_ACCESS_LOCAL_NETWORK);
        }
    }
}