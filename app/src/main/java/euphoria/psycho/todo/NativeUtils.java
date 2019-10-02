package euphoria.psycho.todo;

public class NativeUtils {
    static {
        System.loadLibrary("native-lib");
    }

    public static native String removeRedundancy(String text);
}
