package biz.cstevens.oddsgame;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Locale;

import biz.cstevens.oddsgame.Documents.OddsDocument;
import biz.cstevens.oddsgame.Util.UserListAdapter;

public class NewGameFragment extends Fragment {
    private FirebaseFirestore db;

    private RecyclerView recyclerView;
    private UserListAdapter recAdapter;
    private RecyclerView.LayoutManager recLayoutManager;

    private TextView oddsNo;
    private SeekBar oddsSlider;
    private EditText message;
    private Button oddsSend;

    public static NewGameFragment newInstance() {
        NewGameFragment newGameFragment = new NewGameFragment();
        newGameFragment.setRetainInstance(true);
        return new NewGameFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        int layout = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? R.layout.content_new_game : R.layout.content_new_game_landscape;
        View view = inflater.inflate(layout, container, false);

        oddsNo = view.findViewById(R.id.odds_no);
        oddsSlider = view.findViewById(R.id.odds_slider);
        message = view.findViewById(R.id.message);
        oddsSend = view.findViewById(R.id.odds_send);


        // Setup the list of users
        recyclerView = view.findViewById(R.id.nearby_recycler_view);
        recLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(recLayoutManager); // Use a Linear Layout Manager
        recAdapter = new UserListAdapter();
        recyclerView.setAdapter(recAdapter);
        db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) { // get all users from the users collection
                recAdapter.setUsers(queryDocumentSnapshots.getDocuments());
            }
        });


        oddsSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // when the slider is changed, immediately change the TextView next to it with the relevant number
                oddsNo.setText(String.format(Locale.getDefault(), "%1$d", progress+2));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        oddsSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createOdds();
            }
        });

        return view;
    }

    private void createOdds() {
        oddsSend.setEnabled(false); // prevents multiple clicks

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentSnapshot opponent = recAdapter.getUser();
        if (opponent == null) { // check an opponent is selected
            Toast.makeText(getContext(), getString(R.string.please_select_an_opponent), Toast.LENGTH_SHORT).show();
            oddsSend.setEnabled(true);
            return;
        }
        if (message.getText().length() == 0) { // check a message is provided
            Toast.makeText(getContext(), getString(R.string.please_provide_a_message), Toast.LENGTH_SHORT).show();
            oddsSend.setEnabled(true);
            return;
        }
        OddsDocument odds = new OddsDocument( // Create an OddsDocument object
                user.getUid(), user.getDisplayName(), opponent.getId(), opponent.get("name").toString(), message.getText().toString(), oddsSlider.getProgress()+2
        );

        // add the odds document to the database and when confirmed start the InGameFragment...
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

}
