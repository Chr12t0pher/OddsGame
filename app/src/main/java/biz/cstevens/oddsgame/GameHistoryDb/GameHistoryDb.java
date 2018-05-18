package biz.cstevens.oddsgame.GameHistoryDb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GameHistoryDb extends SQLiteOpenHelper {
    private static String NAME = "gamehistory";
    private static int VERSION = 1;
    private static final String TABLE = "gamehistory";

    private SQLiteDatabase db;

    public GameHistoryDb(Context context) {
        super(context, NAME, null, VERSION);
        this.db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql =    "CREATE TABLE " + TABLE + " ("
                        + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "uid VARCHAR,"
                        + "opponent VARCHAR,"
                        + "message VARCHAR,"
                        + "odds INTEGER,"
                        + "won BOOLEAN,"
                        + "list_text VARCHAR)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Required method.
    }

    public Cursor getAll() {
        return db.query(
                TABLE,
                new String[] {"_id", "opponent", "message", "odds", "won", "list_text"},
                null, null, null, null,
                "_id ASC"
        );
    }

    public void addGame(ContentValues values) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE + " WHERE uid = '" + values.get("uid") + "';", null);
        if (cursor.getCount() > 0) { // check the game has not already been added
            cursor.close();
            return;
        }
        cursor.close();

        db.insert(
                TABLE,
                null,
                values
        );
    }
}
