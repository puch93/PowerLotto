package kr.core.powerlotto.adapter;

import android.app.Activity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.util.ArrayList;

import kr.core.powerlotto.R;
import kr.core.powerlotto.activity.app;
import kr.core.powerlotto.data.RecievenumInfo;
import kr.core.powerlotto.util.LottoUtil;
import kr.core.powerlotto.util.StringUtil;

public class RecievenumListAdapter extends RecyclerView.Adapter<RecievenumListAdapter.ViewHolder> {

    Activity act;
    ArrayList<RecievenumInfo> list;

    public RecievenumListAdapter(Activity act, ArrayList<RecievenumInfo> list) {
        this.act = act;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recievenum2, parent, false);
        RecievenumListAdapter.ViewHolder vh = new RecievenumListAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_no.setText(list.get(position).getRegidx() + ".");
        holder.tv_date.setText(list.get(position).getDate());

        String[] drwNo = list.get(position).getDrwNo().split(",");

        holder.tv_num1.setText(drwNo[0].trim());
        holder.tv_num1.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(drwNo[0].trim()), false));
        holder.tv_num2.setText(drwNo[1].trim());
        holder.tv_num2.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(drwNo[1].trim()), false));
        holder.tv_num3.setText(drwNo[2].trim());
        holder.tv_num3.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(drwNo[2].trim()), false));
        holder.tv_num4.setText(drwNo[3].trim());
        holder.tv_num4.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(drwNo[3].trim()), false));
        holder.tv_num5.setText(drwNo[4].trim());
        holder.tv_num5.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(drwNo[4].trim()), false));
        holder.tv_num6.setText(drwNo[5].trim());
        holder.tv_num6.setBackgroundResource(LottoUtil.getBallResource(Integer.parseInt(drwNo[5].trim()), false));


        if(!StringUtil.isNull(list.get(position).getType())) {
            holder.tv_type.setText(LottoUtil.getResultType(list.get(position).getType()));
            holder.tv_type.setBackgroundResource(LottoUtil.getResultTypeResource(list.get(position).getType()));
        }

//        holder.tv_result.setText(LottoUtil.resultStr(list.get(position).getGameResult()));
        if (list.get(position).getGameResult().length() == 0) {
            holder.tv_result.setText("낙첨");
        } else {
            try {
                Log.d(StringUtil.TAG, "split1: " + list.get(position).getGameResult().getString(0).split("당첨금액 : ")[0]);
                Log.d(StringUtil.TAG, "split2: " + list.get(position).getGameResult().getString(0).split("당첨금액 : ")[1]);

                String price = StringUtil.setNumComma(Long.parseLong(list.get(position).getGameResult().getString(0).split("당첨금액 : ")[1])) + "원";
                holder.price.setText(price);

//                SpannableStringBuilder sp = new SpannableStringBuilder(price);
//                sp.setSpan(new ForegroundColorSpan(act.getResources().getColor(R.color.c_pricetext)), 0, price.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


                String[] info = list.get(position).getGameResult().getString(0).split("당첨금액 : ")[0].split(":");
                CharSequence cs = TextUtils.concat("제 ", info[0].split("\\[")[0].trim(), " ", info[1].replaceAll(" ", ""));
                holder.tv_result.setText(cs);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_result, tv_date, tv_no, tv_num1, tv_num2, tv_num3, tv_num4, tv_num5, tv_num6;
        TextView tv_type, price;

        public ViewHolder(View v) {
            super(v);
            tv_result = v.findViewById(R.id.tv_result);
            tv_date = v.findViewById(R.id.tv_date);
            tv_no = v.findViewById(R.id.tv_no);
            tv_num1 = v.findViewById(R.id.tv_num1);
            tv_num2 = v.findViewById(R.id.tv_num2);
            tv_num3 = v.findViewById(R.id.tv_num3);
            tv_num4 = v.findViewById(R.id.tv_num4);
            tv_num5 = v.findViewById(R.id.tv_num5);
            tv_num6 = v.findViewById(R.id.tv_num6);
            tv_type = v.findViewById(R.id.tv_type);
            price = v.findViewById(R.id.price);

            tv_num1.setTypeface(app.jalnan);
            tv_num2.setTypeface(app.jalnan);
            tv_num3.setTypeface(app.jalnan);
            tv_num4.setTypeface(app.jalnan);
            tv_num5.setTypeface(app.jalnan);
            tv_num6.setTypeface(app.jalnan);

        }
    }
}
