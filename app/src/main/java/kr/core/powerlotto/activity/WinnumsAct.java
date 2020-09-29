package kr.core.powerlotto.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

import kr.core.powerlotto.R;
import kr.core.powerlotto.adapter.WinnumListAdapter;
import kr.core.powerlotto.customWidget.CustomSpinnerAdapter;
import kr.core.powerlotto.data.MainWinInfo;
import kr.core.powerlotto.data.WinnumsInfo;
import kr.core.powerlotto.databinding.ActivityWinnumsBinding;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.util.ConvertPxDp;
import kr.core.powerlotto.util.LayoutWebView;
import kr.core.powerlotto.util.StringUtil;

public class WinnumsAct extends BaseAct implements View.OnClickListener {

    ActivityWinnumsBinding binding;

    WinnumListAdapter adapter;

    ArrayList<MainWinInfo> list = new ArrayList<>();

    LinearLayoutManager mManager;
    public boolean isScroll = false;
    public int page = 1;
    int lastPage = 0;
    Activity act;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_winnums);
        act = this;
        getCoupaBanner();
//        setBannerArea();

        binding.btnClose.setOnClickListener(this);
        binding.btnSidemenu.setOnClickListener(this);
        binding.btnSearch.setOnClickListener(this);
        binding.areaSelect.setOnClickListener(this);

        mManager = new LinearLayoutManager(this);
        binding.rcvWinnums.setLayoutManager(mManager);
        adapter = new WinnumListAdapter(this, list);
        binding.rcvWinnums.setAdapter(adapter);

        // 최신 회차 구하는 방법??
        if (StringUtil.isNull(app.lln_rank)) {
            binding.areaText.setText("883");
        } else {
            binding.areaText.setText(app.lln_rank);
        }

        getWinnumInfo();

        binding.rcvWinnums.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalCount = mManager.getItemCount();
                int lastComplete = mManager.findLastCompletelyVisibleItemPosition();
                if (!isScroll) {
                    if (totalCount - 1 == lastComplete) {
                        if (!list.get(list.size() - 1).getLln_rank().equalsIgnoreCase("1")) {
                            isScroll = true;
                            page++;
                            getWinnumInfo();
                        }
                    }
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
//                                Toast.makeText(WinnumsAct.this, "연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
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

    private void getWinnumInfo() {
        isScroll = true;
        ReqBasic winnumInfo = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(StringUtil.TAG, "code: " + resultCode);
                Log.d(StringUtil.TAG, "getWinnumInfo: " + resultData.getResult());

                if (resultData.getResult() != null) {

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.has("data")) {
                            JSONArray ja = new JSONArray(jo.getString("data"));
                            if (ja.length() > 0) {
                                ArrayList<MainWinInfo> tmplist = new ArrayList<>();

                                for (int i = 0; i < ja.length(); i++) {
                                    JSONObject obj = ja.getJSONObject(i);
                                    MainWinInfo data = new MainWinInfo();

                                    data.setLln_idx(obj.getString("lln_idx"));
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

                                list.addAll(tmplist);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        isScroll = false;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        isScroll = false;
                        if (list.size() > 0) {
                            list.clear();
                        }
                        adapter.notifyDataSetChanged();
                        Toast.makeText(WinnumsAct.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    isScroll = false;
                    if (list.size() > 0) {
                        list.clear();
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(WinnumsAct.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }

            }
        };

        winnumInfo.addParams("APPCONNECTCODE", "APP");
        winnumInfo.addParams("dbControl", "KindWinNumber");
        winnumInfo.addParams("kindnum", binding.areaText.getText().toString());
        winnumInfo.addParams("pagenum", String.valueOf(page));
        winnumInfo.execute(true, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String value = data.getStringExtra("value");
            binding.areaText.setText(value);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                finish();
                break;
            case R.id.btn_search:
                if (list.size() > 0) {
                    list.clear();
                }
                page = 1;
                getWinnumInfo();
                break;

            case R.id.area_select:
                startActivityForResult(new Intent(this, PickerDlg.class)
                        .putExtra("selected", Integer.parseInt(binding.areaText.getText().toString()))
                        , 101);
                break;
        }
    }
}
