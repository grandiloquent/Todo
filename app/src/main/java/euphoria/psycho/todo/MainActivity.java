package euphoria.psycho.todo;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Environment;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    private static final int REQUEST_CODE_EDIT = 679;
    private int mBackgroundId;
    private List<Pair<Integer, String>> mItems = new ArrayList<>();
    private BaseAdapter mAdapter;
    private int mPadding;
    private ListView mListView;
    private Database mDatabase;

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
        mItems.addAll(mDatabase.fetchTitles());
        Logs.d(mItems.size());

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initialize() {
        mBackgroundId = Views.getSelectableItemBackground(this);
        mPadding = Views.dp2px(8);

        ListView listView = new ListView(this);
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
        setContentView(listView);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int requestCodePermissions() {
        return 1;
    }
}
