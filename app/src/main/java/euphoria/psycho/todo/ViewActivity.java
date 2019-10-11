package euphoria.psycho.todo;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import euphoria.psycho.common.Activities;
import euphoria.psycho.common.EditTexts;
import euphoria.psycho.common.Files;
import euphoria.psycho.common.Strings;

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

    private TextView mTextView;

    private void loadNote() {
        int nodeId = getIntent().getIntExtra("id", 0);
        if (nodeId == 0) return;
        mNoteId = nodeId;
        checkDatabase();
        Pair<String, String> note = mDatabase.fetchNote(nodeId);
        setTitle(note.first);
        mTextView = findViewById(R.id.text_view);
        mTextView.setText(note.second);
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
        menu.add(0, 2, 0, "预览")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                editNote();

                return true;
            case 2:
                previewNote();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void previewNote() {

        String val = mTextView.getText().toString();

        File dir = new File(Environment.getExternalStorageDirectory(), "Notes");
        if (!dir.isDirectory()) dir.mkdir();

        String title = Files.getValidFileName(Strings.substringBefore(val.trim(), '\n'), ' ') + ".htm";
        File out = new File(dir, title);

        NativeUtils.renderMarkdown(val, out.getAbsolutePath());

        Intent textIntent = new Intent();
        textIntent.setAction(Intent.ACTION_VIEW);
        textIntent.setDataAndType(Uri.fromFile(out), "multipart/related");
        startActivity(textIntent);
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
