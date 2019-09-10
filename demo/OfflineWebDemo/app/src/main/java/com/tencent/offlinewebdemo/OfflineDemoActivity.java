package com.tencent.offlinewebdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import com.tencent.ai.tvs.offlinewebtemplate.IOfflineWebUniAccessCallback;
import com.tencent.ai.tvs.offlinewebtemplate.JSONUtil;
import com.tencent.ai.tvs.offlinewebtemplate.OfflineWebCallback;
import com.tencent.ai.tvs.offlinewebtemplate.OfflineWebManager;

import org.json.JSONObject;


public class OfflineDemoActivity extends AppCompatActivity {
    private static final String TAG = "OfflineDemoActivity";
    private static Context mContext = null;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private IOfflineWebUniAccessCallback mOfflineCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = this;
        boolean isRequest = verifyStoragePermissions(this);
        if (!isRequest) {
            initOfflineWebManager();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        OfflineWebManager.getInstance().checkNativeWebPkgUpdate();
    }

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
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                isRequest = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isRequest;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
            if (isSuccess) {
                testCallOpenWebTemplateUI();
            }
        }

        @Override
        public void onOpenMediaUi() {
            Log.i(TAG, "OfflineWebManager onOpenMediaUi");
        }

        @Override
        public void uniAccess(String domain, String intent, String jsonBlobInfo, String tag, IOfflineWebUniAccessCallback callback) {
            mOfflineCallback = callback;
        }

        @Override
        public void requestTTS(String text) {

        }

        @Override
        public void reportAction(String domain, String type, String param) {
            Log.i(TAG, "OfflineWebManager reportAction domain=" + domain + " type=" + type + " param=" + param);
        }

        @Override
        public void stopSpeech(String dialogRequestId) {
        }
    };

    public void testCallOpenWebTemplateUI() {
        try {
            String sJason = "{\"controlInfo\":{\"audioConsole\":\"true\",\"backgroundImageValid\":\"true\",\"orientation\":\"portrait\",\"subTitleSpeak\":\"true\",\"textSpeak\":\"false\",\"titleSpeak\":\"false\",\"type\":\"AUDIO\",\"version\":\"1.0.0\"},\"globalInfo\":{\"backgroundImage\":{\"sources\":[{\"url\":\"http://h5app.gtimg.com/html5app/joke/backgroupImage3.png\"}]}},\"listItems\":[{\"audio\":{\"stream\":{\"url\":\"http://softfile.3g.qq.com/myapp/trom_l/dobby/joke/201801/20180111/D_J_2018011120.mp3\"}},\"image\":{\"sources\":[{\"url\":\"http://h5app.gtimg.com/html5app/joke/image3.png\"}]},\"mediaId\":\"9ecdcd5eeee55f68958d35f6e0dd0abd\"}],\"templateInfo\":{\"skill_info\":\"joke|990835372646862848\",\"t_id\":\"20004\"},\"uriInfo\":{\"ui\":{\"url\":\"https://3gimg.qq.com/trom_s/dingdang/sdk/templates/templates.html?_tid=20004\"}}}";
            String sJasonStr = JSONUtil.unescape(sJason);
            JSONObject jsonUi = new JSONObject(sJasonStr);

            String sJasonSemantic = "{ \"bubble_transform_query\": \"\", \"confidence\": -1, \"domain\": \"joke\", \"extra_semantic\": [  ], \"ifttt_this\": \"\", \"intent\": \"tell\", \"invocation_name\": \"\", \"is_semantic_only\": false, \"nlu_match_info\": { \"is_single_entity\": false, \"matched_type\": 3 }, \"query\": \"讲个笑话\", \"query_source_type\": 1, \"query_type\": 2, \"session_complete\": true, \"skill_id\": \"990835372646862848\", \"skill_trigger_type\": 1, \"slots\": [  ], \"slots_v2\": [  ], \"type\": 0, \"voice_query\": { \"asr_results\": [  ], \"compress_type\": 1, \"pre_itn_query\": \"\", \"raw_data\": [  ], \"sample_rate\": 8000 } }";
            if (null != jsonUi) {
                if (OfflineWebManager.getInstance().isCanOpenByWebTemplate(jsonUi.toString(), sJasonSemantic, false, null)) {
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

        finish();
    }
}
