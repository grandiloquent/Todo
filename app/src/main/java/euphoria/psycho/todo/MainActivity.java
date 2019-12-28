package euphoria.psycho.todo;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.Browsers;
import euphoria.psycho.common.Activities;
import euphoria.psycho.common.Contexts;
import euphoria.psycho.common.Files;
import euphoria.psycho.common.Interfaces.Listener;
import euphoria.psycho.common.Logs;
import euphoria.psycho.common.Strings;
import euphoria.psycho.common.Threads;
import euphoria.psycho.common.Views;

public class MainActivity extends Activities implements OnItemClickListener {
    private static final String KEY_FILER_STRING = "filter";
    private static final int MENU_ADD = 2;
    private static final int MENU_BROWSER = 8;
    private static final int MENU_DICTIONARY = 5;
    private static final int MENU_DOWNLOAD = 3;
    private static final int MENU_EXPLAIN = 9;
    private static final int MENU_EXPORT = 10;
    private static final int MENU_SETTINGS = 6;
    private static final int MENU_UPLOAD = 1;
    private static final int REQUEST_CODE_EDIT = 679;
    private int mBackgroundId;
    private List<Pair<Integer, String>> mItems = new ArrayList<>();
    private BaseAdapter mAdapter;
    private int mPadding;
    private Database mDatabase;
    private EditText mEditText;
    private String mFilterString;

    private void checkDatabase() {
        if (mDatabase == null) {
            mDatabase = new Database(this, new File(
                    Environment.getExternalStorageDirectory(),
                    "sql_notes.db"
            ).getAbsolutePath());
        }
    }

    private void deleteNote(int position) {
//        new AlertDialog.Builder(this)
//                .setMessage(String.format("确定删除 ‘%s’ 吗？\n", mItems.get(position).second))
//                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
//
//                }).show();
        checkDatabase();
        Pair<String, String> note = mDatabase.fetchNote(mItems.get(position).first);

        File dir = new File(Environment.getExternalStorageDirectory(), "Notes");
        if (!dir.isDirectory()) dir.mkdir();

        String n = note.first;
        if (n.startsWith("#")) {
            n = Strings.substringAfter(n, ' ').trim();
        }
        if (n.contains(":")) {
            dir = new File(dir, Files.getValidFileName(Strings.substringBefore(n, ':').trim(), ' '));
            if (!dir.isDirectory()) dir.mkdirs();

            n = Strings.substringAfter(n, ':').trim();
        }
        String title = Files.getValidFileName(n, ' ') + ".md";
        File out = new File(dir, title);
        Files.writeText(out, note.second);
        mDatabase.delete(mItems.get(position).first);

        refreshListView();
    }

    private void editNote(int position) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("id", mItems.get(position).first);
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

    private void launchDictionary() {
        Intent i = new Intent(this, DictionaryService.class);
        startService(i);
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

    @TargetApi(VERSION_CODES.O)
    private void menuExport() {
        Path targetDirectory = Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath(), "Notes", "Notes");

        if (!java.nio.file.Files.isDirectory(targetDirectory)) {
            try {
                java.nio.file.Files.createDirectory(targetDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        checkDatabase();

        List<Pair<Integer, String>> titles = mDatabase.fetchTitles(null);
        for (Pair<Integer, String> title : titles) {
            Pair<String, String> note = mDatabase.fetchNote(title.first);
            try {
                if (note.first.contains(":")) {
                    String dir = Strings.substringBefore(note.first, ':').trim();
                    Path t = targetDirectory.resolve(dir);
                    if (!java.nio.file.Files.isDirectory(t))
                        java.nio.file.Files.createDirectory(t);

                    java.nio.file.Files.write(t.resolve(Files.getValidFileName(Strings.substringAfter(note.first,':').trim(), ' ')
                            + ".md"), note.second.getBytes(StandardCharsets.UTF_8));
                } else {
                    java.nio.file.Files.write(targetDirectory.resolve(Files.getValidFileName(note.first, ' ')
                            + ".md"), note.second.getBytes(StandardCharsets.UTF_8));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void menuSettings() {
        Intent settings = new Intent(this, SettingsActivity.class);

        startActivity(settings);
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
            public void afterTextChanged(Editable s) {
                mFilterString = s.toString();
                refreshListView();

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

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
        menu.add(0, MENU_DICTIONARY, 0, "开启字典服务");
        menu.add(0, MENU_EXPLAIN, 0, "字典");
        menu.add(0, MENU_UPLOAD, 0, "上传");
        menu.add(0, MENU_DOWNLOAD, 0, "下载");
        menu.add(0, MENU_SETTINGS, 0, "设置");
        menu.add(0, MENU_BROWSER, 0, "浏览器");
        menu.add(0, MENU_EXPORT, 0, "导出");

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
            case MENU_DICTIONARY:
                launchDictionary();
                return true;
            case MENU_EXPLAIN:
                DictionaryWindow.getInstance(this).show(Contexts.getText().toString().trim());
                return true;
            case MENU_EXPORT:
                menuExport();
                return true;

        }
        return super.onOptionsItemSelected(item);
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
