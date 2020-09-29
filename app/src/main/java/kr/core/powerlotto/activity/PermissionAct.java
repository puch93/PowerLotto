package kr.core.powerlotto.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.android.billingclient.api.Purchase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import kr.core.powerlotto.R;
import kr.core.powerlotto.billing.BillingManager;
import kr.core.powerlotto.databinding.ActivityPermissionBinding;
import kr.core.powerlotto.insidedata.UserPref;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.util.StringUtil;

public class PermissionAct extends BaseAct implements View.OnClickListener {

    ActivityPermissionBinding binding;
    Activity act;
    String subState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_permission);
        act = this;

        binding.btnClose.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);

        subState = getIntent().getStringExtra("subState");
        Log.d(StringUtil.TAG, "subState: "+subState);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한 허용안됨
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean("request_permissions",true)
                        .commit();

                finish();

            } else {
                // 권한 허용됨
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean("request_permissions",false)
                        .commit();

                startActivity(new Intent(act, SplashAct.class));
                finish();
            }
        }

    }

    private void setUserInfo(String token){

        String cellnum;
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        cellnum = tm.getLine1Number();

        ReqBasic userInfo = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(StringUtil.TAG, "code: "+resultCode);
                Log.d(StringUtil.TAG, "userInfo: "+resultData.getResult());
//                {"result":"N","message":"이미 가입된 회원입니다.","midx":"2"}

                if (resultData.getResult() != null){

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (StringUtil.isNull(jo.getString("midx"))) {
                            Toast.makeText(PermissionAct.this, jo.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                        UserPref.setIdx(PermissionAct.this,jo.getString("midx"));
                        startActivity(new Intent(PermissionAct.this, MainActivity.class));
                        finishAffinity();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{

                }
            }
        };

        Log.d(StringUtil.TAG, "setUserInfo: "+cellnum);

        userInfo.addParams("APPCONNECTCODE","APP");
        userInfo.addParams("dbControl","setUserMember");
        userInfo.addParams("fcm",token);
        if (StringUtil.isNull(cellnum)){
            userInfo.addParams("hp", "");
        }else {
            userInfo.addParams("hp", cellnum.replaceFirst("[+]82", "0"));
        }
        userInfo.addParams("subscription",subState);
        userInfo.addParams("m_device_model",Build.MODEL);
        userInfo.addParams("uniq", Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID));
        userInfo.execute(true,true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_close:
                break;
            case R.id.btn_submit:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 권한 요청
                    requestPermissions(new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.CAMERA
                    }, 0);
                }
                break;
        }
    }
}
