package euphoria.psycho.todo;

import android.content.Intent;
import android.os.Environment;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;

import euphoria.psycho.common.Activities;

public class ViewActivity extends Activities {
    private static final int REQUEST_CODE_EDIT = 679;
    private Database mDatabase;
    private int mNoteId;

    private void checkDatabase() {
        if (mDatabase == null) {
            mDatabase = new Database(this, new File(
                    Environment.getExternalStorageDirectory(),
                    "sql_notes.db"
            ).getAbsolutePath());
        }

    }

    private void editNote() {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("id", mNoteId);
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

    private void loadNote() {
        int nodeId = getIntent().getIntExtra("id", 0);
        if (nodeId == 0) return;
        mNoteId = nodeId;
        checkDatabase();
        Pair<String, String> note = mDatabase.fetchNote(nodeId);
        setTitle(note.first);
        TextView textView = findViewById(R.id.text_view);
        textView.setText(note.second);
    }

    @Override
    protected void initialize() {
        setContentView(R.layout.activity_view);
        loadNote();

    }

    @Override
    protected String[] needPermissions() {
        return new String[0];
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "编辑")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                editNote();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_EDIT) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @Override
    protected int requestCodePermissions() {
        return 0;
    }
}
