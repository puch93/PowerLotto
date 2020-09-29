package kr.core.powerlotto.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;

import androidx.databinding.DataBindingUtil;

import kr.core.powerlotto.R;
import kr.core.powerlotto.databinding.DialogPickerBinding;
import kr.core.powerlotto.util.StringUtil;

public class PickerPlaceDlg extends BaseAct {

    DialogPickerBinding binding;
    Activity act;

    String[] values;

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

        binding.title.setText("지역");

        values = getResources().getStringArray(R.array.store_loc);

        binding.picker.setMinValue(0);
        binding.picker.setMaxValue(values.length-1);
        binding.picker.setDisplayedValues(values);

        binding.picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        binding.picker.setWrapSelectorWheel(false);


        String selected = getIntent().getStringExtra("selected");
        for (int i = 0; i < values.length; i++) {
            if(values[i].equalsIgnoreCase(selected)) {
                binding.picker.setValue(i);
                break;
            }
        }

        binding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String selected = values[binding.picker.getValue()];

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
