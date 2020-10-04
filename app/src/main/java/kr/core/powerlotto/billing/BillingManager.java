package kr.core.powerlotto.billing;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

import kr.core.powerlotto.util.StringUtil;

public class BillingManager implements PurchasesUpdatedListener {

    private BillingClient mBillingClient;
    private Activity act;

    public static String USE1MONTH = "1month";
    public static String USE3MONTH = "3month";
    public static String USE6MONTH = "6month";
    public static String USE12MONTH = "12month";

    public enum connectStatusTypes {wating, connected, fail, disconnected}

    public connectStatusTypes connectStatus = connectStatusTypes.wating;

    public List<SkuDetails> mSkuDetailsList;

    private ConsumeResponseListener mConsumeListener;
    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener;

    public AfterBilling afterListener;

    String subscription_state;

    boolean isSubcribe = false;

    public interface AfterBilling {
        void sendResult(Purchase purchase);

        void getSubsriptionState(String subscription, Purchase purchase);
    }


    public BillingManager(Activity act, AfterBilling after) {
        this.act = act;
        this.afterListener = after;

        Log.e(StringUtil.TAG, "구글 결제 매니저를 초기화 하고 있습니다.");

        mBillingClient = BillingClient.newBuilder(act).setListener(this).enablePendingPurchases().build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    connectStatus = connectStatusTypes.connected;
                    Log.e(StringUtil.TAG, "구글 결제 서버에 접속을 성공하였습니다.");

                    getSkuDetailList();

                    Purchase.PurchasesResult result = mBillingClient.queryPurchases(BillingClient.SkuType.SUBS);

                    Log.i(StringUtil.TAG, "result: " + result);
                    Log.i(StringUtil.TAG, "getPurchasesList: " + result.getPurchasesList());
                    Log.i(StringUtil.TAG, "getResponseCode: " + result.getResponseCode());

                    BillingResult result_billing = result.getBillingResult();
                    Log.i(StringUtil.TAG, "result_billing.getResponseCode: " + result_billing.getResponseCode());
                    Log.i(StringUtil.TAG, "result_billing.getDebugMessage: " + result_billing.getDebugMessage());

                    List<Purchase> purchasedList = result.getPurchasesList();
                    if (purchasedList == null || purchasedList.size() == 0) {
                        subscription_state = "N";
                        afterListener.getSubsriptionState(subscription_state, null);
                    } else {
                        for (int i = 0; i < purchasedList.size(); i++) {
                            Log.d(StringUtil.TAG, "onBillingSetupFinished: "+purchasedList.get(i));
                            if (purchasedList.get(i).getSku().equals(USE1MONTH) || purchasedList.get(i).getSku().equals(USE3MONTH)
                            || purchasedList.get(i).getSku().equals(USE6MONTH) || purchasedList.get(i).getSku().equals(USE12MONTH)) {
                                subscription_state = "Y";
                                afterListener.getSubsriptionState(subscription_state, null);
                                break;
                            } else {
                                if (i == purchasedList.size() - 1) {
                                    subscription_state = "N";
                                    afterListener.getSubsriptionState(subscription_state, null);
                                }
                            }
                        }
                    }

                    //오류
                } else {
                    connectStatus = connectStatusTypes.fail;
                    Log.e(StringUtil.TAG, "구글 결제 서버 접속에 실패하였습니다.\n오류코드: " + billingResult.getResponseCode());
                    subscription_state = "noAccount";
                    afterListener.getSubsriptionState(subscription_state, null);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                connectStatus = connectStatusTypes.disconnected;
                Log.e(StringUtil.TAG, "구글 결제 서버와 접속이 끊어졌습니다.");
            }
        });

        mConsumeListener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.e(StringUtil.TAG, "상품을 성공적으로 소모하였습니다. 소모된 상품 => " + purchaseToken);
                    return;
                } else {
                    Log.e(StringUtil.TAG, "상품 소모에 실패하였습니다. 오류코드 (" + billingResult.getResponseCode() + "), 대상 상품 코드: " + purchaseToken);
                    return;
                }
            }
        };

        acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.e(StringUtil.TAG, "상품을 성공적으로 소모하였습니다.");
                    return;
                } else {
                    Log.e(StringUtil.TAG, "상품 소모에 실패하였습니다. 오류코드 (" + billingResult.getResponseCode() + ")");
                    return;
                }
            }
        };
    }

    //구입 가능한 상품의 리스트를 받아 오는 메소드
    public void getSkuDetailList() {
        //구글 상품 정보들의 ID를 만들어 줌
        List<String> Sku_ID_List = new ArrayList<>();
        Sku_ID_List.add(USE1MONTH);
        Sku_ID_List.add(USE3MONTH);
        Sku_ID_List.add(USE6MONTH);
        Sku_ID_List.add(USE12MONTH);
//
//        Sku_ID_List.add(StringUtil.MESSAGE01);
//        Sku_ID_List.add(StringUtil.MESSAGE02);


        //1회성 소모품에 대한 SkuDetailsList 객체를 만듬
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();

        params.setSkusList(Sku_ID_List).setType(BillingClient.SkuType.SUBS);


        //비동기 상태로 앱의 정보를 가지고 옴
        mBillingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                //상품 정보를 가지고 오지 못한 경우
                if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    Log.e(StringUtil.TAG, "상품 정보를 가지고 오던 중 오류가 발생했습니다.\n오류코드: " + billingResult.getResponseCode());
                    subscription_state = "error";
                    return;
                }

                if (skuDetailsList == null) {
                    Log.e(StringUtil.TAG, "상품 정보가 존재하지 않습니다.");
                    return;
                }

                //응답 받은 데이터들의 숫자를 출력
                Log.e(StringUtil.TAG, "응답 받은 데이터 숫자: " + skuDetailsList.size());

                //받아온 상품 정보를 차례로 호출
                for (int sku_idx = 0; sku_idx < skuDetailsList.size(); sku_idx++) {
                    //해당 인덱스의 객체를 가지고 옴
                    SkuDetails _skuDetail = skuDetailsList.get(sku_idx);

                    //해당 인덱스의 상품 정보를 출력
                    Log.e(StringUtil.TAG, _skuDetail.getSku() + ": " + _skuDetail.getTitle() + ", " + _skuDetail.getPrice());
                    Log.e(StringUtil.TAG, _skuDetail.getOriginalJson());
                }

                //받은 값을 멤버 변수로 저장
                mSkuDetailsList = skuDetailsList;
            }
        });
    }


    //실제 구입 처리를 하는 메소드
    public void purchase(String item,boolean isSub) {
        Log.e(StringUtil.TAG, "purchase: " + item);
        isSubcribe = isSub;
        SkuDetails skuDetails = null;
        if (mSkuDetailsList != null) {
            for (int i = 0; i < mSkuDetailsList.size(); i++) {
                SkuDetails details = mSkuDetailsList.get(i);
                if (details.getSku().equals(item)) {
                    skuDetails = details;
                    break;
                }
            }
        }

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        BillingResult billingResult = mBillingClient.launchBillingFlow(act, flowParams);


    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        //결제에 성공한 경우
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            Log.e(StringUtil.TAG, "결제에 성공했으며, 아래에 구매한 상품들이 나열됨");

            for (Purchase _pur : purchases) {
                handlePurchase(_pur);
            }
        }

        //사용자가 결제를 취소한 경우
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.e(StringUtil.TAG, "사용자에 의해 결제취소");
        }

        //그 외에 다른 결제 실패 이유
        else {
//            Toast.makeText(act, "결제가 취소 되었습니다. 종료코드: " + billingResult.getResponseCode(), Toast.LENGTH_SHORT).show();
            Log.e(StringUtil.TAG, "결제가 취소 되었습니다. 종료코드: " + billingResult.getResponseCode());
        }
    }


    void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user.

            if (isSubcribe){
                // Acknowledge the purchase if it hasn't already been acknowledged.
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);


                    afterListener.sendResult(purchase);
                    afterListener.getSubsriptionState("Y", null);
                }
            }else{
                ConsumeParams consumeParams =
                        ConsumeParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                mBillingClient.consumeAsync(consumeParams, mConsumeListener);

                afterListener.sendResult(purchase);
            }

        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            // Here you can confirm to the user that they've started the pending
            // purchase, and to complete it, they should follow instructions that
            // are given to them. You can also choose to remind the user in the
            // future to complete the purchase if you detect that it is still
            // pending.
        }
    }
}
