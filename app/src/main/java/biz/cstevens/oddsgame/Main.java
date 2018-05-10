package biz.cstevens.oddsgame;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.FirebaseApp;

public class Main extends Application {

    public static final int NEW_GAME_FRAGMENT = 1;
    public static final int IN_GAME_FRAGMENT = 2;
    public static final int GAME_REQUEST_FRAGMENT = 3;
    public static final int GAME_HISTORY_FRAGMENT = 4;
    public static final int USER_GUIDE_FRAGMENT = 5;



    @Override
    public void onCreate() {
        super.onCreate();

        // Setup Firebase.
        FirebaseApp.initializeApp(this);

    }
}
