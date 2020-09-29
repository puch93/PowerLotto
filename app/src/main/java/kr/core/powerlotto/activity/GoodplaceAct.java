package kr.core.powerlotto.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.Toast;


import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kr.core.powerlotto.R;
import kr.core.powerlotto.adapter.GoodplaceListAdapter;
import kr.core.powerlotto.adapter.WinnumListAdapter;
import kr.core.powerlotto.customWidget.CustomSpinnerAdapter;
import kr.core.powerlotto.data.GoodplaceInfo;
import kr.core.powerlotto.data.WinnumsInfo;
import kr.core.powerlotto.databinding.ActivityGoodplaceBinding;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.util.ConvertPxDp;
import kr.core.powerlotto.util.LayoutWebView;
import kr.core.powerlotto.util.StringUtil;

public class GoodplaceAct extends BaseAct implements View.OnClickListener, MapView.MapViewEventListener, MapView.POIItemEventListener, MapView.OpenAPIKeyAuthenticationResultListener {

    ActivityGoodplaceBinding binding;

    GoodplaceListAdapter adapter;

    ArrayList<GoodplaceInfo> list1st = new ArrayList<>();
    ArrayList<GoodplaceInfo> list2nd = new ArrayList<>();

    MapView mapView;

    GoodplaceInfo selData;
    Activity act;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_goodplace);
        act = this;
        getCoupaBanner();
