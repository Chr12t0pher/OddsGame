package biz.cstevens.oddsgame.Util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import biz.cstevens.oddsgame.Documents.UserDocument;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        setToken();
    }

    public static void setToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && token != null) {
            db.collection("users").document(user.getUid()).set(new UserDocument(user.getDisplayName(), user.getPhotoUrl().toString(), token), SetOptions.merge());
        }
    }
}
