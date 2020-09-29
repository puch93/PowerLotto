package kr.core.powerlotto.adapter;

import android.app.Activity;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;


import java.util.ArrayList;

import kr.core.powerlotto.R;
import kr.core.powerlotto.activity.app;
import kr.core.powerlotto.data.MainWinInfo;

public class MainviewPagerAdapter extends PagerAdapter {

    LayoutInflater inflater;
    int count;
    ArrayList<MainWinInfo> list;
    Activity act;

    public MainviewPagerAdapter(Activity act, LayoutInflater inflater, int count, ArrayList<MainWinInfo> list){
//    public MainviewPagerAdapter(Activity act, LayoutInflater inflater, int count){
        this.inflater = inflater;
        this.count = count;
        this.act = act;
        this.list = list;
    }

    @Override
    public int getCount() {
        return count;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View v = null;
        v = inflater.inflate(R.layout.maintop_autopagelayout,null);

        TextView date = v.findViewById(R.id.tv_date);
        TextView episode = v.findViewById(R.id.tv_episode);
        TextView winner = v.findViewById(R.id.tv_winnercnt);
        TextView eptime = v.findViewById(R.id.tv_eptime);


        episode.setTypeface(app.jalnan);
        eptime.setTypeface(app.jalnan);

        date.setText(list.get(position).getLln_chu_date());
        episode.setText(list.get(position).getLln_rank());
        winner.setText(list.get(position).getLln_lucky_cnt());

        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }
}
