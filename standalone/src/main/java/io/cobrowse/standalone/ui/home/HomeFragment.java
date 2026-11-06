package io.cobrowse.standalone.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import io.cobrowse.standalone.CobrowsePreferences;
import io.cobrowse.standalone.LocalNetwork;
import io.cobrowse.standalone.R;

public class HomeFragment extends Fragment {

    private static final int REQUEST_PERMISSION_POST_NOTIFICATIONS = 48;

    private HomeViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        root.findViewById(R.id.fragment_home_button_6_digits_code).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_home_to_cobrowse_code);
        });
        root.findViewById(R.id.fragment_home_button_open_camera).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        ? R.id.action_nav_home_to_nav_camera
                        : R.id.action_nav_home_to_nav_legacy_camera);
        });
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            root.findViewById(R.id.fragment_home_button_open_notification_permissions).setOnClickListener(v -> {
                Intent settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                startActivity(settingsIntent);
            });
        }
        updateNotificationPermissionVisibility(root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Only one permissions request can be active at a time, so ask for all of them at once
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN
                && LocalNetwork.isLocalUrl(CobrowsePreferences.getApi(requireContext()))) {
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_LOCAL_NETWORK)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_LOCAL_NETWORK);
            }
        }
        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[0]), REQUEST_PERMISSION_POST_NOTIFICATIONS);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNotificationPermissionVisibility();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_POST_NOTIFICATIONS) {
            updateNotificationPermissionVisibility();
        }
    }

    private void updateNotificationPermissionVisibility() {
        updateNotificationPermissionVisibility(requireView());
    }

    private void updateNotificationPermissionVisibility(@NonNull View root) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                root.findViewById(R.id.fragment_home_button_open_notification_permissions).setVisibility(View.VISIBLE);
                return;
            }
        }
        root.findViewById(R.id.fragment_home_button_open_notification_permissions).setVisibility(View.GONE);
    }
}