package com.mbilab.voca;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity{

    private String URL = "file:///android_asset/index.html";
    private String JS_INTERFACE = "android";
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new mWebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        mWebView.addJavascriptInterface(new JSInterface(), JS_INTERFACE);

        mWebView.loadUrl(URL);
    }

    @Override
    public void onBackPressed() {
        mWebView.loadUrl("javascript:onBackBtnPress()");
    }

    class mWebViewClient extends WebViewClient {
        private static final int LOADED_LOCAL_PAGE = 1;
        private static final int LOADED_APP_PAGE = 2;

        private int loadingCount = 0;

        private boolean isConnect() {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()) return true;
            else return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(URL);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            loadingCount++;
            if((isConnect() && loadingCount == LOADED_APP_PAGE) ||
                    (!isConnect() && loadingCount == LOADED_LOCAL_PAGE)) {
                Handler handler = new Handler();
                handler.postDelayed(new WaitTimeout(view), 1000);
            }
        }
    }

    class WaitTimeout implements Runnable {

        private View _view;
        public WaitTimeout(View view) { _view = view; }

        @Override
        public void run() { _view.setVisibility(View.VISIBLE); }
    }

    class JSInterface {

        @JavascriptInterface
        public void openFeedbackUrl() {
            final String url = "https://merry.ee.ncku.edu.tw/VOCA/feedback/";
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage("com.android.chrome");
            try {
                startActivity(intent);
            } catch(ActivityNotFoundException e) {
                intent.setPackage(null);
                startActivity(intent);
            }
        }

        @JavascriptInterface
        public void finishApp() {
            Log.d("JS-Interface", "back");
            MainActivity.this.finish();
        }
    }
}
