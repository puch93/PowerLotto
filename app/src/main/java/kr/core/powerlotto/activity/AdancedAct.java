package kr.core.powerlotto.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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

import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.onestore.iap.api.PurchaseClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kr.core.powerlotto.R;
import kr.core.powerlotto.databinding.ActivityAdvancedBinding;
import kr.core.powerlotto.insidedata.UserPref;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.MultipartUtility;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.util.LottoUtil;
import kr.core.powerlotto.util.StringUtil;

public class AdancedAct extends BaseAct implements View.OnClickListener {

    ActivityAdvancedBinding binding;

    ArrayList<Integer> randNums = new ArrayList<>();
    HashMap<String, ArrayList<Integer>> randTable = new HashMap<>();

    String makeMode = "";
    boolean numMakeFlag = false;
    boolean isPay = false;
    boolean isMekeFile = false;
    ProgressDialog pd;

    String imgUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_advanced);

        binding.topText1.setTypeface(app.jalnan);
        binding.topText2.setTypeface(app.jalnan);

        pd = new ProgressDialog(this);
        pd.setMessage("파일 생성중입니다...");
        pd.setCanceledOnTouchOutside(false);

        setTextTypeface();

        binding.btnClose.setOnClickListener(this);
        binding.btnSidemenu.setOnClickListener(this);

        binding.btnOddeven.setOnClickListener(this);
        binding.btnCombination.setOnClickListener(this);
        binding.btnWinning2.setOnClickListener(this);
        binding.btnRangestatistics.setOnClickListener(this);

        binding.btnMakenum.setOnClickListener(this);
        binding.btnShowresult.setOnClickListener(this);

        binding.layoutResultview.btnResviewclose.setOnClickListener(this);
        binding.layoutResultview.btnLinkakao.setOnClickListener(this);
        binding.layoutResultview.btnSavegallery.setOnClickListener(this);

        // 결제 체크
//        checkTicket();
        // 결제 제거
