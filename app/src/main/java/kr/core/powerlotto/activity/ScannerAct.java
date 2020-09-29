package kr.core.powerlotto.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.Call;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import kr.core.powerlotto.R;
import kr.core.powerlotto.data.GameNum;
import kr.core.powerlotto.data.QrResultInfo;
import kr.core.powerlotto.databinding.ActivityScannerBinding;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.util.LayoutWebView;
import kr.core.powerlotto.util.LottoUtil;
import kr.core.powerlotto.util.StringUtil;
import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.core.ViewFinderView;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerAct extends BaseAct implements View.OnClickListener, ZXingScannerView.ResultHandler {

    ActivityScannerBinding binding;

    final String DH = "http://m.dhlottery.co.kr/?";
    final String NN = "http://qr.nlotto.co.kr/?";
    final String L645 = "http://qr.645lotto.net/?";
    final String REPLACESTR = "https://m.dhlottery.co.kr/qr.do?method=winQr&";

    private ZXingScannerView mScannerView;
    String resUrl = "";

    QrResultInfo resultInfo;
    Activity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_scanner);
        act = this;

        setTextTypeface();

        getCoupaBanner();
//        setBannerArea();

        Typeface tp = Typeface.createFromAsset(getAssets(),"notosans.otf");
        binding.qrGuidetext1.setTypeface(tp);
        binding.qrGuidetext2.setTypeface(tp);

        mScannerView = new ZXingScannerView(this) {
            @Override
            protected IViewFinder createViewFinderView(Context context) {
                return new CustomViewFinderView(context);
            }
        };

        binding.contentFrame.addView(mScannerView);

        binding.btnClose.setOnClickListener(this);
        binding.llResultView.btnPopclose.setOnClickListener(this);
