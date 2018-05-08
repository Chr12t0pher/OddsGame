package biz.cstevens.oddsgame.GameHistoryDb;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class GameHistoryProvider extends ContentProvider {

    public static final String PROVIDER_NAME = "biz.cstevens.oddsgame.gamehistorydb.gamehistoryprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/gamehistory");
    private static final int GAME_HISTORY = 1;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "gamehistory", GAME_HISTORY);
    }

    GameHistoryDb gameDatabase;
    @Override
    public boolean onCreate() {
        gameDatabase = new GameHistoryDb(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (uriMatcher.match(uri) == GAME_HISTORY) {
            gameDatabase.addGame(values);
            return uri;
        } else {
            return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (uriMatcher.match(uri) == GAME_HISTORY) {
            return gameDatabase.getAll();
        } else {
            return null;
        }
    }


}
