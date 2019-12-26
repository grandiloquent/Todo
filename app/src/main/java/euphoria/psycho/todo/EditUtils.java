package euphoria.psycho.todo;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.util.Log;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import euphoria.psycho.common.Contexts;
import euphoria.psycho.common.EditTexts;
import euphoria.psycho.common.Files;
import euphoria.psycho.common.Markdowns;
import euphoria.psycho.common.Strings;

import static euphoria.psycho.common.EditTexts.detectStrings;

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

    private static void cutLine(EditActivity activity) {
        CharSequence value = EditTexts.cutStrings(activity.getEditText());//EditTexts.cutLine(activity.getEditText());
        if (!Strings.isNullOrWhiteSpace(value)) {
            activity.getClipboardManager().setPrimaryClip(ClipData.newPlainText(null, value.toString().trim()));
        }
    }

    public static CharSequence cutStrings(EditText editText) {
        int[] range = detectStrings(editText);
        if (range.length == 0) return null;
        CharSequence result = editText.getText().subSequence(range[0], range[1]);
        editText.getText().replace(range[0], range[1], Strings.join("\n\n", result.toString().split("[\r\n]+")));
        return result;
    }

    public static int[] extendSelect(EditText editText) {


        int curLine = getCurrentCursorLine(editText);
        if (curLine == -1) return new int[2];

        String[] lines = editText.getText().toString().split("\n");

        int start = curLine;
        int startLine = 0;
        int end = curLine;
        int endLine = lines.length - 1;
        while (--start > -1) {
            if (Strings.isNullOrWhiteSpace(lines[start])) {
                startLine = start;
            }
        }

        while (end++ < lines.length - 1) {
            if (Strings.isNullOrWhiteSpace(lines[end])) {
                endLine = end;
            }
        }
        start = 0;
        for (int i = 0; i < startLine; i++) {
            start += lines[i].length() + 1;
        }
        end = start;
        for (int i = startLine; i < endLine; i++) {
            end += lines[i].length() + 1;
        }
        end += lines[endLine - 1].length();
        return new int[]{start, end};
        //        String value = editText.getText().toString();
//        if (value.length() == 0) {
//            return new int[2];
//        }
//        int len = value.length();
//
//        int start = editText.getSelectionStart();
//        int end = editText.getSelectionEnd();
//        if (start == end) {
//            start--;
//        }
//        while (start > 0 && !(value.charAt(start) == '\n' &&
//                (value.charAt(start - 1) == '\n'
//                        || value.charAt(start - 1) == '\r'
//                ))) {
//            start--;
//        }
//        len = len - 1;
//        while (end < len && !(value.charAt(end) == '\n' && (
//                value.charAt(end + 1) == '\n'
//                        || value.charAt(end + 1) == '\r'
//
//        ))) {
//            end++;
//
//        }
//
//        if (value.charAt(start) == '\n') start++;
//
//        if (end + 1 == len) end = len;
//        return new int[]{
//                start, end
//        };

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
        EditText editText = activity.getEditText();

        selectWholeLines(editText);

        String value = EditTexts.getSelectionText(editText).toString();
        String[] lines = value.split("\n");
        StringBuilder sb = new StringBuilder();
        Pattern p = Pattern.compile("^ +");
        for (String l : lines) {
            Matcher m = p.matcher(l);
            if (m.find()) {
                int len = m.group().length();
                if (len >= 4) {
                    sb.append(l.trim());
                } else
                    sb.append("  ").append(l);
            } else {
                sb.append("  ").append(l);
            }
        }
        editText.getText().replace(editText.getSelectionStart(),
                editText.getSelectionEnd(), sb.toString());
    }

    static void formatList(EditActivity activity) {
        selectWholeLines(activity.getEditText());
        CharSequence charSequence = EditTexts.getSelectionText(activity.getEditText());
        if (Strings.isNullOrWhiteSpace(charSequence)) return;
        String[] sortLines = charSequence.toString().split("\n");

        for (int i = 0; i < sortLines.length; i++) {
            if (sortLines[i].startsWith("- ")) sortLines[i] = sortLines[i].substring(2);
            else sortLines[i] = "- " + sortLines[i];
        }
        activity.getEditText().getText().replace(
                activity.getEditText().getSelectionStart(),
                activity.getEditText().getSelectionEnd(),
                Strings.join("\n", sortLines)
        );
        activity.getEditText().setSelection(activity.getEditText().getSelectionStart());


    }

    static void formatOrder(EditActivity activity) {

        selectWholeLines(activity.getEditText());
        CharSequence charSequence = EditTexts.getSelectionText(activity.getEditText());
        if (Strings.isNullOrWhiteSpace(charSequence)) return;
        activity.getClipboardManager().setPrimaryClip(ClipData.newPlainText(null, charSequence));
        String[] sortLines = charSequence.toString().split("\n");
        Collator collator = Collator.getInstance(Locale.CHINA);
        Arrays.sort(sortLines, (o1, o2) -> collator.compare(o1.trim(), o2.trim()));
        activity.getEditText().getText().replace(
                activity.getEditText().getSelectionStart(),
                activity.getEditText().getSelectionEnd(),
                Strings.join("\n", sortLines)
        );

        //        int[] position = extendSelect(activity.getEditText());
//        activity.getEditText().setSelection(position[0], position[1]);
//        String value = activity.getEditText().getText().toString().substring(position[0], position[1]).trim();
//
//        if (Strings.isNullOrWhiteSpace(value)) return;
//        else {
//
//            Contexts.setText(activity.getClipboardManager(), value);
//        }
//        String[] lines = value.split("\n");
//        Collator collator = Collator.getInstance(Locale.CHINA);
//        Arrays.sort(lines, (o1, o2) -> collator.compare(o1.trim(), o2.trim()));
//        StringBuilder sb = new StringBuilder();
//        for (String l : lines) {
//            sb.append(l).append('\n');
//        }
//        activity.getEditText().getText().replace(activity.getEditText().getSelectionStart(), activity.getEditText().getSelectionEnd(), sb.toString());

    }

    static void formatReorder(EditActivity activity) {
        selectWholeLines(activity.getEditText());
        CharSequence charSequence = EditTexts.getSelectionText(activity.getEditText());
        if (Strings.isNullOrWhiteSpace(charSequence)) return;
        activity.getClipboardManager().setPrimaryClip(ClipData.newPlainText(null, charSequence));
        String[] sortLines = charSequence.toString().split("\n");
        Collator collator = Collator.getInstance(Locale.CHINA);
        Arrays.sort(sortLines, (o1, o2) -> collator.compare(Strings.substringAfterLast(o1.trim(), '/'), Strings.substringAfterLast(o2.trim(), '/')));
        activity.getEditText().getText().replace(
                activity.getEditText().getSelectionStart(),
                activity.getEditText().getSelectionEnd(),
                Strings.join("\n", sortLines)
        );


    }

    static void formatTable(EditActivity activity) {
        selectWholeLines(activity.getEditText());
        CharSequence charSequence = EditTexts.getSelectionText(activity.getEditText());
        if (Strings.isNullOrWhiteSpace(charSequence)) return;
        activity.getClipboardManager().setPrimaryClip(ClipData.newPlainText(null, charSequence));

        activity.getEditText().getText().replace(activity.getEditText().getSelectionStart(), activity.getEditText().getSelectionEnd(),

                Markdowns.convertToTable(charSequence.toString()));

    }

    static void formatTitle(EditActivity activity) {
        // Pattern pattern = Pattern.compile("[\"<>|\0:*?/\\\\]+");

//        if (EditTexts.indexBefore(activity.getEditText(), '#') == -1) {
//            EditTexts.moveNextChar(activity.getEditText());
//            activity.getClipboardManager().setPrimaryClip(ClipData.newPlainText(null,
//                    activity.getEditText().getText().subSequence(0, activity.getEditText().getSelectionStart())));
//            activity.getEditText().getText().replace(0,
//                    activity.getEditText().getSelectionStart(),
//                    "# ");
//            return;
//        }

        int start = getLineStart(activity.getEditText());


        Editable value = activity.getEditText().getText();
        activity.getEditText().setSelection(start);

        if (value.length() == 0 || value.charAt(start) != '#') {
            EditTexts.insertBefore(activity.getEditText(), "## ");
        } else {
            EditTexts.insertBefore(activity.getEditText(), "#");

        }
    }

    public static int getCurrentCursorLine(EditText editText) {
        int selectionStart = Selection.getSelectionStart(editText.getText());
        Layout layout = editText.getLayout();

        if (!(selectionStart == -1)) {
            return layout.getLineForOffset(selectionStart);
        }

        return -1;
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

    public static void selectWholeLine(EditText editText) {
        if (EditTexts.isWhitespace(editText)) {
            return;
        }
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        CharSequence text = editText.getText();
        int len = editText.getText().length();

        while (start - 1 > -1 && text.charAt(start - 1) != '\n') {
            start--;
        }
        while (end + 1 < len && text.charAt(++end) != '\n') ;
        if (end + 1 == len) end++;
        editText.setSelection(start, end);



        /*
         当光标在最尾端时
         editText.getSelectionStart() = editText.getText().length()

         */

    }

    public static void selectWholeLines(EditText editText) {
        if (EditTexts.isWhitespace(editText)) {
            return;
        }
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        CharSequence text = editText.getText();
        int len = editText.getText().length();

        boolean found = false;
        while (start - 1 > -1) {
            char c = text.charAt(start - 1);
            start--;
            if (c == '\n') {
                while (start - 1 > -1 && Character.isWhitespace(text.charAt(start - 1))) {
                    if (text.charAt(start - 1) == '\n') {
                        found = true;
                        break;
                    }
                    start--;
                }
                if (found) break;
            }
        }
        found = false;
        while (end + 1 < len) {
            char c = text.charAt(end + 1);
            end++;
            if (c == '\n') {
                while (end + 1 < len && Character.isWhitespace(text.charAt(end + 1))) {
                    if (text.charAt(end + 1) == '\n') {
                        found = true;
                        break;
                    }
                    end++;
                }
                if (found) break;
            }
        }
        if (start > 0) {
            start++;
        }
        editText.setSelection(start, end);

//        while (start - 1 > -1 && text.charAt(start - 1) != '\n') {
//            start--;
//        }
//        int startTmp;
//
//        do {
//            startTmp = start - 1;
//
//            while (startTmp - 1 > -1 && text.charAt(startTmp - 1) != '\n') {
//                startTmp--;
//            }
//            if (startTmp < 0 || startTmp == start) break;
//            if (text.subSequence(startTmp, start).toString().trim().length() == 0) {
//                break;
//            }
//            start = startTmp;
//        } while (startTmp > -1);
//
//
//        while (end + 1 < len && text.charAt(++end) != '\n') ;
//
//        int endTmp;
//
//        do {
//            endTmp = end;
//
//            while (endTmp + 1 < len && text.charAt(endTmp + 1) != '\n') {
//                endTmp++;
//            }
//            if (endTmp >= len || endTmp == end) break;
//            if (text.subSequence(end, endTmp).toString().trim().length() == 0) {
//                break;
//            }
//            end = endTmp;
//        } while (endTmp < len);
//
//
//        if (end + 1 == len) end++;
//        editText.setSelection(start, end);



        /*
         当光标在最尾端时
         editText.getSelectionStart() = editText.getText().length()

         */

    }

    public static void split(EditText editText) {
        Editable text = editText.getText();
        int len = text.length();
        if (len == 0) return;
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len || text.charAt(start) == '\n') {
            start--;
        }
        if (end < len && text.charAt(end) == '\n' && end - 1 > -1) {
            end--;
        }
        while (start - 1 > -1 && text.charAt(start - 1) != '\n') start--;
        while (start - 1 > -1 && Character.isWhitespace(text.charAt(start - 1))) start--;
        while (end + 1 < len && text.charAt(end + 1) != '\n') end++;
        while (end + 1 < len && Character.isWhitespace(text.charAt(end + 1))) end++;
        if (end + 1 < len) end++;
        CharSequence charSequence = editText.getText().subSequence(start, end);
        editText.getText().replace(start, end, Strings.join(".\n\n", charSequence.toString().split("[\\.\r\n]+\\s*")));

    }

    static void wrapSelection(EditText editText, String s) {


        editText.getText().replace(editText.getSelectionStart(),
                editText.getSelectionEnd(),
                String.format("%s%s%s", s, EditTexts.getSelectionText(editText).toString().trim(), s));
    }

    static void insert(EditText editText, String s) {
        editText.getText().insert(editText.getSelectionStart(), s);
    }
}
