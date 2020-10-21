package kr.core.powerlotto.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.billingclient.api.Purchase;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import kr.core.powerlotto.R;
import kr.core.powerlotto.billing.BillingManager;
import kr.core.powerlotto.insidedata.UserPref;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.util.CoupangReceiver;
import kr.core.powerlotto.util.StringUtil;

public class SplashActTmp extends BaseAct {

    BillingManager billingManager;
    private static final int OVERLAY = 1003;
    Activity act;
    String subState;

    private Timer timer = new Timer();
    boolean isReady = true;
    String fcm_token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        act = this;

        setContentView(R.layout.activity_splash);

        checkAd();

        Log.d(StringUtil.TAG, "getAppVersion: " + getAppVersion());

        getFcmToken();

        checkTimer();
    }

    private void getFcmToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.i("TEST", "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.i("TEST", "myFcmToken: " + token);
                        UserPref.saveFcmToken(act, token);
                        fcm_token = token.replace("%3", ":");
                    }
                });
    }

    public void checkTimer() {
        TimerTask adTask = new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (fcm_token != null && isReady) {
                            isReady = false;
                            checkVer();
                            timer.cancel();
                        }
                    }
                }, 0);
            }
        };
        timer.schedule(adTask, 0, 1000);
    }


    private void billingCheck() {
        billingManager = new BillingManager(this, new BillingManager.AfterBilling() {
            @Override
            public void sendResult(Purchase purchase) {
                // 서버로 값 전송(결제 완료)
            }

            @Override
            public void getSubsriptionState(String subscription, Purchase purchase) {
                // subscription = Y : 구독중, N : 구독X
                Log.d(StringUtil.TAG, "getSubsriptionState: " + subscription);
                subState = subscription;
                moveMain();
            }
        });
    }

    private boolean isReqPermission() {
        // 필요권한 ( 전화 걸기 및 관리, 메세지 전송 및 보기, 주소록 액세스, 사진 및 미디어 파일 액세스, 위치정보)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 배너 출력 api
     * di_terms : 약관
     * di_personal_information : 개인정보
     * di_ad_class : 광고형태(B:관리자서 올린 배너, A:애드몹, N:광고없음)
     */
    private void checkAd() {
//        {"di_terms":"","di_personal":""}
        ReqBasic checkAd = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(StringUtil.TAG, "code: " + resultCode);
                Log.d(StringUtil.TAG, "checkAd: " + resultData.getResult());
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (StringUtil.isNull(jo.getString("banner"))) {
                            app.bannerState = StringUtil.ADMOB;
                        } else {

                            JSONObject banner = jo.getJSONObject("banner");

                            banner.getString("b_idx");
                            banner.getString("b_name");
                            banner.getString("b_img");
                            banner.getString("b_url");
                            banner.getString("b_regdate");
                            banner.getString("b_editdate");
                            banner.getString("b_startdate");    // 시작 일자
                            banner.getString("b_enddate");  // 끝나는 일자
                            banner.getString("b_status");

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = sdf.parse(banner.getString("b_startdate"));
                            Date date2 = sdf.parse(banner.getString("b_enddate"));

                            long ctime = System.currentTimeMillis();
                            if (date.getTime() <= ctime && date2.getTime() >= ctime) {
                                app.bannerState = StringUtil.BANNER;
                                app.bannerLink = banner.getString("b_url");
                                app.bannerImg = NetUrls.IMGDOMAIN + banner.getString("b_img").replaceAll("\\\\", "");
                            } else {
                                app.bannerState = StringUtil.ADMOB;
                            }

                        }

//                        String di_ad_class = jo.getString("banner");

//                        {"di_terms":"","di_personal":"","banner":{"b_idx":"1","b_name":"\ubc30\ub108\ud14c\uc2a4\ud2b8","b_img":"\/UPLOAD\/BANNER\/30726684_QvOEcSVK_Hydrangeas.jpg","b_url":"http:\/\/www.naver.com","b_regdate":"2019-11-22 16:51:58","b_editdate":"0000-00-00 00:00:00","b_startdate":"2019-11-22 17:21:03","b_enddate":"2019-11-28 17:21:08","b_status":"view"}}
//                        app.bannerState = jo.getString("di_ad_class");

//                        switch (di_ad_class){
//                            case "B":
//                                // 관리자 배너 광고
//
//                                break;
//                            case "A":
//                                // 애드몹
//                                break;
//                            case "N":
//                                // 광고없음
//                                break;
//                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SplashActTmp.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(SplashActTmp.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }
            }
        };

