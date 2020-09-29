package kr.core.powerlotto.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import kr.core.powerlotto.R;
import kr.core.powerlotto.databinding.ActivityManualnumBinding;
import kr.core.powerlotto.insidedata.UserPref;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.MultipartUtility;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.util.LayoutWebView;
import kr.core.powerlotto.util.LottoUtil;
import kr.core.powerlotto.util.StringUtil;

public class ManualnumAct extends BaseAct implements View.OnClickListener {

    ActivityManualnumBinding binding;

    ArrayList<Integer> makenumList = new ArrayList<>();

    ArrayList<Integer> manualNums = new ArrayList<>();
    HashMap<String, ArrayList<Integer>> mNumTable = new HashMap<>();

    boolean numMakeFlag = false;
    boolean isMekeFile = false;
    String imgUrl = "";

    ProgressDialog pd;
    Activity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_manualnum);
        act = this;
        getCoupaBanner();

        binding.topText1.setTypeface(app.jalnan);
        binding.topText2.setTypeface(app.jalnan);

        pd = new ProgressDialog(this);
        pd.setMessage("파일 생성중입니다...");
        pd.setCanceledOnTouchOutside(false);

        setTextTypeface();
//        setBannerArea();

        binding.btnClose.setOnClickListener(this);
        binding.btnStartNumber.setOnClickListener(this);
        binding.ballLayout.getRoot().setOnClickListener(this);
        binding.btnSavemanualnum.setOnClickListener(this);

        binding.btnDeltable.setOnClickListener(this);
        binding.btnShowresult.setOnClickListener(this);

        binding.layoutMakenumview.btnMakenumclose.setOnClickListener(this);
        binding.layoutMakenumview.btnReset.setOnClickListener(this);
        binding.layoutMakenumview.btnComplete.setOnClickListener(this);

        binding.layoutResultview.btnResviewclose.setOnClickListener(this);
        binding.layoutResultview.btnSavegallery.setOnClickListener(this);
        binding.layoutResultview.btnLinkakao.setOnClickListener(this);

        binding.layoutResultview.tvResviewtitle.setText("번호 수동 생성 결과보기");

        binding.ivPopback.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (binding.ivPopback.getVisibility() == View.VISIBLE) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        setBallclick();

    }

