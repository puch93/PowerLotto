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
import com.google.android.gms.common.internal.service.Common;
import com.onestore.iap.api.IapEnum;
import com.onestore.iap.api.IapResult;
import com.onestore.iap.api.PurchaseClient;
import com.onestore.iap.api.PurchaseData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import kr.core.powerlotto.R;
import kr.core.powerlotto.billing.BillingManager;
import kr.core.powerlotto.databinding.ActivityPremium2Binding;
import kr.core.powerlotto.insidedata.UserPref;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.util.LayoutWebView;
import kr.core.powerlotto.util.StringUtil;

public class PremiumAct extends BaseAct implements View.OnClickListener {
    ActivityPremium2Binding binding;
    Activity act;
    String id = "";
    String price = "";
    String itemidx = "";
    String term = "";
    BillingManager billingManager;


    /* one store billing */
    private static final int PURCHASE_REQUEST = 9500;
    String productType = "auto";
    PurchaseClient mPurchaseClient;
    boolean isListenerCalled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_premium2);
        act = this;

        mPurchaseClient = new PurchaseClient(act, StringUtil.KEY);
        mPurchaseClient.connect(mServiceConnectionListener);

        getCoupaBanner();

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


    PurchaseClient.ServiceConnectionListener mServiceConnectionListener = new PurchaseClient.ServiceConnectionListener() {
        @Override
        public void onConnected() {
            mPurchaseClient.isBillingSupportedAsync(StringUtil.IAP_API_VERSION, mBillingSupportedListener);
            mPurchaseClient.queryPurchasesAsync(StringUtil.IAP_API_VERSION, productType, mQueryPurchaseListener);
            Log.d("ONE", "Service connected");
            //2. mBillingSupportedListener < 나도모름 / mQueryPurchaseListener << 구매 내역 들고오기
        }

        @Override
        public void onDisconnected() {
            Log.d("ONE", "Service disconnected");
        }

        @Override
        public void onErrorNeedUpdateException() {
            Log.e("ONE", "connect onError, 원스토어 서비스앱의 업데이트가 필요합니다");
            PurchaseClient.launchUpdateOrInstallFlow(act);
        }
    };

    PurchaseClient.BillingSupportedListener mBillingSupportedListener = new PurchaseClient.BillingSupportedListener() {
        @Override
        public void onSuccess() {
            Log.d("ONE", "isBillingSupportedAsync onSuccess");
        }

        @Override
        public void onError(IapResult result) {
            Log.e("ONE", "isBillingSupportedAsync onError, " + result.toString());
        }

        @Override
        public void onErrorRemoteException() {
            Log.e("ONE", "isBillingSupportedAsync onError, 원스토어 서비스와 연결을 할 수 없습니다");
        }

        @Override
        public void onErrorSecurityException() {
            Log.e("ONE", "isBillingSupportedAsync onError, 비정상 앱에서 결제가 요청되었습니다");
        }

        @Override
        public void onErrorNeedUpdateException() {
            Log.e("ONE", "isBillingSupportedAsync onError, 원스토어 서비스앱의 업데이트가 필요합니다");
        }
    };

    PurchaseClient.QueryPurchaseListener mQueryPurchaseListener = new PurchaseClient.QueryPurchaseListener() {
        @Override
        public void onSuccess(List<PurchaseData> purchaseDataList, String productType) {


            Log.d("one", "queryPurchasesAsync onSuccess, " + purchaseDataList.toString());
            //구독
            if (purchaseDataList.size() > 0) {
                for (int i = 0; i < purchaseDataList.size(); i++) {
                    Log.i(StringUtil.TAG, "purchaseDataList.get(" + i + "): " + purchaseDataList.get(i).toString());
                    if (purchaseDataList.get(i).getRecurringState() == 0) {
                        //  구독중
                        UserPref.saveSubscriptionId(act, purchaseDataList.get(i).getProductId());
                        UserPref.saveSubscriptionState(act, true);

                    } else if (purchaseDataList.get(i).getRecurringState() == 1) {
                        //  구독 해지중
                        UserPref.saveSubscriptionId(act, purchaseDataList.get(i).getProductId());
                        UserPref.saveSubscriptionState(act, true);

                    } else if (purchaseDataList.get(i).getRecurringState() == -1) {
                        //  구독 X
                        UserPref.saveSubscriptionId(act, "");
                        UserPref.saveSubscriptionState(act, false);
                    }
                }
            } else {
                UserPref.saveSubscriptionId(act, "");
                UserPref.saveSubscriptionState(act, false);
            }

            isListenerCalled = true;
        }

        @Override
        public void onErrorRemoteException() {
            Log.e("one", "queryPurchasesAsync onError, 원스토어 서비스와 연결을 할 수 없습니다");
        }

        @Override
        public void onErrorSecurityException() {
            Log.e("one", "queryPurchasesAsync onError, 비정상 앱에서 결제가 요청되었습니다");
        }

        @Override
        public void onErrorNeedUpdateException() {
            Log.e("one", "queryPurchasesAsync onError, 원스토어 서비스앱의 업데이트가 필요합니다");
        }

        @Override
        public void onError(IapResult result) {
            Log.e("one", "queryPurchasesAsync onError, " + result.toString());
        }
    };


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
                            // 이용권 있음
                            Toast.makeText(PremiumAct.this, jo.getString("msg"), Toast.LENGTH_SHORT).show();
                        } else {
                            // 이용권 없음, 날짜 만료
                            billingManager.purchase(id, true);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(PremiumAct.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PremiumAct.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }
            }
        };


        checkTicket.addParams("APPCONNECTCODE", "APP");
        checkTicket.addParams("dbControl", "PremiumPassCheck");
        checkTicket.addParams("midx", UserPref.getIdx(this));
        checkTicket.execute(true, true);
    }

    private void sendPurchaseResult(com.onestore.iap.api.PurchaseData p) {
        ReqBasic pResult = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
//                {"result":"Y","message":"성공적으로 등록 완료되었습니다.","url":""}
                Log.d(StringUtil.TAG, "code: " + resultCode);
                Log.d(StringUtil.TAG, "sendPurchaseResult: " + resultData.getResult());
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());
                        Toast.makeText(PremiumAct.this, jo.getString("message"), Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(PremiumAct.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(PremiumAct.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }
            }
        };

        pResult.addParams("APPCONNECTCODE", "APP");
        pResult.addParams("dbControl", "PremiumPass");
        pResult.addParams("midx", UserPref.getIdx(this));
        pResult.addParams("pstoretype", "one");
        pResult.addParams("term", term);
        pResult.addParams("p_purchasePrice", price);
        pResult.addParams("p_purchase_item_name", p.getProductId());
        pResult.addParams("ITEMIDX", itemidx);

        pResult.addParams("p_purchasetime", StringUtil.changeDate(p.getPurchaseTime()));
        pResult.addParams("p_orderid", p.getOrderId());
        pResult.addParams("p_signature", p.getSignature());

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("p_orderid", p.getOrderId());
            jsonObject.put("p_store_type", "OneStore");
            jsonObject.put("p_purchasetime", String.valueOf(p.getPurchaseTime()));
            jsonObject.put("p_purchasePrice", price);
            jsonObject.put("p_signature", p.getSignature());
            jsonObject.put("p_itemtype", p.getProductId());
            jsonObject.put("p_itemcount", "1");
            jsonObject.put("p_purchage_item_name", p.getProductId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pResult.addParams("p_info", jsonObject.toString());
        pResult.execute(true, true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PURCHASE_REQUEST:
                    /*
                     * launchPurchaseFlowAsync API 호출 시 전달받은 intent 데이터를 handlePurchaseData를 통하여 응답값을 파싱합니다.
                     * 파싱 이후 응답 결과를 launchPurchaseFlowAsync 호출 시 넘겨준 PurchaseFlowListener 를 통하여 전달합니다.
                     */
                    if (resultCode == Activity.RESULT_OK) {
                        if (mPurchaseClient.handlePurchaseData(data) == false) {
                            Log.e("ONE", "onActivityResult handlePurchaseData false ");
                            // listener is null
                        }
                    } else {
                        Log.e("ONE", "onActivityResult user canceled");
                        // user canceled , do nothing..
                    }
                    break;
            }
        }
    }

    private void processPay() {
        if (StringUtil.isNull(UserPref.getIdx(this))) {
            Toast.makeText(this, "등록 된 정보가 없어 결제를 진행할 수 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            if (isListenerCalled) {
                if (!UserPref.getSubscriptionState(act)) {
                    String productName = ""; // "" 일때는 개발자센터에 등록된 상품명 노출
                    String productType = IapEnum.ProductType.AUTO.getType(); // "
                    String devPayload = StringUtil.DEVELOPERPAYLOAD;
                    String gameUserId = ""; // 디폴트 ""
                    boolean promotionApplicable = false;
                    mPurchaseClient.launchPurchaseFlowAsync(StringUtil.IAP_API_VERSION, act, PURCHASE_REQUEST, id, productName, productType, devPayload, gameUserId, promotionApplicable, mPurchaseFlowListener);
                } else {
                    Toast.makeText(this, "이미 구독중입니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "결제정보를 받아오고 있습니다. 잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
//            checkTicket();
        }
    }

    //4. 결제 완료 리스너
    PurchaseClient.PurchaseFlowListener mPurchaseFlowListener = new PurchaseClient.PurchaseFlowListener() {
        @Override
        public void onSuccess(com.onestore.iap.api.PurchaseData purchaseData) {
            Log.d("ONE", "launchPurchaseFlowAsync onSuccess, " + purchaseData.toString());
            // 구매완료 후 developer payload 검증을 수해한다.
            if (!purchaseData.getDeveloperPayload().equalsIgnoreCase(StringUtil.DEVELOPERPAYLOAD)) {
                Log.d("ONE", "launchPurchaseFlowAsync onSuccess, Payload is not valid.");
                return;
            }

            // 구매완료 후 signature 검증을 수행한다.
            if (purchaseData.getProductId().equals(id)) {
                {
                    Log.i(StringUtil.TAG, "onSuccess: ");
                    UserPref.saveSubscriptionState(act, true);
                    sendPurchaseResult(purchaseData);
                }
            } else {
                Log.d("ONE", "launchPurchaseFlowAsync onSuccess, Signature is not valid.");
                return;
            }

        }

        @Override
        public void onError(IapResult result) {
            Log.e("ONE", "launchPurchaseFlowAsync onError, " + result.toString());
        }

        @Override
        public void onErrorRemoteException() {
            Log.e("ONE", "launchPurchaseFlowAsync onError, 원스토어 서비스와 연결을 할 수 없습니다");
        }

        @Override
        public void onErrorSecurityException() {
            Log.e("ONE", "launchPurchaseFlowAsync onError, 비정상 앱에서 결제가 요청되었습니다");
        }

        @Override
        public void onErrorNeedUpdateException() {
            Log.e("ONE", "launchPurchaseFlowAsync onError, 원스토어 서비스앱의 업데이트가 필요합니다");
        }
    };


}