//        binding.llResultView.setOnClickListener(this);

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

    private void setBannerArea(){
//        if (StringUtil.isNull(app.bannerState)){
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
//        }else{
//            switch (app.bannerState){
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
//                                Toast.makeText(ScannerAct.this, "연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
//                            }else{
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

    }


    private void setTextTypeface(){
        binding.llResultView.tvWinnum1.setTypeface(app.jalnan);
        binding.llResultView.tvWinnum2.setTypeface(app.jalnan);
        binding.llResultView.tvWinnum3.setTypeface(app.jalnan);
        binding.llResultView.tvWinnum4.setTypeface(app.jalnan);
        binding.llResultView.tvWinnum5.setTypeface(app.jalnan);
        binding.llResultView.tvWinnum6.setTypeface(app.jalnan);
        binding.llResultView.tvBonusnum.setTypeface(app.jalnan);

        binding.llResultView.tvR1c1.setTypeface(app.jalnan);
        binding.llResultView.tvR1c2.setTypeface(app.jalnan);
        binding.llResultView.tvR1c3.setTypeface(app.jalnan);
        binding.llResultView.tvR1c4.setTypeface(app.jalnan);
        binding.llResultView.tvR1c5.setTypeface(app.jalnan);
        binding.llResultView.tvR1c6.setTypeface(app.jalnan);

        binding.llResultView.tvR2c1.setTypeface(app.jalnan);
        binding.llResultView.tvR2c2.setTypeface(app.jalnan);
        binding.llResultView.tvR2c3.setTypeface(app.jalnan);
        binding.llResultView.tvR2c4.setTypeface(app.jalnan);
        binding.llResultView.tvR2c5.setTypeface(app.jalnan);
        binding.llResultView.tvR2c6.setTypeface(app.jalnan);

        binding.llResultView.tvR3c1.setTypeface(app.jalnan);
        binding.llResultView.tvR3c2.setTypeface(app.jalnan);
        binding.llResultView.tvR3c3.setTypeface(app.jalnan);
        binding.llResultView.tvR3c4.setTypeface(app.jalnan);
        binding.llResultView.tvR3c5.setTypeface(app.jalnan);
        binding.llResultView.tvR3c6.setTypeface(app.jalnan);

        binding.llResultView.tvR4c1.setTypeface(app.jalnan);
        binding.llResultView.tvR4c2.setTypeface(app.jalnan);
        binding.llResultView.tvR4c3.setTypeface(app.jalnan);
        binding.llResultView.tvR4c4.setTypeface(app.jalnan);
        binding.llResultView.tvR4c5.setTypeface(app.jalnan);
        binding.llResultView.tvR4c6.setTypeface(app.jalnan);

        binding.llResultView.tvR5c1.setTypeface(app.jalnan);
        binding.llResultView.tvR5c2.setTypeface(app.jalnan);
        binding.llResultView.tvR5c3.setTypeface(app.jalnan);
        binding.llResultView.tvR5c4.setTypeface(app.jalnan);
        binding.llResultView.tvR5c5.setTypeface(app.jalnan);
        binding.llResultView.tvR5c6.setTypeface(app.jalnan);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        binding.llResultView.getRoot().setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_close:
                finish();
                break;
            case R.id.btn_popclose:
                binding.llResultView.getRoot().setVisibility(View.GONE);
                break;
//            case R.id.ll_resultView:
//                Log.d(StringUtil.TAG, "onClick: "+resUrl);
//                if (StringUtil.isNull(resUrl)){
//                    Toast.makeText(this, "스캔 결과가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(resUrl)));
////                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://naver.com")));
//                break;
        }
    }

    @Override
    public void handleResult(Result result) {
//        Toast.makeText(this, "Contents = " + result.getText() +
//                ", Format = " + result.getBarcodeFormat().toString(), Toast.LENGTH_SHORT).show();
        if (result.getText().contains(DH)) {
            resUrl = result.getText().replace(DH, REPLACESTR);
        }else if (result.getText().contains(NN)) {
            resUrl = result.getText().replace(NN, REPLACESTR);
        }else if (result.getText().contains(L645)) {
            resUrl = result.getText().replace(L645, REPLACESTR);
        }

        binding.llResultView.getRoot().setVisibility(View.VISIBLE);

        QrResult qrResult = new QrResult();
        qrResult.execute(resUrl);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(ScannerAct.this);
            }
        }, 2000);
    }

    private class CustomViewFinderView extends ViewFinderView {

        public CustomViewFinderView(Context context) {
            super(context);
            init();
        }

        public CustomViewFinderView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            init();
        }

        private void init() {

            setLaserEnabled(false);
            setBorderColor(android.R.color.transparent);

        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            drawEdge(canvas);
        }

        private void drawEdge(Canvas canvas){
            Rect framingRect = getFramingRect();
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.edge);
//            Rect rect = new Rect(framingRect.left,framingRect.top,framingRect.right,framingRect.bottom);
            try {
                canvas.drawBitmap(bitmap, null, framingRect, null);
            }catch(NullPointerException e){
                float top = ((canvas.getHeight() / 2) - 246);
                float left = (canvas.getWidth()-bitmap.getWidth()) / 2;
                canvas.drawBitmap(bitmap, left, top, null);
                e.printStackTrace();
            }

        }
    }

    private class QrResult extends AsyncTask<String,Void,Boolean>{

        @Override
        protected Boolean doInBackground(String... strings) {

            try {

                Connection.Response response = Jsoup.connect(strings[0])
                        .method(Connection.Method.GET)
                        .execute();
                Log.d(StringUtil.TAG, "doInBackground: "+strings[0]);
                Document document = response.parse();

                resultInfo = new QrResultInfo();
                // 회차
                resultInfo.setKey_clr1(document.body().select("section").get(1).select("span.key_clr1").text().split(" ")[0]);
                // 추첨날짜
                resultInfo.setDate(document.body().select("section").get(1).select("span.date").text());
                // QR 전체결과
                if (document.body().select("section").get(1).select("div[class=bx_notice winner]").text().contains("아쉽게도,")){
                    resultInfo.setBx_notice_winner1("아쉽게도,");
                    resultInfo.setBx_notice_winner2(document.body().select("section").get(1).select("div[class=bx_notice winner]").text().replace("아쉽게도,","").trim());
                }else if (document.body().select("section").get(1).select("div[class=bx_notice winner]").text().contains("축하합니다!")){
                    resultInfo.setBx_notice_winner1("축하합니다!");
                    resultInfo.setPrice(document.body().select("section").get(1).select("div[class=bx_notice winner]").select("span.key_clr1").text());
                }

                // 당첨번호
                Elements element = document.body().select("section").get(1).select("div.list").select("div");

                for (int i = 0; i < element.size(); i++){
                    Element winnum = element.get(i);
                    if (i > 0) {
                        resultInfo.winnums[i-1] = winnum.text();
                    }
                }

                Elements tbody = document.body().select("section").get(1).select("div.list_my_number").select("tbody");

                Elements res = tbody.select("td");
                resultInfo.resPos = new ArrayList<>();
                resultInfo.games = new HashMap<>();
                for (int j = 0; j < res.size(); j++){
                    Element list_my_number = res.get(j);
                    if (list_my_number.select("td").hasClass("result")){
                        resultInfo.resPos.add(list_my_number.select("td").text());
                    }

                    Elements games = list_my_number.select("td").select("span");
                    if (games.size() > 0){
                        resultInfo.game = new ArrayList<>();
                        for (int k = 0; k < games.size(); k++){
                            Element numinfo = games.get(k);
                            GameNum data = new GameNum();
                            data.setNum(numinfo.text());
                            if (numinfo.toString().contains("<span class=\"clr\">")){
//                                // 번호없음 : 회색
//                                data.setWinnum(false);
                            }else{
//                                // 번호있음 : 해당색상
                                data.setWinnum(true);
                            }
                            resultInfo.game.add(data);
                        }
                        if (resultInfo.games.isEmpty()){
                            resultInfo.games.put("r1",resultInfo.game);
                        }else{
                            if (resultInfo.games.containsKey("r2")) {
                                if (resultInfo.games.containsKey("r3")) {
                                    if (resultInfo.games.containsKey("r4")) {
                                        resultInfo.games.put("r5", resultInfo.game);
                                    }else {
                                        resultInfo.games.put("r4", resultInfo.game);
                                    }
                                }else {
                                    resultInfo.games.put("r3", resultInfo.game);
                                }
                            }else{
                                resultInfo.games.put("r2",resultInfo.game);
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ScannerAct.this, "로또 QR스캔 값을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (resultInfo != null){
                return true;
            }else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            Log.d(StringUtil.TAG, "onPostExecute: "+resultInfo.toString());

            binding.llResultView.tvKeyClr1.setText(resultInfo.getKey_clr1());
            binding.llResultView.tvDate.setText(resultInfo.getDate());
            binding.llResultView.tvWinnum1.setText(resultInfo.getWinnums()[0]);
            binding.llResultView.tvWinnum1.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getWinnums()[0]),true));
            binding.llResultView.tvWinnum2.setText(resultInfo.getWinnums()[1]);
            binding.llResultView.tvWinnum2.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getWinnums()[1]),true));
            binding.llResultView.tvWinnum3.setText(resultInfo.getWinnums()[2]);
            binding.llResultView.tvWinnum3.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getWinnums()[2]),true));
            binding.llResultView.tvWinnum4.setText(resultInfo.getWinnums()[3]);
            binding.llResultView.tvWinnum4.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getWinnums()[3]),true));
            binding.llResultView.tvWinnum5.setText(resultInfo.getWinnums()[4]);
            binding.llResultView.tvWinnum5.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getWinnums()[4]),true));
            binding.llResultView.tvWinnum6.setText(resultInfo.getWinnums()[5]);
            binding.llResultView.tvWinnum6.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getWinnums()[5]),true));
            binding.llResultView.tvBonusnum.setText(resultInfo.getWinnums()[6]);
            binding.llResultView.tvBonusnum.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getWinnums()[6]),true));

            binding.llResultView.tvR1res.setText(resultInfo.getResPos().get(0));
            binding.llResultView.tvR2res.setText(resultInfo.getResPos().get(1));
            binding.llResultView.tvR3res.setText(resultInfo.getResPos().get(2));
            binding.llResultView.tvR4res.setText(resultInfo.getResPos().get(3));
            binding.llResultView.tvR5res.setText(resultInfo.getResPos().get(4));

            binding.llResultView.tvRestext1.setText(resultInfo.getBx_notice_winner1());
            if (StringUtil.isNull(resultInfo.getPrice())){
                binding.llResultView.tvLose.setVisibility(View.VISIBLE);
                binding.llResultView.llPriceArea.setVisibility(View.GONE);
                binding.llResultView.tvLose.setText(resultInfo.getBx_notice_winner2());
            }else{
                binding.llResultView.tvLose.setVisibility(View.GONE);
                binding.llResultView.llPriceArea.setVisibility(View.VISIBLE);
                binding.llResultView.tvPrice.setText(resultInfo.getPrice());
            }

            int resid = -1;
            if (StringUtil.isNull(resultInfo.getPrice())){
                resid = R.drawable.badge06;
            }else{
                ArrayList<String> tmplist = new ArrayList<>();
                for (String item : resultInfo.getResPos()){
                    if (item.contains("낙첨")){

                    }else{
                        Log.d(StringUtil.TAG, "substring: "+item.substring(0,1));
                        int badgenum = 6;

                        if (badgenum > Integer.parseInt(item.substring(0,1))){
                            badgenum = Integer.parseInt(item.substring(0,1));
                        }

                        switch (badgenum){
                            case 5:
                                resid = R.drawable.badge05;
                                break;
                            case 4:
                                resid = R.drawable.badge04;
                                break;
                            case 3:
                                resid = R.drawable.badge03;
                                break;
                            case 2:
                                resid = R.drawable.badge02;
                                break;
                            case 1:
                                resid = R.drawable.badge01;
                                break;
                            default:
                                resid = R.drawable.badge06;
                                break;
                        }
                    }
                }
            }

            binding.llResultView.ivBadge.setImageResource(resid);

            binding.llResultView.tvR1c1.setText(resultInfo.getGames().get("r1").get(0).getNum());
            if (resultInfo.getGames().get("r1").get(0).isWinnum()){
                binding.llResultView.tvR1c1.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r1").get(0).getNum()),false));
            }else{
                binding.llResultView.tvR1c1.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR1c2.setText(resultInfo.getGames().get("r1").get(1).getNum());
            if (resultInfo.getGames().get("r1").get(1).isWinnum()){
                binding.llResultView.tvR1c2.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r1").get(1).getNum()),false));
            }else{
                binding.llResultView.tvR1c2.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR1c3.setText(resultInfo.getGames().get("r1").get(2).getNum());
            if (resultInfo.getGames().get("r1").get(2).isWinnum()){
                binding.llResultView.tvR1c3.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r1").get(2).getNum()),false));
            }else{
                binding.llResultView.tvR1c3.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR1c4.setText(resultInfo.getGames().get("r1").get(3).getNum());
            if (resultInfo.getGames().get("r1").get(3).isWinnum()){
                binding.llResultView.tvR1c4.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r1").get(3).getNum()),false));
            }else{
                binding.llResultView.tvR1c4.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR1c5.setText(resultInfo.getGames().get("r1").get(4).getNum());
            if (resultInfo.getGames().get("r1").get(4).isWinnum()){
                binding.llResultView.tvR1c5.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r1").get(4).getNum()),false));
            }else{
                binding.llResultView.tvR1c5.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR1c6.setText(resultInfo.getGames().get("r1").get(5).getNum());
            if (resultInfo.getGames().get("r1").get(5).isWinnum()){
                binding.llResultView.tvR1c6.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r1").get(5).getNum()),false));
            }else{
                binding.llResultView.tvR1c6.setBackgroundResource(R.drawable.s_ball_grey);
            }

            //===========================================================================================================================================================================================================================

            binding.llResultView.tvR2c1.setText(resultInfo.getGames().get("r2").get(0).getNum());
            if (resultInfo.getGames().get("r2").get(0).isWinnum()){
                binding.llResultView.tvR2c1.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r2").get(0).getNum()),false));
            }else{
                binding.llResultView.tvR2c1.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR2c2.setText(resultInfo.getGames().get("r2").get(1).getNum());
            if (resultInfo.getGames().get("r2").get(1).isWinnum()){
                binding.llResultView.tvR2c2.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r2").get(1).getNum()),false));
            }else{
                binding.llResultView.tvR2c2.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR2c3.setText(resultInfo.getGames().get("r2").get(2).getNum());
            if (resultInfo.getGames().get("r2").get(2).isWinnum()){
                binding.llResultView.tvR2c3.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r2").get(2).getNum()),false));
            }else{
                binding.llResultView.tvR2c3.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR2c4.setText(resultInfo.getGames().get("r2").get(3).getNum());
            if (resultInfo.getGames().get("r2").get(3).isWinnum()){
                binding.llResultView.tvR2c4.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r2").get(3).getNum()),false));
            }else{
                binding.llResultView.tvR2c4.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR2c5.setText(resultInfo.getGames().get("r2").get(4).getNum());
            if (resultInfo.getGames().get("r2").get(4).isWinnum()){
                binding.llResultView.tvR2c5.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r2").get(4).getNum()),false));
            }else{
                binding.llResultView.tvR2c5.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR2c6.setText(resultInfo.getGames().get("r2").get(5).getNum());
            if (resultInfo.getGames().get("r2").get(5).isWinnum()){
                binding.llResultView.tvR2c6.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r2").get(5).getNum()),false));
            }else{
                binding.llResultView.tvR2c6.setBackgroundResource(R.drawable.s_ball_grey);
            }

            //===========================================================================================================================================================================================================================

            binding.llResultView.tvR3c1.setText(resultInfo.getGames().get("r3").get(0).getNum());
            if (resultInfo.getGames().get("r3").get(0).isWinnum()){
                binding.llResultView.tvR3c1.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r3").get(0).getNum()),false));
            }else{
                binding.llResultView.tvR3c1.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR3c2.setText(resultInfo.getGames().get("r3").get(1).getNum());
            if (resultInfo.getGames().get("r3").get(1).isWinnum()){
                binding.llResultView.tvR3c2.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r3").get(1).getNum()),false));
            }else{
                binding.llResultView.tvR3c2.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR3c3.setText(resultInfo.getGames().get("r3").get(2).getNum());
            if (resultInfo.getGames().get("r3").get(2).isWinnum()){
                binding.llResultView.tvR3c3.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r3").get(2).getNum()),false));
            }else{
                binding.llResultView.tvR3c3.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR3c4.setText(resultInfo.getGames().get("r3").get(3).getNum());
            if (resultInfo.getGames().get("r3").get(3).isWinnum()){
                binding.llResultView.tvR3c4.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r3").get(3).getNum()),false));
            }else{
                binding.llResultView.tvR3c4.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR3c5.setText(resultInfo.getGames().get("r3").get(4).getNum());
            if (resultInfo.getGames().get("r3").get(4).isWinnum()){
                binding.llResultView.tvR3c5.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r3").get(4).getNum()),false));
            }else{
                binding.llResultView.tvR3c5.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR3c6.setText(resultInfo.getGames().get("r3").get(5).getNum());
            if (resultInfo.getGames().get("r3").get(5).isWinnum()){
                binding.llResultView.tvR3c6.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r3").get(5).getNum()),false));
            }else{
                binding.llResultView.tvR3c6.setBackgroundResource(R.drawable.s_ball_grey);
            }

            //===========================================================================================================================================================================================================================

            binding.llResultView.tvR4c1.setText(resultInfo.getGames().get("r4").get(0).getNum());
            if (resultInfo.getGames().get("r4").get(0).isWinnum()){
                binding.llResultView.tvR4c1.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r4").get(0).getNum()),false));
            }else{
                binding.llResultView.tvR4c1.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR4c2.setText(resultInfo.getGames().get("r4").get(1).getNum());
            if (resultInfo.getGames().get("r4").get(1).isWinnum()){
                binding.llResultView.tvR4c2.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r4").get(1).getNum()),false));
            }else{
                binding.llResultView.tvR4c2.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR4c3.setText(resultInfo.getGames().get("r4").get(2).getNum());
            if (resultInfo.getGames().get("r4").get(2).isWinnum()){
                binding.llResultView.tvR4c3.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r4").get(2).getNum()),false));
            }else{
                binding.llResultView.tvR4c3.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR4c4.setText(resultInfo.getGames().get("r4").get(3).getNum());
            if (resultInfo.getGames().get("r4").get(3).isWinnum()){
                binding.llResultView.tvR4c4.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r4").get(3).getNum()),false));
            }else{
                binding.llResultView.tvR4c4.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR4c5.setText(resultInfo.getGames().get("r4").get(4).getNum());
            if (resultInfo.getGames().get("r4").get(4).isWinnum()){
                binding.llResultView.tvR4c5.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r4").get(4).getNum()),false));
            }else{
                binding.llResultView.tvR4c5.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR4c6.setText(resultInfo.getGames().get("r4").get(5).getNum());
            if (resultInfo.getGames().get("r4").get(5).isWinnum()){
                binding.llResultView.tvR4c6.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r4").get(5).getNum()),false));
            }else{
                binding.llResultView.tvR4c6.setBackgroundResource(R.drawable.s_ball_grey);
            }

            //===========================================================================================================================================================================================================================

            binding.llResultView.tvR5c1.setText(resultInfo.getGames().get("r5").get(0).getNum());
            if (resultInfo.getGames().get("r5").get(0).isWinnum()){
                binding.llResultView.tvR5c1.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r5").get(0).getNum()),false));
            }else{
                binding.llResultView.tvR5c1.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR5c2.setText(resultInfo.getGames().get("r5").get(1).getNum());
            if (resultInfo.getGames().get("r5").get(1).isWinnum()){
                binding.llResultView.tvR5c2.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r5").get(1).getNum()),false));
            }else{
                binding.llResultView.tvR5c2.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR5c3.setText(resultInfo.getGames().get("r5").get(2).getNum());
            if (resultInfo.getGames().get("r5").get(2).isWinnum()){
                binding.llResultView.tvR5c3.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r5").get(2).getNum()),false));
            }else{
                binding.llResultView.tvR5c3.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR5c4.setText(resultInfo.getGames().get("r5").get(3).getNum());
            if (resultInfo.getGames().get("r5").get(3).isWinnum()){
                binding.llResultView.tvR5c4.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r5").get(3).getNum()),false));
            }else{
                binding.llResultView.tvR5c4.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR5c5.setText(resultInfo.getGames().get("r5").get(4).getNum());
            if (resultInfo.getGames().get("r5").get(4).isWinnum()){
                binding.llResultView.tvR5c5.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r5").get(4).getNum()),false));
            }else{
                binding.llResultView.tvR5c5.setBackgroundResource(R.drawable.s_ball_grey);
            }

            binding.llResultView.tvR5c6.setText(resultInfo.getGames().get("r5").get(5).getNum());
            if (resultInfo.getGames().get("r5").get(5).isWinnum()){
                binding.llResultView.tvR5c6.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(resultInfo.getGames().get("r5").get(5).getNum()),false));
            }else{
                binding.llResultView.tvR5c6.setBackgroundResource(R.drawable.s_ball_grey);
            }

        }
    }
}
