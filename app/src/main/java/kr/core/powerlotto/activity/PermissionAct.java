package kr.core.powerlotto.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_permission);
        act = this;

        binding.btnClose.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);

        subState = getIntent().getStringExtra("subState");
        Log.d(StringUtil.TAG, "subState: " + subState);

        binding.btnPrivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://lotto.adamstore.co.kr/term.php"));
                startActivity(intent);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한 허용안됨
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean("request_permissions", true)
                        .commit();

                finish();

            } else {
                // 권한 허용됨
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean("request_permissions", false)
                        .commit();

                startActivity(new Intent(act, SplashAct.class));
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                break;
            case R.id.btn_submit:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
