package euphoria.psycho.todo;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.common.Views;

public class Database extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private final Context mContext;
    private int mBackgroundId;

    public Database(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
        mContext = context;
        mBackgroundId = Views.getSelectableItemBackground(context);
    }

    public void insert(Note note) {
        ContentValues values = new ContentValues();
        values.put("content", note.Content);

        values.put("title", note.Title);
        values.put("createAt", System.currentTimeMillis());
        values.put("updateAt", System.currentTimeMillis());

        getWritableDatabase().insert("strings", null, values);
    }

    public void delete(int id) {
        getWritableDatabase().delete("strings", "_id = ?", new String[]{Integer.toString(id)});

    }

    public void close() {
    }

    public List<Pair<Integer, String>> fetchTitles() {
        Cursor cursor = getReadableDatabase().rawQuery("select _id,title from strings", null);
        List<Pair<Integer, String>> strings = new ArrayList<>();
        while (cursor.moveToNext()) {
            strings.add(Pair.create(cursor.getInt(0), cursor.getString(1)));
        }
        cursor.close();
        return strings;
    }

    public Pair<String, String> fetchNote(int noteId) {
        Cursor cursor = getReadableDatabase().rawQuery("select title,content from strings where _id =" + noteId, null);
        Pair<String, String> pair = null;
        while (cursor.moveToNext()) {
            pair = Pair.create(
                    cursor.getString(0),
                    cursor.getString(1));
        }
        cursor.close();
        return pair;
    }

    public void update(Note note) {
        ContentValues values = new ContentValues();
        values.put("content", note.Content);

        values.put("title", note.Title);
        values.put("updateAt", System.currentTimeMillis());

        getWritableDatabase().update("strings", values, "_id = ?", new String[]{Integer.toString(note.Id)});
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // db.rawQuery("PRAGMA journal_mode=DELETE", null);
        db.execSQL("create table if not exists strings (_id integer PRIMARY KEY AUTOINCREMENT,title text,content text,createAt integer,updateAt integer)");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            db.disableWriteAheadLogging();
        }
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
