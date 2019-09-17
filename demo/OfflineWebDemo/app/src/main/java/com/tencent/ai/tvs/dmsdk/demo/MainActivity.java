package com.tencent.ai.tvs.dmsdk.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.tencent.ai.dobbydemo.R;
import com.tencent.ai.tvs.dmsdk.demo.offlineweb.OfflineAlbumActivity;
import com.tencent.ai.tvs.dmsdk.demo.offlineweb.OfflineWebActivity;
import com.tencent.ai.tvs.env.ELoginEnv;
import com.tencent.ai.tvs.env.EnvManager;
import com.tencent.ai.tvs.offlinewebtemplate.IOfflineWebUniAccessCallback;
import com.tencent.ai.tvs.offlinewebtemplate.JSONUtil;
import com.tencent.ai.tvs.offlinewebtemplate.OfflineWebCallback;
import com.tencent.ai.tvs.offlinewebtemplate.OfflineWebManager;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static Context mContext = null;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private IOfflineWebUniAccessCallback mOfflineCallback;
    private Boolean isOfflineWebInitSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RadioButton testingRadioButton = findViewById(R.id.testingRadioButton);
        testingRadioButton.setOnClickListener(v -> EnvManager.getInstance().setEnv(ELoginEnv.TEST));
        RadioButton experienceRadioButton = findViewById(R.id.experienceRadioButton);
        experienceRadioButton.setOnClickListener(v -> EnvManager.getInstance().setEnv(ELoginEnv.EX));
        RadioButton productionRadioButton = findViewById(R.id.productionRadioButton);
        productionRadioButton.setOnClickListener(v -> EnvManager.getInstance().setEnv(ELoginEnv.FORMAL));
        ELoginEnv env = EnvManager.getInstance().getEnv();
        switch (env) {
            case FORMAL:
                productionRadioButton.setChecked(true);
                break;
            case TEST:
                testingRadioButton.setChecked(true);
                break;
            case EX:
                experienceRadioButton.setChecked(true);
                break;
        }

        mContext = this;
        boolean isRequest = verifyStoragePermissions(this);
        if (!isRequest) {
            initOfflineWebManager();
        }

        findViewById(R.id.openWebViewButton).setOnClickListener(view ->
                startActivity(new Intent(this, WebDemoActivity.class))
        );

        findViewById(R.id.openWebTemplateUiButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isOfflineWebInitSuccess) {
                            testCallOpenWebTemplateUI();
                        } else {
                            Log.e(TAG, "Offline web init failed!");
                            Toast.makeText(mContext, "离线Web模版初始化失败", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        OfflineWebManager.getInstance().checkNativeWebPkgUpdate();
    }

    /**
     * 需要注意，离线web的初始化，在应用启动时初始化一次即可，
     * 尽量不要重复初始化，除非出现初始化失败可以进行重试
     **/
    private void initOfflineWebManager() {
        String sPath = "sdcard/nativeweb/";
        OfflineWebManager.getInstance().init(this, sPath, mWebCallback);
    }

    public static boolean verifyStoragePermissions(Activity activity) {
        boolean isRequest = false;
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE);
                isRequest = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isRequest;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意授权，执行读取文件的代码
                initOfflineWebManager();
            } else {
                //若用户不同意授权，直接暴力退出应用。
                // 当然，这里也可以有比较温柔的操作。
                finish();
            }
        }
    }

    private OfflineWebCallback mWebCallback = new OfflineWebCallback() {

        @Override
        public void initStatusCallback(boolean isSuccess) {
            Log.i(TAG, "OfflineWebManager initStatusCallback isSuccess=" + isSuccess);
            isOfflineWebInitSuccess = isSuccess;
        }

        @Override
        public void onOpenMediaUi() {
            Log.i(TAG, "OfflineWebManager onOpenMediaUi");
        }

        @Override
        public void uniAccess(String domain, String intent, String jsonBlobInfo, String tag,
                              IOfflineWebUniAccessCallback callback) {
            mOfflineCallback = callback;
            //TODO 下面的逻辑如果接入了tvs sdk后，可能需要调用(接sdk后实现该接口可以支持离线包动态更新到最新版本)
            /*TVSApi.getInstance().uniAccess(domain, intent, jsonBlobInfo, "请填入项目的bot key",
            "请填入项目的bot token", tag, mUniAccessCallback);*/
        }

        @Override
        public void requestTTS(String text) {
            //TODO 下面的逻辑如果接入了tvs sdk后，可能需要调用
/*
            TVSApi.getInstance().requestTTS(text);
*/
        }

        @Override
        public void reportAction(String domain, String type, String param) {
            Log.i(TAG, "OfflineWebManager reportAction domain=" + domain + " type=" + type + " " +
                    "param=" + param);
            //TODO 下面的逻辑如果接入了tvs sdk后，可能需要调用
/*            TVSApi.getInstance().sendActionTriggeredEvent(domain, type, param, new
IResponseListener() {

                @Override
                public void onSucceed(int statusCode) {
                    TVSLog.i(TAG, "OfflineWebManager reportAction onSucceed=" + statusCode);
                }

                @Override
                public void onFailed(int errorCode, String errorMessage) {
                    TVSLog.i(TAG, "OfflineWebManager reportAction errorCode=" + errorCode + "
                    errorMessage=" + errorMessage);
                }

                @Override
                public void onAllResponseReceived(List<TVSResponse> responseBodies) {

                }
            });*/
        }

        @Override
        public void stopSpeech(String dialogRequestId) {
            //TODO 下面的逻辑如果接入了tvs sdk后，可能需要调用
/*
            TVSApi.getInstance().stopSpeech(dialogRequestId);
*/
        }

        @Override
        public void startRecognize() {
            //TODO 下面的逻辑如果接入了tvs sdk后，可能需要调用
/*
            TVSApi.getInstance().startRecognize(IVoiceInputListener.RECO_TYPE_MANUAL_MORE_SPEECH);
*/
        }

        @Override
        public void stopRecognize() {
            //TODO 下面的逻辑如果接入了tvs sdk后，可能需要调用
/*
            TVSApi.getInstance().stopRecognize(false);
*/
        }
    };

    //TODO 下面的逻辑如果接入了tvs sdk后，可能需要调用
