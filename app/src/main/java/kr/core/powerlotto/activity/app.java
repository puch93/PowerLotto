package kr.core.powerlotto.activity;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import androidx.core.content.IntentCompat;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import io.fabric.sdk.android.Fabric;
import kr.core.powerlotto.R;
import kr.core.powerlotto.adapter.MainviewPagerAdapter;
import kr.core.powerlotto.data.MainWinInfo;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.reciever.AlarmReceiver;
import kr.core.powerlotto.util.ConvertPxDp;
import kr.core.powerlotto.util.StringUtil;

public class app extends Application {

    public static String lln_rank;
    public static String lln_num1;
    public static String lln_num2;
    public static String lln_num3;
    public static String lln_num4;
    public static String lln_num5;
    public static String lln_num6;
    public static String lln_bnum;

    public static String lln_lucky_price_one;

    public static String BottomAdState;
    public static Calendar satCalender;
    public static String bannerState;
    public static String bannerLink;
    public static String bannerImg;

    public static SoundPool soundPool;
    public static int soundId;
    public static Typeface jalnan;

    private static final int TYPE_ALARM_01 = 1001;
    private static final int TYPE_ALARM_02 = 1002;
    private static final int TYPE_ALARM_03 = 1003;

    private static final int TYPE_ALARM_CODE_01 = 0;
    private static final int TYPE_ALARM_CODE_02 = 1;
    private static final int TYPE_ALARM_CODE_03 = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        setAlarm();
        jalnan = Typeface.createFromAsset(getAssets(),"Jalnan.ttf");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            soundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(1).build();
        }else{
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        }

        soundPool.load(getApplicationContext(),R.raw.sound_1,1);

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                soundId = i;
            }
        });

        satCalender = getCurSaturday();
        currentWinInfo();
    }

    private void currentWinInfo(){
        ReqBasic winInfo = new ReqBasic(getApplicationContext(), NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(StringUtil.TAG, "code: "+resultCode);
                Log.d(StringUtil.TAG, "currentWinInfo: "+resultData.getResult());

                if (resultData.getResult() != null){

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        JSONArray ja = new JSONArray(jo.getString("data"));

                        JSONObject obj = ja.getJSONObject(0);
                        lln_rank = obj.getString("lln_rank");
                        lln_lucky_price_one = obj.getString("lln_lucky_price_one");
                        lln_num1 = obj.getString("lln_num_1");
                        lln_num2 = obj.getString("lln_num_2");
                        lln_num3 = obj.getString("lln_num_3");
                        lln_num4 = obj.getString("lln_num_4");
                        lln_num5 = obj.getString("lln_num_5");
                        lln_num6 = obj.getString("lln_num_6");
                        lln_bnum = obj.getString("lln_num_bonus");


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{

                }

            }
        };

        winInfo.addParams("APPCONNECTCODE","APP");
        winInfo.addParams("dbControl","MainLottoLuckyNum");
        winInfo.execute(true,false);
    }

    private Calendar getCurSaturday(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
        c.set(Calendar.HOUR_OF_DAY,20);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);

        return c;
//        Log.d(StringUtil.TAG, "getCurSaturday: "+sdf.format(c.getTime()));
    }


    private void setAlarm() {
        Log.e(StringUtil.TAG, "setAlarm");

        /* initialize alarm service */
        Calendar mCalendar = Calendar.getInstance();


        /* set compare data */
        int LAST_DAY_OF_YEAR = mCalendar.getMaximum(Calendar.DAY_OF_YEAR);
        int NOW_DAY_OF_YEAR = mCalendar.get(Calendar.DAY_OF_YEAR);


        /* set calendar */
        Calendar calendar01 = setCalendar(TYPE_ALARM_01, NOW_DAY_OF_YEAR, LAST_DAY_OF_YEAR);
        Calendar calendar02 = setCalendar(TYPE_ALARM_02, NOW_DAY_OF_YEAR, LAST_DAY_OF_YEAR);
        Calendar calendar03 = setCalendar(TYPE_ALARM_03, NOW_DAY_OF_YEAR, LAST_DAY_OF_YEAR);


        /* set alarm manager */
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        /* set pending intent */
        PendingIntent pendingIntent01 = PendingIntent.getBroadcast(this, TYPE_ALARM_CODE_01, setIntent(TYPE_ALARM_01), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent02 = PendingIntent.getBroadcast(this, TYPE_ALARM_CODE_02, setIntent(TYPE_ALARM_02), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent03 = PendingIntent.getBroadcast(this, TYPE_ALARM_CODE_03, setIntent(TYPE_ALARM_03), PendingIntent.FLAG_UPDATE_CURRENT);


        /* register alarm (버전별로 따로) */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e(StringUtil.TAG, "first");
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar01.getTimeInMillis(), pendingIntent01);
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar02.getTimeInMillis(), pendingIntent02);
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar03.getTimeInMillis(), pendingIntent03);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.e("TEST_HOME", "second");
            manager.setExact(AlarmManager.RTC_WAKEUP, calendar01.getTimeInMillis(), pendingIntent01);
            manager.setExact(AlarmManager.RTC_WAKEUP, calendar02.getTimeInMillis(), pendingIntent02);
            manager.setExact(AlarmManager.RTC_WAKEUP, calendar03.getTimeInMillis(), pendingIntent03);
        } else {
            Log.e("TEST_HOME", "third");
            manager.set(AlarmManager.RTC_WAKEUP, calendar01.getTimeInMillis(), pendingIntent01);
            manager.set(AlarmManager.RTC_WAKEUP, calendar02.getTimeInMillis(), pendingIntent02);
            manager.set(AlarmManager.RTC_WAKEUP, calendar03.getTimeInMillis(), pendingIntent03);
        }
    }

    private Calendar setCalendar(int type, int NOW_DAY_OF_YEAR, int LAST_DAY_OF_YEAR) {
        Calendar calendar = Calendar.getInstance();

        switch (type) {
            case TYPE_ALARM_01:
                calendar.set(Calendar.HOUR_OF_DAY, 11);
                calendar.set(Calendar.MINUTE, 10);
                calendar.set(Calendar.SECOND, 0);
                break;

            case TYPE_ALARM_02:
                calendar.set(Calendar.HOUR_OF_DAY, 11);
                calendar.set(Calendar.MINUTE, 20);
                calendar.set(Calendar.SECOND, 0);
                break;

            case TYPE_ALARM_03:
                calendar.set(Calendar.HOUR_OF_DAY, 11);
                calendar.set(Calendar.MINUTE, 30);
                calendar.set(Calendar.SECOND, 0);
                break;
        }

        // manager.set 할때 현재시간보다 이전 시간대면, 리시버가 바로 실행됨, 다음날로 지정해줘야 함 => ex) 세팅시간 8:00, 현재시간 9:20 이면, 다음날 8:00로 지정
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            Log.e(StringUtil.TAG, "현재시간보다 작음");

            // 올해의 마지막 날일 경우
            if (NOW_DAY_OF_YEAR == LAST_DAY_OF_YEAR) {
                NOW_DAY_OF_YEAR = 1;
            } else {
                NOW_DAY_OF_YEAR++;
            }
            calendar.set(Calendar.DAY_OF_YEAR, NOW_DAY_OF_YEAR);
        }

        return calendar;
    }

    private Intent setIntent(int type) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("type", type);
        return intent;
    }
}
