package co.dtc.fieldwork.pactflow.ui.settings;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.materialswitch.MaterialSwitch;

import co.dtc.fieldwork.pactflow.R;
import co.dtc.fieldwork.pactflow.model.Contract;
import co.dtc.fieldwork.pactflow.utils.NotificationHelper;

public class SettingsFragment extends Fragment {
    private static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private SharedPreferences sharedPreferences;
    private MaterialSwitch notificationSwitch;
    private com.google.android.material.button.MaterialButton demoNotificationButton;

    // For handling notification permission result
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted, update UI accordingly
                    if (notificationSwitch != null) {
                        notificationSwitch.setChecked(true);
                    }
                } else {
                    // Permission denied, show explanation if needed
                    if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                        showPermissionExplanation();
                    }
                    if (notificationSwitch != null) {
                        notificationSwitch.setChecked(false);
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    private void showPermissionExplanation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Notification Permission")
                .setMessage("This app needs notification permission to show important updates. Please allow the permission to continue.")
                .setPositiveButton("Allow", (dialog, which) -> requestNotificationPermission())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (notificationSwitch != null) {
                        notificationSwitch.setChecked(false);
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show explanation dialog
                new AlertDialog.Builder(requireContext())
                        .setTitle("Notification Permission Required")
                        .setMessage("To show notifications, please grant the notification permission.")
                        .setPositiveButton("OK", (dialog, which) ->
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        )
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            if (notificationSwitch != null) {
                                notificationSwitch.setChecked(false);
                            }
                        })
                        .setCancelable(false)
                        .show();
            } else {
                // No explanation needed, request the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void updateDemoButtonState(boolean enabled) {
        if (demoNotificationButton != null) {
            demoNotificationButton.setEnabled(enabled);
            demoNotificationButton.setAlpha(enabled ? 1.0f : 0.5f);
        }
    }

    private void showDemoNotification() {
        if (!notificationSwitch.isChecked()) {
            Toast.makeText(requireContext(), "Please enable notifications first", Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationHelper notificationHelper = new NotificationHelper(requireContext());
        if (notificationHelper.areNotificationsEnabled()) {
            Contract testContract = new Contract();
            testContract.setTitle("Demo Notification");
            testContract.setDescription("This is a demo notification from PactFlow");
            notificationHelper.showContractCreatedNotification(testContract);
        } else {
            Toast.makeText(requireContext(), "Notifications are disabled in settings", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            notificationSwitch = view.findViewById(R.id.switch_notifications);

            if (notificationSwitch == null) {
                Log.e("SettingsFragment", "Switch not found in layout");
                return;
            }

            // Initialize demo notification button
            demoNotificationButton = view.findViewById(R.id.btn_demo_notification);
            demoNotificationButton.setOnClickListener(v -> showDemoNotification());

            // Load current preference - default to false (off)
            boolean notificationsEnabled = sharedPreferences.getBoolean(PREF_NOTIFICATIONS_ENABLED, false);
            notificationSwitch.setChecked(notificationsEnabled);
            updateDemoButtonState(notificationsEnabled);

            // Set up switch listener
            notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                try {
                    if (isChecked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            // Check and request notification permission for Android 13+
                            if (ContextCompat.checkSelfPermission(requireContext(),
                                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                requestNotificationPermission();
                                // Don't update the preference until permission is granted
                                notificationSwitch.setChecked(false);
                                return;
                            }
                        }
                        // Update demo button state when notifications are enabled/disabled
                        updateDemoButtonState(isChecked);
                    }

                    // Save preference
                    sharedPreferences.edit()
                            .putBoolean(PREF_NOTIFICATIONS_ENABLED, isChecked)
                            .apply();

                    // Show toast for immediate feedback
                    String message = isChecked ? "Notifications enabled" : "Notifications disabled";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Log.e("SettingsFragment", "Error in switch listener", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error updating notification settings", Toast.LENGTH_SHORT).show();
                    }
                    // Reset switch state if there was an error
                    notificationSwitch.setChecked(!isChecked);
                }
            });
        } catch (Exception e) {
            Log.e("SettingsFragment", "Error in onViewCreated", e);
        }
    }
}
