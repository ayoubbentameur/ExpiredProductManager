package com.benayoub.superettebozar;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.Map;

public class AlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        int requestCode = intent.getIntExtra("REQUEST_CODE",0);
        String productName = intent.getStringExtra("productName");
        String expiryDate = intent.getStringExtra("expiryDate");
        Log.d("AlarmReceiver", "onReceive called");
        Log.d("AlarmReceiver", "Request Code: " + requestCode);

        // Check if the notification has already been shown for this alarm
        if (!isNotificationShown(context, requestCode)) {
            // Display the notification
            showNotification(context, productName, "It's time for your task! Expires on: " + expiryDate);
            Log.d("AlarmReceiver", "Showing Notification");


            // Update the shared preference to indicate that the notification has been shown
            setNotificationShown(context, requestCode);
            SharedPreferences preferences = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
            Map<String, ?> allEntries = preferences.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                Log.d("SharedPreferences", entry.getKey() + ": " + entry.getValue().toString());
        }
        // Extract product name and expiry date from the intent


        }else {
            Log.d("AlarmReceiver", "Notification already shown for this alarm");

        }
        // Update the shared preference to indicate that the notification has been shown


        // Update the shared preference to indicate that the notification has been shown

    }

    private boolean isNotificationShown(Context context, int requestCode) {
        // Retrieve the value from shared preferences using the request code as the key
        SharedPreferences preferences = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        boolean isShown = preferences.getBoolean(getPreferenceKey(requestCode), false);
        Log.d("AlarmReceiver", "Is Notification Shown: " + isShown);
        return isShown;
    }

    private void setNotificationShown(Context context, int requestCode) {
        // Update the value in shared preferences using the request code as the key
        SharedPreferences preferences = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getPreferenceKey(requestCode), true);
        editor.apply();
        Log.d("AlarmReceiver", "Notification set as shown for this alarm");
    }

    private String getPreferenceKey(int requestCode) {
        return "NotificationShown_" + requestCode;
    }
    private void showNotification(Context context, String title, String message) {
        // Example notification code, replace with your implementation
        Log.d("AlarmReceiver", "Notification displayed");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                context,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(resultPendingIntent);
        notificationManager.notify(1, builder.build()); // Use a unique notification ID for each notification
    }}
