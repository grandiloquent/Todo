package euphoria.psycho.todo;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.Browsers;
import euphoria.psycho.common.Activities;
import euphoria.psycho.common.Contexts;
import euphoria.psycho.common.Interfaces.Listener;
import euphoria.psycho.common.Logs;
import euphoria.psycho.common.Threads;
import euphoria.psycho.common.Views;

public class MainActivity extends Activities implements OnItemClickListener {
    private static final int MENU_ADD = 2;
    private static final int MENU_DOWNLOAD = 3;
    private static final int MENU_EDIT = 5;
    private static final int MENU_UPLOAD = 1;
    private static final int MENU_SETTINGS = 6;
    private static final int MENU_BROWSER = 8;

    private static final int REQUEST_CODE_EDIT = 679;
    private int mBackgroundId;
    private List<Pair<Integer, String>> mItems = new ArrayList<>();
    private BaseAdapter mAdapter;
    private int mPadding;
    private ListView mListView;
    private Database mDatabase;
    private EditText mEditText;
    private String mFilterString;
    private static final String KEY_FILER_STRING = "filter";

    private void checkDatabase() {
        if (mDatabase == null) {
            mDatabase = new Database(this, new File(
                    Environment.getExternalStorageDirectory(),
                    "sql_notes.db"
            ).getAbsolutePath());
        }
    }

    private void deleteNote(int position) {
        new AlertDialog.Builder(this)
                .setMessage(String.format("确定删除 ‘%s’ 吗？\n", mItems.get(position).second))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    checkDatabase();
                    mDatabase.delete(mItems.get(position).first);
                    refreshListView();
                }).show();
    }

    private void editNote(int position) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("id", mItems.get(position).first);
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

    private void menuAdd() {
        Intent editActivity = new Intent(this, EditActivity.class);
        startActivityForResult(editActivity, REQUEST_CODE_EDIT);
    }

    private void menuDownload() {
        Threads.postOnBackgroundThread(() -> {
            SSHActivity.downloadDatabase(MainActivity.this, new File(
                    Environment.getExternalStorageDirectory(),
                    "sql_notes.db"
            ), "/root/sql_notes.db", new Listener<String>() {
                @Override
                public void onFailure(String reson) {
                    Threads.postOnUiThread(() -> {
                        Contexts.toast(reson);
                    });
                }

                @Override
                public void onSuccess(String s) {
                    Threads.postOnUiThread(() -> {
                        Contexts.toast(s);
                        refreshListView();
                    });
                }
            });
        });
    }

    private void menuSync() {
        Threads.postOnBackgroundThread(() -> {
            SSHActivity.uploadDatabase(MainActivity.this, new File(
                    Environment.getExternalStorageDirectory(),
                    "sql_notes.db"
            ), "/root/sql_notes.db", new Listener<String>() {
                @Override
                public void onFailure(String reson) {
                    Threads.postOnUiThread(() -> {
                        Contexts.toast(reson);
                    });
                }

                @Override
                public void onSuccess(String s) {
                    Threads.postOnUiThread(() -> {
                        Contexts.toast(s);
                    });
                }
            });
        });
    }

    private void refreshListView() {
        checkDatabase();
        mItems.clear();
        mItems.addAll(mDatabase.fetchTitles(mFilterString));
        Logs.d(mItems.size());

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initialize() {
        mFilterString = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_FILER_STRING, null);
        setContentView(R.layout.activity_main);
        mBackgroundId = Views.getSelectableItemBackground(this);
        mPadding = Views.dp2px(8);

        mEditText = findViewById(R.id.filter);
        mEditText.setText(mFilterString);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mFilterString = s.toString();
                refreshListView();

            }
        });

        ListView listView = findViewById(R.id.list_view);
        listView.setOnItemClickListener(this);


        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenu().add(0, 2, 0, "编辑");
            popupMenu.getMenu().add(0, 1, 0, "删除");
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1:
                        deleteNote(position);
                        return true;
                    case 2:
                        editNote(position);
                        return true;
                }
                return true;
            });
            popupMenu.show();
            return true;
        });

        mAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return mItems.size();
            }

            @Override
            public Pair<Integer, String> getItem(int position) {
                return mItems.get(position);
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
//                    textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
//                            LayoutParams.WRAP_CONTENT));

                    textView.setPadding(mPadding, mPadding, mPadding, mPadding);
                    convertView = textView;
                }
                ((TextView) convertView).setText(mItems.get(position).second);
                return convertView;
            }
        };
        listView.setAdapter(mAdapter);
        mListView = listView;
        refreshListView();
    }

    @Override
    protected String[] needPermissions() {
        return new String[]{
                permission.INTERNET,
                permission.WRITE_EXTERNAL_STORAGE
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_EDIT:
                    refreshListView();
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_UPLOAD, 0, "上传");
        menu.add(0, MENU_DOWNLOAD, 0, "下载");
        menu.add(0, MENU_SETTINGS, 0, "设置");
        menu.add(0, MENU_BROWSER, 0, "浏览器");

        MenuItem addMenuItem = menu.add(0, MENU_ADD, 0, "添加");
        addMenuItem.setIcon(R.drawable.ic_action_add);
        addMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, ViewActivity.class);
        intent.putExtra("id", mItems.get(position).first);
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_UPLOAD:
                menuSync();
                break;
            case MENU_ADD:
                menuAdd();
                break;
            case MENU_DOWNLOAD:
                menuDownload();
                return true;
            case MENU_SETTINGS:
                menuSettings();
                return true;
            case MENU_BROWSER:
                Intent browser = new Intent(this, Browsers.class);
                startActivity(browser);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void menuSettings() {
        Intent settings = new Intent(this, SettingsActivity.class);

        startActivity(settings);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFilterString == null || mFilterString.length() == 0) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().remove(KEY_FILER_STRING).apply();
            ;
        } else
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(KEY_FILER_STRING,
                    mFilterString).apply();
    }

    @Override
    protected int requestCodePermissions() {
        return 1;
    }
}
