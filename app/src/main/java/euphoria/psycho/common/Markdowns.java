package euphoria.psycho.common;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Markdowns {
    public static String convertToTable(String text) {
        String[] lines = text.split("\n");
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (int i = 0, j = lines.length; i < j; i++) {
            String[] pieces = lines[i].trim().split("\\s{2,}");
            sb.append('|');
            for (int k = 0, o = pieces.length; k < o; k++) {
                sb.append(pieces[k]).append('|');
            }
            sb.append('\n');
            if (isFirst) {
                sb.append('|');
                for (int k = 0, o = pieces.length; k < o; k++) {
                    sb.append("---|");
                }
                sb.append('\n');
                isFirst = false;
                continue;
            }
        }
        return sb.toString();
    }
    public static String toggleBold(String text) {
        if (text.startsWith("**") && text.endsWith("**")) {
            text = Strings.substringAfter(text, "**");
            return Strings.substringBeforeLast(text, "**");
        }
        return "**"
                .concat(text)
                .concat("**");
    }
    public static String formatSubOrSup(String text, boolean isSub) {
        Pattern pattern = Pattern.compile("(?<=[a-zA-Z])\\d+");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.replaceAll(String.format("%s$0%s",
                    isSub ? "<sub>" : "<sup>",
                    isSub ? "</sub>" : "</sup>"
            ));
        } else {
            return (isSub ? "<sub>" : "<sup>")
                    .concat(text)
                    .concat(isSub ? "</sub>" : "</sup>");
        }
    }
    public static void toggleBold(EditText editText) {
        if (EditTexts.isWhitespace(editText)) return;
        if (editText.getSelectionStart() == editText.getSelectionEnd()) {
            String text = EditTexts.selectLine(editText);
            editText.getText().replace(editText.getSelectionStart(), editText.getSelectionEnd(), toggleBold(text));
        } else {
            String text = EditTexts.getSelectionText(editText).toString();
            editText.getText().replace(editText.getSelectionStart(), editText.getSelectionEnd(), toggleBold(text));
        }
    }
    public static String removeRedundancyLines(String text) {
        String[] pieces = text.trim().split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0, j = pieces.length; i < j; i++) {
            String piece = pieces[i];
            if (Strings.isNullOrWhiteSpace(piece)) {
                sb.append('\n');
                while (i + 1 < j && Strings.isNullOrWhiteSpace(pieces[i + 1])) {
                    i++;
                }
            } else {
                sb.append(piece).append('\n');
            }
        }
        return sb.toString();
    }
    private static char checkNextChar(Editable text, int index) {
        if (!Character.isWhitespace(text.charAt(index))) return text.charAt(index);
        int len = text.length();
        if (index + 1 >= len) return ' ';
        while (index + 1 < len && Character.isWhitespace(text.charAt(index + 1))) {
            index++;
        }
        if (index + 1 < len) {
            return text.charAt(index + 1);
        }
        return ' ';
    }
    private static final String TAG = "TAG/" + Markdowns.class.getSimpleName();
    public static void smartIncreaseIndent(EditText editText, boolean isIncrease) {
        Editable text = editText.getText();
        int start = editText.getSelectionStart();
        int len = text.length();
        if (len == 0) return;
        while (start - 1 > -1 && text.charAt(start - 1) != '\n') {
            start--;
        }
        boolean bList = false;
        boolean bNumberList = false;
        for (int i = start; i < len; i++) {
            if (text.charAt(i) == '\n') {
                String line = text.subSequence(start, i).toString().trim();
                bList = isList(line);
                bNumberList = isNumberList(line);
                break;
            }
        }
        int end = start;
        for (int i = end; i < len; i++) {
            if (text.charAt(i) == '\n') {
                String line = text.subSequence(end, i).toString().trim();
                if (Strings.isNullOrWhiteSpace(line)) continue;
                boolean a = isList(line);
                boolean b = isNumberList(line);
                if ((bList && a) || (bNumberList && b)
                        || (!bList && !bNumberList && !a && !b)) {
                    end = i;
                } else {
                    break;
                }
            }
        }
//        int len = text.length();
//        if (len == 0) return;
//        while (start - 1 > -1 && text.charAt(start - 1) != '\n') {
//            start--;
//        }
//        StringBuilder sb = new StringBuilder();
//        boolean bList = false;
//        boolean bNumberList = false;
//        for (int i = start; i < len; i++) {
//
//            if (text.charAt(i) == '\n') {
//                String line = text.subSequence(start, i).toString();
//                bList = isList(line);
//                bNumberList = isNumberList(line);
//            }
//        }
//        int end = start;
//        for (int i = end; i < len; i++) {
//
//            if (text.charAt(i) == '\n') {
//                String line = text.subSequence(start, i).toString();
//                bList = isList(line);
//                bNumberList = isNumberList(line);
//            }
//        }
//        char[] chars = new char[]{'#',
//                '*',
//                '1',
//                '2', '3', '4', '5', '6', '7', '8', '9', '0', '>'};
//
//        int end = start;
//        char[] chars1 = new char[]{
//                '1',
//                '2', '3', '4', '5', '6', '7', '8', '9', '0'};
//
//
//        if (Strings.indexOf(chars1, checkNextChar(text, start)) != -1) {
//            while (end + 1 < len && (text.charAt(end + 1) != '\n' ||
//                    text.charAt(end + 1) == '\n' &&
//                            ((end + +2 < len && text.charAt(end + 2) == '\n') ||
//                                    (end + 2 < len
//                                            &&
//                                            (Strings.indexOf(chars1, checkNextChar(text, end + 2)) != -1))))
//                    ) {
//                end++;
//            }
//        } else if (checkNextChar(text, start) == '*') {
//            while (end + 1 < len && (text.charAt(end + 1) != '\n' ||
//                    text.charAt(end + 1) == '\n' &&
//                            ((end + +2 < len && text.charAt(end + 2) == '\n') ||
//                                    (end + 2 < len
//                                            &&
//                                            (checkNextChar(text, end + 2) == '*'))))) {
//                end++;
//            }
//        } else {
//
//            while (end + 1 < len && (text.charAt(end + 1) != '\n' ||
//                    (text.charAt(end + 1) == '\n' &&
//                            end + 2 < len
//                            &&
//                            (Strings.indexOf(chars, checkNextChar(text, end + 2)) == -1))
//            )) {
//
//                if (text.charAt(end + 1) == '\n' && end + 2 < len)
//                    Log.e(TAG, "Debug: smartIncreaseIndent, " + checkNextChar(text, end + 2));
//
//                end++;
//            }
//        }
//
//
        CharSequence value = text.subSequence(start, end);
        String[] lines = value.toString().split("\n");
        StringBuilder sb = new StringBuilder();
        String prefix = Strings.repeat(' ', 4);
        for (int i = 0, j = lines.length; i < j; i++) {
            if (!Strings.isNullOrWhiteSpace(lines[i])) {
                if (isIncrease)
                    sb.append(prefix).append(lines[i]);
                else {
                    if (lines[i].startsWith("    "))
                        sb.append(lines[i].substring(4));
                    else
                        sb.append(lines[i]);
                }
            }
            if (i + 1 < j) {
                sb.append('\n');
            }
        }
        text.replace(start, end, sb.toString());
    }
    //    public static String smartIndent(String s) {
