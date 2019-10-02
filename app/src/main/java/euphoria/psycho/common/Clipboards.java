package euphoria.psycho.common;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Clipboards extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static Clipboards sClipboards;
    private final Context mContext;
    private int mPadding;
    private int mBackgroundId;
    private int mWidth;

    public Clipboards(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
        mContext = context;
        mPadding = Views.dp2px(8);
        mBackgroundId = Views.getSelectableItemBackground(context);
        mWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

    }

    public List<String> fetchStrings() {
        Cursor cursor = getReadableDatabase().rawQuery("select string from strings", null);
        List<String> strings = new ArrayList<>();
        while (cursor.moveToNext()) {
            strings.add(cursor.getString(0));
        }
        cursor.close();
        return strings;
    }

    public void insert(String string) {
        ContentValues values = new ContentValues();
        values.put("string", string);
        getWritableDatabase().insert("strings", null, values);
    }

    public void delete(String string) {

        getWritableDatabase().delete("strings", "string = ?", new String[]{
                string
        });
    }


    public void show(View anchor, OnItemClickListener listener) {
        PopupWindow popupWindow = new PopupWindow(mContext);
        ListView listView = new ListView(mContext);
        listView.setOnItemClickListener(listener);
        List<String> items = fetchStrings();

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenu().add(0, 1, 0, "删除");
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1:
                        delete(items.get(position));
                        return true;
                }
                return true;
            });
            popupMenu.show();
            return false;
        });
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return items.size();
            }

            @Override
            public String getItem(int position) {
                return items.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, android.view.View convertView, ViewGroup parent) {

                if (convertView == null) {
                    TextView textView = new TextView(parent.getContext());
                    textView.setBackgroundResource(mBackgroundId);
                    textView.setLayoutParams(new LayoutParams(mWidth,
                            LayoutParams.WRAP_CONTENT));
                    textView.setPadding(mPadding, mPadding, mPadding, mPadding);
                    convertView = textView;
                }
                ((TextView) convertView).setText(items.get(position));
                return convertView;
            }
        });
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xFFFEFEFE));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        listView.setPadding(mPadding, mPadding, mPadding, mPadding);

        popupWindow.setContentView(listView);

        popupWindow.showAtLocation(anchor, Gravity.CENTER, 0, 0);
        Logs.d(listView.getWidth());
    }

    public static Clipboards getInstance() {
        return sClipboards;
    }

    public static Clipboards newInstance(Context context, String name) {
        if (sClipboards == null) {
            synchronized (Clipboards.class) {
                if (sClipboards == null) {
                    sClipboards = new Clipboards(context, name);
                }
            }
        }
        return sClipboards;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL("PRAGMA journal_mode=DELETE");
        db.execSQL("create table if not exists strings (_id integer PRIMARY KEY AUTOINCREMENT,string value)");
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
