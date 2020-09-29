package kr.core.powerlotto.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.core.powerlotto.R;
import kr.core.powerlotto.adapter.RecievenumListAdapter;
import kr.core.powerlotto.adapter.WinnumListAdapter;
import kr.core.powerlotto.customWidget.CustomSpinnerAdapter;
import kr.core.powerlotto.data.RecievenumInfo;
import kr.core.powerlotto.data.WinnumsInfo;
import kr.core.powerlotto.databinding.ActivityRecievenumBinding;
import kr.core.powerlotto.insidedata.UserPref;
import kr.core.powerlotto.network.ReqBasic;
import kr.core.powerlotto.network.netUtil.HttpResult;
import kr.core.powerlotto.network.netUtil.NetUrls;
import kr.core.powerlotto.util.LayoutWebView;
import kr.core.powerlotto.util.StringUtil;

public class RecievenumAct extends BaseAct implements View.OnClickListener {
    Activity act;
    ActivityRecievenumBinding binding;

    RecievenumListAdapter adapter;

    ArrayList<RecievenumInfo> list = new ArrayList<>();

    String mode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_recievenum);
        act = this;
        getCoupaBanner();
//        setBannerArea();

        binding.btnClose.setOnClickListener(this);
        binding.btnSidemenu.setOnClickListener(this);

        binding.btnAllnumlist.setOnClickListener(this);
        binding.btnManunumlist.setOnClickListener(this);
        binding.btnRandnumlist.setOnClickListener(this);
        binding.btnAdvnumlist.setOnClickListener(this);

        CustomSpinnerAdapter sortAdapter = new CustomSpinnerAdapter(this,setSpinnerList(R.array.sort_type));
        binding.spnSort.setAdapter(sortAdapter);

        CustomSpinnerAdapter gsAdapter = new CustomSpinnerAdapter(this,setSpinnerList(R.array.winlose));
        binding.spnWinlose.setAdapter(gsAdapter);

        binding.rcvRecievenum.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecievenumListAdapter(this,list);
        binding.rcvRecievenum.setAdapter(adapter);


