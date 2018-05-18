package biz.cstevens.oddsgame;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import biz.cstevens.oddsgame.Util.GameReqListAdapter;

public class GameRequestFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth user;

    public static GameRequestFragment newInstance() {
        GameRequestFragment gameRequestFragment = new GameRequestFragment();
        gameRequestFragment.setRetainInstance(true);
        return gameRequestFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();

        View view = inflater.inflate(R.layout.content_game_requests, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.oddsreq_recycler_view);
        LinearLayoutManager recLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(recLayoutManager);
        final GameReqListAdapter recAdapter = new GameReqListAdapter();
        recyclerView.setAdapter(recAdapter); // attach adapter


        // Firestore doesn't support OR queries, so we have to nest a query to get a_id and b_id, then merge them.
        db.collection("odds").whereEqualTo("a_id", user.getUid()).whereEqualTo("a_odds", -1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot aSnapshot) {
                db.collection("odds").whereEqualTo("b_id", user.getUid()).whereEqualTo("b_odds", -1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot bSnapshot) {
                        List<DocumentSnapshot> odds = aSnapshot.getDocuments();
                        odds.addAll(bSnapshot.getDocuments());
                        recAdapter.setGames(odds);
                    }
                });
            }
        });


        return view;
    }
}