//        /*
//
    //1. asds
//2. asda
// 1. asdas
// 10. sada
// * asd
// *
//         */
//        String[] lines = s.split("\n");
//        Pattern pattern = Pattern.compile("(^ *\\* )|(^ *\\d+\\. )");
//        boolean enter = false;
//        StringBuilder sb = new StringBuilder();
//        List<String> list = new ArrayList<>();
//
//        int count = 0;
//
//        for (int i = 0, j = lines.length; i < j; i++) {
//            String line = lines[i];
//            Matcher matcher = pattern.matcher(line);
//            if (matcher.find()) {
//                enter = true;
//                if (Character.isDigit(line.charAt(0))) {
//                    count = 4;
//                    list.add(line);
//                } else if (Character.isDigit(line.trim().charAt(0))) {
//                    list.add(line);
//                    count += Strings.countStart(line, ' ');
//                } else if (line.trim().charAt(0) == '*') {
//                    list.add(Strings.repeat(' ', count).concat(line.trim()));
//
//
////                    if (list.size() > 1) {
////                        for (int k = list.size() - 2; k > -1; k--) {
////                            if (list.get(k).length() > 0 && (list.get(k).charAt(0) == '#' || list.get(k).charAt(0) == '>')) {
////                                break;
////                            }
////                            matcher = pattern.matcher(list.get(k));
////
////                            if (matcher.find()) {
////                                char c1 = line.trim().charAt(0);
////                                char c2 = list.get(k).trim().charAt(0);
////
////                                if ((Character.isDigit(c2) && c1 == '*')) {
////                                    list.remove(list.size() - 1);
////                                    list.add(Strings.repeat(' ', count).concat(line.trim()));
////                                    count += 4;
////                                } else if ((c1 == '*' && c2 == '*') && Strings.countStart(list.get(k), ' ') > Strings.countStart(line, ' ')) {
////                                    list.remove(list.size() - 1);
////                                    int c = Strings.countStart(list.get(k), ' ');
////                                    list.add(Strings.repeat(' ', c).concat(line.trim()));
////                                }
////                                break;
////
////                            }
////                        }
////                    }
//                }
//
////
////                if (count == 0)
////                    count = Strings.countStart(line, ' ') + 4;
////                else if (!line.trim().startsWith("*"))
////                    count += Strings.countStart(line, ' ');
////                else if (Character.isDigit(line.charAt(0))) {
////                    count = 0;
////                }
////
////                if (list.size() > 1) {
////                    for (int k = list.size() - 2; k > -1; k--) {
////                        if (list.get(k).length() > 0 && (list.get(k).charAt(0) == '#' || list.get(k).charAt(0) == '>')) {
////                            break;
////                        }
////                        matcher = pattern.matcher(list.get(k));
////
////                        if (matcher.find()) {
////                            char c1 = line.trim().charAt(0);
////                            char c2 = list.get(k).trim().charAt(0);
////
////                            if ((Character.isDigit(c2) && c1 == '*')) {
////                                list.remove(list.size() - 1);
////                                list.add(Strings.repeat(' ', count).concat(line.trim()));
////                                count += 4;
////                            } else if ((c1 == '*' && c2 == '*') && Strings.countStart(list.get(k), ' ') > Strings.countStart(line, ' ')) {
////                                list.remove(list.size() - 1);
////                                int c = Strings.countStart(list.get(k), ' ');
////                                list.add(Strings.repeat(' ', c).concat(line.trim()));
////                            }
////                            break;
////
////                        }
////                    }
////                }
//
//                //sb.append(line).append('\n');
//            } else {
//                if (line.length() > 0 && (line.charAt(0) == '#' || line.charAt(0) == '>')) {
//                    enter = false;
//                    count = 0;
//                }
//                if (enter) {
//                    if (Strings.isNullOrWhiteSpace(line))
//                        list.add("");
//                    else {
//                        boolean found = false;
//                        for (int k = list.size() - 1; k > -1; k--) {
//                            matcher = pattern.matcher(list.get(k));
//                            if (matcher.find()) {
//                                list.add(Strings.repeat(' ', Strings.countStart(list.get(k), ' ') + 4)
//                                        .concat(line));
//                                found = true;
//                                break;
//                            }
//                        }
//                        if (!found)
//                            list.add(line);
//                    }
//                    //sb.append(Strings.repeat(' ', count)).append(line).append('\n');
//                } else {
//                    list.add(line);
//                    // sb.append(line).append('\n');
//                }
//            }
//
//
//        }
//        return Strings.join("\n", list);
//        //return sb.toString();
//    }
    private static boolean isList(String line) {
        if (line.length() == 0) return false;
        if (line.charAt(0) != '*') return false;
        int c = 0;
        while (c < line.length() && line.charAt(c) == '*') {
            c++;
        }
        if (c < line.length() && line.charAt(c) == ' ') {
            return true;
        }
        return false;
    }
    public static int parseInt(String s, int defaultValue) {
        if (s == null || s.length() == 0) return defaultValue;
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (int i = 0, j = s.length(); i < j; i++) {
            if (Character.isDigit(s.charAt(i))) {
                sb.append(s.charAt(i));
                found = true;
            } else if (found) {
                return Integer.parseInt(sb.toString());
            }
        }
        return defaultValue;
    }
    private static boolean isNumberList(String line) {
        if (line.length() == 0) return false;
        if (!Character.isDigit(line.charAt(0))) return false;
        int c = 0;
        while (c < line.length() && Character.isDigit(line.charAt(c))) {
            c++;
        }
        if ((c < line.length() && line.charAt(c) == '.') &&
                (c + 1 < line.length() && line.charAt(c + 1) == ' ')) {
            return true;
        }
        return false;
    }
    public static String smartIndent(String text) {
        if (text == null) return null;
        String[] lines = text.split("\n");
        if (lines.length == 0) return null;
        List<String> list = new ArrayList<>();
        boolean enter = false;
        for (int i = 0, j = lines.length; i < j; i++) {
            String line = lines[i];
            if (Strings.isNullOrWhiteSpace(line)) {
                list.add("");
                continue;
            }
            boolean bList = isList(line.trim());
            boolean bNumberList = isNumberList(line.trim());
            if (bList || bNumberList) {
                enter = true;
            }
            if (enter) {
                if (line.charAt(0) == '#' || line.charAt(0) == '>')
                    enter = false;
            }
            if (!enter) {
                list.add(line);
            } else {
//                if ((bList || bNumberList) && countStart(line, ' ') == 0) {
//                    list.add(line);
//                } else { }
                if (list.size() == 0) {
                    list.add(line);
                } else {
                    boolean found = false;
                    for (int k = list.size() - 1; k > -1; k--) {
                        String currentLine = list.get(k);
                        if (Strings.isNullOrWhiteSpace(currentLine)) continue;
                        if (currentLine.charAt(0) == '#' || currentLine.charAt(0) == '>') {
                            break;
                        }
                        boolean a = isList(currentLine.trim());
                        boolean b = isNumberList(currentLine.trim());
//                        if (line.trim().startsWith("2. 其他")) {
//                            list.add(line + " " + a + " " + b + " " + bList + " " + bNumberList+" "+parseInt(line, 0)+"\n");
//                        }
                        if (a || b) {
                            if ((!bList && !bNumberList)) {
                                list.add(Strings.repeat(' ', Strings.countStart(currentLine, ' ') + 4).concat(line.trim()));
                            } else if (a && bNumberList) {
                                if (parseInt(line, 0) > 1) {
                                    //Strings.repeat(' ', Strings.countStart(currentLine, ' ') - 4)
                                    list.add(Strings.repeat(' ', Strings.countStart(currentLine, ' ') - 4).concat(line.trim()));
                                } else
                                    list.add(Strings.repeat(' ', Strings.countStart(currentLine, ' ') + 4).concat(line.trim()));
                            } else if (a) {
                                Pattern pattern = Pattern.compile("^\\s*\\*\\s+[a-z]\\.");
                                boolean a1 = pattern.matcher(currentLine).find();
                                boolean b1 = pattern.matcher(line).find();
                                if (a1 && !b1) {
                                    list.add(Strings.repeat(' ', Strings.countStart(currentLine, ' ') + 4).concat(line.trim()));
                                } else if (b1 & !a1) {
                                    list.add(Strings.repeat(' ', Strings.countStart(currentLine, ' ') - 4).concat(line.trim()));
                                } else {
                                    list.add(Strings.repeat(' ', Strings.countStart(currentLine, ' ')).concat(line.trim()));
                                }
                            } else if (a || bNumberList) {
                                list.add(Strings.repeat(' ', Strings.countStart(currentLine, ' ')).concat(line.trim()));
                            } else {
                                if (Strings.countStart(currentLine, ' ') >= 4) {
                                    list.add(Strings.repeat(' ', Strings.countStart(currentLine, ' ') - 4).concat(line.trim()));
                                } else {
                                    list.add(Strings.repeat(' ', Strings.countStart(currentLine, ' ') + 4).concat(line.trim()));
                                }
                            }
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        list.add(line);
                    }
                }
            }
        }
        if (list.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0, j = list.size(); i < j; i++) {
                sb.append(list.get(i)).append('\n');
            }
            return sb.toString();
        }
        return null;
    }