//        isPay = true;

        binding.layoutResultview.tvResviewtitle.setText("번호 고급 생성 결과보기");
        setBtnSelected(0);

        binding.ivPopback.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (binding.ivPopback.getVisibility() == View.VISIBLE){
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void setTextTypeface(){

        binding.ballLayout.tvNum1.setTypeface(app.jalnan);
        binding.ballLayout.tvNum2.setTypeface(app.jalnan);
        binding.ballLayout.tvNum3.setTypeface(app.jalnan);
        binding.ballLayout.tvNum4.setTypeface(app.jalnan);
        binding.ballLayout.tvNum5.setTypeface(app.jalnan);
        binding.ballLayout.tvNum6.setTypeface(app.jalnan);

        binding.table.tvR1c1.setTypeface(app.jalnan);
        binding.table.tvR1c2.setTypeface(app.jalnan);
        binding.table.tvR1c3.setTypeface(app.jalnan);
        binding.table.tvR1c4.setTypeface(app.jalnan);
        binding.table.tvR1c5.setTypeface(app.jalnan);
        binding.table.tvR1c6.setTypeface(app.jalnan);

        binding.table.tvR2c1.setTypeface(app.jalnan);
        binding.table.tvR2c2.setTypeface(app.jalnan);
        binding.table.tvR2c3.setTypeface(app.jalnan);
        binding.table.tvR2c4.setTypeface(app.jalnan);
        binding.table.tvR2c5.setTypeface(app.jalnan);
        binding.table.tvR2c6.setTypeface(app.jalnan);

        binding.table.tvR3c1.setTypeface(app.jalnan);
        binding.table.tvR3c2.setTypeface(app.jalnan);
        binding.table.tvR3c3.setTypeface(app.jalnan);
        binding.table.tvR3c4.setTypeface(app.jalnan);
        binding.table.tvR3c5.setTypeface(app.jalnan);
        binding.table.tvR3c6.setTypeface(app.jalnan);

        binding.table.tvR4c1.setTypeface(app.jalnan);
        binding.table.tvR4c2.setTypeface(app.jalnan);
        binding.table.tvR4c3.setTypeface(app.jalnan);
        binding.table.tvR4c4.setTypeface(app.jalnan);
        binding.table.tvR4c5.setTypeface(app.jalnan);
        binding.table.tvR4c6.setTypeface(app.jalnan);

        binding.table.tvR5c1.setTypeface(app.jalnan);
        binding.table.tvR5c2.setTypeface(app.jalnan);
        binding.table.tvR5c3.setTypeface(app.jalnan);
        binding.table.tvR5c4.setTypeface(app.jalnan);
        binding.table.tvR5c5.setTypeface(app.jalnan);
        binding.table.tvR5c6.setTypeface(app.jalnan);

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

    private void setBtnSelected(int idx) {
        binding.llGuidetextArea.setVisibility(View.VISIBLE);
        switch (idx) {
            case 0:
                makeMode = "0";
                binding.btnOddeven.setSelected(true);
                binding.btnCombination.setSelected(false);
                binding.btnWinning2.setSelected(false);
                binding.btnRangestatistics.setSelected(false);
                binding.tvGuidetext1.setText(getResources().getString(R.string.advanced_oddeven1));
                binding.tvGuidetext2.setText(getResources().getString(R.string.advanced_oddeven2));
                break;
            case 1:
                makeMode = "1";
                binding.btnOddeven.setSelected(false);
                binding.btnCombination.setSelected(true);
                binding.btnWinning2.setSelected(false);
                binding.btnRangestatistics.setSelected(false);
                binding.tvGuidetext1.setText(getResources().getString(R.string.advanced_combination1));
                binding.tvGuidetext2.setText(getResources().getString(R.string.advanced_combination2));
                break;
            case 2:
                makeMode = "2";
                binding.btnOddeven.setSelected(false);
                binding.btnCombination.setSelected(false);
                binding.btnWinning2.setSelected(true);
                binding.btnRangestatistics.setSelected(false);
                binding.tvGuidetext1.setText(getResources().getString(R.string.advanced_winning2_1));
                binding.tvGuidetext2.setText(getResources().getString(R.string.advanced_winning2_2));
                break;
            case 3:
                makeMode = "3";
                binding.btnOddeven.setSelected(false);
                binding.btnCombination.setSelected(false);
                binding.btnWinning2.setSelected(false);
                binding.btnRangestatistics.setSelected(true);
                binding.tvGuidetext1.setText(getResources().getString(R.string.advanced_range1));
                binding.tvGuidetext2.setText(getResources().getString(R.string.advanced_range2));
                break;
        }
    }

    private void initBalls(){
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

    private void makeNumAni(final Techniques techniques, final long duration, final TextView view, final JSONArray res, final int idx) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (!isFinishing()) {
                    app.soundPool.play(app.soundId, 1f, 1f, 0, 0, 1f);
                }

                YoYo.with(techniques)
                        .duration(duration)
                        .playOn(view);

                try {
                    randNums.add(res.getInt(idx));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                view.setText(String.valueOf(randNums.get(idx)));
                view.setBackgroundResource(LottoUtil.getBallResource(randNums.get(idx), true));
            }
        });

    }

    private void makeHighNum() {
        ReqBasic makeNum = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(StringUtil.TAG, "code: " + resultCode);
                Log.d(StringUtil.TAG, "makeHighNum: " + resultData.getResult());

                if (resultData.getResult() != null) {

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());
                        Object res = jo.get("result");

                        if (jo.has("result")) {
                            if (res instanceof JSONArray) {
                                JSONArray result = new JSONArray(jo.getString("result"));

                                if (result.length() > 0) {
                                    initBalls();
                                    ballAniMakeData(result);
                                }
                            }
                        } else {
                            Toast.makeText(AdancedAct.this, getString(R.string.net_errmsg) + "\n문제 : 수신 값이 없음", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(AdancedAct.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(AdancedAct.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }

            }
        };

        makeNum.addParams("APPCONNECTCODE", "APP");
        makeNum.addParams("dbControl", "LottoNumHighRankMake");
        makeNum.addParams("mode", makeMode);
        makeNum.execute(true, true);
    }

    private void ballAniMakeData(final JSONArray ja) {
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
                            makeNumAni(Techniques.SlideInDown, 700, binding.ballLayout.tvNum1, ja, 0);
//                            ZoomInUp
                            break;
                        case 1:
                            makeNumAni(Techniques.SlideInDown, 700, binding.ballLayout.tvNum2, ja, 1);
                            break;
                        case 2:
                            makeNumAni(Techniques.SlideInDown, 700, binding.ballLayout.tvNum3, ja, 2);
                            break;
                        case 3:
                            makeNumAni(Techniques.SlideInDown, 700, binding.ballLayout.tvNum4, ja, 3);
                            break;
                        case 4:
                            makeNumAni(Techniques.SlideInDown, 700, binding.ballLayout.tvNum5, ja, 4);
                            break;
                        case 5:
                            makeNumAni(Techniques.SlideInDown, 700, binding.ballLayout.tvNum6, ja, 5);
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
                                    binding.table.tvR1c1.setText(String.valueOf(integers.get(0)));
                                    binding.table.tvR1c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
                                    binding.table.tvR1c2.setText(String.valueOf(integers.get(1)));
                                    binding.table.tvR1c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
                                    binding.table.tvR1c3.setText(String.valueOf(integers.get(2)));
                                    binding.table.tvR1c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
                                    binding.table.tvR1c4.setText(String.valueOf(integers.get(3)));
                                    binding.table.tvR1c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
                                    binding.table.tvR1c5.setText(String.valueOf(integers.get(4)));
                                    binding.table.tvR1c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
                                    binding.table.tvR1c6.setText(String.valueOf(integers.get(5)));
                                    binding.table.tvR1c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));

                                } else {
                                    Log.d(StringUtil.TAG, "!randTable.isEmpty(): ");
                                    if (randTable.containsKey("t1")) {
                                        if (randTable.containsKey("t2")) {
                                            if (randTable.containsKey("t3")) {
                                                if (randTable.containsKey("t4")) {
                                                    randTable.put("t5", randNums);
                                                    integers = randTable.get("t5");
                                                    binding.table.tvR5c1.setText(String.valueOf(integers.get(0)));
                                                    binding.table.tvR5c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
                                                    binding.table.tvR5c2.setText(String.valueOf(integers.get(1)));
                                                    binding.table.tvR5c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
                                                    binding.table.tvR5c3.setText(String.valueOf(integers.get(2)));
                                                    binding.table.tvR5c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
                                                    binding.table.tvR5c4.setText(String.valueOf(integers.get(3)));
                                                    binding.table.tvR5c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
                                                    binding.table.tvR5c5.setText(String.valueOf(integers.get(4)));
                                                    binding.table.tvR5c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
                                                    binding.table.tvR5c6.setText(String.valueOf(integers.get(5)));
                                                    binding.table.tvR5c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
                                                } else {
                                                    randTable.put("t4", randNums);
                                                    integers = randTable.get("t4");
                                                    binding.table.tvR4c1.setText(String.valueOf(integers.get(0)));
                                                    binding.table.tvR4c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
                                                    binding.table.tvR4c2.setText(String.valueOf(integers.get(1)));
                                                    binding.table.tvR4c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
                                                    binding.table.tvR4c3.setText(String.valueOf(integers.get(2)));
                                                    binding.table.tvR4c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
                                                    binding.table.tvR4c4.setText(String.valueOf(integers.get(3)));
                                                    binding.table.tvR4c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
                                                    binding.table.tvR4c5.setText(String.valueOf(integers.get(4)));
                                                    binding.table.tvR4c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
                                                    binding.table.tvR4c6.setText(String.valueOf(integers.get(5)));
                                                    binding.table.tvR4c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
                                                }
                                            } else {
                                                randTable.put("t3", randNums);
                                                integers = randTable.get("t3");
                                                binding.table.tvR3c1.setText(String.valueOf(integers.get(0)));
                                                binding.table.tvR3c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
                                                binding.table.tvR3c2.setText(String.valueOf(integers.get(1)));
                                                binding.table.tvR3c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
                                                binding.table.tvR3c3.setText(String.valueOf(integers.get(2)));
                                                binding.table.tvR3c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
                                                binding.table.tvR3c4.setText(String.valueOf(integers.get(3)));
                                                binding.table.tvR3c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
                                                binding.table.tvR3c5.setText(String.valueOf(integers.get(4)));
                                                binding.table.tvR3c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
                                                binding.table.tvR3c6.setText(String.valueOf(integers.get(5)));
                                                binding.table.tvR3c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
                                            }
                                        } else {
                                            randTable.put("t2", randNums);
                                            integers = randTable.get("t2");
                                            binding.table.tvR2c1.setText(String.valueOf(integers.get(0)));
                                            binding.table.tvR2c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
                                            binding.table.tvR2c2.setText(String.valueOf(integers.get(1)));
                                            binding.table.tvR2c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
                                            binding.table.tvR2c3.setText(String.valueOf(integers.get(2)));
                                            binding.table.tvR2c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
                                            binding.table.tvR2c4.setText(String.valueOf(integers.get(3)));
                                            binding.table.tvR2c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
                                            binding.table.tvR2c5.setText(String.valueOf(integers.get(4)));
                                            binding.table.tvR2c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
                                            binding.table.tvR2c6.setText(String.valueOf(integers.get(5)));
                                            binding.table.tvR2c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
                                        }
                                    } else {
                                        randTable.put("t1", randNums);
                                        integers = randTable.get("t1");
                                        binding.table.tvR1c1.setText(String.valueOf(integers.get(0)));
                                        binding.table.tvR1c1.setBackgroundResource(LottoUtil.getBallResource(integers.get(0), false));
                                        binding.table.tvR1c2.setText(String.valueOf(integers.get(1)));
                                        binding.table.tvR1c2.setBackgroundResource(LottoUtil.getBallResource(integers.get(1), false));
                                        binding.table.tvR1c3.setText(String.valueOf(integers.get(2)));
                                        binding.table.tvR1c3.setBackgroundResource(LottoUtil.getBallResource(integers.get(2), false));
                                        binding.table.tvR1c4.setText(String.valueOf(integers.get(3)));
                                        binding.table.tvR1c4.setBackgroundResource(LottoUtil.getBallResource(integers.get(3), false));
                                        binding.table.tvR1c5.setText(String.valueOf(integers.get(4)));
                                        binding.table.tvR1c5.setBackgroundResource(LottoUtil.getBallResource(integers.get(4), false));
                                        binding.table.tvR1c6.setText(String.valueOf(integers.get(5)));
                                        binding.table.tvR1c6.setBackgroundResource(LottoUtil.getBallResource(integers.get(5), false));
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

    private void setLoseMsg(LinearLayout parentView){
        LinearLayout child = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_resultitem,null,false);
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
                            Toast.makeText(AdancedAct.this, jo.getString("msg"), Toast.LENGTH_SHORT).show();
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
                                    LottoUtil.resultAdd(game,binding.layoutResultview.resultTable.tvR1result,getLayoutInflater());
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
                                    LottoUtil.resultAdd(game,binding.layoutResultview.resultTable.tvR2result,getLayoutInflater());
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
                                    LottoUtil.resultAdd(game,binding.layoutResultview.resultTable.tvR3result,getLayoutInflater());
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
                                    LottoUtil.resultAdd(game,binding.layoutResultview.resultTable.tvR4result,getLayoutInflater());
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
                                    LottoUtil.resultAdd(game,binding.layoutResultview.resultTable.tvR5result,getLayoutInflater());
                                } else {
                                    setLoseMsg(binding.layoutResultview.resultTable.tvR5result);
                                }
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(AdancedAct.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(AdancedAct.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }

            }
        };

        numResult.addParams("APPCONNECTCODE", "APP");
        numResult.addParams("dbControl", "LottoNumHighRankMatch");
        numResult.addParams("midx", UserPref.getIdx(this));
        numResult.addParams("gametype", "adv");

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
                            final JSONObject jo = new JSONObject(res);
                            final String msg = jo.getString("message");

                            if (jo.getString("result").equalsIgnoreCase("Y")) {
                                imgUrl = NetUrls.IMGDOMAIN + jo.getString("url");
                                Log.d(StringUtil.TAG, "imgUrl: " + imgUrl);
                                sendKakaoLink(imgUrl);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(AdancedAct.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AdancedAct.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AdancedAct.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        if (binding.layoutResultview.getRoot().getVisibility() == View.VISIBLE) {
            binding.layoutResultview.getRoot().setVisibility(View.GONE);
            binding.ivPopback.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                finish();
                break;
            case R.id.btn_oddeven:
                setBtnSelected(0);
                break;
            case R.id.btn_combination:
                setBtnSelected(1);
                break;
            case R.id.btn_winning2:
                setBtnSelected(2);
                break;
            case R.id.btn_rangestatistics:
                setBtnSelected(3);
                break;
            case R.id.btn_makenum:
                // 번호생성 모드 선택확인 후 진행
                if (StringUtil.isNull(makeMode)) {
                    Toast.makeText(this, "생성 공식을 선택해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!UserPref.getSubscriptionState(getApplicationContext())){
                    Toast.makeText(this, "이용권 구입 후 사용가능 합니다.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, PaymentDlg.class));
                    return;
                }

                binding.llGuidetextArea.setVisibility(View.GONE);

                if (randTable.containsKey("t5")) {
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


                    binding.table.tvR1c1.setText("?");
                    binding.table.tvR1c1.setBackgroundResource(R.drawable.s_ball_grey);
                    binding.table.tvR1c2.setText("?");
                    binding.table.tvR1c2.setBackgroundResource(R.drawable.s_ball_grey);
                    binding.table.tvR1c3.setText("?");
                    binding.table.tvR1c3.setBackgroundResource(R.drawable.s_ball_grey);
                    binding.table.tvR1c4.setText("?");
                    binding.table.tvR1c4.setBackgroundResource(R.drawable.s_ball_grey);
                    binding.table.tvR1c5.setText("?");
                    binding.table.tvR1c5.setBackgroundResource(R.drawable.s_ball_grey);
                    binding.table.tvR1c6.setText("?");
                    binding.table.tvR1c6.setBackgroundResource(R.drawable.s_ball_grey);

                    binding.table.tvR2c1.setText("");
                    binding.table.tvR2c1.setBackground(null);
                    binding.table.tvR2c2.setText("");
                    binding.table.tvR2c2.setBackground(null);
                    binding.table.tvR2c3.setText("");
                    binding.table.tvR2c3.setBackground(null);
                    binding.table.tvR2c4.setText("");
                    binding.table.tvR2c4.setBackground(null);
                    binding.table.tvR2c5.setText("");
                    binding.table.tvR2c5.setBackground(null);
                    binding.table.tvR2c6.setText("");
                    binding.table.tvR2c6.setBackground(null);

                    binding.table.tvR3c1.setText("");
                    binding.table.tvR3c1.setBackground(null);
                    binding.table.tvR3c2.setText("");
                    binding.table.tvR3c2.setBackground(null);
                    binding.table.tvR3c3.setText("");
                    binding.table.tvR3c3.setBackground(null);
                    binding.table.tvR3c4.setText("");
                    binding.table.tvR3c4.setBackground(null);
                    binding.table.tvR3c5.setText("");
                    binding.table.tvR3c5.setBackground(null);
                    binding.table.tvR3c6.setText("");
                    binding.table.tvR3c6.setBackground(null);

                    binding.table.tvR4c1.setText("");
                    binding.table.tvR4c1.setBackground(null);
                    binding.table.tvR4c2.setText("");
                    binding.table.tvR4c2.setBackground(null);
                    binding.table.tvR4c3.setText("");
                    binding.table.tvR4c3.setBackground(null);
                    binding.table.tvR4c4.setText("");
                    binding.table.tvR4c4.setBackground(null);
                    binding.table.tvR4c5.setText("");
                    binding.table.tvR4c5.setBackground(null);
                    binding.table.tvR4c6.setText("");
                    binding.table.tvR4c6.setBackground(null);

                    binding.table.tvR5c1.setText("");
                    binding.table.tvR5c1.setBackground(null);
                    binding.table.tvR5c2.setText("");
                    binding.table.tvR5c2.setBackground(null);
                    binding.table.tvR5c3.setText("");
                    binding.table.tvR5c3.setBackground(null);
                    binding.table.tvR5c4.setText("");
                    binding.table.tvR5c4.setBackground(null);
                    binding.table.tvR5c5.setText("");
                    binding.table.tvR5c5.setBackground(null);
                    binding.table.tvR5c6.setText("");
                    binding.table.tvR5c6.setBackground(null);
                }

                if (numMakeFlag) {
                    Toast.makeText(this, "번호생성 중입니다.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    numMakeFlag = true;
                    makeHighNum();
                }

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

//                if (!isPay){
//                    Toast.makeText(this, "이용권 구입 후 사용가능 합니다.", Toast.LENGTH_SHORT).show();
//                    return;
//                }

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
                                Toast.makeText(AdancedAct.this, "공유이미지 생성 중입니다. 잠시만 기다려 주세요.", Toast.LENGTH_SHORT).show();
                                File tmpFile = null;
                                try {
                                    tmpFile = File.createTempFile("uploadimg", ".jpg", getCacheDir());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                Log.d(StringUtil.TAG, "create file: " + tmpFile.exists());
                                binding.layoutResultview.resultTable.getRoot().buildDrawingCache();

                                FileOutputStream fos;

                                try {
                                    Bitmap bitmap = Bitmap.createBitmap(binding.layoutResultview.resultTable.getRoot().getMeasuredWidth(), binding.layoutResultview.resultTable.getRoot().getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(bitmap);
                                    binding.layoutResultview.resultTable.getRoot().draw(canvas);
//                        Bitmap bitmap = Bitmap.createBitmap(binding.layoutResultview.resultTable.getRoot().getDrawingCache());
                                    Log.d(StringUtil.TAG, "bitmap: " + bitmap.getConfig() + " / " + bitmap.getWidth());
                                    fos = new FileOutputStream(tmpFile);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);        // 오류 : java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.graphics.Bitmap.compress(android.graphics.Bitmap$CompressFormat, int, java.io.OutputStream)' on a null object reference
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
                                    Toast.makeText(AdancedAct.this, "천천히 시도해주세요.", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    isMekeFile = false;
                                    if (pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                    Toast.makeText(AdancedAct.this, "천천히 시도해주세요.", Toast.LENGTH_SHORT).show();
                                }

                                imgUpload(tmpFile);
                            }
                        });
                    } else {
                        Toast.makeText(AdancedAct.this, "파일 생성중입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
            case R.id.btn_savegallery:
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
                                File dir = new File(path);

                                String filename = System.currentTimeMillis() + "_hqresult.jpg";
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
                                    Bitmap bitmap = Bitmap.createBitmap(binding.layoutResultview.resultTable.getRoot().getMeasuredWidth(), binding.layoutResultview.resultTable.getRoot().getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                                    Canvas canvas = new Canvas(bitmap);
                                    binding.layoutResultview.resultTable.getRoot().draw(canvas);
//                        Bitmap bitmap = binding.layoutResultview.resultTable.getRoot().getDrawingCache();
                                    fos = new FileOutputStream(f);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                    fos.close();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    Toast.makeText(AdancedAct.this, "파일을 저장하고 있습니다. 천천히 시도해주세요.", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    Toast.makeText(AdancedAct.this, "파일을 저장하고 있습니다. 천천히 시도해주세요.", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }

                                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                Uri contentUri = Uri.fromFile(f);
                                mediaScanIntent.setData(contentUri);
                                sendBroadcast(mediaScanIntent);

                            }
                        });

                        isMekeFile = false;
                        Toast.makeText(AdancedAct.this, "저장 되었습니다.", Toast.LENGTH_SHORT).show();
                        if (pd.isShowing()) {
                            pd.dismiss();
                        }

                    } else {
                        Toast.makeText(AdancedAct.this, "파일 생성중입니다.", Toast.LENGTH_SHORT).show();
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

    private void checkTicket() {
//        {"result":"N","msg":"\uad6c\ub9e4\ub0b4\uc5ed\uc774 \uc5c6\uc2b5\ub2c8\ub2e4."}
        ReqBasic checkTicket = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(StringUtil.TAG, "code: " + resultCode);
                Log.d(StringUtil.TAG, "checkTicket: " + resultData.getResult());

                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            // 이용권 있음 => 번호생성
                            isPay = true;
                        } else {
                            // 이용권 없음, 날짜 만료
                            isPay = false;
//                            Toast.makeText(AdancedAct.this, jo.getString("msg"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(AdancedAct.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(AdancedAct.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }

            }
        };


        checkTicket.addParams("APPCONNECTCODE", "APP");
        checkTicket.addParams("dbControl", "PremiumPassCheck");
        checkTicket.addParams("midx", UserPref.getIdx(this));
        checkTicket.execute(true, true);
    }


    private void sendKakaoLink(String imgUrl) {
        // 카카오링크(이미지 공유)
        FeedTemplate params = FeedTemplate.newBuilder(ContentObject.newBuilder("파워로또",
                imgUrl,
                LinkObject.newBuilder().setWebUrl(imgUrl)
                        .setMobileWebUrl(imgUrl).build())
                .setDescrption("고급생성 결과보기 공유\n" + imgUrl)
                .setImageHeight(50)
                .build())
//                        .setSocial(SocialObject.newBuilder().setLikeCount(10).setCommentCount(20)
//                                .setSharedCount(30).setViewCount(40).build())

//                .addButton(new ButtonObject("웹에서 보기", LinkObject.newBuilder().setWebUrl("'"+imgUrl).setMobileWebUrl("'"+imgUrl).build()))
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
}
