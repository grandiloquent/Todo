package euphoria.psycho;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import java.io.IOException;

import euphoria.psycho.common.Utils;
import euphoria.psycho.todo.R;

public class Browsers extends Activity {

    private static final int MENU_JD = 1;
    private static final String URL_JD = "https://m.jd.com";
    WebView mWebView;
    EditText mEditText;

    private int mMode = 0;


    private void evaluateJavascript() {
        String fileName = null;
        switch (mMode) {
            case 0:
                fileName = "jd.js";
                break;
        }
        try {
            String javaScript = Utils.readAsset(this, fileName);

            //Log.e(TAG, "Debug: evaluateJavascript, " + javaScript);

            if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
                mWebView.evaluateJavascript(javaScript, value -> {
                    //Toast.makeText(Browsers.this, value, Toast.LENGTH_SHORT).show();
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initialize() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        EditText editText = new EditText(this);
        LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(editText, layoutParams);
        editText.setMaxLines(1);
        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

                    switch (mMode) {
                        case 0:
                            mWebView.loadUrl(String.format("https://so.m.jd.com/ware/search.action?keyword=%s&filt_type=col_type,L0M0;redisstore,1;&sort_type=sort_dredisprice_asc&sf=11&as=1&qp_disable=no", mEditText.getText().toString()));
                            return true;
                    }
                    return true;
                }
                return false;
            }
        });
        ;

        mEditText = editText;
        mWebView = setupWebView(linearLayout);

        setContentView(linearLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void menuJd() {
        mWebView.loadUrl(URL_JD);
    }

    private WebView setupWebView(LinearLayout linearLayout) {
        WebView webView = new WebView(this);


        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        String appCachePath = getApplication().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);


        //Log.e(TAG, "Debug: setupWebView, " + appCachePath);

        webSettings.setDatabaseEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onLoadResource(WebView view, String url) {
//                super.onLoadResource(view, url);
//            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mEditText.setText(url);
                evaluateJavascript();
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                //Log.e(TAG, "Debug: shouldOverrideUrlLoading, " + url);

                if (url.startsWith("openapp.jdmobile:")) return false;
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {


               // Log.e(TAG, "Debug: onConsoleMessage, " + consoleMessage.message());

                return super.onConsoleMessage(consoleMessage);
            }
        });

        LinearLayout.LayoutParams webViewLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        webViewLayoutParams.weight = 1;
        linearLayout.addView(webView, webViewLayoutParams);

        return webView;
    }

    private static final String TAG = "TAG/" + Browsers.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_JD, 0, "京东");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_JD:
                menuJd();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }
}
