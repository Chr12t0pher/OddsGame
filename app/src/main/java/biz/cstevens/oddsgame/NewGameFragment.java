package biz.cstevens.oddsgame;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewGameFragment extends Fragment {
    private FirebaseFirestore db;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter recAdapter;
    private RecyclerView.LayoutManager recLayoutManager;

    private TextView oddsNo;
    private SeekBar oddsSlider;
    private EditText message;
    private Button oddsSend;

    public static NewGameFragment newInstance() {
        return new NewGameFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        View view = inflater.inflate(R.layout.content_new_game, container, false);

        oddsNo = view.findViewById(R.id.odds_no);
        oddsSlider = view.findViewById(R.id.odds_slider);
        message = view.findViewById(R.id.message);
        oddsSend = view.findViewById(R.id.odds_send);


        // Setup the list of nearby users.
        recyclerView = view.findViewById(R.id.nearby_recycler_view);
        recLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(recLayoutManager); // Use a Linear Layout Manager
        recyclerView.setAdapter(recAdapter);

        oddsSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                OddsDocument odds = new OddsDocument(
                    user.getUid(), user.getDisplayName(), "123", "Test", message.getText().toString(), oddsSlider.getProgress()
                );

                db.collection("odds").add(odds).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        FragmentManager fragmentManager = getFragmentManager();
                        Fragment fragment = InGameFragment.newInstance(documentReference.getId(), true);

                        // Switch over the fragments.
                        fragmentManager.beginTransaction().replace(R.id.frag_content, fragment).commit();
                    }
                });

            }
        });

        return view;
    }

}
