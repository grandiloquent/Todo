package euphoria.psycho.todo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.File;

import euphoria.psycho.common.Contexts;
import euphoria.psycho.common.Dictionaries;
import euphoria.psycho.common.Threads;
import euphoria.psycho.common.Views;

import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

class DictionaryWindow {

    private static final int DEFAULT_MARGIN = 24;
    private final Context mContext;
    private WindowManager mWindowManager;
    private LayoutParams mLayoutParams;
    private FrameLayout mView;
    private TextView mTextView;
    private boolean mIsShowing;
    private boolean mHasPermission;
    private static DictionaryWindow sDictionaryWindow;

    private DictionaryWindow(Context context) {
        mContext = context;
        initialize();
        DisplayMetrics displayMetrics = new DisplayMetrics();

        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);

        Point screenSize = new Point(displayMetrics.widthPixels, displayMetrics.heightPixels);
        mLayoutParams = makeLayoutParams();
        mLayoutParams.width = LayoutParams.MATCH_PARENT;
        mLayoutParams.height = LayoutParams.MATCH_PARENT;

        int padding = Views.dp2px(displayMetrics, 12);

        mView = new FrameLayout(mContext);

        ScrollView scrollView = new ScrollView(mContext);
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            scrollView.setBackground(new ColorDrawable(0xFFF2F2F2));
        }
        scrollView.setPadding(padding, padding, padding, padding);

        mTextView = new TextView(mContext);
        mTextView.setTextIsSelectable(true);
        scrollView.addView(mTextView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int margin = Views.dp2px(displayMetrics, DEFAULT_MARGIN);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                screenSize.x - margin,
                screenSize.y / 5);

        params.leftMargin = margin >> 1;
        params.topMargin = screenSize.y - screenSize.y / 5 - margin;

        mView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DictionaryWindow.this.hide();
            }
        });
        mView.addView(scrollView, params);
    }

    private void initialize() {
        Dictionaries.newInstance(mContext, getDictionaryPath());
        mWindowManager = Contexts.getWindowManager(mContext);
        if (mContext instanceof Activity) {
            mHasPermission = checkPermission((Activity) mContext, 100);
        }
    }

    private static String getDictionaryPath() {
        return new File(Environment.getExternalStorageDirectory(), "dictionary.db").getAbsolutePath();
    }


    public void show(String word) {

        Threads.postOnBackgroundThread(() -> {
            String value = Dictionaries.instance().queryYouDao(word);
            Threads.postOnUiThread(() -> {
                mTextView.setText(value);
            });
        });


        mWindowManager.addView(mView, mLayoutParams);
        mIsShowing = true;
    }

    private void hide() {
        mWindowManager.removeView(mView);
        mIsShowing = false;
    }

    private static boolean checkPermission(Activity context, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                context.startActivityForResult(intent, requestCode);
                return false;
            }
        }
        return true;
    }

    public static DictionaryWindow getInstance(Context context) {
        if (sDictionaryWindow == null) {
            sDictionaryWindow = new DictionaryWindow(context);
        }
        return sDictionaryWindow;
    }

    private static LayoutParams makeLayoutParams() {
        LayoutParams params = new LayoutParams();
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            params.type = LayoutParams.TYPE_APPLICATION_OVERLAY;//WindowManager.LayoutParams.TYPE_PHONE;
        } else {
            params.type = LayoutParams.TYPE_PHONE;

        }
        params.format = PixelFormat.TRANSLUCENT;
        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.TOP | Gravity.END;

        // | LayoutParams.FLAG_NOT_FOCUSABLE
        params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
        ;
        return params;
    }
}