//        setBannerArea();

        mapView = new MapView(this);
        binding.mapView.addView(mapView);

        setSpinner();
        getPlaceInfo();

        binding.btnClose.setOnClickListener(this);
        binding.btnSidemenu.setOnClickListener(this);

        binding.btnSearch.setOnClickListener(this);
        binding.btnPopclose.setOnClickListener(this);
        binding.btn1st.setOnClickListener(this);
        binding.btn2nd.setOnClickListener(this);
        binding.areaSelectGrade.setOnClickListener(this);
        binding.areaSelectPlace.setOnClickListener(this);

        binding.btn1st.setSelected(true);

        binding.rcvPlacelist.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GoodplaceListAdapter(this);
        binding.rcvPlacelist.setAdapter(adapter);

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
//                                Toast.makeText(GoodplaceAct.this, "연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        if (binding.llPlaceinfoArea.getVisibility() == View.VISIBLE) {
            popClose();
        } else {
            super.onBackPressed();
        }
    }

    private void setSpinner() {
        // 최신 회차 구하는 방법??
        if (StringUtil.isNull(app.lln_rank)) {
            binding.areaTextGrade.setText("883");
        } else {
            binding.areaTextGrade.setText(app.lln_rank);
        }

        binding.areaTextPlace.setText("전체");

    }

    public void spinnerSetting(Spinner spinner, ArrayList<String> data, Context context) {
        int dataSize = 0;

        if (data.size() > 6) {
            dataSize = 6;
        } else {
            dataSize = data.size();
        }

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            ListPopupWindow window = (ListPopupWindow) popup.get(spinner);
            window.setHeight((int) ConvertPxDp.convertDpToPixel((dataSize * 50), context));

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setPopView(GoodplaceInfo data) {
        binding.llPlaceinfoArea.setVisibility(View.VISIBLE);
        binding.ivPopback.setVisibility(View.VISIBLE);
        binding.tvPoptitle.setText(data.getStorename());
        binding.tvInfostorename.setText(data.getStorename());
        binding.tvInfotel.setText(data.getTel());
        binding.tvInfoaddr.setText(data.getAddr());

        binding.tvMaprank.setText(data.getRank() + "등 - ");
        binding.tvMapamount.setText(StringUtil.setNumComma(Long.parseLong(data.getPrice())));

        selData = data;

        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(data.getLat()), Double.parseDouble(data.getLon())), true);
        mapView.setZoomLevel(2, true);
        mapView.zoomIn(true);
        mapView.zoomOut(true);

        MapPOIItem marker = new MapPOIItem();
        marker.setItemName(data.getStorename());
        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(data.getLat()), Double.parseDouble(data.getLon())));
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

        mapView.addPOIItem(marker);
    }

    private void getPlaceInfo() {
        ReqBasic placeInfo = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
//                {"1stplace":"\ud574\ub2f9 \ud68c\ucc28 1\ub4f1 \ub2f9\ucca8 \uac80\uc0c9\uc7a5\uc18c\uac00 \uc5c6\uc2b5\ub2c8\ub2e4.","2stplace":"\ud574\ub2f9 \ud68c\ucc28 2\ub4f1 \ub2f9\ucca8 \uac80\uc0c9\uc7a5\uc18c\uac00 \uc5c6\uc2b5\ub2c8\ub2e4.","1stprice":"1081093920","2ndprice":"33996665"}
                Log.d(StringUtil.TAG, "code: " + resultCode);
                Log.d(StringUtil.TAG, "placeinfo: " + resultData.getResult());

                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        Object p1st = jo.get("1stplace");
                        Object p2nd = jo.get("2stplace");

                        jo.getString("1stprice");
                        jo.getString("2ndprice");
                        jo.getString("chudate");

                        if (list1st.size() > 0) {
                            list1st.clear();
                        }
                        if (list2nd.size() > 0) {
                            list2nd.clear();
                        }

                        if (p1st instanceof JSONArray) {
                            JSONArray place1st = new JSONArray(jo.getString("1stplace"));
                            list1st.addAll(addPlaceArr(place1st, jo.getString("1stprice"), jo.getString("chudate")));
                        } else {
                            if (jo.has("1stplace")) {
                                Log.d(StringUtil.TAG, "1stplace : obj");
                                Toast.makeText(GoodplaceAct.this, jo.getString("1stplace"), Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (p2nd instanceof JSONArray) {
                            JSONArray place2nd = new JSONArray(jo.getString("2stplace"));
                            list2nd.addAll(addPlaceArr(place2nd, jo.getString("2ndprice"), jo.getString("chudate")));
                        } else {
                            if (jo.has("2stplace")) {
                                Toast.makeText(GoodplaceAct.this, jo.getString("2stplace"), Toast.LENGTH_SHORT).show();
                                Log.d(StringUtil.TAG, "2stplace : obj");
                            }
                        }

                        if (binding.btn1st.isSelected()) {
                            adapter.setList(list1st);
                        } else {
                            adapter.setList(list2nd);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                        if (list1st.size() > 0) {
                            list1st.clear();
                        }

                        if (list2nd.size() > 0) {
                            list2nd.clear();
                        }

                        if (binding.btn1st.isSelected()) {
                            adapter.setList(list1st);
                        } else {
                            adapter.setList(list2nd);
                        }
                        Toast.makeText(GoodplaceAct.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    if (list1st.size() > 0) {
                        list1st.clear();
                    }

                    if (list2nd.size() > 0) {
                        list2nd.clear();
                    }

                    if (binding.btn1st.isSelected()) {
                        adapter.setList(list1st);
                    } else {
                        adapter.setList(list2nd);
                    }
                    Toast.makeText(GoodplaceAct.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }
            }
        };

        placeInfo.addParams("APPCONNECTCODE", "APP");
        placeInfo.addParams("dbControl", "SchLottoLuckyPlace");
        placeInfo.addParams("drwNo", binding.areaTextGrade.getText().toString());

        if (binding.areaTextPlace.getText().toString().equals("전체")) {
            placeInfo.addParams("store_loc", "");
        } else {
            placeInfo.addParams("store_loc", binding.areaTextPlace.getText().toString());
        }
        // keyword 입력값 있는 경우
        if (binding.etSearchstr.length() > 0) {
            placeInfo.addParams("keyword", binding.etSearchstr.getText().toString());
        }
        placeInfo.execute(true, true);

    }

    private ArrayList<GoodplaceInfo> addPlaceArr(JSONArray ja, String price, String date) {

        ArrayList<GoodplaceInfo> tmpList = new ArrayList<>();
        if (ja.length() > 0) {

            try {
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    GoodplaceInfo data = new GoodplaceInfo();

                    data.setLlsp_idx(jo.getString("llsp_idx"));
                    data.setLln_idx(jo.getString("lln_idx"));
                    data.setRank(jo.getString("rank"));
                    data.setGametype(jo.getString("gametype"));
                    data.setLlspd_idx(jo.getString("llspd_idx"));
                    data.setStorename(jo.getString("storename"));
                    data.setStore_loc(jo.getString("store_loc"));
                    data.setAddr(jo.getString("addr"));
                    data.setLat(jo.getString("lat"));
                    data.setLon(jo.getString("lon"));
                    data.setTel(jo.getString("tel"));
                    data.setRegdate(date);

                    data.setPrice(price);

                    tmpList.add(data);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(GoodplaceAct.this, getString(R.string.net_errmsg) + "\n문제 : 리스트 데이터 형태", Toast.LENGTH_SHORT).show();
            }
        }
        return tmpList;
    }

    private ArrayList<String> setSpinnerList(int arrId) {
        ArrayList<String> tmpList = new ArrayList<>();
        for (String item : getResources().getStringArray(arrId)) {
            tmpList.add(item);
        }
        return tmpList;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                finish();
                break;
            case R.id.btn_search:
                getPlaceInfo();
//                Snackbar.make(binding.getRoot(), "조회", BaseTransientBottomBar.LENGTH_SHORT).show();
                break;
            case R.id.btn_1st:
                binding.btn2nd.setSelected(false);
                binding.btn1st.setSelected(true);
                adapter.setList(list1st);
                break;
            case R.id.btn_2nd:
                binding.btn1st.setSelected(false);
                binding.btn2nd.setSelected(true);
                adapter.setList(list2nd);
                break;
            case R.id.btn_popclose:
                popClose();
                break;

            case R.id.area_select_grade:
                startActivityForResult(new Intent(this, PickerDlg.class)
                                .putExtra("selected", Integer.parseInt(binding.areaTextGrade.getText().toString()))
                        , 101);
                break;

            case R.id.area_select_place:
                startActivityForResult(new Intent(this, PickerPlaceDlg.class)
                                .putExtra("selected", binding.areaTextPlace.getText().toString())
                        , 102);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 101) {
                String value = data.getStringExtra("value");
                binding.areaTextGrade.setText(value);
            } else {
                String value = data.getStringExtra("value");
                binding.areaTextPlace.setText(value);
            }
        }
    }

    private void popClose() {
        binding.llPlaceinfoArea.setVisibility(View.GONE);
        binding.ivPopback.setVisibility(View.GONE);
        mapView.removeAllPOIItems();
        selData = null;
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onDaumMapOpenAPIKeyAuthenticationResult(MapView mapView, int i, String s) {

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        Log.d(StringUtil.TAG, "onPOIItemSelected: ");

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        Log.d(StringUtil.TAG, "onCalloutBalloonOfPOIItemTouched: ");
        if (selData != null) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + selData.getTel()));
            startActivity(intent);
        }
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        Log.d(StringUtil.TAG, "onCalloutBalloonOfPOIItemTouched: ");
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }
}
