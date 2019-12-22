package euphoria.psycho.todo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import euphoria.psycho.common.Contexts;
import euphoria.psycho.common.Strings;

public class DictionaryService extends Service {
    ClipboardManager mClipboardManager;
    OnPrimaryClipChangedListener mOnPrimaryClipChangedListener;
    CharSequence mCharSequence;

    @Override
    public void onCreate() {
        super.onCreate();

        mClipboardManager = Contexts.getClipboardManager(this);
        mOnPrimaryClipChangedListener = new OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                CharSequence charSequence = Contexts.getText(mClipboardManager);
                if (Strings.isNullOrWhiteSpace(charSequence)) return;
                if (mCharSequence.equals(charSequence)) {
                    return;
                } else {
                    mCharSequence = charSequence;
                }
                DictionaryWindow.getInstance(DictionaryService.this).show(charSequence.toString());
            }
        };
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        createNotificationChannel("Default", "Default");
        startForeground(1, new Notification.Builder(this, "Default").build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void createNotificationChannel(String channelId, String channelName) {
        NotificationChannel channel = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        NotificationManager m = getSystemService(NotificationManager.class);
        m.createNotificationChannel(channel);
    }


    @Override
    public void onDestroy() {
        if (mClipboardManager != null && mOnPrimaryClipChangedListener != null) {
            mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        }
        super.onDestroy();
    }
}
