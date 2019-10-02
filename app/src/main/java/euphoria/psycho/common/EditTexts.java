package euphoria.psycho.common;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

public class EditTexts {
    private static final String TAG = "TAG/" + EditTexts.class.getSimpleName();

    public static void copy(EditText editText, ClipboardManager clipboardManager) {
        if (!isWhitespace(editText)) {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, editText.getText().toString()));
        }
    }

    public static CharSequence cutLine(EditText editText) {
        Editable text = editText.getText();
        int len = text.length();
        if (len == 0) return null;
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len || text.charAt(start) == '\n') {
            start--;
        }
        while (start > 0 && text.charAt(start) != '\n') {
            start--;
        }
        while (end < len && text.charAt(end) != '\n') {
            end++;
        }
        CharSequence value = editText.getText().subSequence(start, end);
        editText.getText().delete(start, end);
        return value;
    }

    public static CharSequence cutStrings(EditText editText) {
        int[] range = detectStrings(editText);
        if (range.length == 0) return null;
        CharSequence result = editText.getText().subSequence(range[0], range[1]);
        editText.getText().replace(range[0], range[1], "\n\n");
        return result;
    }

    public static CharSequence deleteExtend(EditText editText) {
        Editable text = editText.getText();
        int len = text.length();
        if (len == 0) return null;
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len) {
            start--;
        }
        boolean found = false;
        for (int i = start; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '\n') {
                for (int j = i - 1; j >= 0; j--) {
                    c = text.charAt(j);
                    if (!Character.isWhitespace(c)) break;
                    if (c == '\n') {
                        start = j;
                        found = true;
                        break;
                    }
                }
            }
            if (found) break;
        }
        if (!found) {
            start = 0;
        }
        found = false;
        for (int i = end; i < len; i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                for (int j = i + 1; j < len; j++) {
                    c = text.charAt(j);
                    if (!Character.isWhitespace(c)) break;
                    if (c == '\n') {
                        end = j + 1;
                        found = true;
                        break;
                    }
                }
            }
            if (found) break;
        }
        if (!found) {
            end = len;
        }
        CharSequence value = text.subSequence(start, end);
        text.replace(start, end, "\n");
        return value;
    }

    public static CharSequence deleteLineStrict(EditText editText) {
        Editable text = editText.getText();
        int len = text.length();
        if (len == 0) return null;
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len || text.charAt(start) == '\n') {
            start--;
        }
        boolean found = false;
        for (int i = start; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '\n') {
                for (int j = i - 1; j >= 0; j--) {
                    c = text.charAt(j);
                    if (!Character.isWhitespace(c)) {
                        start = j + 1;
                        found = true;
                        break;
                    }
                }
            }
            if (found) break;
        }
        if (!found) {
            start = 0;
        }
        found = false;
        for (int i = end; i < len; i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                for (int j = i + 1; j < len; j++) {
                    c = text.charAt(j);
                    if (!Character.isWhitespace(c)) {
                        end = j;
                        found = true;
                        break;
                    }
                }
            }
            if (found) break;
        }
        if (!found) {
            end = len;
        }
        CharSequence value = text.subSequence(start, end);
        text.delete(start, end);
        return value;
    }

    /*
     *
     * 从光标点向前匹配第一个换行符，匹配成功后，继续向前匹配非空字符，匹配成功后停止
     * 以同样的道理向后匹配
     * */
    public static CharSequence deleteLineWithWhitespace(EditText editText) {
        Editable text = editText.getText();
        int len = text.length();
        if (len == 0) return null;
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
        editText.getText().replace(start, end, "\n\n");
        return charSequence;
    }

    // 选择相邻的非空行
    public static int[] detectStrings(EditText editText) {
        if (isWhitespace(editText)) {
            return new int[0];
        }
        Editable text = editText.getText();
        //char[] chars = text.toString().toCharArray();
        int len = text.length();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        //|| (text.charAt(start) == '\n' && start - 1 > -1)
        if (start == len) {
            start--;
        }
        if (start > 0) {
            for (int i = start - 1; i > -1; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    while (i - 1 > -1 && Character.isWhitespace(text.charAt(i - 1)) && text.charAt(i - 1) != '\n') {
                        i--;
                    }
                    if (i == 0) {
                        start = 0;
                        break;
                    }
                    if (text.charAt(i - 1) == '\n') {
                        start = i - 1;
                        break;
                    }
                }
            }
        }
        if (end != len) {
            for (int i = end; i < len; i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    while (i + 1 < len && Character.isWhitespace(text.charAt(i + 1)) && text.charAt(i + 1) != '\n') {
                        i++;
                    }
                    if (i == len) {
                        end = len;
                        break;
                    }
                    if (text.charAt(i + 1) == '\n') {
                        end = i + 1;
                        break;
                    }
                }
            }
        }
