package euphoria.psycho.todo;

import android.app.Activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import euphoria.psycho.common.Activities;

public class CommonActivity extends Activities {

    @Override
    protected void initialize() {
        NativeUtils.youdaoDictionary("word");
    }

    @Override
    protected String[] needPermissions() {
        return new String[0];
    }

    @Override
    protected int requestCodePermissions() {
        return 0;
    }
}
