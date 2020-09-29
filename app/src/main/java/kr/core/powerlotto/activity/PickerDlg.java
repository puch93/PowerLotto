package kr.core.powerlotto.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;

import androidx.databinding.DataBindingUtil;

import java.util.ArrayList;

import kr.core.powerlotto.R;
import kr.core.powerlotto.databinding.DialogPickerBinding;
import kr.core.powerlotto.util.StringUtil;

public class PickerDlg extends BaseAct {

    DialogPickerBinding binding;
    Activity act;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.dialog_picker, null);
        act = this;

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

        setFinishOnTouchOutside(false);

        binding.picker.setMinValue(1);
        if (StringUtil.isNull(app.lln_rank)){
            binding.picker.setMaxValue(883);
        }else {
            binding.picker.setMaxValue(Integer.parseInt(app.lln_rank));
        }

        binding.picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        binding.picker.setWrapSelectorWheel(false);


        int selected = getIntent().getIntExtra("selected", -1);
        if(selected < 0) {
            selected = Integer.parseInt(app.lln_rank);
        }
        binding.picker.setValue(selected);

        binding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selected = String.valueOf(binding.picker.getValue());

                Intent intent = new Intent();
                intent.putExtra("value", selected);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}
