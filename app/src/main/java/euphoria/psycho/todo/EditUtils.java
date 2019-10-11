package euphoria.psycho.todo;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import euphoria.psycho.common.Contexts;
import euphoria.psycho.common.EditTexts;
import euphoria.psycho.common.Files;
import euphoria.psycho.common.Markdowns;
import euphoria.psycho.common.Strings;

public class EditUtils {
    static void addLink(EditText editText, ClipboardManager clipboardManager) {

        CharSequence strings = Contexts.getClipboardString(clipboardManager);
        if (strings == null) {
            editText.getText().insert(editText.getSelectionStart(), "[]()");
        } else {
            editText.getText().replace(editText.getSelectionStart(),
                    editText.getSelectionEnd(),
                    "[".concat(EditTexts.getSelectionText(editText).toString().trim())
                            .concat("](").concat(strings.toString().trim()).concat(")"));
        }
    }

    static void wrapSelection(EditText editText, String s) {


        editText.getText().replace(editText.getSelectionStart(),
                editText.getSelectionEnd(),
                String.format("%s%s%s", s, EditTexts.getSelectionText(editText).toString().trim(), s));
    }

    public static int[] extendSelect(EditText editText) {
        String value = editText.getText().toString();
        if (value.length() == 0) {
            return new int[2];
        }
        int len = value.length();

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == end) {
            start--;
        }
        while (start > 0 && !(value.charAt(start) == '\n' && value.charAt(start - 1) == '\n')) {
            start--;
        }
        len = len - 1;
        while (end < len && !(value.charAt(end) == '\n' && value.charAt(end + 1) == '\n')) {
            end++;

        }

        if (value.charAt(start) == '\n') start++;

