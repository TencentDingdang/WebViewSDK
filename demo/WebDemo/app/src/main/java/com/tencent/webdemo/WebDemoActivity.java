package com.tencent.webdemo;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.tencent.ai.tvs.web.DeviceWebController;
import com.tencent.ai.tvs.web.TVSWebView;
import com.tencent.ai.tvs.web.WebController;
import com.tencent.ai.tvs.web.common.TVSWebDevice;
import com.tencent.ai.tvs.web.util.AndroidBug5497Workaround;

import org.json.JSONObject;

public class WebDemoActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private DeviceWebController mController;

    private static final boolean REQURLWITHCACHE = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * 1、设置样式
         */
        setFullScreen();
        setTranslucentStatus();
        setContentView(R.layout.activity_webdemo);
        setDebugModeAndCompat();

        mProgressBar = findViewById(R.id.demoprogressbar);
        mController = ((TVSWebView) findViewById(R.id.demowebview)).getDeviceController();

        /**
         * 2、初始化TVSWebDevice 设置设备信息
         */
        TVSWebDevice tvsDevice = new TVSWebDevice();
        tvsDevice.productID = DemoConstant.PRODUCT_ID;
        tvsDevice.dsn = DemoConstant.DSN;
        mController.setWebDeviceInfo(tvsDevice);
        /**
         * 3、设置TVSToken，有的话就填写
         */
        mController.setTVSToken(DemoConstant.TVSTOKEN);
        /**
         * 4、设置WebUI相关回调事件处理（包括加载状态）
         */
        mController.setUIEventListener(new WebDemoUIEventListener());
        /**
         * 5、设置叮当内置JS回调
         */
        mController.setSimpleBusinessEventListener(new WebDemoBusinessEventListener());
        /**
         * 6、打开URL（是否需要缓存、是否带上tvsToken）
         */
        if (REQURLWITHCACHE) {
            // 带Cache的需要引入okhttp
            mController.setLoadCacheOnDisconnected(true);
        }
        else {
            // 不带Cache
            mController.setLoadCacheOnDisconnected(false);
        }
        mController.loadURL("https://ddsdk.html5.qq.com/v2/opt/music/login", true);
    }

    @Override
    public void onBackPressed() {
        if (!mController.goBack()) {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!mController.goBack()) {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 设置全屏
     */
    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 设置状态栏透明
     */
    private void setTranslucentStatus() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * 设置WebDebug模式以及AndroidWebView原生bug
     */
    private void setDebugModeAndCompat() {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        AndroidBug5497Workaround.assistActivity(this);
    }

    private class WebDemoUIEventListener implements WebController.UIEventListener {

        @Override
        public void onLoadStarted(String url) {
            mProgressBar.setAlpha(1);
        }

        @Override
        public void onLoadProgress(int progress) {
            mProgressBar.setProgress(progress);
        }

        @Override
        public void onLoadFinished(String url) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mProgressBar, "alpha", 0);
            animator.setDuration(500);
            animator.start();
        }

        @Override
        public void onLoadError() {

        }

        @Override
        public void onReceiveTitle(String title) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(title);
            }
        }

        @Override
        public void requireUISettings(String settings) {

        }

        @Override
        public boolean shouldOverrideUrlLoading(String url) {
            return false;
        }
    }

    private class WebDemoBusinessEventListener implements WebController.SimpleBusinessEventListener {
        @Override
        public void requireCloseWebView() {
            finish();
        }

        @Override
        public void onReceiveProxyData(JSONObject data) {
        }

        @Override
        public void onPickFile(Intent fileChooser) {
        }
    }
}
