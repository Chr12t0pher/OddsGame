package biz.cstevens.oddsgame;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class Main extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup Firebase.
        FirebaseApp.initializeApp(this);

    }
}
