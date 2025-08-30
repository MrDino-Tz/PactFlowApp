package co.dtc.fieldwork.pactflow.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;

import co.dtc.fieldwork.pactflow.R;
import co.dtc.fieldwork.pactflow.model.Contract;

public class NotificationHelper {
    private static final String CHANNEL_ID = "contract_notification_channel";
    private static final String CHANNEL_NAME = "Contract Notifications";
    private static final String CHANNEL_DESCRIPTION = "Shows notifications for contract operations";
    private static final int NOTIFICATION_ID = 1001;

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.util.Log.d("NotificationHelper", "Creating notification channel");
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            notificationManager.createNotificationChannel(channel);
            android.util.Log.d("NotificationHelper", "Notification channel created");
        }
    }

    public boolean areNotificationsEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("notifications_enabled", true);
    }

    public void showContractCreatedNotification(Contract contract) {
        if (!areNotificationsEnabled()) {
            android.util.Log.d("NotificationHelper", "Notifications are disabled in settings");
            return;
        }

        android.util.Log.d("NotificationHelper", "Preparing to show notification for contract: " + contract.getTitle());

        try {
            // Create an intent for when the notification is tapped
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (intent == null) {
                android.util.Log.e("NotificationHelper", "Launch intent is null");
                return;
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            android.util.Log.d("NotificationHelper", "Building notification");
            // Build the notification
            Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.pf_icon_bw)
                    .setContentTitle(context.getString(R.string.contract_created_title))
                    .setContentText(context.getString(R.string.contract_created_message, contract.getTitle()))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{100, 200, 300, 400, 500})
                    .setLights(Color.BLUE, 1000, 1000)
                    .build();

            // Show the notification
            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID, notification);
                android.util.Log.d("NotificationHelper", "Notification shown successfully");
            } else {
                android.util.Log.e("NotificationHelper", "NotificationManager is null");
            }
        } catch (Exception e) {
            android.util.Log.e("NotificationHelper", "Error showing notification: " + e.getMessage(), e);
        }
    }
}