//    private void setBannerArea() {
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
//                                Toast.makeText(ManualnumAct.this, "연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
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
//
//    }
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

        binding.ballLayout.tvNum1.setTypeface(app.jalnan);
        binding.ballLayout.tvNum2.setTypeface(app.jalnan);
        binding.ballLayout.tvNum3.setTypeface(app.jalnan);
        binding.ballLayout.tvNum4.setTypeface(app.jalnan);
        binding.ballLayout.tvNum5.setTypeface(app.jalnan);
        binding.ballLayout.tvNum6.setTypeface(app.jalnan);

        binding.layoutMakenumview.maballLayout.tvNum1.setTypeface(app.jalnan);
        binding.layoutMakenumview.maballLayout.tvNum2.setTypeface(app.jalnan);
        binding.layoutMakenumview.maballLayout.tvNum3.setTypeface(app.jalnan);
        binding.layoutMakenumview.maballLayout.tvNum4.setTypeface(app.jalnan);
        binding.layoutMakenumview.maballLayout.tvNum5.setTypeface(app.jalnan);
        binding.layoutMakenumview.maballLayout.tvNum6.setTypeface(app.jalnan);

        binding.numTable.tvR1c1.setTypeface(app.jalnan);
        binding.numTable.tvR1c2.setTypeface(app.jalnan);
        binding.numTable.tvR1c3.setTypeface(app.jalnan);
        binding.numTable.tvR1c4.setTypeface(app.jalnan);
        binding.numTable.tvR1c5.setTypeface(app.jalnan);
        binding.numTable.tvR1c6.setTypeface(app.jalnan);

        binding.numTable.tvR2c1.setTypeface(app.jalnan);
        binding.numTable.tvR2c2.setTypeface(app.jalnan);
        binding.numTable.tvR2c3.setTypeface(app.jalnan);
        binding.numTable.tvR2c4.setTypeface(app.jalnan);
        binding.numTable.tvR2c5.setTypeface(app.jalnan);
        binding.numTable.tvR2c6.setTypeface(app.jalnan);

        binding.numTable.tvR3c1.setTypeface(app.jalnan);
        binding.numTable.tvR3c2.setTypeface(app.jalnan);
        binding.numTable.tvR3c3.setTypeface(app.jalnan);
        binding.numTable.tvR3c4.setTypeface(app.jalnan);
        binding.numTable.tvR3c5.setTypeface(app.jalnan);
        binding.numTable.tvR3c6.setTypeface(app.jalnan);

        binding.numTable.tvR4c1.setTypeface(app.jalnan);
        binding.numTable.tvR4c2.setTypeface(app.jalnan);
        binding.numTable.tvR4c3.setTypeface(app.jalnan);
        binding.numTable.tvR4c4.setTypeface(app.jalnan);
        binding.numTable.tvR4c5.setTypeface(app.jalnan);
        binding.numTable.tvR4c6.setTypeface(app.jalnan);

        binding.numTable.tvR5c1.setTypeface(app.jalnan);
        binding.numTable.tvR5c2.setTypeface(app.jalnan);
        binding.numTable.tvR5c3.setTypeface(app.jalnan);
        binding.numTable.tvR5c4.setTypeface(app.jalnan);
        binding.numTable.tvR5c5.setTypeface(app.jalnan);
        binding.numTable.tvR5c6.setTypeface(app.jalnan);

        binding.layoutResultview.resultTable.tvR1c1.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR1c2.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR1c3.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR1c4.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR1c5.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR1c6.setTypeface(app.jalnan);

        binding.layoutResultview.resultTable.tvR2c1.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR2c2.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR2c3.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR2c4.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR2c5.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR2c6.setTypeface(app.jalnan);

        binding.layoutResultview.resultTable.tvR3c1.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR3c2.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR3c3.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR3c4.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR3c5.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR3c6.setTypeface(app.jalnan);

        binding.layoutResultview.resultTable.tvR4c1.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR4c2.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR4c3.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR4c4.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR4c5.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR4c6.setTypeface(app.jalnan);

        binding.layoutResultview.resultTable.tvR5c1.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR5c2.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR5c3.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR5c4.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR5c5.setTypeface(app.jalnan);
        binding.layoutResultview.resultTable.tvR5c6.setTypeface(app.jalnan);

        binding.layoutMakenumview.totalballArea.num1.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num2.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num3.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num4.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num5.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num6.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num7.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num8.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num9.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num10.setTypeface(app.jalnan);

        binding.layoutMakenumview.totalballArea.num11.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num12.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num13.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num14.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num15.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num16.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num17.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num18.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num19.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num20.setTypeface(app.jalnan);

        binding.layoutMakenumview.totalballArea.num21.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num22.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num23.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num24.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num25.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num26.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num27.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num28.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num29.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num30.setTypeface(app.jalnan);

        binding.layoutMakenumview.totalballArea.num31.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num32.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num33.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num34.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num35.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num36.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num37.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num38.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num39.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num40.setTypeface(app.jalnan);

        binding.layoutMakenumview.totalballArea.num41.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num42.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num43.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num44.setTypeface(app.jalnan);
        binding.layoutMakenumview.totalballArea.num45.setTypeface(app.jalnan);

    }

    @Override
    public void onBackPressed() {
        if (binding.layoutResultview.getRoot().getVisibility() == View.VISIBLE) {
            binding.ivPopback.setVisibility(View.GONE);
            binding.layoutResultview.getRoot().setVisibility(View.GONE);
        } else if (binding.layoutMakenumview.getRoot().getVisibility() == View.VISIBLE) {
            binding.ivPopback.setVisibility(View.GONE);
            binding.layoutMakenumview.getRoot().setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private void setNumlist(int selNum) {

        if (makenumList.size() == 6) {
            Toast.makeText(this, "번호 선택을 완료 했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (makenumList.size() < 6) {
            if (makenumList.contains(selNum)) {
                Toast.makeText(this, "다른번호를 선택하세요.", Toast.LENGTH_SHORT).show();
                return;
            } else {
                makenumList.add(selNum);
            }

            switch (makenumList.size()) {
                case 1:
                    binding.layoutMakenumview.maballLayout.ivNum1.setVisibility(View.GONE);
                    binding.layoutMakenumview.maballLayout.ivNum2.setVisibility(View.VISIBLE);
                    binding.layoutMakenumview.maballLayout.tvNum1.setText(String.valueOf(selNum));
                    binding.layoutMakenumview.maballLayout.tvNum1.setBackgroundResource(LottoUtil.getBallResource(selNum, true));
                    break;
                case 2:
                    binding.layoutMakenumview.maballLayout.ivNum2.setVisibility(View.GONE);
                    binding.layoutMakenumview.maballLayout.ivNum3.setVisibility(View.VISIBLE);
                    binding.layoutMakenumview.maballLayout.tvNum2.setText(String.valueOf(selNum));
                    binding.layoutMakenumview.maballLayout.tvNum2.setBackgroundResource(LottoUtil.getBallResource(selNum, true));
                    break;
                case 3:
                    binding.layoutMakenumview.maballLayout.ivNum3.setVisibility(View.GONE);
                    binding.layoutMakenumview.maballLayout.ivNum4.setVisibility(View.VISIBLE);
                    binding.layoutMakenumview.maballLayout.tvNum3.setText(String.valueOf(selNum));
                    binding.layoutMakenumview.maballLayout.tvNum3.setBackgroundResource(LottoUtil.getBallResource(selNum, true));
                    break;
                case 4:
                    binding.layoutMakenumview.maballLayout.ivNum4.setVisibility(View.GONE);
                    binding.layoutMakenumview.maballLayout.ivNum5.setVisibility(View.VISIBLE);
                    binding.layoutMakenumview.maballLayout.tvNum4.setText(String.valueOf(selNum));
                    binding.layoutMakenumview.maballLayout.tvNum4.setBackgroundResource(LottoUtil.getBallResource(selNum, true));
                    break;
                case 5:
                    binding.layoutMakenumview.maballLayout.ivNum5.setVisibility(View.GONE);
                    binding.layoutMakenumview.maballLayout.ivNum6.setVisibility(View.VISIBLE);
                    binding.layoutMakenumview.maballLayout.tvNum5.setText(String.valueOf(selNum));
                    binding.layoutMakenumview.maballLayout.tvNum5.setBackgroundResource(LottoUtil.getBallResource(selNum, true));
                    break;
                case 6:
                    binding.layoutMakenumview.maballLayout.ivNum6.setVisibility(View.GONE);
                    binding.layoutMakenumview.maballLayout.tvNum6.setText(String.valueOf(selNum));
                    binding.layoutMakenumview.maballLayout.tvNum6.setBackgroundResource(LottoUtil.getBallResource(selNum, true));
                    break;
            }
        }

        switch (makenumList.size()) {
            case 0:
                binding.layoutMakenumview.tvBallno.setText("1번");
                break;
            case 1:
                binding.layoutMakenumview.tvBallno.setText("2번");
                break;
            case 2:
                binding.layoutMakenumview.tvBallno.setText("3번");
                break;
            case 3:
                binding.layoutMakenumview.tvBallno.setText("4번");
                break;
            case 4:
                binding.layoutMakenumview.tvBallno.setText("5번");
                break;
            case 5:
                binding.layoutMakenumview.tvBallno.setText("6번");
                break;
        }

    }

    private void initMakenUm() {
        if (makenumList.size() > 0) {
            makenumList.clear();
        }

        binding.layoutMakenumview.maballLayout.ivNum1.setVisibility(View.VISIBLE);
        binding.layoutMakenumview.maballLayout.ivNum2.setVisibility(View.GONE);
        binding.layoutMakenumview.maballLayout.ivNum3.setVisibility(View.GONE);
        binding.layoutMakenumview.maballLayout.ivNum4.setVisibility(View.GONE);
        binding.layoutMakenumview.maballLayout.ivNum5.setVisibility(View.GONE);
        binding.layoutMakenumview.maballLayout.ivNum6.setVisibility(View.GONE);

        binding.layoutMakenumview.maballLayout.tvNum1.setText("?");
        binding.layoutMakenumview.maballLayout.tvNum1.setBackgroundResource(R.drawable.b_ball_grey);
        binding.layoutMakenumview.maballLayout.tvNum2.setText("?");
        binding.layoutMakenumview.maballLayout.tvNum2.setBackgroundResource(R.drawable.b_ball_grey);
        binding.layoutMakenumview.maballLayout.tvNum3.setText("?");
        binding.layoutMakenumview.maballLayout.tvNum3.setBackgroundResource(R.drawable.b_ball_grey);
        binding.layoutMakenumview.maballLayout.tvNum4.setText("?");
        binding.layoutMakenumview.maballLayout.tvNum4.setBackgroundResource(R.drawable.b_ball_grey);
        binding.layoutMakenumview.maballLayout.tvNum5.setText("?");
        binding.layoutMakenumview.maballLayout.tvNum5.setBackgroundResource(R.drawable.b_ball_grey);
        binding.layoutMakenumview.maballLayout.tvNum6.setText("?");
        binding.layoutMakenumview.maballLayout.tvNum6.setBackgroundResource(R.drawable.b_ball_grey);

        binding.layoutMakenumview.tvBallno.setText("1번");
    }

    private void setBallclick() {

        binding.layoutMakenumview.totalballArea.ball1.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball2.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball3.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball4.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball5.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball6.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball7.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball8.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball9.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball10.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball11.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball12.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball13.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball14.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball15.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball16.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball17.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball18.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball19.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball20.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball21.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball22.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball23.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball24.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball25.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball26.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball27.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball28.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball29.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball30.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball31.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball32.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball33.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball34.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball35.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball36.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball37.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball38.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball39.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball40.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball41.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball42.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball43.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball44.setOnClickListener(this);
        binding.layoutMakenumview.totalballArea.ball45.setOnClickListener(this);

    }

    private void makeNumAni(final Techniques techniques, final long duration, final TextView view, final ArrayList<Integer> res, final int idx) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    app.soundPool.play(app.soundId, 1f, 1f, 0, 0, 1f);
                }

                YoYo.with(techniques)
                        .duration(duration)
                        .playOn(view);

                manualNums.add(res.get(idx));

                view.setText(String.valueOf(manualNums.get(idx)));
                view.setBackgroundResource(LottoUtil.getBallResource(manualNums.get(idx), true));
            }
        });

    }

    private void initBalls() {
        binding.ballLayout.tvNum1.setText("?");
        binding.ballLayout.tvNum1.setBackgroundResource(R.drawable.b_ball_grey);
        binding.ballLayout.tvNum2.setText("?");
        binding.ballLayout.tvNum2.setBackgroundResource(R.drawable.b_ball_grey);
        binding.ballLayout.tvNum3.setText("?");
        binding.ballLayout.tvNum3.setBackgroundResource(R.drawable.b_ball_grey);
        binding.ballLayout.tvNum4.setText("?");
        binding.ballLayout.tvNum4.setBackgroundResource(R.drawable.b_ball_grey);
        binding.ballLayout.tvNum5.setText("?");
        binding.ballLayout.tvNum5.setBackgroundResource(R.drawable.b_ball_grey);
        binding.ballLayout.tvNum6.setText("?");
        binding.ballLayout.tvNum6.setBackgroundResource(R.drawable.b_ball_grey);
    }

    private void saveNum() {
        if (manualNums.size() == 6) {
            Log.d(StringUtil.TAG, "manualNums.size(): " + manualNums.size());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Integer> integers = new ArrayList<>();
                    if (mNumTable.isEmpty()) {
                        mNumTable.put("t1", manualNums);

                        integers = mNumTable.get("t1");
                        binding.numTable.tvR1c1.setText(String.valueOf(integers.get(0)));
                        binding.numTable.tvR1c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
                        binding.numTable.tvR1c2.setText(String.valueOf(integers.get(1)));
                        binding.numTable.tvR1c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
                        binding.numTable.tvR1c3.setText(String.valueOf(integers.get(2)));
                        binding.numTable.tvR1c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
                        binding.numTable.tvR1c4.setText(String.valueOf(integers.get(3)));
                        binding.numTable.tvR1c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
                        binding.numTable.tvR1c5.setText(String.valueOf(integers.get(4)));
                        binding.numTable.tvR1c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
                        binding.numTable.tvR1c6.setText(String.valueOf(integers.get(5)));
                        binding.numTable.tvR1c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));

                    } else {
                        Log.d(StringUtil.TAG, "!mNumTable.isEmpty(): ");
                        if (mNumTable.containsKey("t1")) {
                            if (mNumTable.containsKey("t2")) {
                                if (mNumTable.containsKey("t3")) {
                                    if (mNumTable.containsKey("t4")) {
                                        mNumTable.put("t5", manualNums);
                                        integers = mNumTable.get("t5");
                                        binding.numTable.tvR5c1.setText(String.valueOf(integers.get(0)));
                                        binding.numTable.tvR5c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
                                        binding.numTable.tvR5c2.setText(String.valueOf(integers.get(1)));
                                        binding.numTable.tvR5c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
                                        binding.numTable.tvR5c3.setText(String.valueOf(integers.get(2)));
                                        binding.numTable.tvR5c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
                                        binding.numTable.tvR5c4.setText(String.valueOf(integers.get(3)));
                                        binding.numTable.tvR5c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
                                        binding.numTable.tvR5c5.setText(String.valueOf(integers.get(4)));
                                        binding.numTable.tvR5c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
                                        binding.numTable.tvR5c6.setText(String.valueOf(integers.get(5)));
                                        binding.numTable.tvR5c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
                                    } else {
                                        mNumTable.put("t4", manualNums);
                                        integers = mNumTable.get("t4");
                                        binding.numTable.tvR4c1.setText(String.valueOf(integers.get(0)));
                                        binding.numTable.tvR4c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
                                        binding.numTable.tvR4c2.setText(String.valueOf(integers.get(1)));
                                        binding.numTable.tvR4c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
                                        binding.numTable.tvR4c3.setText(String.valueOf(integers.get(2)));
                                        binding.numTable.tvR4c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
                                        binding.numTable.tvR4c4.setText(String.valueOf(integers.get(3)));
                                        binding.numTable.tvR4c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
                                        binding.numTable.tvR4c5.setText(String.valueOf(integers.get(4)));
                                        binding.numTable.tvR4c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
                                        binding.numTable.tvR4c6.setText(String.valueOf(integers.get(5)));
                                        binding.numTable.tvR4c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
                                    }
                                } else {
                                    mNumTable.put("t3", manualNums);
                                    integers = mNumTable.get("t3");
                                    binding.numTable.tvR3c1.setText(String.valueOf(integers.get(0)));
                                    binding.numTable.tvR3c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
                                    binding.numTable.tvR3c2.setText(String.valueOf(integers.get(1)));
                                    binding.numTable.tvR3c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
                                    binding.numTable.tvR3c3.setText(String.valueOf(integers.get(2)));
                                    binding.numTable.tvR3c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
                                    binding.numTable.tvR3c4.setText(String.valueOf(integers.get(3)));
                                    binding.numTable.tvR3c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
                                    binding.numTable.tvR3c5.setText(String.valueOf(integers.get(4)));
                                    binding.numTable.tvR3c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
                                    binding.numTable.tvR3c6.setText(String.valueOf(integers.get(5)));
                                    binding.numTable.tvR3c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
                                }
                            } else {
                                mNumTable.put("t2", manualNums);
                                integers = mNumTable.get("t2");
                                binding.numTable.tvR2c1.setText(String.valueOf(integers.get(0)));
                                binding.numTable.tvR2c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
                                binding.numTable.tvR2c2.setText(String.valueOf(integers.get(1)));
                                binding.numTable.tvR2c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
                                binding.numTable.tvR2c3.setText(String.valueOf(integers.get(2)));
                                binding.numTable.tvR2c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
                                binding.numTable.tvR2c4.setText(String.valueOf(integers.get(3)));
                                binding.numTable.tvR2c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
                                binding.numTable.tvR2c5.setText(String.valueOf(integers.get(4)));
                                binding.numTable.tvR2c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
                                binding.numTable.tvR2c6.setText(String.valueOf(integers.get(5)));
                                binding.numTable.tvR2c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
                            }
                        } else {
                            mNumTable.put("t1", manualNums);
                            integers = mNumTable.get("t1");
                            binding.numTable.tvR1c1.setText(String.valueOf(integers.get(0)));
                            binding.numTable.tvR1c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
                            binding.numTable.tvR1c2.setText(String.valueOf(integers.get(1)));
                            binding.numTable.tvR1c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
                            binding.numTable.tvR1c3.setText(String.valueOf(integers.get(2)));
                            binding.numTable.tvR1c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
                            binding.numTable.tvR1c4.setText(String.valueOf(integers.get(3)));
                            binding.numTable.tvR1c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
                            binding.numTable.tvR1c5.setText(String.valueOf(integers.get(4)));
                            binding.numTable.tvR1c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
                            binding.numTable.tvR1c6.setText(String.valueOf(integers.get(5)));
                            binding.numTable.tvR1c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
                        }
                    }

                }
            });

        }
        manualNums = new ArrayList<>();
    }

    private void ballAniMakeData(final ArrayList<Integer> intArr) {
        initBalls();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (manualNums.size() < 6) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    switch (manualNums.size()) {
                        case 0:
                            makeNumAni(Techniques.SlideInDown, 700, binding.ballLayout.tvNum1, intArr, 0);
                            break;
                        case 1:
                            makeNumAni(Techniques.SlideInDown, 700, binding.ballLayout.tvNum2, intArr, 1);
                            break;
                        case 2:
                            makeNumAni(Techniques.SlideInDown, 700, binding.ballLayout.tvNum3, intArr, 2);
                            break;
                        case 3:
                            makeNumAni(Techniques.SlideInDown, 700, binding.ballLayout.tvNum4, intArr, 3);
                            break;
                        case 4:
                            makeNumAni(Techniques.SlideInDown, 700, binding.ballLayout.tvNum5, intArr, 4);
                            break;
                        case 5:
                            makeNumAni(Techniques.SlideInDown, 700, binding.ballLayout.tvNum6, intArr, 5);
                            break;
                    }
                    Log.d(StringUtil.TAG, "onClick: " + manualNums.size());
                }
                numMakeFlag = false;
            }
        }).start();
    }

    private void initResultView() {

//        binding.layoutResultview.resultTable.tvR1result.setText(null);
//        binding.layoutResultview.resultTable.tvR2result.setText(null);
//        binding.layoutResultview.resultTable.tvR3result.setText(null);
//        binding.layoutResultview.resultTable.tvR4result.setText(null);
//        binding.layoutResultview.resultTable.tvR5result.setText(null);

        binding.layoutResultview.resultTable.tvR1c1.setText("");
        binding.layoutResultview.resultTable.tvR1c1.setBackground(null);
        binding.layoutResultview.resultTable.tvR1c2.setText("");
        binding.layoutResultview.resultTable.tvR1c2.setBackground(null);
        binding.layoutResultview.resultTable.tvR1c3.setText("");
        binding.layoutResultview.resultTable.tvR1c3.setBackground(null);
        binding.layoutResultview.resultTable.tvR1c4.setText("");
        binding.layoutResultview.resultTable.tvR1c4.setBackground(null);
        binding.layoutResultview.resultTable.tvR1c5.setText("");
        binding.layoutResultview.resultTable.tvR1c5.setBackground(null);
        binding.layoutResultview.resultTable.tvR1c6.setText("");
        binding.layoutResultview.resultTable.tvR1c6.setBackground(null);

        binding.layoutResultview.resultTable.tvR2c1.setText("");
        binding.layoutResultview.resultTable.tvR2c1.setBackground(null);
        binding.layoutResultview.resultTable.tvR2c2.setText("");
        binding.layoutResultview.resultTable.tvR2c2.setBackground(null);
        binding.layoutResultview.resultTable.tvR2c3.setText("");
        binding.layoutResultview.resultTable.tvR2c3.setBackground(null);
        binding.layoutResultview.resultTable.tvR2c4.setText("");
        binding.layoutResultview.resultTable.tvR2c4.setBackground(null);
        binding.layoutResultview.resultTable.tvR2c5.setText("");
        binding.layoutResultview.resultTable.tvR2c5.setBackground(null);
        binding.layoutResultview.resultTable.tvR2c6.setText("");
        binding.layoutResultview.resultTable.tvR2c6.setBackground(null);

        binding.layoutResultview.resultTable.tvR3c1.setText("");
        binding.layoutResultview.resultTable.tvR3c1.setBackground(null);
        binding.layoutResultview.resultTable.tvR3c2.setText("");
        binding.layoutResultview.resultTable.tvR3c2.setBackground(null);
        binding.layoutResultview.resultTable.tvR3c3.setText("");
        binding.layoutResultview.resultTable.tvR3c3.setBackground(null);
        binding.layoutResultview.resultTable.tvR3c4.setText("");
        binding.layoutResultview.resultTable.tvR3c4.setBackground(null);
        binding.layoutResultview.resultTable.tvR3c5.setText("");
        binding.layoutResultview.resultTable.tvR3c5.setBackground(null);
        binding.layoutResultview.resultTable.tvR3c6.setText("");
        binding.layoutResultview.resultTable.tvR3c6.setBackground(null);

        binding.layoutResultview.resultTable.tvR4c1.setText("");
        binding.layoutResultview.resultTable.tvR4c1.setBackground(null);
        binding.layoutResultview.resultTable.tvR4c2.setText("");
        binding.layoutResultview.resultTable.tvR4c2.setBackground(null);
        binding.layoutResultview.resultTable.tvR4c3.setText("");
        binding.layoutResultview.resultTable.tvR4c3.setBackground(null);
        binding.layoutResultview.resultTable.tvR4c4.setText("");
        binding.layoutResultview.resultTable.tvR4c4.setBackground(null);
        binding.layoutResultview.resultTable.tvR4c5.setText("");
        binding.layoutResultview.resultTable.tvR4c5.setBackground(null);
        binding.layoutResultview.resultTable.tvR4c6.setText("");
        binding.layoutResultview.resultTable.tvR4c6.setBackground(null);

        binding.layoutResultview.resultTable.tvR5c1.setText("");
        binding.layoutResultview.resultTable.tvR5c1.setBackground(null);
        binding.layoutResultview.resultTable.tvR5c2.setText("");
        binding.layoutResultview.resultTable.tvR5c2.setBackground(null);
        binding.layoutResultview.resultTable.tvR5c3.setText("");
        binding.layoutResultview.resultTable.tvR5c3.setBackground(null);
        binding.layoutResultview.resultTable.tvR5c4.setText("");
        binding.layoutResultview.resultTable.tvR5c4.setBackground(null);
        binding.layoutResultview.resultTable.tvR5c5.setText("");
        binding.layoutResultview.resultTable.tvR5c5.setBackground(null);
        binding.layoutResultview.resultTable.tvR5c6.setText("");
        binding.layoutResultview.resultTable.tvR5c6.setBackground(null);

    }

    private void setLoseMsg(LinearLayout parentView) {
        LinearLayout child = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_resultitem, null, false);
        TextView tv_episode = child.findViewById(R.id.tv_episode);
