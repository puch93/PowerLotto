package kr.core.powerlotto.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.android.billingclient.api.Purchase;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import kr.core.powerlotto.R;
import kr.core.powerlotto.billing.BillingManager;
import kr.core.powerlotto.databinding.ActivityPremium2Binding;
import kr.core.powerlotto.insidedata.UserPref;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.util.LayoutWebView;
import kr.core.powerlotto.util.StringUtil;

public class PremiumActTmp extends BaseAct implements View.OnClickListener {
    ActivityPremium2Binding binding;
    Activity act;
    String id = "";
    String price = "";
    String itemidx = "";
    String term = "";
    BillingManager billingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_premium2);
        act = this;
        getCoupaBanner();

        setBilling();
//        setBannerArea();

//        AdRequest adRequest = new AdRequest.Builder().build();
//        binding.bannerArea.bannerAdmob.loadAd(adRequest);

        binding.btnClose.setOnClickListener(this);
        binding.btnSidemenu.setOnClickListener(this);

        binding.ll1monthArea.setOnClickListener(this);
        binding.ll3monthArea.setOnClickListener(this);
        binding.ll6monthArea.setOnClickListener(this);
        binding.ll12monthArea.setOnClickListener(this);

        id = BillingManager.USE12MONTH;
        price = "55000";
        itemidx = "3";
        term = "12m";
        binding.ll1monthArea.setSelected(false);
        binding.ll3monthArea.setSelected(false);
        binding.ll6monthArea.setSelected(false);
        binding.ll12monthArea.setSelected(true);
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


//    private void setBannerArea(){
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
//                                Toast.makeText(PremiumAct.this, "연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
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
//
//    }

    private void setBilling(){
        billingManager = new BillingManager(this, new BillingManager.AfterBilling() {
            @Override
            public void sendResult(Purchase purchase) {
                // 서버로 값 전송(결제 완료)
                sendPurchaseResult(purchase);
            }

            @Override
            public void getSubsriptionState(String subscription, Purchase purchase) {

            }
        });
    }

    private void checkTicket(){
//        {"result":"N","msg":"\uad6c\ub9e4\ub0b4\uc5ed\uc774 \uc5c6\uc2b5\ub2c8\ub2e4."}
        ReqBasic checkTicket = new ReqBasic(this,NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(StringUtil.TAG, "code: "+resultCode);
                Log.d(StringUtil.TAG, "checkTicket: "+resultData.getResult());

                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            // 이용권 있음
                            Toast.makeText(PremiumActTmp.this, jo.getString("msg"), Toast.LENGTH_SHORT).show();
                        } else {
                            // 이용권 없음, 날짜 만료
                            billingManager.purchase(id, true);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(PremiumActTmp.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(PremiumActTmp.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }
            }
        };


        checkTicket.addParams("APPCONNECTCODE","APP");
        checkTicket.addParams("dbControl","PremiumPassCheck");
        checkTicket.addParams("midx", UserPref.getIdx(this));
        checkTicket.execute(true,true);
    }

    private void sendPurchaseResult(Purchase p){
        ReqBasic pResult = new ReqBasic(this,NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
//                {"result":"Y","message":"성공적으로 등록 완료되었습니다.","url":""}
                Log.d(StringUtil.TAG, "code: "+resultCode);
                Log.d(StringUtil.TAG, "sendPurchaseResult: "+resultData.getResult());
                if (resultData.getResult() != null){

                    try {

                        JSONObject jo = new JSONObject(resultData.getResult());
                        Toast.makeText(PremiumActTmp.this, jo.getString("message"), Toast.LENGTH_SHORT).show();

                    }catch (JSONException e){
                        e.printStackTrace();
                        Toast.makeText(PremiumActTmp.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(PremiumActTmp.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }
            }
        };

        pResult.addParams("APPCONNECTCODE","APP");
        pResult.addParams("dbControl","PremiumPass");
        pResult.addParams("midx", UserPref.getIdx(this));
        pResult.addParams("pstoretype","google");
        pResult.addParams("term",term);
        pResult.addParams("p_purchasePrice",price);
        pResult.addParams("p_purchase_item_name",p.getSku());
        pResult.addParams("ITEMIDX",itemidx);

        pResult.addParams("p_purchasetime",StringUtil.changeDate(p.getPurchaseTime()));
//        pResult.addParams("p_purchasetime",StringUtil.changeDate(System.currentTimeMillis()));

        pResult.addParams("p_orderid",p.getOrderId());
        pResult.addParams("p_info",p.getOriginalJson());
        pResult.addParams("p_signature",p.getSignature());
        pResult.execute(true,true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_close:
                finish();
                break;
            case R.id.ll_1month_area:
                id = BillingManager.USE1MONTH;
                price = "11000";
                itemidx = "0";
                term = "1m";
                Log.d(StringUtil.TAG, "onClick: 1");

                processPay();
                break;
            case R.id.ll_3month_area:
                id = BillingManager.USE3MONTH;
                price = "22000";
                itemidx = "1";
                term = "3m";
                Log.d(StringUtil.TAG, "onClick: 2");

                processPay();
                break;
            case R.id.ll_6month_area:
                id = BillingManager.USE6MONTH;
                price = "33000";
                itemidx = "2";
                term = "6m";
                Log.d(StringUtil.TAG, "onClick: 3");

                processPay();
                break;
            case R.id.ll_12month_area:
                id = BillingManager.USE12MONTH;
                price = "55000";
                itemidx = "3";
                term = "12m";
                Log.d(StringUtil.TAG, "onClick: 4");

                processPay();
                break;
        }
    }

    private void processPay() {
        if (StringUtil.isNull(UserPref.getIdx(this))) {
            Toast.makeText(this, "등록 된 정보가 없어 결제를 진행할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }else{
            checkTicket();
        }
    }
}