        if (end + 1 == len) end = len;
        return new int[]{
                start, end
        };

    }

    static void formatHtm(EditActivity activity) throws IOException {

        if (EditTexts.isWhitespace(activity.getEditText())) return;
        String val = activity.getEditText().getText().toString();

        File dir = new File(Environment.getExternalStorageDirectory(), "Notes");
        if (!dir.isDirectory()) dir.mkdir();

        String title = Files.getValidFileName(Strings.substringBefore(val.trim(), '\n'), ' ') + ".htm";
        File out = new File(dir, title);

        NativeUtils.renderMarkdown(val, out.getAbsolutePath());

        Intent textIntent = new Intent();
        textIntent.setAction(Intent.ACTION_VIEW);
        textIntent.setDataAndType(Uri.fromFile(out), "multipart/related");
        activity.startActivity(textIntent);
    }

    static void formatIndentIncrease(EditActivity activity) {


        int[] position = extendSelect(activity.getEditText());
        activity.getEditText().setSelection(position[0], position[1]);
        String value = activity.getEditText().getText().toString().substring(position[0], position[1]);
        String[] lines = value.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String l : lines) {


            sb.append("    ").append(l).append('\n');
        }
        activity.getEditText().getText().replace(activity.getEditText().getSelectionStart(), activity.getEditText().getSelectionEnd(), sb.toString());

    }

    static void formatList(EditActivity activity) {


        int[] position = extendSelect(activity.getEditText());
        activity.getEditText().setSelection(position[0], position[1]);
        String value = activity.getEditText().getText().toString().substring(position[0], position[1]).trim();
        String[] lines = value.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String l : lines) {
            if (Strings.isNullOrWhiteSpace(l)) continue;
            if (l.startsWith("* ")) {
                sb.append(l.substring(2)).append('\n');
            } else {

                sb.append("* ").append(l).append('\n');
            }
        }
        activity.getEditText().getText().replace(activity.getEditText().getSelectionStart(), activity.getEditText().getSelectionEnd(), sb.toString());

    }

    static void formatOrder(EditActivity activity) {


        int[] position = extendSelect(activity.getEditText());
        activity.getEditText().setSelection(position[0], position[1]);
        String value = activity.getEditText().getText().toString().substring(position[0], position[1]).trim();
        String[] lines = value.split("\n");
        Collator collator = Collator.getInstance(Locale.CHINA);
        Arrays.sort(lines, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return collator.compare(o1.trim(), o2.trim());
            }
        });
        StringBuilder sb = new StringBuilder();
        for (String l : lines) {
            sb.append(l).append('\n');
        }
        activity.getEditText().getText().replace(activity.getEditText().getSelectionStart(), activity.getEditText().getSelectionEnd(), sb.toString());

    }

    static void formatTable(EditActivity activity) {
        int[] position = extendSelect(activity.getEditText());
        activity.getEditText().setSelection(position[0], position[1]);
        String value = activity.getEditText().getText().toString().substring(position[0], position[1]).trim();

        activity.getEditText().getText().replace(activity.getEditText().getSelectionStart(), activity.getEditText().getSelectionEnd(),

                Markdowns.convertToTable(value));

    }

    static void formatTitle(EditActivity activity) {
        // Pattern pattern = Pattern.compile("[\"<>|\0:*?/\\\\]+");

        if (EditTexts.indexBefore(activity.getEditText(), '#') == -1) {
            EditTexts.moveNextChar(activity.getEditText());
            activity.getClipboardManager().setPrimaryClip(ClipData.newPlainText(null,
                    activity.getEditText().getText().subSequence(0, activity.getEditText().getSelectionStart())));
            activity.getEditText().getText().replace(0,
                    activity.getEditText().getSelectionStart(),
                    "# ");
            return;
        }

        int start = getLineStart(activity.getEditText());


        Editable value = activity.getEditText().getText();
        activity.getEditText().setSelection(start);

        if (value.length() == 0 || value.charAt(start) != '#') {
            EditTexts.insertBefore(activity.getEditText(), "## ");
        } else {
            EditTexts.insertBefore(activity.getEditText(), "#");

        }
    }

    public static int getLineStart(EditText editText) {

        String text = editText.getText().toString();
        int len = text.length();
        if (len == 0) return 0;

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len) {
            start--;
        }
        if (start == end && text.charAt(start) == '\n') {
            if (start != 0)
                start--;

            while (start > 0 && text.charAt(start) != '\n') {
                start--;
            }
            if (text.charAt(start) == '\n') {
                start++;
            }

        } else {


            while (start > 0 && text.charAt(start) != '\n') {
                start--;
            }
            if (text.charAt(start) == '\n') {
                start++;
            }


        }
        return start;
    }

    static void replace(EditActivity activity) {
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_find, null);

        EditText findEditText = view.findViewById(R.id.find);
        EditText replaceEditText = view.findViewById(R.id.replace);

        findEditText.setText(activity.getPreferences().getString("find_pattern", null));
        replaceEditText.setText(activity.getPreferences().getString("replace_pattern", null));

        new AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String find = findEditText.getText().toString();
                    String replace = replaceEditText.getText().toString();
                    activity.getPreferences().edit()
                            .putString("find_pattern", find)
                            .putString("replace_pattern", replace)
                            .apply();
                    String value = activity.getEditText().getText().toString().replaceAll(
                            find,
                            replace
                    );
                    activity.getEditText().setText(value.replaceAll("\\\\n", "\n"));
                    dialog.dismiss();

                })
                .setNeutralButton("保留匹配项", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String find = findEditText.getText().toString();
                        activity.getPreferences().edit()
                                .putString("find_pattern", find)
                                .apply();
                        String value = activity.getEditText().getText().toString();
                        activity.getEditText().setText(Strings.join("", Strings.matchAll(value, find)));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("替换选择", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String find = findEditText.getText().toString();
                        String replace = replaceEditText.getText().toString();
                        activity.getPreferences().edit()
                                .putString("find_pattern", find)
                                .putString("replace_pattern", replace)
                                .apply();
                        String value = EditTexts.getSelectionText(activity.getEditText()).toString().replaceAll(
                                find,
                                replace
                        );
                        activity.getEditText().getText().replace(
                                activity.getEditText().getSelectionStart(),
                                activity.getEditText().getSelectionEnd(),
                                value.replaceAll("\\\\n", "\n"));
                        dialog.dismiss();

                    }
                }).show();
    }
}