//        binding.btnRandnumlist.setSelected(true);
//        mode = "rand";
        binding.btnAllnumlist.setSelected(true);
        binding.triMenu01.setVisibility(View.VISIBLE);
        mode = "all";
        getRecieveNum();
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
//                                Toast.makeText(RecievenumAct.this, "연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
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

    private ArrayList<String > setSpinnerList(int arrId){
        ArrayList<String> tmpList = new ArrayList<>();
        for(String item : getResources().getStringArray(arrId)){
            tmpList.add(item);
        }
        return tmpList;
    }

    private void getRecieveNum(){
        ReqBasic recieveNum = new ReqBasic(this, NetUrls.DOMAIN) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.d(StringUtil.TAG, "code: "+resultCode);
                Log.d(StringUtil.TAG, "getRecieveNum: "+resultData.getResult());

                if (resultData.getResult() != null){

                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        Object recieveList = jo.get("list");

                        if (recieveList instanceof JSONArray){
                            JSONArray rlist = new JSONArray(jo.getString("list"));
                            ArrayList<RecievenumInfo> tmplist = new ArrayList<>();
                            if (rlist.length() > 0) {
                                for (int i = 0; i < rlist.length(); i++) {
                                    JSONObject obj = rlist.getJSONObject(i);
                                    RecievenumInfo data = new RecievenumInfo();

                                    data.setDrwNo(obj.getString("drwNo"));
                                    data.setType(obj.getString("gametype"));
                                    data.setRegidx(obj.getString("regidx"));
                                    data.setDate(obj.getString("date").split(" ")[0]);

                                    JSONObject gameresult = obj.getJSONObject("gameresult");
                                    Object gres = gameresult.get("case1");
                                    if (gres instanceof JSONArray) {
                                        Log.d(StringUtil.TAG, "case1: " + gameresult.getJSONArray("case1"));
                                        data.setGameResult(gameresult.getJSONArray("case1"));
                                    } else {
                                        // jarray 없을 경우
                                    }

                                    tmplist.add(data);
                                }
                            }else{
                                Toast.makeText(RecievenumAct.this,"받은번호가 없습니다.",Toast.LENGTH_SHORT).show();
                            }

                            if (list.size() > 0){
                                list.clear();
                            }

                            list.addAll(tmplist);
                            adapter.notifyDataSetChanged();
                        }else{
                            if (jo.has("list")){
                                Toast.makeText(RecievenumAct.this,"데이터가 없습니다.",Toast.LENGTH_SHORT).show();
                            }
                            if (list.size() > 0){
                                list.clear();
                            }

                            adapter.notifyDataSetChanged();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (list.size() > 0){
                            list.clear();
                        }
                        adapter.notifyDataSetChanged();
                        Toast.makeText(RecievenumAct.this, getString(R.string.net_errmsg) + "\n문제 : 데이터 형태", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    if (list.size() > 0){
                        list.clear();
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(RecievenumAct.this, getString(R.string.net_errmsg) + "\n문제 : 값이 없음", Toast.LENGTH_SHORT).show();
                }

            }
        };

        recieveNum.addParams("APPCONNECTCODE","APP");
        recieveNum.addParams("dbControl","LottoNumMake");
        recieveNum.addParams("midx", UserPref.getIdx(this));
        if (!StringUtil.isNull(mode)){
            recieveNum.addParams("makemode",mode);
        }
        recieveNum.execute(true,true);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_close:
                finish();
                break;
            case R.id.btn_allnumlist:
                mode = "all";
                binding.btnAdvnumlist.setSelected(false);
                binding.btnRandnumlist.setSelected(false);
                binding.btnManunumlist.setSelected(false);
                binding.btnAllnumlist.setSelected(true);

                binding.triMenu01.setVisibility(View.VISIBLE);
                binding.triMenu02.setVisibility(View.INVISIBLE);
                binding.triMenu03.setVisibility(View.INVISIBLE);
                binding.triMenu04.setVisibility(View.INVISIBLE);
                getRecieveNum();
                break;

            case R.id.btn_manunumlist:
                mode = "custom";
                binding.btnAllnumlist.setSelected(false);
                binding.btnAdvnumlist.setSelected(false);
                binding.btnRandnumlist.setSelected(false);
                binding.btnManunumlist.setSelected(true);

                binding.triMenu01.setVisibility(View.INVISIBLE);
                binding.triMenu02.setVisibility(View.VISIBLE);
                binding.triMenu03.setVisibility(View.INVISIBLE);
                binding.triMenu04.setVisibility(View.INVISIBLE);
                getRecieveNum();
                break;
            case R.id.btn_randnumlist:
                mode = "rand";
                binding.btnAllnumlist.setSelected(false);
                binding.btnManunumlist.setSelected(false);
                binding.btnAdvnumlist.setSelected(false);
                binding.btnRandnumlist.setSelected(true);

                binding.triMenu01.setVisibility(View.INVISIBLE);
                binding.triMenu02.setVisibility(View.INVISIBLE);
                binding.triMenu03.setVisibility(View.VISIBLE);
                binding.triMenu04.setVisibility(View.INVISIBLE);
                getRecieveNum();
                break;
            case R.id.btn_advnumlist:
                mode = "adv";
                binding.btnAllnumlist.setSelected(false);
                binding.btnManunumlist.setSelected(false);
                binding.btnRandnumlist.setSelected(false);
                binding.btnAdvnumlist.setSelected(true);

                binding.triMenu01.setVisibility(View.INVISIBLE);
                binding.triMenu02.setVisibility(View.INVISIBLE);
                binding.triMenu03.setVisibility(View.INVISIBLE);
                binding.triMenu04.setVisibility(View.VISIBLE);
                getRecieveNum();
                break;
        }
    }
}