//        checkAd.addParams("siteUrl","");
        checkAd.addParams("APPCONNECTCODE", "APP");
//        checkAd.addParams("_APP_MEM_IDX","");
        checkAd.addParams("dbControl", "getTerm");
        checkAd.execute(true, true);
    }

    private void lastProcess() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActTmp.this, MainActivity.class));
                finish();
            }
        }, 2000);
    }

    private void setUserInfo() {
        String cellnum;
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        cellnum = tm.getLine1Number();

        Crashlytics.log("cellnum : " + cellnum);
        Log.d(StringUtil.TAG, "setUserInfo: " + cellnum);

        ReqBasic userInfo = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(StringUtil.TAG, "code: " + resultCode);
                Log.d(StringUtil.TAG, "userInfo: " + resultData.getResult());
//                {"result":"N","message":"이미 가입된 회원입니다.","midx":"2"}
                Crashlytics.log("userInfo : " + resultData.getResult());

                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (StringUtil.isNull(jo.getString("midx"))) {
                            Toast.makeText(SplashActTmp.this, jo.getString("message"), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            UserPref.setIdx(SplashActTmp.this, jo.getString("midx"));

                            /* check coupa alarm */
                            if (StringUtil.isNull(UserPref.getCoupaAlarmState(act))) {
                                alarmSetting();
                            }

                            preSetting();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SplashActTmp.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SplashActTmp.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }
            }
        };

        userInfo.addParams("APPCONNECTCODE", "APP");
        userInfo.addParams("dbControl", "setUserMember");
        userInfo.addParams("fcm", fcm_token);
        if (StringUtil.isNull(cellnum)) {
            userInfo.addParams("hp", "");
        } else {
            userInfo.addParams("hp", cellnum.replaceFirst("[+]82", "0"));
        }
        userInfo.addParams("subscription", subState);

