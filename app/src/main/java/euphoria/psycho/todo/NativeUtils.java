package euphoria.psycho.todo;

public class NativeUtils {
    static {
        System.loadLibrary("native-lib");
    }

    public static native String removeRedundancy(String text);

    public static native void renderMarkdown(String text, String outFile);

    public static native String youdaoDictionary(String word);
}
