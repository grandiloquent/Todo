package euphoria.psycho.common;

import android.util.Log;

public class Logs {

    private static final String TAG = "TAG/";

    public static void d(Object... messages) {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        for (Object m : messages) sb.append(m).append('\n');
        Log.e(TAG, sb.toString());

    }
}
