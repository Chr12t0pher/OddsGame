package biz.cstevens.oddsgame;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.FirebaseApp;

public class Main extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup Firebase.
        FirebaseApp.initializeApp(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // if Android 8.0+ register the notification channel
            CharSequence name = "New odds games";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("odds_notification", name, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }
}
