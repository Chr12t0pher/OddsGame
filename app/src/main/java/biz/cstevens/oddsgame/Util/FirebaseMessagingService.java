package biz.cstevens.oddsgame.Util;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import biz.cstevens.oddsgame.Container;
import biz.cstevens.oddsgame.R;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.d("", message.toString());

        if (message.getNotification() != null) {

            Intent intent = new Intent(this, Container.class);
            intent
                    .putExtra("type", "new_odds")
                    .putExtra("oddsId", message.getData().get("oddsId"));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "odds_notification");
            notificationBuilder
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(message.getNotification().getTitle())
                    .setContentText(message.getNotification().getBody())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, notificationBuilder.build());
        }
    }
}
