package kr.core.powerlotto.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import kr.core.powerlotto.R;
import kr.core.powerlotto.databinding.ActivityPrivacyBinding;

public class PrivacyAct extends BaseAct implements View.OnClickListener {

    ActivityPrivacyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_privacy);

        binding.btnClose.setOnClickListener(this);

        binding.tvPrivacy.setText(getResources().getString(R.string.privacy));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_close:
                finish();
                break;
        }
    }
}
