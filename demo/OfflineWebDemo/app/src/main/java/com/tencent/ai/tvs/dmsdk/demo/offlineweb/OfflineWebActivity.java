package com.tencent.ai.tvs.dmsdk.demo.offlineweb;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.ai.dobbydemo.BuildConfig;
import com.tencent.ai.dobbydemo.R;
import com.tencent.ai.tvs.offlinewebtemplate.OfflineWebConstants;
import com.tencent.ai.tvs.offlinewebtemplate.OfflineWebManager;
import com.tencent.ai.tvs.web.tms.ITMSWebViewJsListener;
import com.tencent.ai.tvs.web.tms.ITMSWebViewListener;
import com.tencent.ai.tvs.web.tms.TMSWebView;
import com.tencent.ai.tvs.web.util.AndroidBug5497Workaround;

import org.json.JSONException;
import org.json.JSONObject;

public class OfflineWebActivity extends AppCompatActivity {
    private String LOG_TAG = "DMSDK_NativeWebActivity";
    private String LOG_TAG_TIME = "NativeWebActivity_Time";

    private boolean isDebugTime = true;

    private TMSWebView mWebView;
    private RelativeLayout mTvsHoloLightLayout;
    private LinearLayout mTvsHoloLightBackbtnLayout;
    private ImageView mTvsHoloLightBackbtn;

    private String mH5Id = "";
    private long startTime, deltaTime;
    private String sCurJason;
    private String sCurTid;
    private String sDialogRequestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (isDebugTime) {
            startTime = System.nanoTime();
        }

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        if (isDebugTime) {
            deltaTime = System.nanoTime() - startTime;
            Log.v(LOG_TAG_TIME, deltaTime / 1e6f + "ms----" + "setWebContentsDebuggingEnabled");
            startTime = System.nanoTime();
        }

        setContentView(R.layout.nativeweb_holo_light);
        mTvsHoloLightLayout = (RelativeLayout) findViewById(R.id.tvs_holo_light_layout);
        mTvsHoloLightBackbtnLayout =
                (LinearLayout) findViewById(R.id.tvs_holo_light_backbtn_layout);
        mTvsHoloLightBackbtn = (ImageView) findViewById(R.id.tvs_holo_light_backbtn);
        mTvsHoloLightBackbtn.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        mTvsHoloLightBackbtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (isDebugTime) {
            deltaTime = System.nanoTime() - startTime;
            Log.v(LOG_TAG_TIME, deltaTime / 1e6f + "ms----" + "setContentView");
            startTime = System.nanoTime();
        }

        mWebView = OfflineWebManager.getInstance().getLoadWebView();
        if (null != mWebView) {
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            mWebView.setLayoutParams(params);
            mWebView.setListener(mViewListener);
            mWebView.registerService(OfflineWebConstants.TVS_SERVICENAME, mJSListener);
        }

        if (null != mTvsHoloLightLayout && null != mWebView) {
            mTvsHoloLightLayout.addView(mWebView, 0);
        }

        if (isDebugTime) {
            deltaTime = System.nanoTime() - startTime;
            Log.v(LOG_TAG_TIME, deltaTime / 1e6f + "ms----" + "create webview finish");
            startTime = System.nanoTime();
        }
        sCurJason = OfflineWebManager.getInstance().getLoadJasonData();
        sCurTid = OfflineWebManager.getInstance().getCurTid();
        sDialogRequestId = OfflineWebManager.getInstance().mDialogRequestId;
        AndroidBug5497Workaround.assistActivity(this);
        OfflineWebManager.getInstance().setWebActivity(sDialogRequestId, this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isDebugTime) {
            deltaTime = System.nanoTime() - startTime;
            Log.v(LOG_TAG_TIME, deltaTime / 1e6f + "ms----" + "Act_onStart");
            startTime = System.nanoTime();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isDebugTime) {
            deltaTime = System.nanoTime() - startTime;
            Log.v(LOG_TAG_TIME, deltaTime / 1e6f + "ms----" + "Act_onResume");
            startTime = System.nanoTime();
        }

