package kr.core.powerlotto.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.core.view.GravityCompat;
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
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.SocialObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.rey.material.widget.ProgressView;

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
import kr.core.powerlotto.databinding.ActivityRandnumBinding;
import kr.core.powerlotto.insidedata.UserPref;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.MultipartUtility;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.popup.MyProgress;
import kr.core.powerlotto.util.LayoutWebView;
import kr.core.powerlotto.util.LottoUtil;
import kr.core.powerlotto.util.StringUtil;

public class RandnumAct extends BaseAct implements View.OnClickListener {

    ActivityRandnumBinding binding;

    ArrayList<Integer> randNums = new ArrayList<>();
    HashMap<String, ArrayList<Integer>> randTable = new HashMap<>();

    boolean numMakeFlag = false;
    boolean isMekeFile = false;
    String imgUrl = "";

    ProgressDialog pd;

    Activity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_randnum);
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
        binding.btnSidemenu.setOnClickListener(this);
        binding.btnMakerandnum.setOnClickListener(this);
        binding.btnDeltable.setOnClickListener(this);
        binding.btnShowresult.setOnClickListener(this);

        binding.layoutResultview.btnResviewclose.setOnClickListener(this);
        binding.layoutResultview.btnLinkakao.setOnClickListener(this);
        binding.layoutResultview.btnSavegallery.setOnClickListener(this);

        binding.layoutResultview.tvResviewtitle.setText("번호 랜덤 생성 결과보기");

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
//                                Toast.makeText(RandnumAct.this, "연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
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

    private void setTextTypeface() {

        binding.ballLayout.tvNum1.setTypeface(app.jalnan);
        binding.ballLayout.tvNum2.setTypeface(app.jalnan);
        binding.ballLayout.tvNum3.setTypeface(app.jalnan);
        binding.ballLayout.tvNum4.setTypeface(app.jalnan);
        binding.ballLayout.tvNum5.setTypeface(app.jalnan);
        binding.ballLayout.tvNum6.setTypeface(app.jalnan);

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

    }


    @Override
    public void onBackPressed() {
        if (binding.layoutResultview.getRoot().getVisibility() == View.VISIBLE) {
            binding.layoutResultview.getRoot().setVisibility(View.GONE);
            binding.ivPopback.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private void setLoseMsg(LinearLayout parentView) {
        LinearLayout child = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_resultitem, null, false);
        TextView tv_episode = child.findViewById(R.id.tv_episode);

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
                            Toast.makeText(RandnumAct.this, jo.getString("msg"), Toast.LENGTH_SHORT).show();
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
                                } else {
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
                        Toast.makeText(RandnumAct.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(RandnumAct.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }

            }
        };

        numResult.addParams("APPCONNECTCODE", "APP");
        numResult.addParams("dbControl", "LottoNumHighRankMatch");
        numResult.addParams("midx", UserPref.getIdx(this));
        numResult.addParams("gametype", "rand");

        if (randTable.containsKey("t1")) {
            numResult.addParams("game1", randTable.get("t1").toString().replace("[", "").replace("]", ""));
        }

        if (randTable.containsKey("t2")) {
            numResult.addParams("game2", randTable.get("t2").toString().replace("[", "").replace("]", ""));
        }

        if (randTable.containsKey("t3")) {
            numResult.addParams("game3", randTable.get("t3").toString().replace("[", "").replace("]", ""));
        }

        if (randTable.containsKey("t4")) {
            numResult.addParams("game4", randTable.get("t4").toString().replace("[", "").replace("]", ""));
        }

        if (randTable.containsKey("t5")) {
            numResult.addParams("game5", randTable.get("t5").toString().replace("[", "").replace("]", ""));
        }

        numResult.execute(true, true);

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

                randNums.add(res.get(idx));

                view.setText(String.valueOf(randNums.get(idx)));
                view.setBackgroundResource(LottoUtil.getBallResource(randNums.get(idx), true));
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

    private void setResultTable() {
        ArrayList<Integer> integers = new ArrayList<>();
        initResultView();
        if (randTable.containsKey("t1")) {
            integers = randTable.get("t1");
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

        if (randTable.containsKey("t2")) {
            integers = randTable.get("t2");
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

        if (randTable.containsKey("t3")) {
            integers = randTable.get("t3");
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

        if (randTable.containsKey("t4")) {
            integers = randTable.get("t4");
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

        if (randTable.containsKey("t5")) {
            integers = randTable.get("t5");
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
                                        Toast.makeText(RandnumAct.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RandnumAct.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RandnumAct.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
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

    private void initResultView() {

//        binding.layoutResultview.resultTable.tvR1result.removeAllViews();
//        binding.layoutResultview.resultTable.tvR2result.removeAllViews();
//        binding.layoutResultview.resultTable.tvR3result.removeAllViews();
//        binding.layoutResultview.resultTable.tvR4result.removeAllViews();
//        binding.layoutResultview.resultTable.tvR5result.removeAllViews();

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

    private void ballAniMakeData(final ArrayList<Integer> intArr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (randNums.size() < 6) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    switch (randNums.size()) {
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
                    if (randNums.size() == 6) {
                        Log.d(StringUtil.TAG, "randNums.size(): " + randNums.size());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<Integer> integers = new ArrayList<>();
                                if (randTable.isEmpty()) {
                                    randTable.put("t1", randNums);

                                    integers = randTable.get("t1");
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
                                    Log.d(StringUtil.TAG, "!randTable.isEmpty(): ");
                                    if (randTable.containsKey("t1")) {
                                        if (randTable.containsKey("t2")) {
                                            if (randTable.containsKey("t3")) {
                                                if (randTable.containsKey("t4")) {
                                                    randTable.put("t5", randNums);
                                                    integers = randTable.get("t5");
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
                                                    randTable.put("t4", randNums);
                                                    integers = randTable.get("t4");
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
                                                randTable.put("t3", randNums);
                                                integers = randTable.get("t3");
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
                                            randTable.put("t2", randNums);
                                            integers = randTable.get("t2");
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
                                        randTable.put("t1", randNums);
                                        integers = randTable.get("t1");
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
                    Log.d(StringUtil.TAG, "onClick: " + randNums.size());
                }
                numMakeFlag = false;
            }
        }).start();
        randNums = new ArrayList<>();
    }

    private void makeRandNums() {

        ArrayList<Integer> randNums = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
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

        ballAniMakeData(randNums);

        Log.d(StringUtil.TAG, "makeRandNums: " + randNums);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                finish();
                break;
            case R.id.btn_makerandnum:
                if (randTable.containsKey("t5")) {
                    Toast.makeText(this, "5게임 생성 완료. 번호를 지워주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (numMakeFlag) {
                    Toast.makeText(this, "번호생성 중입니다.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    numMakeFlag = true;
                    initBalls();
                    makeRandNums();
                    Log.d(StringUtil.TAG, "randTable: " + randTable.toString());
                }
                break;
            case R.id.btn_deltable:
                if (numMakeFlag) {
                    Toast.makeText(this, "번호생성 중입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                randTable.clear();
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

                Log.d(StringUtil.TAG, "randTable: " + randTable.toString());
                break;
            case R.id.btn_showresult:
                if (numMakeFlag) {
                    Toast.makeText(this, "번호생성 중입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (randTable.isEmpty()) {
                    Toast.makeText(this, "생성 번호가 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                setResultTable();
                getNumResult();
                binding.ivPopback.setVisibility(View.VISIBLE);
                binding.layoutResultview.getRoot().setVisibility(View.VISIBLE);
                break;
            case R.id.btn_linkakao:
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
                                Toast.makeText(RandnumAct.this, "공유이미지 생성 중입니다. 잠시만 기다려 주세요.", Toast.LENGTH_SHORT).show();
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
                                    isMekeFile = false;
                                    if (pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                    Toast.makeText(RandnumAct.this, "천천히 시도해주세요.", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    isMekeFile = false;
                                    if (pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                    Toast.makeText(RandnumAct.this, "천천히 시도해주세요.", Toast.LENGTH_SHORT).show();
                                }

                                imgUpload(tmpFile);
                            }
                        });
                    } else {
                        Toast.makeText(RandnumAct.this, "파일 생성중입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
            case R.id.btn_savegallery:
                if (isReqPermission()) {
                    reqPermission();
                } else {

//                    final MyProgress pDlg = new MyProgress(this);
//                    pDlg.setCanceledOnTouchOutside(false);
//                    pDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            pDlg.show();
//                        }
//                    });

//                    new MakeFile().execute();

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
                                String filename = System.currentTimeMillis() + "_randresult.jpg";
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
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Toast.makeText(RandnumAct.this, "저장 되었습니다.", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    Toast.makeText(RandnumAct.this, "파일을 저장하고 있습니다. 천천히 시도해주세요.", Toast.LENGTH_SHORT).show();

                                } catch (IOException e) {
                                    Toast.makeText(RandnumAct.this, "파일을 저장하고 있습니다. 천천히 시도해주세요.", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }

                                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                Uri contentUri = Uri.fromFile(f);
                                mediaScanIntent.setData(contentUri);
                                sendBroadcast(mediaScanIntent);
                            }
                        });

                        Toast.makeText(RandnumAct.this, "저장 되었습니다.", Toast.LENGTH_SHORT).show();
                        isMekeFile = false;

                        if (pd.isShowing()) {
                            pd.dismiss();
                        }

                    } else {
                        Toast.makeText(RandnumAct.this, "파일 생성중입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }


                }
                break;
            case R.id.btn_resviewclose:
                binding.ivPopback.setVisibility(View.GONE);
                binding.layoutResultview.getRoot().setVisibility(View.GONE);
                break;
        }
    }

    private void sendKakaoLink(String imgUrl) {
        // 카카오링크(이미지 공유)
        FeedTemplate params = FeedTemplate.newBuilder(ContentObject.newBuilder("파워로또",
                imgUrl,
                LinkObject.newBuilder().setWebUrl(imgUrl)
                        .setMobileWebUrl(imgUrl).build())
                .setDescrption("랜덤생성 결과보기 공유\n" + imgUrl)
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

    public class MakeFile extends AsyncTask<Void, Void, Void> {

        //        MyProgress pDlg;
        ProgressDialog pDlg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDlg = new ProgressDialog(RandnumAct.this);
//            pDlg = new MyProgress(getApplicationContext());
            pDlg.setCanceledOnTouchOutside(false);
            pDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pDlg.show();
                }
            });


        }

        @Override
        protected Void doInBackground(Void... voids) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PowerLotto";
                    String filename = System.currentTimeMillis() + "_randresult.jpg";
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
                        Toast.makeText(RandnumAct.this, "저장 되었습니다.", Toast.LENGTH_SHORT).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        if (pDlg.isShowing()) {
                            pDlg.dismiss();
                        }
                        Toast.makeText(RandnumAct.this, "저장중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        if (pDlg.isShowing()) {
                            pDlg.dismiss();
                        }
                        Toast.makeText(RandnumAct.this, "저장중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            if (pDlg.isShowing()) {
//                pDlg.dismiss();
//            }
        }
    }

}
