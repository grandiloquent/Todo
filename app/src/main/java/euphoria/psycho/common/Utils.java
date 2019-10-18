package euphoria.psycho.common;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {

    public static String readAsset(Context context, String fileName) throws IOException {

        AssetManager manager = context.getAssets();
        InputStream in = manager.open(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

        String line;
        StringBuilder sb = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        return sb.toString();

    }
}
