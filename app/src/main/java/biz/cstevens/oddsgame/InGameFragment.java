package biz.cstevens.oddsgame;

import android.content.ContentValues;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Locale;

import biz.cstevens.oddsgame.Documents.OddsDocument;
import biz.cstevens.oddsgame.GameHistoryDb.GameHistoryDb;

public class InGameFragment extends Fragment {
    private FirebaseFirestore db;
    private GameHistoryDb gameHistoryDb;
    private DocumentReference odds_ref;
    private boolean isCreator;
    private boolean isReversed;

    private TextView odds_title;
    private TextView odds_subtitle;
    private NumberPicker odds_picker;
    private Button odds_lock;
    private Button reverse;

    private ImageView user_a_img;
    private TextView user_a_name;
    private TextView user_a_status;
    private CheckBox user_a_check;
    private TextView user_a_odds;

    private ImageView user_b_img;
    private TextView user_b_name;
    private TextView user_b_status;
    private CheckBox user_b_check;
    private TextView user_b_odds;

    ListenerRegistration odds_listener;


    public static InGameFragment newInstance(String oddsID, Boolean isCreator) {
        InGameFragment inGameFragment = new InGameFragment();

        Bundle args = new Bundle();
        args.putString("oddsId", oddsID);
        args.putBoolean("isCreator", isCreator);
        inGameFragment.setArguments(args);

        inGameFragment.setRetainInstance(true);

        return inGameFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int layout = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? R.layout.content_in_game : R.layout.content_in_game_landscape;
        View view = inflater.inflate(layout, container, false);

        db = FirebaseFirestore.getInstance();
        gameHistoryDb = new GameHistoryDb(getContext());

        odds_ref = db.collection("odds").document(getArguments().getString("oddsId"));
        isCreator = getArguments().getBoolean("isCreator");
        isReversed = false;

        odds_title = view.findViewById(R.id.odds_title);
        odds_subtitle = view.findViewById(R.id.odds_subtitle);
        odds_picker = view.findViewById(R.id.odds_picker);
        odds_lock = view.findViewById(R.id.odds_lock);
        reverse = view.findViewById(R.id.reverse);

        user_a_img = view.findViewById(R.id.user_a_img);
        user_a_name = view.findViewById(R.id.user_a_name);
        user_a_status = view.findViewById(R.id.user_a_status);
        user_a_check = view.findViewById(R.id.user_a_checkbox);
        user_a_odds = view.findViewById(R.id.user_a_odds);

        user_b_img = view.findViewById(R.id.user_b_img);
        user_b_name = view.findViewById(R.id.user_b_name);
        user_b_status = view.findViewById(R.id.user_b_status);
        user_b_check = view.findViewById(R.id.user_b_checkbox);
        user_b_odds = view.findViewById(R.id.user_b_odds);


        // register listener for changes on the odds document
        odds_listener = odds_ref.addSnapshotListener(new EventListener<DocumentSnapshot>() { // Register realtime monitoring.
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("", "Listen failed.", e);
                    return;
                }
                if (snapshot == null || !snapshot.exists()) {
                    Log.w("InGame", "Current data: null");
                    return;
                }
                OddsDocument odds = snapshot.toObject(OddsDocument.class);
                if (odds.reversed && !isReversed) { // if it's just been reversed...
                    isReversed = true;
                    reverseReset();
                }
                handleData(odds);
            }
        });

        // register listener for clicks on the 'Lock In' button
        odds_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                odds_ref.update((isCreator ? "a_odds" : "b_odds"), odds_picker.getValue()); // set the users choice in the odds document

                odds_lock.setEnabled(false);
                odds_picker.setEnabled(false);
            }
        });

        // register listener for clicks on the 'Reverse' button
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reverseReset();
                odds_ref.update( // reset the data in the odds document
                        "a_odds", -1,
                        "b_odds", -1,
                        "reversed", true
                );
                reverse.setEnabled(false);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        odds_listener.remove();
    }

    private void handleData(OddsDocument odds) {
        odds_title.setText(getString(R.string.odds_of_to, odds.odds));
        odds_subtitle.setText(odds.message);

        user_a_name.setText(odds.a_name);
        user_a_status.setText(
                (odds.a_odds > 0 ? getString(R.string.locked_in) : getString(R.string.choosing_a_number))
        );
        user_a_check.setChecked(odds.a_odds > 0);
        user_a_check.setButtonTintList(getActivity().getResources().getColorStateList(
                (odds.a_odds > 0 ? R.color.green : R.color.red)
        ));

        user_b_name.setText(odds.b_name);
        user_b_status.setText(
                (odds.b_odds > 0 ? getString(R.string.locked_in) : getString(R.string.choosing_a_number))
        );
        user_b_check.setChecked(odds.b_odds > 0);
        user_b_check.setButtonTintList(getActivity().getResources().getColorStateList(
                (odds.b_odds > 0 ? R.color.green : R.color.red)
        ));

        odds_picker.setEnabled((isCreator ? odds.a_odds < 0 : odds.b_odds < 0));
        odds_picker.setMinValue(1);
        odds_picker.setMaxValue(odds.odds);
        odds_lock.setEnabled((isCreator ? odds.a_odds < 0 : odds.b_odds < 0));

        // If both players have locked in, reveal the odds.
        if (odds.a_odds > 0 && odds.b_odds > 0) revealOdds(odds);
    }

    private void revealOdds(final OddsDocument odds) {
        final Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        final Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);

        odds_picker.startAnimation(fadeOut);
        odds_lock.startAnimation(fadeOut);
        user_a_check.startAnimation(fadeOut);
        user_b_check.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) { // When the checkboxes & UI have faded out...
                odds_picker.setVisibility(View.GONE);
                odds_lock.setVisibility(View.GONE);
                user_a_check.setVisibility(View.GONE);
                user_b_check.setVisibility(View.GONE);


                if (odds.a_odds == odds.b_odds) { // If there's a match...
                    user_a_odds.setTextColor(getResources().getColor( // Colour the odds green/red accordingly.
                            (odds.reversed) ? R.color.red : R.color.green
                    ));
                    user_b_odds.setTextColor(getResources().getColor(
                            (odds.reversed) ? R.color.green : R.color.red
                    ));

                    addHistory(odds, (isReversed == isCreator));
                }

                user_a_odds.setText(String.format(Locale.getDefault(), "%1$d", odds.a_odds));
                user_b_odds.setText(String.format(Locale.getDefault(), "%1$d", odds.b_odds));

                if (!odds.reversed && odds.a_odds != odds.b_odds) // If there isn't a match, and hasn't been reversed yet...
                    reverse.startAnimation(fadeIn);
                user_a_odds.startAnimation(fadeIn);
                user_b_odds.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) { // When the odds have faded in...
                if (!odds.reversed && odds.a_odds != odds.b_odds) // If there isn't a match, and hasn't been reversed yet...
                    reverse.setVisibility(View.VISIBLE);
                user_a_odds.setVisibility(View.VISIBLE);
                user_b_odds.setVisibility(View.VISIBLE);


                if (odds.a_odds == odds.b_odds) {
                    user_a_status.setText((odds.reversed) ? getString(R.string.loser) : getString(R.string.winner));
                    user_b_status.setText((odds.reversed) ? getString(R.string.winner) : getString(R.string.loser));
                } else {
                    user_a_status.setText(getString(R.string.no_winner));
                    user_b_status.setText(getString(R.string.no_winner));
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void reverseReset() {
        final Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        final Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);

        user_a_odds.startAnimation(fadeOut);
        user_b_odds.startAnimation(fadeOut);
        reverse.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) { // When the odds and reverse button have faded out...
                user_a_odds.setVisibility(View.GONE);
                user_b_odds.setVisibility(View.GONE);
                reverse.setVisibility(View.GONE);

                // Set the odds colour to the default grey.
                user_a_odds.setTextColor(getResources().getColor(android.R.color.secondary_text_light));
                user_b_odds.setTextColor(getResources().getColor(android.R.color.secondary_text_light));

                odds_picker.startAnimation(fadeIn);
                odds_lock.startAnimation(fadeIn);
                user_a_check.startAnimation(fadeIn);
                user_b_check.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) { // When the odds have faded in...
                odds_picker.setVisibility(View.VISIBLE);
                odds_lock.setVisibility(View.VISIBLE);
                user_a_check.setVisibility(View.VISIBLE);
                user_b_check.setVisibility(View.VISIBLE);

                user_a_status.setText(getString(R.string.choosing_a_number));
                user_b_status.setText(getString(R.string.choosing_a_number));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void addHistory(final OddsDocument odds, boolean won) { // add items to the SQLite database
        ContentValues values = new ContentValues();
        values.put("uid", getArguments().getString("oddsID"));
        values.put("opponent", (isCreator ? odds.b_name : odds.a_name));
        values.put("message", odds.message);
        values.put("odds", odds.odds);
        values.put("won", won);
        values.put("list_text",
                (won ? "Won" : "Lost") + " odds of " + odds.odds + " to " + odds.message + "."
        );
        gameHistoryDb.addGame(values);
    }
}