//        userInfo.addParams("hp","01000000000");
        userInfo.addParams("m_device_model", Build.MODEL);
        userInfo.addParams("uniq", StringUtil.getDeviceId(act));
        userInfo.execute(true, true);
    }

    private String getAppVersion() {
        String version = "";
        try {
            PackageInfo i = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = i.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    private void checkVer() {
        ReqBasic checkVer = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(StringUtil.TAG, "checkVer: " + resultData.getResult());
                if (resultData.getResult() != null) {

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            android.app.AlertDialog.Builder alertDialogBuilder =
                                    new android.app.AlertDialog.Builder(new ContextThemeWrapper(act, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert));
                            alertDialogBuilder.setTitle("업데이트");
                            alertDialogBuilder.setMessage("새로운 버전이 있습니다.")
                                    .setPositiveButton("업데이트 바로가기", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            if (StringUtil.isGoogle) {
                                                intent.setData(Uri.parse(StringUtil.URL_PLAY_STORE));
                                            } else {
                                                intent.setData(Uri.parse(StringUtil.URL_ONE_STORE_APP));
                                            }
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                            android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.show();


//                            AlertDialog.Builder dialog = new AlertDialog.Builder(SplashAct.this);
//                            dialog.setTitle("업데이트 알림");
//                            dialog.setMessage("플레이 스토어에 업데이트 버전이 있습니다. 업데이트 하시겠습니까?");
//                            dialog.setPositiveButton("업데이트", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(NetUrls.MARKETADDR)));
//                                    dialogInterface.dismiss();
//                                }
//                            });
//
//                            dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    dialogInterface.dismiss();
//                                    billingCheck();
//                                }
//                            });
//                            dialog.show();
                        } else {
                            billingCheck();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        billingCheck();
                    }

                } else {
                    billingCheck();
                }

            }
        };

        checkVer.addParams("APPCONNECTCODE", "APP");
        checkVer.addParams("dbControl", "versionchk");
        checkVer.addParams("thisVer", getAppVersion());
        checkVer.execute(true, true);
    }

    private void moveMain() {
        if (isReqPermission()) {
            Intent intent = new Intent(this, PermissionAct.class);
            startActivity(intent);
            finish();
        } else {
            requestPermissionOverlay();
        }
    }

    private void requestPermissionOverlay() {
        // Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.i("TEST_HOME", "requestPermissionOverlay: ");
                // Show alert dialog to the user saying a separate permission is needed
                // Launch the settings activity if the user prefers
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY);
            } else {
                setUserInfo();
            }
        } else {
            setUserInfo();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(StringUtil.TAG, "resultCode: " + resultCode);

        if (resultCode != RESULT_OK && resultCode != RESULT_CANCELED)
            return;

        switch (requestCode) {
            case OVERLAY:
                requestPermissionOverlay();
                break;
        }
    }


    private void alarmSetting() {
        Log.i("TEST_HOME", "alarmSetting: ");
        /* initialize alarm service */
        Calendar mCalendar = Calendar.getInstance();

        if (!StringUtil.isNull(UserPref.getCoupaAlarmState(act))) {
            //푸시 알림 셋팅값이 있으면, 설정된 푸시시간의 12시 30분 이후에 알람등록.
            //푸시알림과 겹치지 않게 하기 위함.
            mCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(UserPref.getCoupaAlarmState(act)) + 12);
            mCalendar.set(Calendar.MINUTE, 30);
            mCalendar.set(Calendar.SECOND, 0);
        } else {
            //푸시 알림 셋팅값이 없으면, 현재시간으로 부터 1시간 후  값으로 설정
            mCalendar.add(Calendar.HOUR_OF_DAY, 1);
        }

        UserPref.saveCoupaAlarmState(act, mCalendar.getTimeInMillis() + "");

        /* set alarm manager */
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        /* set pending intent */
        PendingIntent pendingIntent01 = PendingIntent.getBroadcast(act, 10, new Intent(act, CoupangReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);

        /* register alarm (버전별로 따로) */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pendingIntent01);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pendingIntent01);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pendingIntent01);
        }
    }

    private void preSetting() {
        if (!StringUtil.isNull(UserPref.getFCheck(act))) {
            Log.i(StringUtil.TAG, "UserPref.getFCheck(act): " + UserPref.getFCheck(act));
            lastProcess();
        } else {
            String h = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date(System.currentTimeMillis()));
            if (h == "23") {
                h = "00";
            } else {
                h = (Integer.valueOf(h) + 1) + "";
            }
            doCsetting(h);
        }
    }

    private void doCsetting(final String checkTime) {
        ReqBasic checkVer = new ReqBasic(this, "https://coupang.adamstore.co.kr/lib/control.siso") {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                final String res = resultData.getResult();
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!StringUtil.isNull(res)) {
                                JSONObject jo = new JSONObject(res);
                                if (StringUtil.getStr(jo, "result").equalsIgnoreCase("Y")) {
                                    UserPref.saveFCheck(act, checkTime);
                                }
                            }
                            lastProcess();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        checkVer.addParams("dbControl", "setCoupangPartnersPush");
        checkVer.addParams("grouptime", checkTime);
        checkVer.addParams("site", "1");
        checkVer.addParams("m_idx", StringUtil.getDeviceId(act));
        checkVer.addParams("fcm", fcm_token);
        if (StringUtil.isGoogle) {
            checkVer.addParams("m_store", "Google");
        } else {
            checkVer.addParams("m_store", "One");
        }

        checkVer.addParams("idx", UserPref.getIdx(act));
        checkVer.addParams("m_uniq", StringUtil.getDeviceId(act));
        checkVer.addParams("m_hp", StringUtil.getPhoneNumber(act));
        checkVer.addParams("m_model", Build.MODEL);
        checkVer.addParams("m_agent", StringUtil.getTelecom(act));
        checkVer.execute(true, true);
    }
}
