package euphoria.psycho.todo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Environment;
import android.os.Process;
import android.text.Editable;
import android.text.style.EasyEditSpan;
import android.util.Pair;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import euphoria.psycho.common.Activities;
import euphoria.psycho.common.Contexts;
import euphoria.psycho.common.EditTexts;
import euphoria.psycho.common.Strings;
import euphoria.psycho.common.Threads;

public class EditActivity extends Activities {
    private static final Object sLock = new Object();
    private Database mDatabase;
    private boolean mFinished = false;
    private EditText mEditText;
    private Note mNote;
    private boolean mUpdated;
    private ClipboardManager mClipboardManager;

    private void checkDatabase() {
        if (mDatabase == null) {
            mDatabase = new Database(this, new File(
                    Environment.getExternalStorageDirectory(),
                    "sql_notes.db"
            ).getAbsolutePath());
        }
    }

    private void formatBold() {
        EditUtils.wrapSelection(mEditText, "**");
    }

    private void formatChineseToEnglish() {
        if (EditTexts.isWhitespace(mEditText)) return;
        String line = EditTexts.selectLine(mEditText).trim();

        new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            String result = NativeUtils.youdaoDictionary(line, true, false);
            synchronized (sLock) {
                Threads.postOnUiThread(() -> {
                    mEditText.getText().insert(
                            mEditText.getSelectionEnd(),
                            "\n" + (result == null ? "youdaoDictionary" : result));
                });
            }
        }).start();
        new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            String result = NativeUtils.baiduTranslate(line, false);
            synchronized (sLock) {
                Threads.postOnUiThread(() -> {
                    mEditText.getText().insert(
                            mEditText.getSelectionEnd(),
                            "\n" + (result == null ? "baiduTranslate" : result));
                });
            }
        }).start();
        new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            String result = NativeUtils.googleTranslate(line, false);
            synchronized (sLock) {
                Threads.postOnUiThread(() -> {
                    mEditText.getText().insert(
                            mEditText.getSelectionEnd(),
                            "\n" + (result == null ? "googleTranslate" : result));
                });
            }
        }).start();
    }

    private void formatCode() {
        String s = EditTexts.getSelectionText(mEditText).toString().trim();
        if (s.length() == 0
                || s.contains("\n")) {
            EditUtils.wrapSelection(mEditText, "\n```\n");

        } else {
            EditUtils.wrapSelection(mEditText, "`");

        }
    }

    private void formatCut() {

        CharSequence result = EditTexts.deleteLineWithWhitespace(mEditText);
        if (Strings.isNullOrWhiteSpace(result)) return;
        getClipboardManager().setPrimaryClip(ClipData.newPlainText(null, result));
    }

    private void formatEnglishToChinese() {
        if (EditTexts.isWhitespace(mEditText)) return;
        String line = EditTexts.selectLine(mEditText).trim();

        new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            String result = NativeUtils.youdaoDictionary(line, true, true);
            synchronized (sLock) {
                Threads.postOnUiThread(() -> {
                    mEditText.getText().insert(
                            mEditText.getSelectionEnd(),
                            "\n" + (result == null ? "youdaoDictionary" : result));
                });
            }
        }).start();
        new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            String result = NativeUtils.baiduTranslate(line, true);

            synchronized (sLock) {
                Threads.postOnUiThread(() -> {
                    mEditText.getText().insert(
                            mEditText.getSelectionEnd(),
                            "\n" + (result == null ? "baiduTranslate" : result));
                });
            }
        }).start();
        new Thread(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            String result = NativeUtils.googleTranslate(line, true);
            synchronized (sLock) {
                Threads.postOnUiThread(() -> {
                    mEditText.getText().insert(
                            mEditText.getSelectionEnd(),
                            "\n" + (result == null ? "googleTranslate" : result));
                });
            }
        }).start();
    }

    private void formatFunctions() {
        if (EditTexts.isWhitespace(mEditText)) return;

        String text = EditTexts.selectLine(mEditText);

        double ret = NativeUtils.calculateExpr(text);

        mEditText.getText().insert(mEditText.getSelectionEnd(), String.format(" = %f", ret));

    }

    private void formatHr() {
        mEditText.getText().insert(
                mEditText.getSelectionStart(),
                "\n---\n"
        );
    }

    private void formatHtm() {
        mEditText.setText(EditTexts.removeRedundancyLines(mEditText.getText().toString()));
        try {
            EditUtils.formatHtm(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void formatIndentIncrease() {
        EditUtils.formatIndentIncrease(this);
    }

    private void formatItalic() {
        EditUtils.wrapSelection(mEditText, "*");
    }

    private void formatLineSpacing() {


        if (!EditTexts.isWhitespace(mEditText)) {
            EditUtils.split(mEditText);
            // mEditText.setText(NativeUtils.removeRedundancy(mEditText.getText().toString().trim().replaceAll("\r+", "")));
        }
    }

    private void formatLink() {

        EditUtils.addLink(mEditText, getClipboardManager());
    }

    private void formatList() {
        EditUtils.formatList(this);
    }

    private void formatOrder() {
        EditUtils.formatOrder(this);
    }

    private void formatReorder() {
        EditUtils.formatReorder(this);
    }

    private void formatSearch() {
//        NativeUtils.renderMarkdown(mEditText.getText().toString(),
//                new File(Environment.getExternalStorageDirectory(), "1.htm").getAbsolutePath());
//        //mEditText.setText(NativeUtils.removeRedundancy(mEditText.getText().toString()));
        EditUtils.replace(this);
    }

    private void formatTable() {
        EditUtils.formatTable(this);
    }

    private void formatTitle() {
        EditUtils.formatTitle(this);
    }

    public ClipboardManager getClipboardManager() {
        if (mClipboardManager == null) {
            mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        }
        return mClipboardManager;
    }

    public EditText getEditText() {
        return mEditText;
    }

    private void loadNote() {
        int nodeId = getIntent().getIntExtra("id", 0);
        if (nodeId == 0) return;
        checkDatabase();
        Pair<String, String> note = mDatabase.fetchNote(nodeId);
        mNote = new Note();
        mNote.Id = nodeId;
        mNote.Title = note.first;
        mNote.Content = note.second;
        mEditText.setText(mNote.Content);
    }

    private void updateNote() {
        checkDatabase();
        String content = mEditText.getText().toString();
        if (content.trim().length() == 0) return;
        if (mNote == null) {
            mNote = new Note();

            mNote.Title = Strings.substringBefore(content.trim(), '\n');
            mNote.Content = content;
            mDatabase.insert(mNote);

        } else {
            mNote.Title = Strings.substringBefore(content.trim(), '\n');
            mNote.Content = content;
            mDatabase.update(mNote);
        }
        mUpdated = true;


    }

    @Override
    public void finish() {
        updateNote();
        if (mUpdated) {
            setResult(RESULT_OK);
        }
        mFinished = true;
        super.finish();
    }

    @Override
    protected void initialize() {
        setContentView(R.layout.activity_edit);
        mEditText = findViewById(R.id.edit);
        loadNote();
        findViewById(R.id.format_hr).setOnClickListener(v -> formatHr());

        findViewById(R.id.format_bold).setOnClickListener(v -> formatBold());
        findViewById(R.id.format_chinese_to_english).setOnClickListener(v -> formatChineseToEnglish());
        findViewById(R.id.format_code).setOnClickListener(v -> formatCode());
        findViewById(R.id.format_english_to_chinese).setOnClickListener(v -> formatEnglishToChinese());
        findViewById(R.id.format_functions).setOnClickListener(v -> formatFunctions());
        findViewById(R.id.format_htm).setOnClickListener(v -> formatHtm());
        findViewById(R.id.format_indent_increase).setOnClickListener(v -> formatIndentIncrease());
        findViewById(R.id.format_italic).setOnClickListener(v -> formatItalic());
        findViewById(R.id.format_line_spacing).setOnClickListener(v -> formatLineSpacing());
        findViewById(R.id.format_link).setOnClickListener(v -> formatLink());
        findViewById(R.id.format_list).setOnClickListener(v -> formatList());
        findViewById(R.id.format_order).setOnClickListener(v -> formatOrder());
        findViewById(R.id.format_reorder).setOnClickListener(v -> formatReorder());
        findViewById(R.id.format_search).setOnClickListener(v -> formatSearch());
        findViewById(R.id.format_table).setOnClickListener(v -> formatTable());
        findViewById(R.id.format_title).setOnClickListener(v -> formatTitle());
        findViewById(R.id.format_cut).setOnClickListener(v -> formatCut());


        //mEditText.setText(Contexts.getText());
    }

    @Override
    protected String[] needPermissions() {
        return new String[0];
    }

    @Override
    protected void onPause() {
        if (!mFinished)
            updateNote();
        super.onPause();
    }

    @Override
    protected int requestCodePermissions() {
        return 0;
    }
}
