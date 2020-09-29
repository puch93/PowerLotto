package kr.core.powerlotto.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import kr.core.powerlotto.R;
import kr.core.powerlotto.activity.GoodplaceAct;
import kr.core.powerlotto.data.GoodplaceInfo;
import kr.core.powerlotto.util.StringUtil;

public class GoodplaceListAdapter extends RecyclerView.Adapter<GoodplaceListAdapter.ViewHolder> {

    Activity act;
    ArrayList<GoodplaceInfo> list = new ArrayList<>();

//    public GoodplaceListAdapter(Activity act, ArrayList<GoodplaceInfo> list){
    public GoodplaceListAdapter(Activity act){
        this.act = act;
//        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goodplace,parent,false);
        GoodplaceListAdapter.ViewHolder vh = new GoodplaceListAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_storename.setText(list.get(position).getStorename());
        holder.tv_gametype.setText(list.get(position).getGametype());
        holder.tv_address.setText(list.get(position).getAddr());

        if (list.get(position).getRank().equalsIgnoreCase("1")){
            holder.tv_rank.setText("1등");
        }else{
            holder.tv_rank.setText("2등");
        }

        holder.tv_amount.setText(StringUtil.setNumComma(Long.parseLong(list.get(position).getPrice())));
        holder.tv_date.setText(list.get(position).getRegdate());

    }

    public void setList(ArrayList<GoodplaceInfo> addlist){
        if (this.list.size() > 0) {
            this.list.clear();
        }
        this.list.addAll(addlist);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_storename,tv_gametype,tv_address,tv_rank,tv_amount,tv_date;
        ImageView btn_showmap;

        public ViewHolder(View v){
            super(v);
            tv_storename = v.findViewById(R.id.tv_storename);
            tv_gametype = v.findViewById(R.id.tv_gametype);
            tv_address = v.findViewById(R.id.tv_address);
            btn_showmap = v.findViewById(R.id.btn_showmap);

            tv_rank = v.findViewById(R.id.tv_rank);
            tv_amount = v.findViewById(R.id.tv_amount);
            tv_date = v.findViewById(R.id.tv_date);

            btn_showmap.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_showmap:
                    ((GoodplaceAct)act).setPopView(list.get(getAdapterPosition()));
                    break;
            }
        }
    }
}
