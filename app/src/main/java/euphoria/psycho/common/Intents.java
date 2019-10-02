package euphoria.psycho.common;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.util.Set;
public class Intents {
    private static final String TAG = "TAG/" + Intents.class.getSimpleName();

    public static IntentFilter buildBecomingNoisyIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
         /*
         Broadcast intent, a hint for applications that audio is about to become
         'noisy' due to a change in audio outputs. For example, this intent may
         be sent when a wired headset is unplugged, or when an A2DP audio
         sink is disconnected, and the audio system is about to automatically
         switch audio route to the speaker. Applications that are controlling
         audio streams may consider pausing, reducing volume or some other action
         on receipt of this intent so as not to surprise the user with audio
         from the speaker.
         @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
         public static final String ACTION_AUDIO_BECOMING_NOISY = "android.media.AUDIO_BECOMING_NOISY";
         */
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        return intentFilter;
    }

    public static IntentFilter buildLocalChangedIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
         /*
         Broadcast Action: The current device's locale has changed.
         This is a protected intent that can only be sent
         by the system.
         @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
         public static final String ACTION_LOCALE_CHANGED = "android.intent.action.LOCALE_CHANGED";
         */
        intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
        return intentFilter;
    }

    public static IntentFilter buildMediaIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MEDIA_SCANNER_STARTED");
        intentFilter.addAction("android.intent.action.MEDIA_SCANNER_FINISHED");
        intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
         /*
         Add a new Intent data scheme to match against.  If any schemes are
         included in the filter, then an Intent's data must be either
         one of these schemes or a matching data type.  If no schemes
         are included, then an Intent will match only if it includes no data.
         Note: scheme matching in the Android framework is
         case-sensitive, unlike formal RFC schemes.  As a result,
         you should always write your schemes with lower case letters,
         and any schemes you receive from outside of Android should be
         converted to lower case before supplying them here.
         @param scheme Name of the scheme to match, such as "http".
         @see #matchData
         */
        intentFilter.addDataScheme("file");
        return intentFilter;
    }

    public static IntentFilter buildScreenIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
         /*
         Broadcast Action: Sent when the device goes to sleep and becomes non-interactive.
         For historical reasons, the name of this broadcast action refers to the power
         state of the screen but it is actually sent in response to changes in the
         overall interactive state of the device.
         This broadcast is sent when the device becomes non-interactive which may have
         nothing to do with the screen turning off.  To determine the
         actual state of the screen, use android.view.Display#getState.
         See android.os.PowerManager#isInteractive for details.
         You cannot receive this through components declared in
         manifests, only by explicitly registering for it with
         Context#registerReceiver(BroadcastReceiver, IntentFilter)
         Context.registerReceiver().
         This is a protected intent that can only be sent
         by the system.
         @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
         public static final String ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
         */
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
         /*
         Broadcast Action: Sent when the device wakes up and becomes interactive.
         For historical reasons, the name of this broadcast action refers to the power
         state of the screen but it is actually sent in response to changes in the
         overall interactive state of the device.
         This broadcast is sent when the device becomes interactive which may have
         nothing to do with the screen turning on.  To determine the
         actual state of the screen, use android.view.Display#getState.
         See android.os.PowerManager#isInteractive for details.
         You cannot receive this through components declared in
         manifests, only by explicitly registering for it with
         Context#registerReceiver(BroadcastReceiver, IntentFilter)
         Context.registerReceiver().
         This is a protected intent that can only be sent
         by the system.
         @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
         public static final String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
         */
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
         /*
         Add a new Intent data scheme to match against.  If any schemes are
         included in the filter, then an Intent's data must be either
         one of these schemes or a matching data type.  If no schemes
         are included, then an Intent will match only if it includes no data.
         Note: scheme matching in the Android framework is
         case-sensitive, unlike formal RFC schemes.  As a result,
         you should always write your schemes with lower case letters,
         and any schemes you receive from outside of Android should be
         converted to lower case before supplying them here.
         @param scheme Name of the scheme to match, such as "http".
         @see #matchData
         */
        return intentFilter;
    }

    public static Intent createShareIntent(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        return intent;
    }

    public static void dumpIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("action = %s,\n", intent.getAction()));
        sb.append(String.format("data = %s", intent.getData()));
        if (extras != null) {
            Set<String> keys = extras.keySet();
            for (String key : keys) {
                sb.append(",\n").append(String.format("%s = %s", key, extras.get(key)));
            }
        }
        Log.e(TAG, "Error: dumpIntent, " + sb.toString());
    }

    public static void notifyAudioEffect(Context context, int sessionId, String packageName, boolean close) {
        Intent intent;
        if (close) {
         /*
         Intent to signal to the effect control application or service that an audio session
         is closed and that effects should not be applied anymore.
         The effect control application receiving this intent will delete all effects on
         this session and store current settings in package specific storage.
         The calling package name is indicated by the EXTRA_PACKAGE_NAME extra and the
         audio session ID by the EXTRA_AUDIO_SESSION extra. Both extras are mandatory.
         It is good practice for applications to broadcast this intent when music playback stops
         and/or when exiting to free system resources consumed by audio effect engines.
         @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
         public static final String ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION =
         "android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION";
         */
            intent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        } else {
         /*
         Intent to signal to the effect control application or service that a new audio session
         is opened and requires audio effects to be applied.
         This is different from ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL in that no
         UI should be displayed in this case. Music player applications can broadcast this intent
         before starting playback to make sure that any audio effect settings previously selected
         by the user are applied.
         The effect control application receiving this intent will look for previously stored
         settings for the calling application, create all required audio effects and apply the
         effect settings to the specified audio session.
         The calling package name is indicated by the EXTRA_PACKAGE_NAME extra and the
         audio session ID by the EXTRA_AUDIO_SESSION extra. Both extras are mandatory.
         If no stored settings are found for the calling application, default settings for the
         content type indicated by EXTRA_CONTENT_TYPE will be applied. The default settings
         for a given content type are platform specific.
         @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
         public static final String ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION =
         "android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION";
         */
            intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        }
         /*
         Contains the ID of the audio session the effects should be applied to.
         This extra is for use with ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL,
         ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION and
         ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION intents.
         The extra value is of type int and is the audio session ID.
         @see android.media.MediaPlayer#getAudioSessionId() for details on audio sessions.
         public static final String EXTRA_AUDIO_SESSION = "android.media.extra.AUDIO_SESSION";
         */
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId);
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName);
         /*
         Broadcast the given intent to all interested BroadcastReceivers.  This
         call is asynchronous; it returns immediately, and you will continue
         executing while the receivers are run.  No results are propagated from
         receivers and receivers can not abort the broadcast. If you want
         to allow receivers to propagate results or abort the broadcast, you must
         send an ordered broadcast using
         sendOrderedBroadcast(Intent, String).
         See BroadcastReceiver for more information on Intent broadcasts.
         @param intent The Intent to broadcast; all receivers matching this
         Intent will receive the broadcast.
         @see android.content.BroadcastReceiver
         @see #registerReceiver
         @see #sendBroadcast(Intent, String)
         @see #sendOrderedBroadcast(Intent, String)
         @see #sendOrderedBroadcast(Intent, String, BroadcastReceiver, Handler, int, String, Bundle)
         */
        context.sendBroadcast(intent);
    }

    public static void shareAudioFile(Context context, File audioFile, String title) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/*");
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(audioFile));
        context.startActivity(Intent.createChooser(share, title));
    }
}