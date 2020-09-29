package kr.core.powerlotto.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;

import kr.core.powerlotto.R;
import kr.core.powerlotto.activity.app;
import kr.core.powerlotto.data.MainWinInfo;
import kr.core.powerlotto.data.WinnumsInfo;
import kr.core.powerlotto.util.LottoUtil;
import kr.core.powerlotto.util.StringUtil;

public class WinnumListAdapter extends RecyclerView.Adapter<WinnumListAdapter.ViewHolder>{

    Activity act;
    ArrayList<MainWinInfo> list;

    public WinnumListAdapter(Activity act, ArrayList<MainWinInfo> list){
        this.act = act;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_winnum,parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_lln_rank.setText(list.get(position).getLln_rank());
        holder.tv_lln_chu_date.setText(list.get(position).getLln_chu_date());
        holder.lln_lucky_cnt.setText(list.get(position).getLln_lucky_cnt());
        holder.tv_lln_lucky_price_one.setText(StringUtil.setNumComma(Long.parseLong(list.get(position).getLln_lucky_price_one())));

        holder.tv_winnum1.setText(list.get(position).getLln_num_1());
        holder.tv_winnum1.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(list.get(position).getLln_num_1()),true));
        holder.tv_winnum2.setText(list.get(position).getLln_num_2());
        holder.tv_winnum2.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(list.get(position).getLln_num_2()),true));
        holder.tv_winnum3.setText(list.get(position).getLln_num_3());
        holder.tv_winnum3.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(list.get(position).getLln_num_3()),true));
        holder.tv_winnum4.setText(list.get(position).getLln_num_4());
        holder.tv_winnum4.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(list.get(position).getLln_num_4()),true));
        holder.tv_winnum5.setText(list.get(position).getLln_num_5());
        holder.tv_winnum5.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(list.get(position).getLln_num_5()),true));
        holder.tv_winnum6.setText(list.get(position).getLln_num_6());
        holder.tv_winnum6.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(list.get(position).getLln_num_6()),true));

        holder.tv_bonus.setText(list.get(position).getLln_num_bonus());
        holder.tv_bonus.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(list.get(position).getLln_num_bonus()),true));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_winnum1,tv_winnum2,tv_winnum3,tv_winnum4,tv_winnum5,tv_winnum6,tv_bonus;
        TextView tv_lln_rank,tv_lln_chu_date,lln_lucky_cnt,tv_lln_lucky_price_one;
        public ViewHolder(View v){
            super(v);

            tv_lln_rank = v.findViewById(R.id.tv_lln_rank);
            tv_lln_chu_date = v.findViewById(R.id.tv_lln_chu_date);
            lln_lucky_cnt = v.findViewById(R.id.lln_lucky_cnt);
            tv_lln_lucky_price_one = v.findViewById(R.id.tv_lln_lucky_price_one);

            tv_winnum1 = v.findViewById(R.id.tv_winnum1);
            tv_winnum2 = v.findViewById(R.id.tv_winnum2);
            tv_winnum3 = v.findViewById(R.id.tv_winnum3);
            tv_winnum4 = v.findViewById(R.id.tv_winnum4);
            tv_winnum5 = v.findViewById(R.id.tv_winnum5);
            tv_winnum6 = v.findViewById(R.id.tv_winnum6);
            tv_bonus = v.findViewById(R.id.tv_bonusnum);

            tv_winnum1.setTypeface(app.jalnan);
            tv_winnum2.setTypeface(app.jalnan);
            tv_winnum3.setTypeface(app.jalnan);
            tv_winnum4.setTypeface(app.jalnan);
            tv_winnum5.setTypeface(app.jalnan);
            tv_winnum6.setTypeface(app.jalnan);
            tv_bonus.setTypeface(app.jalnan);

        }
    }
}
