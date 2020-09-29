package kr.core.powerlotto.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.core.powerlotto.R;
import kr.core.powerlotto.adapter.MainviewPagerAdapter;
import kr.core.powerlotto.data.MainWinInfo;
import kr.core.powerlotto.databinding.ActivityMainBinding;
import kr.core.powerlotto.insidedata.UserPref;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.popup.PopupAlarmMsg;
import kr.core.powerlotto.util.ConvertPxDp;
import kr.core.powerlotto.util.LayoutWebView;
import kr.core.powerlotto.util.LottoUtil;
import kr.core.powerlotto.util.StringUtil;

public class MainActivity extends BaseAct implements View.OnClickListener {
    Activity act;

    ActivityMainBinding binding;

    MainviewPagerAdapter autosc;

    ArrayList<MainWinInfo> winlist = new ArrayList<>();

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainRecentInfo();
        act = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.layoutNavi.swAlarm.setSelected(UserPref.isAlarmState(this));
        setTextTypeface();

        getCoupaBanner();

//        if (StringUtil.isNull(app.bannerState)) {
//            binding.bannerArea.getRoot().setVisibility(View.VISIBLE);
//            binding.bannerArea.bannerAdmob.setVisibility(View.VISIBLE);
//            // admob 설정
//            MobileAds.initialize(this, new OnInitializationCompleteListener() {
//                @Override
//                public void onInitializationComplete(InitializationStatus initializationStatus) {
//
//                }
//            });
//            AdRequest adRequest = new AdRequest.Builder().build();
//            binding.bannerArea.bannerAdmob.loadAd(adRequest);
////            binding.bannerArea.getRoot().setVisibility(View.GONE);
//        } else {
//            switch (app.bannerState) {
//                case StringUtil.BANNER:
//                    binding.bannerArea.getRoot().setVisibility(View.VISIBLE);
//                    binding.bannerArea.bannerAdmob.setVisibility(View.GONE);
//                    binding.bannerArea.bannerCore.setVisibility(View.VISIBLE);
//
//                    // 이미지 세팅
//                    Glide.with(this)
//                            .load(app.bannerImg)
//                            .into(binding.bannerArea.bannerCore);
//                    binding.bannerArea.bannerCore.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            if (StringUtil.isNull(app.bannerLink)) {
//                                Toast.makeText(MainActivity.this, "연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
//                            } else {
//                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(app.bannerLink)));
//                            }
//                        }
//                    });
//
//                    break;
//                case StringUtil.ADMOB:
//                    binding.bannerArea.getRoot().setVisibility(View.VISIBLE);
//                    binding.bannerArea.bannerAdmob.setVisibility(View.VISIBLE);
//                    // admob 설정
//                    MobileAds.initialize(this, new OnInitializationCompleteListener() {
//                        @Override
//                        public void onInitializationComplete(InitializationStatus initializationStatus) {
//
//                        }
//                    });
//                    AdRequest adRequest = new AdRequest.Builder().build();
//                    binding.bannerArea.bannerAdmob.loadAd(adRequest);
//                    break;
//                case StringUtil.NONE:
//                    binding.bannerArea.getRoot().setVisibility(View.GONE);
//                    break;
//            }
//        }

        binding.btnDrawermenu.setOnClickListener(this);
        binding.btnTopqrscan.setOnClickListener(this);

        binding.btnPre.setOnClickListener(this);
        binding.btnNext.setOnClickListener(this);

        binding.btnMainqrscan.setOnClickListener(this);
        binding.btnMainwinnum.setOnClickListener(this);
        binding.btnMaingoodplace.setOnClickListener(this);
        binding.btnMainrandnum.setOnClickListener(this);
        binding.btnMainadvnum.setOnClickListener(this);
        binding.btnMainrecievenum.setOnClickListener(this);
        binding.btnMainmanual.setOnClickListener(this);

        binding.layoutNavi.btnDrclose.setOnClickListener(this);
        binding.layoutNavi.btnQrscan.setOnClickListener(this);
        binding.layoutNavi.btnWinnum.setOnClickListener(this);
        binding.layoutNavi.btnGoodplace.setOnClickListener(this);
        binding.layoutNavi.btnRandnum.setOnClickListener(this);
        binding.layoutNavi.btnAdvnum.setOnClickListener(this);
        binding.layoutNavi.btnRecievenum.setOnClickListener(this);
        binding.layoutNavi.btnPremium.setOnClickListener(this);
        binding.layoutNavi.swAlarm.setOnClickListener(this);
        binding.layoutNavi.btnPrivacy.setOnClickListener(this);
        binding.layoutNavi.btnManualnum.setOnClickListener(this);

        binding.mainAutoscroll.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                binding.mainIndicator.selectDot(position);