//    public static String smart(String text) {
//        String[] lines = text.split("\n");
//        Pattern listPattern = Pattern.compile("^\\s*\\* +");
//        Pattern listLetterPattern = Pattern.compile("^\\s*\\* +[a-z]+\\.");
//        Pattern listNumberPattern = Pattern.compile("^\\s*\\d+\\. +");
//        int count = 0;
//        for (int i = 0, j = lines.length; i < j; i++) {
//            String line = lines[i];
//            boolean a = listLetterPattern.matcher(line).find();
//            if (a) {
//
//
//                for (int k = i - 1; k > -1; k--) {
//                    if (Strings.isNullOrWhiteSpace(lines[k])) continue;
//
//                    if (lines[k].charAt(0) == '#' || lines[k].charAt(0) == '>') {
//                        count = 4;
//                        break;
//                    } else if (listNumberPattern.matcher(lines[k]).find()) {
//                        count = Strings.countStart(lines[k], ' ') + 4;
//                        System.out.println("j " + k);
//
//                        lines[i] = Strings.repeat(' ', count) + lines[i].trim();
//                        break;
//                    } else if (listLetterPattern.matcher(lines[k]).find()) {
//                        count = Strings.countStart(lines[k], ' ');
//                        lines[i] = Strings.repeat(' ', count) + lines[i].trim();
//                        count += 4;
//                        break;
//                    }
//                }
//                continue;
//            }
//            boolean b = listPattern.matcher(line).find();
//            if (b) {
//
//                for (int k = i - 1; k > -1; k--) {
//                    if (Strings.isNullOrWhiteSpace(lines[k])) continue;
//
//                    if (lines[k].charAt(0) == '#' || lines[k].charAt(0) == '>') {
//                        count = 4;
//                        break;
//                    } else if (listNumberPattern.matcher(lines[k]).find() || listLetterPattern.matcher(lines[k]).find()) {
//                        count = Strings.countStart(lines[k], ' ') + 4;
//
//                        lines[i] = Strings.repeat(' ', count) + lines[i].trim();
//                        count += 4;
//                        break;
//                    } else if (listPattern.matcher(lines[k]).find()) {
//                        count = Strings.countStart(lines[k], ' ');
//                        lines[i] = Strings.repeat(' ', count) + lines[i].trim();
//                        count += 4;
//                        break;
//                    }
//                }
//                continue;
//            }
//            boolean c = listNumberPattern.matcher(line).find();
//            if (c) {
//
//                for (int k = i - 1; k > -1; k--) {
//                    if (Strings.isNullOrWhiteSpace(lines[k])) continue;
//
//                    if (lines[k].charAt(0) == '#' || lines[k].charAt(0) == '>') {
//                        count = 4;
//                        break;
//                    } else if (listNumberPattern.matcher(lines[k]).find()) {
//                        count = Strings.countStart(lines[k], ' ');
//
//                        if (lines[k].trim().endsWith("：")) {
//                            count += 4;
//                            lines[i] = Strings.repeat(' ', count) + lines[i].trim();
//                        } else
//                            lines[i] = Strings.repeat(' ', count) + lines[i].trim();
//                        count += 4;
//                        break;
//                    }
////                    else if (listPattern.matcher(lines[k]).find()) {
////                        count = countStart(lines[k], ' ');
////                        lines[i] = repeat(' ', count) + lines[i].trim();
////                        count += 4;
////                        break;
////                    }
//                }
//                continue;
//            }
//            if (!Strings.isNullOrWhiteSpace(lines[i]) && !lines[i].startsWith("#"))
//                lines[i] = Strings.repeat(' ', count) + lines[i].trim();
//            if (lines[i].startsWith("#")) count = 0;
//        }
//
//        return Strings.join("\n", lines);
//    }
    public static String smart(String text) {
        String[] lines = text.split("\n");
        Pattern listPattern = Pattern.compile("^\\s*\\* +");
        Pattern listNumberPattern = Pattern.compile("^\\s*\\d+\\. +");
        int count = 0;
        for (int i = 0, j = lines.length; i < j; i++) {
            String line = lines[i];
            boolean b = listPattern.matcher(line).find();
            if (b) {
                count = Strings.countStart(line, ' ') + 4;
                continue;
            }
            boolean c = listNumberPattern.matcher(line).find();
            if (c) {
                count = Strings.countStart(line, ' ') + 4;
                continue;
            }
            if (!Strings.isNullOrWhiteSpace(lines[i]) && !lines[i].startsWith("#"))
                lines[i] = Strings.repeat(' ', count) + lines[i].trim();
            if (lines[i].startsWith("#")) count = 0;
        }
        return Strings.join("\n", lines);
    }
}