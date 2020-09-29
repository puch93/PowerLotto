package kr.core.powerlotto.popup;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import kr.core.powerlotto.R;
import kr.core.powerlotto.activity.RandnumAct;
import kr.core.powerlotto.activity.SplashAct;
import kr.core.powerlotto.activity.app;
import kr.core.powerlotto.databinding.AlarmLayoutBinding;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.util.LottoUtil;
import kr.core.powerlotto.util.StringUtil;

public class PopupAlarmMsg extends Activity implements View.OnClickListener {

    AlarmLayoutBinding binding;
    Activity act;

    int type = 0;
    final long millisInFuture = 86400000;

    CountDownTimer cTimer;

    ArrayList<Integer> randIdx;
    ArrayList<Integer> randNums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.alarm_layout);

        act = this;

        setTextTypeface();

        String title = getIntent().getStringExtra("title");
        String contents = getIntent().getStringExtra("contents");
        type = getIntent().getIntExtra("type", 0);

        Log.d(StringUtil.TAG, "onCreate: PopupAlarmMsg "+ type);

        binding.shimmerAnim.startShimmerAnimation();
//        YoYo.with(Techniques.FadeIn)
//                .duration(1000)
//                .repeat(YoYo.INFINITE)
//                .playOn(binding.btnAlarmbottom);

        binding.btnClose.setOnClickListener(this);
        binding.btnAlarmbottom.setOnClickListener(this);

        binding.tvTitle.setText(title);

        randIdx = new ArrayList<>();
        for (int i = 0; i < 4; i++){
            int num = LottoUtil.getRand(6);
            if (randIdx.contains(num)) {
                int tmpnum = num;

                while (randIdx.contains(tmpnum)) {
                    tmpnum = LottoUtil.getRand(6);
                }
                randIdx.add(tmpnum);
            } else {
                randIdx.add(num);
            }
        }
        Collections.sort(randIdx, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        switch (type){
            case 0:
                binding.ivParticle.setVisibility(View.VISIBLE);
                binding.btnAlarmbottom.setText("번호 확인하기");
                binding.alarmType1.getRoot().setVisibility(View.VISIBLE);

//                ArrayList<Integer> randNums = new ArrayList<>();
//
//                for (int i = 0; i < 4; i++){
//                    int num = LottoUtil.getRandnum();
//                    if (randNums.contains(num)) {
//                        int tmpnum = num;
//
//                        while (randNums.contains(tmpnum)) {
//                            tmpnum = LottoUtil.getRandnum();
//                        }
//                        randNums.add(tmpnum);
//                    } else {
//                        randNums.add(num);
//                    }
//                }
//
//                Collections.sort(randNums, new Comparator<Integer>() {
//                    @Override
//                    public int compare(Integer o1, Integer o2) {
//                        return o1.compareTo(o2);
//                    }
//                });

                makeRandNums();
//                Log.d(StringUtil.TAG, "makeRandNums: "+randNums);

                Log.d(StringUtil.TAG, "setting: ");
                for (int i = 0; i < randIdx.size(); i++){

                    switch (randIdx.get(i)){
                        case 0:
                            binding.alarmType1.num1.setText(String.valueOf(randNums.get(i)));
                            binding.alarmType1.num1.setBackgroundResource(LottoUtil.getPopBallResource(randNums.get(i)));
                            break;
                        case 1:
                            binding.alarmType1.num2.setText(String.valueOf(randNums.get(i)));
                            binding.alarmType1.num2.setBackgroundResource(LottoUtil.getPopBallResource(randNums.get(i)));
                            break;
                        case 2:
                            binding.alarmType1.num3.setText(String.valueOf(randNums.get(i)));
                            binding.alarmType1.num3.setBackgroundResource(LottoUtil.getPopBallResource(randNums.get(i)));
                            break;
                        case 3:
                            binding.alarmType1.num4.setText(String.valueOf(randNums.get(i)));
                            binding.alarmType1.num4.setBackgroundResource(LottoUtil.getPopBallResource(randNums.get(i)));
                            break;
                        case 4:
                            binding.alarmType1.num5.setText(String.valueOf(randNums.get(i)));
                            binding.alarmType1.num5.setBackgroundResource(LottoUtil.getPopBallResource(randNums.get(i)));
                            break;
                        case 5:
                            binding.alarmType1.num6.setText(String.valueOf(randNums.get(i)));
                            binding.alarmType1.num6.setBackgroundResource(LottoUtil.getPopBallResource(randNums.get(i)));
                            break;
                        default:
                            binding.alarmType1.num1.setText(String.valueOf(randNums.get(i)));
                            binding.alarmType1.num1.setBackgroundResource(LottoUtil.getPopBallResource(randNums.get(i)));
                            break;
                    }
                }

                YoYo.with(Techniques.FadeIn)
                        .duration(1000)
                        .repeat(YoYo.INFINITE)
                        .playOn(binding.alarmType1.num1);

                YoYo.with(Techniques.FadeIn)
                        .duration(1000)
                        .repeat(YoYo.INFINITE)
                        .playOn(binding.alarmType1.num2);

                YoYo.with(Techniques.FadeIn)
                        .duration(1000)
                        .repeat(YoYo.INFINITE)
                        .playOn(binding.alarmType1.num3);

                YoYo.with(Techniques.FadeIn)
                        .duration(1000)
                        .repeat(YoYo.INFINITE)
                        .playOn(binding.alarmType1.num4);

                YoYo.with(Techniques.FadeIn)
                        .duration(1000)
                        .repeat(YoYo.INFINITE)
                        .playOn(binding.alarmType1.num5);

                YoYo.with(Techniques.FadeIn)
                        .duration(1000)
                        .repeat(YoYo.INFINITE)
                        .playOn(binding.alarmType1.num6);

//                YoYo.with(Techniques.FadeIn)
//                        .duration(1000)
//                        .repeat(YoYo.INFINITE)
//                        .playOn(binding.alarmType1.plus);
//
//                YoYo.with(Techniques.FadeIn)
//                        .duration(1000)
//                        .repeat(YoYo.INFINITE)
//                        .playOn(binding.alarmType1.bnum);
                break;
            case 1:
                binding.ivParticle.setVisibility(View.GONE);
                binding.alarmType2.tvWinneramount.setTypeface(app.jalnan);
                if (StringUtil.isNull(app.lln_rank)) {
                    currentWinInfo();
                }

                binding.alarmType2.getRoot().setVisibility(View.VISIBLE);
                if (!StringUtil.isNull(app.lln_rank)) {
                    binding.alarmType2.tvEpisode.setText(app.lln_rank);
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
                        LottoUtil.animationPriceTextfloat(0, Float.parseFloat(app.lln_lucky_price_one), binding.alarmType2.tvWinneramount,Long.parseLong(app.lln_lucky_price_one));
                    }else{
                        binding.alarmType2.tvWinneramount.setText(StringUtil.setNumComma(Long.parseLong(app.lln_lucky_price_one)));
                    }
//                    binding.alarmType2.tvWinneramount.setText(StringUtil.setNumComma(Long.parseLong(app.lln_lucky_price_one)));
                    binding.alarmType2.num1.setText(app.lln_num1);
                    binding.alarmType2.num1.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_num1),true));
                    binding.alarmType2.num2.setText(app.lln_num2);
                    binding.alarmType2.num2.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_num2),true));
                    binding.alarmType2.num3.setText(app.lln_num3);
                    binding.alarmType2.num3.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_num3),true));
                    binding.alarmType2.num4.setText(app.lln_num4);
                    binding.alarmType2.num4.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_num4),true));
                    binding.alarmType2.num5.setText(app.lln_num5);
                    binding.alarmType2.num5.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_num5),true));
                    binding.alarmType2.num6.setText(app.lln_num6);
                    binding.alarmType2.num6.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_num6),true));
                    binding.alarmType2.bnum.setText(app.lln_bnum);
                    binding.alarmType2.bnum.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_bnum),true));
                }
                break;
            case 2:
                binding.ivParticle.setVisibility(View.GONE);
                binding.alarmType3.getRoot().setVisibility(View.VISIBLE);
                binding.alarmType3.tvCountdown.setTypeface(app.jalnan);
                cTimer = new CountDownTimer(millisInFuture,1000) {
                    @Override
                    public void onTick(long l) {
                        binding.alarmType3.tvCountdown.setText(getGoalTime());
                    }

                    @Override
                    public void onFinish() {
                        Log.d(StringUtil.TAG, "CountDownTimer onFinish: ");
                    }
                }.start();
                break;
        }

    }

    private void setTextTypeface(){
        binding.alarmType1.num1.setTypeface(app.jalnan);
        binding.alarmType1.num2.setTypeface(app.jalnan);
        binding.alarmType1.num3.setTypeface(app.jalnan);
        binding.alarmType1.num4.setTypeface(app.jalnan);
        binding.alarmType1.num5.setTypeface(app.jalnan);
        binding.alarmType1.num6.setTypeface(app.jalnan);

        binding.alarmType2.num1.setTypeface(app.jalnan);
        binding.alarmType2.num2.setTypeface(app.jalnan);
        binding.alarmType2.num3.setTypeface(app.jalnan);
        binding.alarmType2.num4.setTypeface(app.jalnan);
        binding.alarmType2.num5.setTypeface(app.jalnan);
        binding.alarmType2.num6.setTypeface(app.jalnan);
        binding.alarmType2.bnum.setTypeface(app.jalnan);
    }

    private void makeRandNums(){

        randNums = new ArrayList<>();

        for (int i = 0; i < 4; i++){
            int num = LottoUtil.getRandnum();
            if (randNums.contains(num)) {
                int tmpnum = num;

                while (randNums.contains(tmpnum)) {
                    tmpnum = LottoUtil.getRandnum();
                }
                randNums.add(tmpnum);
            } else {
                randNums.add(num);
            }
        }

        Collections.sort(randNums, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        Log.d(StringUtil.TAG, "makeRandNums: "+randNums);
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

                        if (jo.has("data")) {
                            JSONArray ja = new JSONArray(jo.getString("data"));

                            JSONObject obj = ja.getJSONObject(0);
                            app.lln_rank = obj.getString("lln_rank");
                            app.lln_lucky_price_one = obj.getString("lln_lucky_price_one");
                            app.lln_num1 = obj.getString("lln_num_1");
                            app.lln_num2 = obj.getString("lln_num_2");
                            app.lln_num3 = obj.getString("lln_num_3");
                            app.lln_num4 = obj.getString("lln_num_4");
                            app.lln_num5 = obj.getString("lln_num_5");
                            app.lln_num6 = obj.getString("lln_num_6");
                            app.lln_bnum = obj.getString("lln_num_bonus");

                            binding.alarmType2.tvEpisode.setText(app.lln_rank);
                            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
                                LottoUtil.animationPriceTextfloat(0, Float.parseFloat(app.lln_lucky_price_one), binding.alarmType2.tvWinneramount,Long.parseLong(app.lln_lucky_price_one));
//                                LottoUtil.animationPriceText(0,Integer.parseInt(app.lln_lucky_price_one),binding.alarmType2.tvWinneramount);
                            }else{
                                binding.alarmType2.tvWinneramount.setText(StringUtil.setNumComma(Long.parseLong(app.lln_lucky_price_one)));
                            }
//                            binding.alarmType2.tvWinneramount.setText(StringUtil.setNumComma(Long.parseLong(app.lln_lucky_price_one)));
                            binding.alarmType2.num1.setText(app.lln_num1);
                            binding.alarmType2.num1.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_num1),true));
                            binding.alarmType2.num2.setText(app.lln_num2);
                            binding.alarmType2.num2.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_num2),true));
                            binding.alarmType2.num3.setText(app.lln_num3);
                            binding.alarmType2.num3.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_num3),true));
                            binding.alarmType2.num4.setText(app.lln_num4);
                            binding.alarmType2.num4.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_num4),true));
                            binding.alarmType2.num5.setText(app.lln_num5);
                            binding.alarmType2.num5.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_num5),true));
                            binding.alarmType2.num6.setText(app.lln_num6);
                            binding.alarmType2.num6.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_num6),true));
                            binding.alarmType2.bnum.setText(app.lln_bnum);
                            binding.alarmType2.bnum.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(app.lln_bnum),true));

                        }else{

                        }

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


    private String getGoalTime(){

        String timetext = "";

        if (app.satCalender == null){
            app.satCalender = getCurSaturday();
        }

        if (app.satCalender.getTimeInMillis() < System.currentTimeMillis()){
            app.satCalender.add(Calendar.DATE,7);
        }

        long totalTime = (app.satCalender.getTimeInMillis() - System.currentTimeMillis()) / 1000;

        Log.d(StringUtil.TAG, "Time: "+app.satCalender.getTimeInMillis() + " / " + System.currentTimeMillis());
        Log.d(StringUtil.TAG, "totalTime: "+totalTime);

        int day = (int)totalTime / (60 * 60 * 24);
        int hour = (int)(totalTime - day * 60 * 60 * 24) / (60 * 60);
        int minute = (int)(totalTime - day * 60 * 60 * 24 - hour *3600) / 60;
        int second = (int)totalTime % 60;

        Log.d(StringUtil.TAG, "day: "+day);
        Log.d(StringUtil.TAG, "hour: "+hour);
        Log.d(StringUtil.TAG, "minute: "+minute);
        Log.d(StringUtil.TAG, "second: "+second);

        timetext = day + "일 " + hour + "시간 " + String.format("%02d",minute) + "분 " + String.format("%02d",second)  + "초";
        return timetext;
    }

    private Calendar getCurSaturday(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
        c.set(Calendar.HOUR_OF_DAY,20);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        return c;
//        Log.d(StringUtil.TAG, "getCurSaturday: "+sdf.format(c.getTime()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cTimer != null){
            try {
                cTimer.cancel();
            }catch (Exception e){
                cTimer = null;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_close:
                finish();
                break;
            case R.id.btn_alarmbottom:
                Intent mStartActivity = new Intent(this, SplashAct.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(act, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager)act.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);
//                switch (type){
//                    case 0:
//                        startActivity(new Intent(this, RandnumAct.class));
//                        finish();
//                        Toast.makeText(this,"0: "+binding.tvTitle.getText(),Toast.LENGTH_SHORT).show();
//                        break;
//                    case 1:
//                        startActivity(new Intent(this, RandnumAct.class));
//                        finish();
//                        Toast.makeText(this,"1: "+binding.tvTitle.getText(),Toast.LENGTH_SHORT).show();
//                        break;
//                    case 2:
//                        startActivity(new Intent(this, RandnumAct.class));
//                        finish();
//                        Toast.makeText(this,"2: "+binding.tvTitle.getText(),Toast.LENGTH_SHORT).show();
//                        break;
//                }
                break;
        }
    }
}
