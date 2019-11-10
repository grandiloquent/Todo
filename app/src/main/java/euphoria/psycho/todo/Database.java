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
        values.put("Content", note.Content);

        values.put("Title", note.Title);
        values.put("CreateAt", System.currentTimeMillis());
        values.put("UpdateAt", System.currentTimeMillis());

        note.Id = (int) getWritableDatabase().insert("Article", null, values);
    }

    public void delete(int id) {
        getWritableDatabase().delete("Article", "Id = ?", new String[]{Integer.toString(id)});

    }

    public void close() {
    }

    public List<Pair<Integer, String>> fetchTitles(String filter) {
        Cursor cursor = getReadableDatabase().rawQuery("select ID,Title from Article order by UpdateAt desc", null);
        List<Pair<Integer, String>> strings = new ArrayList<>();
        while (cursor.moveToNext()) {
            if (filter != null && filter.length() != 0) {
                String title = cursor.getString(1);
                if (title.contains(filter))
                    strings.add(Pair.create(cursor.getInt(0),
                            title));

            } else
                strings.add(Pair.create(cursor.getInt(0), cursor.getString(1)));
        }
        cursor.close();
        return strings;
    }

    public Pair<String, String> fetchNote(int noteId) {
        Cursor cursor = getReadableDatabase().rawQuery("select Title,Content from Article where Id =" + noteId, null);
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
        values.put("Content", note.Content);

        values.put("Title", note.Title);
        values.put("UpdateAt", System.currentTimeMillis());

        getWritableDatabase().update("Article", values, "Id = ?", new String[]{Integer.toString(note.Id)});
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // db.rawQuery("PRAGMA journal_mode=DELETE", null);
        db.execSQL("CREATE TABLE IF NOT EXISTS \"Article\" (\n" +
                "\"Id\" integer primary key autoincrement not null ,\n" +
                "\"Title\" varchar ,\n" +
                "\"Content\" varchar ,\n" +
                "\"CreateAt\" bigint ,\n" +
                "\"UpdateAt\" bigint )");
        db.execSQL("CREATE UNIQUE INDEX \"index_title\" on \"Article\"(\"Title\")");
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
