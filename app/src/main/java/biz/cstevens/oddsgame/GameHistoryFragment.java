package biz.cstevens.oddsgame;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.google.firebase.auth.FirebaseAuth;

import biz.cstevens.oddsgame.GameHistoryDb.GameHistoryProvider;

public class GameHistoryFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter gameHistoryAdapter;

    public static GameHistoryFragment newInstance() {
        GameHistoryFragment gameRequestFragment = new GameHistoryFragment();
        gameRequestFragment.setRetainInstance(true);
        return gameRequestFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.content_game_history, container, false);

        gameHistoryAdapter = new SimpleCursorAdapter(
                getActivity().getBaseContext(),
                R.layout.oddshistory_list_item,
                null,
                new String[] { "list_text", "opponent" },
                new int[] { R.id.history_odds_msg, R.id.history_odds_user},
                0
        );
        setListAdapter(gameHistoryAdapter);

        getActivity().getLoaderManager().initLoader(0, null, this);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = GameHistoryProvider.CONTENT_URI;
        return new CursorLoader(
                getContext(), uri, null, null, null, null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        gameHistoryAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        gameHistoryAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Cursor game = (Cursor)gameHistoryAdapter.getItem(position);


        String shareText = "I " + (game.getInt(4) > 0 ? "won" : "lost") + " against "
                            + game.getString(1) + " with odds of " + game.getInt(3)
                            + " to " + game.getString(2) + ".";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share odds result"));
    }
}
