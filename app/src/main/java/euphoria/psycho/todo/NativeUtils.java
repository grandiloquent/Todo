package euphoria.psycho.todo;

public class NativeUtils {
    static {
        System.loadLibrary("native-lib");
    }

    public static native String removeRedundancy(String text);

    public static native String toggleList(String text);

    public static native String toggleNumberList(String text);

    public static native void renderMarkdown(String text, String outFile);

    public static native String googleTranslate(String word, boolean englishToChinese);

    public static native String baiduTranslate(String word, boolean englishToChinese);

    public static native String youdaoDictionary(String word, boolean translate, boolean englishToChinese);


    public static native double calculateExpr(String expr);


}
