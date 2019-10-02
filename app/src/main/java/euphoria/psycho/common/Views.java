package euphoria.psycho.common;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;

public class Views {
    private static final float DENSITY = Resources.getSystem().getDisplayMetrics().density;

    public static int dp2px(DisplayMetrics displayMetrics, float dp) {
        final float scale = displayMetrics.density;
        return (int) (dp * scale + 0.5f);
    }

    public static int dp2px(int dp) {
        return Math.round(dp * DENSITY);
    }

    public static int getScreenHeight(DisplayMetrics displayMetrics) {
        return displayMetrics.heightPixels;
    }

    public static int getSelectableItemBackground(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);

        return outValue.resourceId;
    }

    public static void onClicks(OnClickListener onClickListener, Activity parent, int... resIds) {
        for (int i = 0, j = resIds.length; i < j; i++) {
            View view = parent.findViewById(resIds[i]);
            if (view != null) {
                view.setOnClickListener(onClickListener);
            }
        }
    }
}