        if (null != mTvsHoloLightBackbtnLayout) {
            mTvsHoloLightBackbtnLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(LOG_TAG, "OfflineWebActivity-----onNewIntent");
        //这里可以通过接口获取到本轮会话的jason数据，模版id,会话id等。保存到界面实例中，退出时可能需要调用
        sCurJason = OfflineWebManager.getInstance().getLoadJasonData();
        sCurTid = OfflineWebManager.getInstance().getCurTid();
        sDialogRequestId = OfflineWebManager.getInstance().mDialogRequestId;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        Log.v(LOG_TAG, "OfflineWebActivity-----onDestroy");
        if (mWebView != null) {
            //OfflineWebManager.getInstance().destroyWebView(mWebView);
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
        }
        OfflineWebManager.getInstance().setWebActivity(sDialogRequestId, null);

        //TODO 下面的逻辑如果接入了tvs sdk后，可能需要调用，停止tts播报，退出多轮
/*        if (!TextUtils.isEmpty(sDialogRequestId)) {
            TVSApi.getInstance().stopSpeech(sDialogRequestId);
            TVSApi.getInstance().exitMultiSpeech(sDialogRequestId);
        }*/
        super.onDestroy();
    }


    private void hideInputMethod(boolean isHoloTheme) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && isHoloTheme) {
            //imm.hideSoftInputFromWindow(mTvsHoloLayout.getWindowToken(), 0);
        }
    }

    ITMSWebViewJsListener mJSListener = new ITMSWebViewJsListener() {

        @Override
        public boolean onJsCallNativeFunc(String funcName, JSONObject funcParam) {
            Log.v(LOG_TAG,
                    "onJsCallNativeFunc funcName = " + funcName + ", funcParam = " + funcParam);
            if (OfflineWebConstants.JS_CMD_PROXYDATA.equals(funcName)) {
                return true;
            } else if (OfflineWebConstants.JS_CMD_FINISHACT.equals(funcName)) {
                finish();
                return true;
            } else if (OfflineWebConstants.JS_CMD_SETTINGS.equals(funcName)) {
                jsWebpageSettings(funcParam);
                return true;
            }

            //web化模版需要处理的一些webview的回调
            if (OfflineWebManager.getInstance().handleJsCallNative(mWebView, funcName, funcParam)) {
                return true;
            }

            return false;
        }

    };

    ITMSWebViewListener mViewListener = new ITMSWebViewListener() {
        @Override
        public void onReceivedTitle(WebView webView, String s) {
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String s) {
            return false;
        }

        @Override
        public void onReceivedError(WebView webView, int i, String s, String s1) {

        }

        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler,
                                       SslError sslError) {

        }

        @Override
        public void onScrollToBottom() {

        }

        @Override
        public void onProgressChanged(WebView webView, int i) {
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            if (isDebugTime) {
                deltaTime = System.nanoTime() - startTime;
                Log.v(LOG_TAG_TIME, deltaTime / 1e6f + "ms----" + "ITMSWebViewListener " +
                        "onPageFinished");
                startTime = System.nanoTime();
            }
        }

        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            if (isDebugTime) {
                deltaTime = System.nanoTime() - startTime;
                Log.v(LOG_TAG_TIME, deltaTime / 1e6f + "ms----" + "ITMSWebViewListener " +
                        "onPageStarted");
                startTime = System.nanoTime();
            }
        }

        @Override
        public boolean onJsCallNativeFunc(String s, JSONObject jsonObject) {
            return false;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView webView, String s) {

            return null;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          WebResourceRequest request) {
            return OfflineWebManager.getInstance().handleInterceptRequest(view, request);
        }

        @Override
        public boolean onShowFileChooser(ValueCallback<Uri[]> valueCallback, String acceptType) {
            return true;
        }
    };


    private void jsWebpageSettings(JSONObject jsonObj) {
        String data = jsonObj.optString("data");
        try {
            JSONObject dataJSON = new JSONObject(data);
            String backbtnStyle = dataJSON.optString("backbtnstyle");
            if ("#ff000000".equals(backbtnStyle)) {
                mTvsHoloLightBackbtn.setImageDrawable(getResources().getDrawable(R.drawable.dingdang_btn_back_holo_light));
                mTvsHoloLightBackbtnLayout.setVisibility(View.VISIBLE);
            } else if ("#ffffffff".equals(backbtnStyle)) {
                mTvsHoloLightBackbtn.setImageDrawable(getResources().getDrawable(R.drawable.dingdang_btn_back_holo));
                mTvsHoloLightBackbtnLayout.setVisibility(View.VISIBLE);
            } else if ("#00000000".equals(backbtnStyle)) {
                mTvsHoloLightBackbtnLayout.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}