/*    private IUniAccessCallback mUniAccessCallback = new IUniAccessCallback() {
        @Override
        public void onGetResult(int reqId, int resultCode, String errorMsg, String tag,
        MsgResponseHeader serverResponseHeader, String jsonBlobInfo) {
            int nCode = -1;
            if (null != serverResponseHeader) {
                nCode = serverResponseHeader.retCode;
            }
            mOfflineCallback.onGetResult(reqId, resultCode, errorMsg, tag, nCode, jsonBlobInfo);
        }
    };*/

    /**
     * 打开离线Web模版ui的测试接口
     */
    public void testCallOpenWebTemplateUI() {
        try {
            String sJason = "{\"controlInfo\":{\"textSpeak\":\"false\",\"titleSpeak\":\"false\"," +
                    "\"type\":\"TEXT\",\"version\":\"1.0.0\"}," +
                    "\"listItems\":[{\"textContent\":\"明朝的开创者是朱元璋。\"}]," +
                    "\"templateInfo\":{\"skill_info\":\"history_kbqa|1026355973436379136\"," +
                    "\"t_id\":\"10001\"},\"uriInfo\":{\"ui\":{\"url\":\"https://3gimg.qq" +
                    ".com/trom_s/dingdang/sdk/templates/templates.html?_tid=10001\"}}}";
            String sJasonStr = JSONUtil.unescape(sJason);
            JSONObject jsonUi = new JSONObject(sJasonStr);

            //这是测试使用的dialogRequestId,如果接入tvs sdk后，可以从ui数据的回调接口取到该参数
            String sTestDialogId = "this is test id";

            //这是模版界面的标题栏显示的内容，默认填入语音交互的识别文本结果
            String sQueryStr = "{ \"query\": \"明朝的开国皇帝是谁\"}";
            if (null != jsonUi) {
                if (OfflineWebManager.getInstance().isCanOpenByWebTemplate(jsonUi.toString(),
                        sQueryStr, false, sTestDialogId)) {
                    Log.i(TAG, "testCallOpenWebTemplateUI isCanOpenByWebTemplate is success");

                    //需要打开一个装载webview的activity
                    openNativeWebActivity();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openNativeWebActivity() {
        Intent intent = null;
        if (OfflineWebManager.getInstance().isSingleTaskShowID()) { //要独立的窗口显示的就新开启独立窗口
            intent = new Intent(mContext, OfflineAlbumActivity.class);
        } else {
            intent = new Intent(mContext, OfflineWebActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
