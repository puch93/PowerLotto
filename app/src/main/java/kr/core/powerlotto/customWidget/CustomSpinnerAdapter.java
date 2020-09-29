package kr.core.powerlotto.customWidget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import kr.core.powerlotto.R;

public class CustomSpinnerAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> data;
    LayoutInflater inflater;
    boolean isSelected = false;

    public CustomSpinnerAdapter(Context context, ArrayList<String> data){
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (data != null){
            return data.size();
        }else {
            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = inflater.inflate(R.layout.spinner_normal,parent,false);
        }

        ((ImageView) convertView.findViewById(R.id.iv_spinarrow)).setSelected(isSelected);

        if (data != null){
            ((TextView)convertView.findViewById(R.id.tv_spintext)).setText(data.get(position));
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.spinner_dropdown,parent,false);
        }

         ((TextView)convertView.findViewById(R.id.tv_dropdown_text)).setText(data.get(position));

        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
