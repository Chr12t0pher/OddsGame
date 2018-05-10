package biz.cstevens.oddsgame.Util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import biz.cstevens.oddsgame.Container;
import biz.cstevens.oddsgame.R;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.d("", message.toString());

        Intent intent = new Intent(this, Container.class);
        intent
                .putExtra("type", message.getData().get("type"))
                .putExtra("oddsId", message.getData().get("oddsId"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "odds_notification");
        notificationBuilder
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(message.getData().get("title"))
                .setContentText(message.getData().get("body"))
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // if Android 8.0+ register the notification channel
            CharSequence name = "New odds games";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("odds_notification", name, importance);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, notificationBuilder.build());

    }
}
