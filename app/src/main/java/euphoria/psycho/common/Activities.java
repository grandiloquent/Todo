package euphoria.psycho.common;

import android.Manifest;
import android.Manifest.permission;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public abstract class Activities extends Activity {

    private static final String TAG = "TAG/" + Activities.class.getSimpleName();
    private SharedPreferences mPreferences;


    public SharedPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        }
        return mPreferences;
    }

    public View click(int resId, OnClickListener listener) {
        View view = findViewById(resId);
        if (view != null && listener != null) view.setOnClickListener(listener);
        return view;
    }

    public void click(OnClickListener listener, int... resIds) {
        for (int resId : resIds) click(resId, listener);
    }

    protected abstract void initialize();

    protected abstract String[] needPermissions();

    protected abstract int requestCodePermissions();

    public void service(Class<?> serviceClass) {

        Intent intent = new Intent(this, serviceClass);
        startService(intent);
    }

    public static void requestOverlayPermission(Activity context, int requestCode) {
//
        if (!Settings.canDrawOverlays(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + context.getPackageName()));

            context.startActivityForResult(intent,
                    requestCode);
        }


    }

    public static void requestScreenCapture(Activity activity, int requestCode) {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) activity.getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);
        if (mediaProjectionManager == null) {
            return;
        }
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();

        activity.startActivityForResult(intent, requestCode);
    }

    public static void stopSelf() {
        Process.killProcess(Process.myPid());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] permissions = needPermissions();
        if (VERSION.SDK_INT >= VERSION_CODES.M
                && permissions != null
                && permissions.length > 0) {
            List<String> shouldPermissions = new ArrayList<>();
            for (String permission : permissions) {
                if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) continue;
                shouldPermissions.add(permission);
            }
            if (shouldPermissions.size() > 0) {
                requestPermissions(shouldPermissions.toArray(new String[0]), requestCodePermissions());
                return;
            }

        }
        initialize();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
//        for (int i = 0, j = grantResults.length; i < j; i++) {
//            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, permissions[i], Toast.LENGTH_LONG).show();
//                return;
//            }
//        }
        initialize();
    }
    //android.settings.action.MANAGE_OVERLAY_PERMISSION


}