                binding.tvNum1.setText(winlist.get(position).getLln_num_1());
                binding.tvNum1.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(position).getLln_num_1()), true));
                binding.tvNum2.setText(winlist.get(position).getLln_num_2());
                binding.tvNum2.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(position).getLln_num_2()), true));
                binding.tvNum3.setText(winlist.get(position).getLln_num_3());
                binding.tvNum3.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(position).getLln_num_3()), true));
                binding.tvNum4.setText(winlist.get(position).getLln_num_4());
                binding.tvNum4.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(position).getLln_num_4()), true));
                binding.tvNum5.setText(winlist.get(position).getLln_num_5());
                binding.tvNum5.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(position).getLln_num_5()), true));
                binding.tvNum6.setText(winlist.get(position).getLln_num_6());
                binding.tvNum6.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(position).getLln_num_6()), true));
                binding.tvBonusnum.setText(winlist.get(position).getLln_num_bonus());
                binding.tvBonusnum.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(position).getLln_num_bonus()), true));

                binding.tvTotalamount.setText(changePrice(winlist.get(position).getLln_lucky_price()));
                binding.tvWinamount.setText(changePrice(winlist.get(position).getLln_lucky_price_one()));

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            finishAffinity();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(this, "뒤로 버튼을 한번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT).show();
        }

    }

    private void getCoupaBanner() {
        ReqBasic server = new ReqBasic(act, "https://coupang.adamstore.co.kr/lib/control.siso") {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    final String res = resultData.getResult();

                    if (!StringUtil.isNull(res)) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //Log.d(HoUtils.TAG, "결과 : " + result);
                                    final JSONObject jo = new JSONObject(res);
                                    if (StringUtil.getStr(jo, "result").equalsIgnoreCase("Y")) {
                                        JSONObject job = jo.getJSONObject("data");

                                        String coupang_url = StringUtil.getStr(job, "coupang_url");
                                        String banner = StringUtil.getStr(job, "banner");

                                        Log.i("TEST_HOME", "coupang_url: " + coupang_url);
                                        Log.i("TEST_HOME", "banner: " + banner);


                                        Glide.with(act).load(banner).into(binding.coupaBanner);
                                        binding.coupaBanner.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(act, LayoutWebView.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        });
                                    } else {

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } else {
                }
            }
        };

        server.addParams("dbControl", "getCoupangPartnersInfo");
        server.addParams("si_idx", "1");
        server.execute(true, false);
    }

    private void setTextTypeface() {
        binding.tvNum1.setTypeface(app.jalnan);
        binding.tvNum2.setTypeface(app.jalnan);
        binding.tvNum3.setTypeface(app.jalnan);
        binding.tvNum4.setTypeface(app.jalnan);
        binding.tvNum5.setTypeface(app.jalnan);
        binding.tvNum6.setTypeface(app.jalnan);
        binding.tvBonusnum.setTypeface(app.jalnan);
    }

    public String changePrice(String price) {
        String changeStr = "";
        changeStr = price.substring(0, price.length() - 8) + "억";

        return changeStr;
    }

    private void mainRecentInfo() {
        ReqBasic recentInfo = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(StringUtil.TAG, "code: " + resultCode);
                Log.d(StringUtil.TAG, "mainRecentInfo: " + resultData.getResult());

                if (resultData.getResult() != null) {

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        JSONArray ja = new JSONArray(jo.getString("data"));
                        ArrayList<MainWinInfo> tmplist = new ArrayList<>();
                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject obj = ja.getJSONObject(i);
                            MainWinInfo data = new MainWinInfo();

                            data.setLln_idx(obj.getString("lln_idx"));
                            if (i == 0) {
                                app.lln_rank = obj.getString("lln_rank");
                            }
                            data.setLln_rank(obj.getString("lln_rank"));
                            data.setLln_total_sell_price(obj.getString("lln_total_sell_price"));
                            data.setLln_lucky_price(obj.getString("lln_lucky_price"));
                            data.setLln_lucky_price_one(obj.getString("lln_lucky_price_one"));
                            data.setLln_lucky_cnt(obj.getString("lln_lucky_cnt"));
                            data.setLln_num_1(obj.getString("lln_num_1"));
                            data.setLln_num_2(obj.getString("lln_num_2"));
                            data.setLln_num_3(obj.getString("lln_num_3"));
                            data.setLln_num_4(obj.getString("lln_num_4"));
                            data.setLln_num_5(obj.getString("lln_num_5"));
                            data.setLln_num_6(obj.getString("lln_num_6"));
                            data.setLln_num_bonus(obj.getString("lln_num_bonus"));
                            data.setLln_chu_date(obj.getString("lln_chu_date"));

                            tmplist.add(data);

                        }

                        winlist.addAll(tmplist);

                        autosc = new MainviewPagerAdapter(MainActivity.this, getLayoutInflater(), winlist.size(), winlist);
                        binding.mainAutoscroll.setAdapter(autosc);

                        binding.mainAutoscroll.setInterval(1500);
                        binding.mainAutoscroll.startAutoScroll();

                        binding.mainIndicator.createDotPanel(winlist.size(), R.drawable.roll_btn_off, R.drawable.roll_btn_on, (int) ConvertPxDp.convertPixelsToDp((float) 24, MainActivity.this));

                        binding.tvNum1.setText(winlist.get(0).getLln_num_1());
                        binding.tvNum1.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(0).getLln_num_1()), true));
                        binding.tvNum2.setText(winlist.get(0).getLln_num_2());
                        binding.tvNum2.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(0).getLln_num_2()), true));
                        binding.tvNum3.setText(winlist.get(0).getLln_num_3());
                        binding.tvNum3.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(0).getLln_num_3()), true));
                        binding.tvNum4.setText(winlist.get(0).getLln_num_4());
                        binding.tvNum4.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(0).getLln_num_4()), true));
                        binding.tvNum5.setText(winlist.get(0).getLln_num_5());
                        binding.tvNum5.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(0).getLln_num_5()), true));
                        binding.tvNum6.setText(winlist.get(0).getLln_num_6());
                        binding.tvNum6.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(0).getLln_num_6()), true));
                        binding.tvBonusnum.setText(winlist.get(0).getLln_num_bonus());
                        binding.tvBonusnum.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(winlist.get(0).getLln_num_bonus()), true));

                        binding.tvTotalamount.setText(changePrice(winlist.get(0).getLln_lucky_price()));
                        binding.tvWinamount.setText(changePrice(winlist.get(0).getLln_lucky_price_one()));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }

            }
        };

        recentInfo.addParams("APPCONNECTCODE", "APP");
        recentInfo.addParams("dbControl", "MainLottoLuckyNum");
        recentInfo.execute(true, true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_topqrscan:
                startActivity(new Intent(this, ScannerAct.class));
                break;
            case R.id.btn_mainqrscan:
                startActivity(new Intent(this, ScannerAct.class));
                break;
            case R.id.btn_mainwinnum:
                startActivity(new Intent(this, WinnumsAct.class));
                break;
            case R.id.btn_maingoodplace:
                startActivity(new Intent(this, GoodplaceAct.class));
                break;
            case R.id.btn_mainmanual:
                startActivity(new Intent(this, ManualnumAct.class));
                break;
            case R.id.btn_mainrandnum:
                startActivity(new Intent(this, RandnumAct.class));
                break;
            case R.id.btn_mainadvnum:
                startActivity(new Intent(this, AdancedAct.class));
                break;
            case R.id.btn_mainrecievenum:
                startActivity(new Intent(this, RecievenumAct.class));
                break;
            case R.id.btn_pre:
                if (binding.mainAutoscroll.getCurrentItem() == 0) {
                    binding.mainAutoscroll.setCurrentItem(winlist.size() - 1);
                } else {
                    binding.mainAutoscroll.setCurrentItem(binding.mainAutoscroll.getCurrentItem() - 1);
                }
                break;
            case R.id.btn_next:
                if (binding.mainAutoscroll.getCurrentItem() == 4) {
                    binding.mainAutoscroll.setCurrentItem(0);
                } else {
                    binding.mainAutoscroll.setCurrentItem(binding.mainAutoscroll.getCurrentItem() + 1);
                }
                break;
            case R.id.btn_drawermenu:
                binding.mainDrawer.openDrawer(GravityCompat.START);
                break;
            case R.id.btn_drclose:
                binding.mainDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.btn_qrscan:
                startActivity(new Intent(this, ScannerAct.class));
//                scanQrCode();
                binding.mainDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.btn_winnum:
                startActivity(new Intent(this, WinnumsAct.class));
                binding.mainDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.btn_goodplace:
                startActivity(new Intent(this, GoodplaceAct.class));
                binding.mainDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.btn_manualnum:
                startActivity(new Intent(this, ManualnumAct.class));
                binding.mainDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.btn_randnum:
                startActivity(new Intent(this, RandnumAct.class));
                binding.mainDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.btn_advnum:
                startActivity(new Intent(this, AdancedAct.class));
                binding.mainDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.btn_recievenum:
                startActivity(new Intent(this, RecievenumAct.class));
                binding.mainDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.btn_premium:
                startActivity(new Intent(this, PremiumAct.class));
                binding.mainDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.sw_alarm:
                binding.layoutNavi.swAlarm.setSelected(!binding.layoutNavi.swAlarm.isSelected());
                UserPref.setAlarmState(this, binding.layoutNavi.swAlarm.isSelected());
                break;
            case R.id.btn_privacy:
                startActivity(new Intent(this, PrivacyAct.class));
                binding.mainDrawer.closeDrawer(GravityCompat.START);
                break;
        }
    }
}