//        Log.e(TAG, "Debug: flatLine, \n"
//                + "\\n: " + ((int) '\n') + "\n"
//                + "start: " + start + "\n"
//                + "start char: " + ((int) text.charAt(start)) + "\n"
//                + "len: " + len + "\n"
//                + "end: " + end + "\n"
//                + "end char: " + ((int) text.charAt(end - 1)) + "\n"
//        );
        if (end + 1 < len) end++;
        if (end + 1 == len) end = len;
        //editText.setSelection(start, end);
        return new int[]{start, end};
    }

    public static CharSequence flatLine(EditText editText) {
        Editable text = editText.getText();
        int len = text.length();
        if (len == 0) return null;
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len || text.charAt(start) == '\n') {
            start--;
        }
        if (end < len && text.charAt(end) == '\n' && end - 1 > -1) {
            end--;
        }
        start = lookBack(text, start);
        end = lookForward(text, end);
        if (end + 1 < len) end++;
        if (end > len) end = len;
        CharSequence charSequence = editText.getText().subSequence(start, end);
        String result = charSequence.toString().replaceAll("\n+", " ");
        result = result.replaceAll("\\s{2,}", " ");
        editText.getText().replace(start, end, result + "\n\n");
        return charSequence;
    }

    public static CharSequence getSelectionText(EditText editText) {
        return editText.getText().subSequence(editText.getSelectionStart(), editText.getSelectionEnd());
    }

    public static void indentDecrease(EditText editText) {
        Editable text = editText.getText();
        int len = text.length();
        if (len == 0) return;
        int start = editText.getSelectionStart();
//        if (start == len || text.charAt(start) == '\n') {
//            start--;
//        }
        while (start - 1 > -1 && text.charAt(start - 1) != '\n') {
            start--;
        }
//        Log.e(TAG, "Debug: indentDecrease, " + start
//                + " " + (int) text.charAt(start)
//                + " " + (int) ' '
//                + " " + (int) text.charAt(start + 1)
//                + " " + text.charAt(start + 2)
//                + " " + text.charAt(start + 3));
        if (text.charAt(start) == ' '
                && (start + 1 < len && text.charAt(start + 1) == ' ')
                && (start + 2 < len && text.charAt(start + 2) == ' ')
                && (start + 3 < len && text.charAt(start + 3) == ' ')
                )
            editText.getText().delete(start, start + 4);
    }

    public static void indentIncrease(EditText editText) {
        Editable text = editText.getText();
        int len = text.length();
        if (len == 0) return;
        int start = editText.getSelectionStart();
        while (start - 1 > -1 && text.charAt(start - 1) != '\n') {
            start--;
        }
        editText.getText().insert(start, Strings.repeat(' ', 4));
    }

    public static int indexBefore(EditText editText, char c) {
        Editable text = editText.getText();
        int start = editText.getSelectionStart();
        for (int i = 0; i < start; i++) {
            if (text.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(char[] chars, char c) {
        for (int i = 0, j = chars.length; i < j; i++) {
            if (chars[i] == c) return i;
        }
        return -1;
    }

    public static void insertAfter(EditText editText, String text) {
        int end = editText.getSelectionEnd();
        editText.getText().insert(end, text);
        editText.setSelection(end);
    }

    public static void insertBefore(EditText editText, String text) {
        int start = editText.getSelectionStart();
        editText.getText().insert(start, text);
        editText.setSelection(start);
    }

    public static boolean isWhitespace(EditText editText) {
        Editable editable = editText.getText();
        if (editable.length() == 0) return true;
        for (int i = 0, j = editable.length(); i < j; i++) {
            if (!Character.isWhitespace(editable.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static int lookBack(Editable text, int start) {
        int end = start;
        while (end - 1 > 0 && ((end == start) || Strings.count(text.subSequence(end, start), '\n') < 2)) {
            start = end;
            while (start - 1 > -1 && !Character.isWhitespace(text.charAt(start - 1))) {
                start--;
            }
            if (start == 0) return 0;
            end = start;
            while (end - 1 > 0 && Character.isWhitespace(text.charAt(end - 1))) {
                end--;
            }
            end--;
        }
        return end;
    }

    public static int lookForward(Editable text, int start) {
        int end = start;
        int len = text.length();
        while (end + 1 < len && ((end == start) || Strings.count(text.subSequence(start, end), '\n') < 2)) {
            start = end;
            while (start + 1 < len && !Character.isWhitespace(text.charAt(start + 1))) {
                start++;
            }
            if (start + 1 >= len) return start + 1;
            end = start;
            while (end + 1 < len && Character.isWhitespace(text.charAt(end + 1))) {
                end++;
            }
            end++;
        }
        return end;
    }

    private static int lookupNegative(CharSequence s, int start) {
        int idx = start;
        while (idx > 0 && Character.isWhitespace(s.charAt(idx))) {
            idx--;
        }
        return idx;
    }

    private static int lookupPositive(CharSequence s, int end) {
        int idx = end;
        int len = s.length();
        while (idx < len && Character.isWhitespace(s.charAt(idx))) {
            idx++;
        }
        return idx;
    }

    public static void moveNextChar(EditText editText) {
        if (isWhitespace(editText)) return;
        Editable text = editText.getText();
        int len = text.length();
        int start = editText.getSelectionStart();
        if (start == len) {
            start--;
        }
        while (start < len && Character.isWhitespace(text.charAt(start))) {
            start++;
        }
        editText.setSelection(start);
    }

    public static void selectAt(EditText editText, char[] anchorChars) {
        if (isWhitespace(editText)) return;
        Editable text = editText.getText();
        int len = text.length();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len) {
            start--;
        }
        while (start > 0 && indexOf(anchorChars, text.charAt(start)) == -1) {
            start--;
        }
        if (start != 0 && start + 1 < len) start++;
        while (end < len && indexOf(anchorChars, text.charAt(end)) == -1) {
            end++;
        }
        if (end + 1 == len) end = len;
        else if (end + 1 < len) end++;
        editText.setSelection(start, end);
    }

    public static void selectAt(EditText editText, char anchorChar) {
        if (isWhitespace(editText)) return;
        Editable text = editText.getText();
        int len = text.length();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len) {
            start--;
        }
        while (start > 0 && text.charAt(start) != '\n' && text.charAt(start) != anchorChar) {
            start--;
        }
        if (start != 0 && start + 1 < len) start++;
        while (end < len && text.charAt(end) != '\n' && text.charAt(end) != anchorChar) {
            end++;
        }
        if (end + 1 == len) end = len;
        else if (end + 1 < len) end++;
        editText.setSelection(start, end);
    }

    public static CharSequence selectExtends(EditText editText) {
        Editable text = editText.getText();
        int len = text.length();
        if (len == 0) return null;
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len || text.charAt(start) == '\n') {
            start--;
        }
        while (start > 0 && text.charAt(start) != '\n') {
            start--;
        }
        while (start > 0 && Character.isWhitespace(text.charAt(start - 1))) {
            start--;
        }
        while (end < len && text.charAt(end) != '\n') {
            end++;
        }
        while (end < len && Character.isWhitespace(text.charAt(end))) {
            end++;
        }
        CharSequence value = editText.getText().subSequence(start, end);
        editText.setSelection(start, end);
        return value;
    }

    public static void paste(EditText editText, String text) {
        editText.getText().replace(
                editText.getSelectionStart(),
                editText.getSelectionEnd(),
                text
        );
    }

    public static String selectLine(EditText editText) {
        if (isWhitespace(editText)) return null;
        Editable text = editText.getText();
        int len = text.length();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len) {
            start--;
        }
        while (start > 0 && text.charAt(start - 1) != '\n') {
            start--;
        }
        while (end + 1 < len && text.charAt(end) != '\n') {
            end++;
        }
//        char c=text.charAt(end);
//        String v=text.subSequence(start, end).toString();;
        if (end < len && text.charAt(end) != '\n') {
            end++;
        }
        editText.setSelection(start, end);
        return text.subSequence(start, end).toString();
//
//        int len = text.length();
//
//        int start = editText.getSelectionStart();
//        int end = editText.getSelectionEnd();
//        if (start == len) {
//            start--;
//        }
//        if (start == end && text.charAt(start) == '\n') {
//            start--;
//
//            while (start > 0 && text.charAt(start) != '\n') {
//                start--;
//            }
//            if (text.charAt(start) == '\n') {
//                start++;
//            }
//
//        } else {
//
//
//            while (start > 0 && text.charAt(start) != '\n') {
//                start--;
//            }
//            if (text.charAt(start) == '\n') {
//                start++;
//            }
//
//            while (end < len && text.charAt(end) != '\n') {
//                end++;
//            }
//
//
//        }
//        editText.setSelection(start, end);
//
//        // String str=text.substring(start, end);
//
//        return text.substring(start, end);
    }

    public static void selectRegion(EditText editText) {
        if (isWhitespace(editText)) return;
        Editable text = editText.getText();
        int len = text.length();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == len) {
            start--;
        }
        while (start > 0) {
            if (text.charAt(start - 1) == '\n') {
                int idx = lookupNegative(text, start);
                if (Strings.count(text.subSequence(idx, start + 1), '\n') == 3) {
                    start = idx + 1;
                    break;
                }
            }
            start--;
        }
        while (end + 1 < len) {
            if (text.charAt(end + 1) == '\n') {
                int idx = lookupPositive(text, end);
                if (Strings.count(text.subSequence(end, idx), '\n') == 3) {
                    end = idx;
                    break;
                }
            }
            end++;
        }
        if (text.charAt(start) == '\n') {
            start++;
        }
        if (end < len && text.charAt(end) == '\n') {
            end--;
        }
        if (end == len - 1) {
            end = len;
        }
        Log.e(TAG, "Debug: selectRegion, " + start + " " + end);
        editText.setSelection(start, end);
    }
}