//        TextView tv_price = child.findViewById(R.id.tv_price);

        tv_episode.setText(getString(R.string.lose_msg));
        parentView.addView(child);
    }

    private void getNumResult() {
        ReqBasic numResult = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(StringUtil.TAG, "code: " + resultCode);
                Log.d(StringUtil.TAG, "getNumResult: " + resultData.getResult());

                if (resultData.getResult() != null) {

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("fail")) {
                            Toast.makeText(ManualnumAct.this, jo.getString("msg"), Toast.LENGTH_SHORT).show();
                        } else {

                            JSONObject result = new JSONObject(jo.getString("result"));

                            JSONObject gameRes = null;
                            JSONArray game = null;

                            if (result.has("game1result")) {
                                result.getString("game1result");
                                gameRes = new JSONObject(result.getString("game1result"));
                                game = gameRes.getJSONArray("case1");

                                Log.d(StringUtil.TAG, "res1: " + LottoUtil.resultStr(game));
                                if (game.length() > 0) {
                                    LottoUtil.resultAdd(game, binding.layoutResultview.resultTable.tvR1result, getLayoutInflater());
//                                    binding.layoutResultview.resultTable.tvR1result.setText(LottoUtil.resultStr(game));
                                } else {
//                                    binding.layoutResultview.resultTable.tvR1result.setText(getString(R.string.lose_msg));
                                    setLoseMsg(binding.layoutResultview.resultTable.tvR1result);
                                }
                            }

                            if (result.has("game2result")) {
                                result.getString("game2result");
                                gameRes = new JSONObject(result.getString("game2result"));
                                game = gameRes.getJSONArray("case1");

                                Log.d(StringUtil.TAG, "res2: " + LottoUtil.resultStr(game));
                                if (game.length() > 0) {
                                    LottoUtil.resultAdd(game, binding.layoutResultview.resultTable.tvR2result, getLayoutInflater());
                                } else {
                                    setLoseMsg(binding.layoutResultview.resultTable.tvR2result);
                                }
                            }

                            if (result.has("game3result")) {
                                result.getString("game3result");
                                gameRes = new JSONObject(result.getString("game3result"));
                                game = gameRes.getJSONArray("case1");

                                Log.d(StringUtil.TAG, "res3: " + LottoUtil.resultStr(game));
                                if (game.length() > 0) {
                                    LottoUtil.resultAdd(game, binding.layoutResultview.resultTable.tvR3result, getLayoutInflater());
                                } else {
                                    setLoseMsg(binding.layoutResultview.resultTable.tvR3result);
                                }
                            }

                            if (result.has("game4result")) {
                                result.getString("game4result");
                                gameRes = new JSONObject(result.getString("game4result"));
                                game = gameRes.getJSONArray("case1");

                                Log.d(StringUtil.TAG, "res4: " + LottoUtil.resultStr(game));
                                if (game.length() > 0) {
                                    LottoUtil.resultAdd(game, binding.layoutResultview.resultTable.tvR4result, getLayoutInflater());
                                } else {
                                    setLoseMsg(binding.layoutResultview.resultTable.tvR4result);
                                }
                            }

                            if (result.has("game5result")) {
                                result.getString("game5result");
                                gameRes = new JSONObject(result.getString("game5result"));
                                game = gameRes.getJSONArray("case1");

                                Log.d(StringUtil.TAG, "res5: " + LottoUtil.resultStr(game));
                                if (game.length() > 0) {
                                    LottoUtil.resultAdd(game, binding.layoutResultview.resultTable.tvR5result, getLayoutInflater());
                                } else {
                                    setLoseMsg(binding.layoutResultview.resultTable.tvR5result);
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ManualnumAct.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(ManualnumAct.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }

            }
        };

        numResult.addParams("APPCONNECTCODE", "APP");
        numResult.addParams("dbControl", "LottoNumHighRankMatch");
        numResult.addParams("midx", UserPref.getIdx(this));
        numResult.addParams("gametype", "custom");

        if (mNumTable.containsKey("t1")) {
            numResult.addParams("game1", mNumTable.get("t1").toString().replace("[", "").replace("]", ""));
        }

        if (mNumTable.containsKey("t2")) {
            numResult.addParams("game2", mNumTable.get("t2").toString().replace("[", "").replace("]", ""));
        }

        if (mNumTable.containsKey("t3")) {
            numResult.addParams("game3", mNumTable.get("t3").toString().replace("[", "").replace("]", ""));
        }

        if (mNumTable.containsKey("t4")) {
            numResult.addParams("game4", mNumTable.get("t4").toString().replace("[", "").replace("]", ""));
        }

        if (mNumTable.containsKey("t5")) {
            numResult.addParams("game5", mNumTable.get("t5").toString().replace("[", "").replace("]", ""));
        }

        numResult.execute(true, true);

    }


    private void setResultTable() {
        ArrayList<Integer> integers = new ArrayList<>();
        initResultView();
        if (mNumTable.containsKey("t1")) {
            integers = mNumTable.get("t1");
            binding.layoutResultview.resultTable.tvR1c1.setText(String.valueOf(integers.get(0)));
            binding.layoutResultview.resultTable.tvR1c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
            binding.layoutResultview.resultTable.tvR1c2.setText(String.valueOf(integers.get(1)));
            binding.layoutResultview.resultTable.tvR1c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
            binding.layoutResultview.resultTable.tvR1c3.setText(String.valueOf(integers.get(2)));
            binding.layoutResultview.resultTable.tvR1c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
            binding.layoutResultview.resultTable.tvR1c4.setText(String.valueOf(integers.get(3)));
            binding.layoutResultview.resultTable.tvR1c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
            binding.layoutResultview.resultTable.tvR1c5.setText(String.valueOf(integers.get(4)));
            binding.layoutResultview.resultTable.tvR1c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
            binding.layoutResultview.resultTable.tvR1c6.setText(String.valueOf(integers.get(5)));
            binding.layoutResultview.resultTable.tvR1c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
        }

        if (mNumTable.containsKey("t2")) {
            integers = mNumTable.get("t2");
            binding.layoutResultview.resultTable.tvR2c1.setText(String.valueOf(integers.get(0)));
            binding.layoutResultview.resultTable.tvR2c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
            binding.layoutResultview.resultTable.tvR2c2.setText(String.valueOf(integers.get(1)));
            binding.layoutResultview.resultTable.tvR2c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
            binding.layoutResultview.resultTable.tvR2c3.setText(String.valueOf(integers.get(2)));
            binding.layoutResultview.resultTable.tvR2c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
            binding.layoutResultview.resultTable.tvR2c4.setText(String.valueOf(integers.get(3)));
            binding.layoutResultview.resultTable.tvR2c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
            binding.layoutResultview.resultTable.tvR2c5.setText(String.valueOf(integers.get(4)));
            binding.layoutResultview.resultTable.tvR2c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
            binding.layoutResultview.resultTable.tvR2c6.setText(String.valueOf(integers.get(5)));
            binding.layoutResultview.resultTable.tvR2c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
        }

        if (mNumTable.containsKey("t3")) {
            integers = mNumTable.get("t3");
            binding.layoutResultview.resultTable.tvR3c1.setText(String.valueOf(integers.get(0)));
            binding.layoutResultview.resultTable.tvR3c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
            binding.layoutResultview.resultTable.tvR3c2.setText(String.valueOf(integers.get(1)));
            binding.layoutResultview.resultTable.tvR3c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
            binding.layoutResultview.resultTable.tvR3c3.setText(String.valueOf(integers.get(2)));
            binding.layoutResultview.resultTable.tvR3c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
            binding.layoutResultview.resultTable.tvR3c4.setText(String.valueOf(integers.get(3)));
            binding.layoutResultview.resultTable.tvR3c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
            binding.layoutResultview.resultTable.tvR3c5.setText(String.valueOf(integers.get(4)));
            binding.layoutResultview.resultTable.tvR3c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
            binding.layoutResultview.resultTable.tvR3c6.setText(String.valueOf(integers.get(5)));
            binding.layoutResultview.resultTable.tvR3c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
        }

        if (mNumTable.containsKey("t4")) {
            integers = mNumTable.get("t4");
            binding.layoutResultview.resultTable.tvR4c1.setText(String.valueOf(integers.get(0)));
            binding.layoutResultview.resultTable.tvR4c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
            binding.layoutResultview.resultTable.tvR4c2.setText(String.valueOf(integers.get(1)));
            binding.layoutResultview.resultTable.tvR4c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
            binding.layoutResultview.resultTable.tvR4c3.setText(String.valueOf(integers.get(2)));
            binding.layoutResultview.resultTable.tvR4c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
            binding.layoutResultview.resultTable.tvR4c4.setText(String.valueOf(integers.get(3)));
            binding.layoutResultview.resultTable.tvR4c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
            binding.layoutResultview.resultTable.tvR4c5.setText(String.valueOf(integers.get(4)));
            binding.layoutResultview.resultTable.tvR4c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
            binding.layoutResultview.resultTable.tvR4c6.setText(String.valueOf(integers.get(5)));
            binding.layoutResultview.resultTable.tvR4c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
        }

        if (mNumTable.containsKey("t5")) {
            integers = mNumTable.get("t5");
            binding.layoutResultview.resultTable.tvR5c1.setText(String.valueOf(integers.get(0)));
            binding.layoutResultview.resultTable.tvR5c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
            binding.layoutResultview.resultTable.tvR5c2.setText(String.valueOf(integers.get(1)));
            binding.layoutResultview.resultTable.tvR5c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
            binding.layoutResultview.resultTable.tvR5c3.setText(String.valueOf(integers.get(2)));
            binding.layoutResultview.resultTable.tvR5c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
            binding.layoutResultview.resultTable.tvR5c4.setText(String.valueOf(integers.get(3)));
            binding.layoutResultview.resultTable.tvR5c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
            binding.layoutResultview.resultTable.tvR5c5.setText(String.valueOf(integers.get(4)));
            binding.layoutResultview.resultTable.tvR5c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
            binding.layoutResultview.resultTable.tvR5c6.setText(String.valueOf(integers.get(5)));
            binding.layoutResultview.resultTable.tvR5c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
        }
    }

    private void imgUpload(final File img) {
        Toast.makeText(this, "공유이미지 생성 중입니다. 잠시만 기다려 주세요.", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MultipartUtility mu = new MultipartUtility(NetUrls.DOMAIN, "UTF-8");

                    mu.addFormField("APPCONNECTCODE", "APP");
                    mu.addFormField("dbControl", "LottoNumMakeImageUpload");
                    mu.addFilePart("lu_img", img);

                    String res = mu.finish();
                    Log.d(StringUtil.TAG, "run: " + res);

                    if (!StringUtil.isNull(res)) {
                        try {
                            JSONObject jo = new JSONObject(res);
                            final String msg = jo.getString("message");
                            if (jo.getString("result").equalsIgnoreCase("Y")) {
                                imgUrl = NetUrls.IMGDOMAIN + jo.getString("url");
                                Log.d(StringUtil.TAG, "imgUrl: " + imgUrl);
                                sendKakaoLink(imgUrl);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ManualnumAct.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ManualnumAct.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ManualnumAct.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

//                    if (img.exists()) {
//                        img.deleteOnExit();
//                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    private void sendKakaoLink(String imgUrl) {
        // 카카오링크(이미지 공유)
        FeedTemplate params = FeedTemplate.newBuilder(ContentObject.newBuilder("파워로또",
                imgUrl,
                LinkObject.newBuilder().setWebUrl(imgUrl)
                        .setMobileWebUrl(imgUrl).build())
                .setDescrption("수동생성 결과보기 공유\n" + imgUrl)
                .setImageHeight(50)
                .build())
//                        .setSocial(SocialObject.newBuilder().setLikeCount(10).setCommentCount(20)
//                                .setSharedCount(30).setViewCount(40).build())

//                .addButton(new ButtonObject("웹에서 보기", LinkObject.newBuilder().setWebUrl("'https://developers.kakao.com").setMobileWebUrl("'https://developers.kakao.com").build()))
//                .addButton(new ButtonObject("앱에서 보기", LinkObject.newBuilder()
//                        .setWebUrl("'https://developers.kakao.com")
//                        .setMobileWebUrl("'https://developers.kakao.com")
//                        .setAndroidExecutionParams("key1=value1")
//                        .setIosExecutionParams("key1=value1")
//                        .build()))
                .build();

        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");
//                serverCallbackArgs.put("user_id", "${current_user_id}");
//                serverCallbackArgs.put("product_id", "${shared_product_id}");

        KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.d(StringUtil.TAG, "onFailure: " + errorResult);
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                finish();
                break;
            case R.id.btn_deltable:
                // 초기화
                if (numMakeFlag) {
                    Toast.makeText(this, "번호생성 중입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mNumTable.clear();
                binding.ballLayout.tvNum1.setText("?");
                binding.ballLayout.tvNum1.setBackgroundResource(R.drawable.b_ball_grey);
                binding.ballLayout.tvNum2.setText("?");
                binding.ballLayout.tvNum2.setBackgroundResource(R.drawable.b_ball_grey);
                binding.ballLayout.tvNum3.setText("?");
                binding.ballLayout.tvNum3.setBackgroundResource(R.drawable.b_ball_grey);
                binding.ballLayout.tvNum4.setText("?");
                binding.ballLayout.tvNum4.setBackgroundResource(R.drawable.b_ball_grey);
                binding.ballLayout.tvNum5.setText("?");
                binding.ballLayout.tvNum5.setBackgroundResource(R.drawable.b_ball_grey);
                binding.ballLayout.tvNum6.setText("?");
                binding.ballLayout.tvNum6.setBackgroundResource(R.drawable.b_ball_grey);


                binding.numTable.tvR1c1.setText("?");
                binding.numTable.tvR1c1.setBackgroundResource(R.drawable.s_ball_grey);
                binding.numTable.tvR1c2.setText("?");
                binding.numTable.tvR1c2.setBackgroundResource(R.drawable.s_ball_grey);
                binding.numTable.tvR1c3.setText("?");
                binding.numTable.tvR1c3.setBackgroundResource(R.drawable.s_ball_grey);
                binding.numTable.tvR1c4.setText("?");
                binding.numTable.tvR1c4.setBackgroundResource(R.drawable.s_ball_grey);
                binding.numTable.tvR1c5.setText("?");
                binding.numTable.tvR1c5.setBackgroundResource(R.drawable.s_ball_grey);
                binding.numTable.tvR1c6.setText("?");
                binding.numTable.tvR1c6.setBackgroundResource(R.drawable.s_ball_grey);

                binding.numTable.tvR2c1.setText("");
                binding.numTable.tvR2c1.setBackground(null);
                binding.numTable.tvR2c2.setText("");
                binding.numTable.tvR2c2.setBackground(null);
                binding.numTable.tvR2c3.setText("");
                binding.numTable.tvR2c3.setBackground(null);
                binding.numTable.tvR2c4.setText("");
                binding.numTable.tvR2c4.setBackground(null);
                binding.numTable.tvR2c5.setText("");
                binding.numTable.tvR2c5.setBackground(null);
                binding.numTable.tvR2c6.setText("");
                binding.numTable.tvR2c6.setBackground(null);

                binding.numTable.tvR3c1.setText("");
                binding.numTable.tvR3c1.setBackground(null);
                binding.numTable.tvR3c2.setText("");
                binding.numTable.tvR3c2.setBackground(null);
                binding.numTable.tvR3c3.setText("");
                binding.numTable.tvR3c3.setBackground(null);
                binding.numTable.tvR3c4.setText("");
                binding.numTable.tvR3c4.setBackground(null);
                binding.numTable.tvR3c5.setText("");
                binding.numTable.tvR3c5.setBackground(null);
                binding.numTable.tvR3c6.setText("");
                binding.numTable.tvR3c6.setBackground(null);

                binding.numTable.tvR4c1.setText("");
                binding.numTable.tvR4c1.setBackground(null);
                binding.numTable.tvR4c2.setText("");
                binding.numTable.tvR4c2.setBackground(null);
                binding.numTable.tvR4c3.setText("");
                binding.numTable.tvR4c3.setBackground(null);
                binding.numTable.tvR4c4.setText("");
                binding.numTable.tvR4c4.setBackground(null);
                binding.numTable.tvR4c5.setText("");
                binding.numTable.tvR4c5.setBackground(null);
                binding.numTable.tvR4c6.setText("");
                binding.numTable.tvR4c6.setBackground(null);

                binding.numTable.tvR5c1.setText("");
                binding.numTable.tvR5c1.setBackground(null);
                binding.numTable.tvR5c2.setText("");
                binding.numTable.tvR5c2.setBackground(null);
                binding.numTable.tvR5c3.setText("");
                binding.numTable.tvR5c3.setBackground(null);
                binding.numTable.tvR5c4.setText("");
                binding.numTable.tvR5c4.setBackground(null);
                binding.numTable.tvR5c5.setText("");
                binding.numTable.tvR5c5.setBackground(null);
                binding.numTable.tvR5c6.setText("");
                binding.numTable.tvR5c6.setBackground(null);

                Log.d(StringUtil.TAG, "randTable: " + mNumTable.toString());
                break;
            case R.id.btn_showresult:
                // 결과보기
                if (numMakeFlag) {
                    Toast.makeText(this, "번호생성 중입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mNumTable.isEmpty()) {
                    Toast.makeText(this, "생성 번호가 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                setResultTable();
                getNumResult();
                binding.ivPopback.setVisibility(View.VISIBLE);
                binding.layoutResultview.getRoot().setVisibility(View.VISIBLE);
                break;
            case R.id.btn_resviewclose:
                binding.ivPopback.setVisibility(View.GONE);
                binding.layoutResultview.getRoot().setVisibility(View.GONE);
                break;
            case R.id.btn_savegallery:
                // 결과 이미지 저장
                if (isReqPermission()) {
                    reqPermission();
                } else {

                    if (!isMekeFile) {
                        isMekeFile = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.show();
                            }
                        });

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PowerLotto";
                                String filename = System.currentTimeMillis() + "_manualresult.jpg";
                                File dir = new File(path);
                                File f = new File(path, filename);
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }

                                try {
                                    f.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                Log.d(StringUtil.TAG, "create file: " + f.exists());
                                binding.layoutResultview.resultTable.getRoot().buildDrawingCache();

                                FileOutputStream fos;

                                try {
//                        Bitmap bitmap = binding.layoutResultview.resultTable.getRoot().getDrawingCache();
                                    Bitmap bitmap = Bitmap.createBitmap(binding.layoutResultview.resultTable.getRoot().getMeasuredWidth(), binding.layoutResultview.resultTable.getRoot().getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(bitmap);
                                    binding.layoutResultview.resultTable.getRoot().draw(canvas);

                                    fos = new FileOutputStream(f);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                    fos.close();

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    Toast.makeText(ManualnumAct.this, "파일을 저장하고 있습니다. 천천히 시도해주세요.", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    Toast.makeText(ManualnumAct.this, "파일을 저장하고 있습니다. 천천히 시도해주세요.", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }

                                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                Uri contentUri = Uri.fromFile(f);
                                mediaScanIntent.setData(contentUri);
                                sendBroadcast(mediaScanIntent);
                            }
                        });

                        Toast.makeText(ManualnumAct.this, "저장 되었습니다.", Toast.LENGTH_SHORT).show();
                        isMekeFile = false;
                        if (pd.isShowing()) {
                            pd.dismiss();
                        }
                    } else {
                        Toast.makeText(ManualnumAct.this, "파일 생성중입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
            case R.id.btn_linkakao:
                // 카카오 링크로 보내기
                if (isReqPermission()) {
                    reqPermission();
                } else {
                    if (!isMekeFile) {
                        isMekeFile = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.show();
                            }
                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ManualnumAct.this, "공유이미지 생성 중입니다. 잠시만 기다려 주세요.", Toast.LENGTH_SHORT).show();
                                File tmpFile = null;
                                try {
                                    tmpFile = File.createTempFile("uploadimg", ".jpg", getCacheDir());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                binding.layoutResultview.resultTable.getRoot().buildDrawingCache();

                                FileOutputStream fos;

                                try {
//                        Bitmap bitmap = binding.layoutResultview.resultTable.getRoot().getDrawingCache();
                                    Bitmap bitmap = Bitmap.createBitmap(binding.layoutResultview.resultTable.getRoot().getMeasuredWidth(), binding.layoutResultview.resultTable.getRoot().getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(bitmap);
                                    binding.layoutResultview.resultTable.getRoot().draw(canvas);

                                    fos = new FileOutputStream(tmpFile);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                    fos.close();
                                    isMekeFile = false;
                                    if (pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    if (pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                    isMekeFile = false;
                                    Toast.makeText(ManualnumAct.this, "천천히 시도해주세요.", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    if (pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                    isMekeFile = false;
                                    Toast.makeText(ManualnumAct.this, "천천히 시도해주세요.", Toast.LENGTH_SHORT).show();
                                }

                                imgUpload(tmpFile);
                            }
                        });
                    } else {
                        Toast.makeText(ManualnumAct.this, "파일 생성중입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                break;
//            case R.id.ball_layout:
            case R.id.btn_start_number:
                if (numMakeFlag) {
                    Toast.makeText(this, "번호생성 중입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                initMakenUm();
                binding.ivPopback.setVisibility(View.VISIBLE);
                binding.layoutMakenumview.getRoot().setVisibility(View.VISIBLE);
                break;

            case R.id.btn_savemanualnum:
                if (manualNums.size() < 6) {
                    Toast.makeText(this, "번호를 생성해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveNum();
                break;
            case R.id.btn_makenumclose:
                binding.ivPopback.setVisibility(View.GONE);
                binding.layoutMakenumview.getRoot().setVisibility(View.GONE);
                break;
            case R.id.btn_reset:
                initMakenUm();
                break;
            case R.id.btn_complete:
                Log.d(StringUtil.TAG, "makenumList.size(): " + makenumList.size());
                if (makenumList.size() < 6) {
                    Toast.makeText(this, "번호 선택을 완료해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (manualNums.size() > 0) {
                    manualNums.clear();
                }

                numMakeFlag = true;
                binding.ivPopback.setVisibility(View.GONE);
                binding.layoutMakenumview.getRoot().setVisibility(View.GONE);
                Collections.sort(makenumList, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1.compareTo(o2);
                    }
                });

                if (makenumList.size() == 6) {
                    ballAniMakeData(makenumList);
                }

                // 애니메이션?
                break;
            case R.id.ball1:
                setNumlist(1);
//                Toast.makeText(this, "1번 공", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ball2:
                setNumlist(2);
                break;
            case R.id.ball3:
                setNumlist(3);
                break;
            case R.id.ball4:
                setNumlist(4);
                break;
            case R.id.ball5:
                setNumlist(5);
                break;
            case R.id.ball6:
                setNumlist(6);
                break;
            case R.id.ball7:
                setNumlist(7);
                break;
            case R.id.ball8:
                setNumlist(8);
                break;
            case R.id.ball9:
                setNumlist(9);
                break;
            case R.id.ball10:
                setNumlist(10);
                break;
            case R.id.ball11:
                setNumlist(11);
                break;
            case R.id.ball12:
                setNumlist(12);
                break;
            case R.id.ball13:
                setNumlist(13);
                break;
            case R.id.ball14:
                setNumlist(14);
                break;
            case R.id.ball15:
                setNumlist(15);
                break;
            case R.id.ball16:
                setNumlist(16);
                break;
            case R.id.ball17:
                setNumlist(17);
                break;
            case R.id.ball18:
                setNumlist(18);
                break;
            case R.id.ball19:
                setNumlist(19);
                break;
            case R.id.ball20:
                setNumlist(20);
                break;
            case R.id.ball21:
                setNumlist(21);
                break;
            case R.id.ball22:
                setNumlist(22);
                break;
            case R.id.ball23:
                setNumlist(23);
                break;
            case R.id.ball24:
                setNumlist(24);
                break;
            case R.id.ball25:
                setNumlist(25);
                break;
            case R.id.ball26:
                setNumlist(26);
                break;
            case R.id.ball27:
                setNumlist(27);
                break;
            case R.id.ball28:
                setNumlist(28);
                break;
            case R.id.ball29:
                setNumlist(29);
                break;
            case R.id.ball30:
                setNumlist(30);
                break;
            case R.id.ball31:
                setNumlist(31);
                break;
            case R.id.ball32:
                setNumlist(32);
                break;
            case R.id.ball33:
                setNumlist(33);
                break;
            case R.id.ball34:
                setNumlist(34);
                break;
            case R.id.ball35:
                setNumlist(35);
                break;
            case R.id.ball36:
                setNumlist(36);
                break;
            case R.id.ball37:
                setNumlist(37);
                break;
            case R.id.ball38:
                setNumlist(38);
                break;
            case R.id.ball39:
                setNumlist(39);
                break;
            case R.id.ball40:
                setNumlist(40);
                break;
            case R.id.ball41:
                setNumlist(41);
                break;
            case R.id.ball42:
                setNumlist(42);
                break;
            case R.id.ball43:
                setNumlist(43);
                break;
            case R.id.ball44:
                setNumlist(44);
                break;
            case R.id.ball45:
                setNumlist(45);
                break;
        }
    }

    private void reqPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 0);
        }
    }

    private boolean isReqPermission() {
        // 필요권한 ( 전화 걸기 및 관리, 메세지 전송 및 보기, 주소록 액세스, 사진 및 미디어 파일 액세스, 위치정보)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (
                    checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
