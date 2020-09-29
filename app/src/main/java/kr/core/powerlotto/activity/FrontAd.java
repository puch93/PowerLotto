package kr.core.powerlotto.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;

import kr.core.powerlotto.R;
import kr.core.powerlotto.databinding.FrontadLayoutBinding;
import kr.core.powerlotto.network.netUtil.NetUrls;


public class FrontAd extends BaseAct {

    private final String _P_FRONT = "p_front";

    FrontadLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this,R.layout.frontad_layout);

        String type = getIntent().getStringExtra("type");
        if(type == null){
            type = "";
        }

        if(type.equals(_P_FRONT)){
            AudioManager mAudioManger = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

            if(mAudioManger.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vib.vibrate(1000);
            }

            if(mAudioManger.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                Uri nofitication = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), nofitication);
                ringtone.play();
            }
        }

        final String target = getIntent().getStringExtra("targeturl");
        String imgurl = getIntent().getStringExtra("imgurl");

        ImageView adimg = (ImageView)findViewById(R.id.iv_adimg);
        LinearLayout close = (LinearLayout)findViewById(R.id.ll_adview_close);

        Glide.with(this)
                .load(NetUrls.IMGDOMAIN+imgurl)
                .into(binding.ivAdimg);

        binding.ivAdimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (URLUtil.isValidUrl(target)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(target)));
                } else {
                    Toast.makeText(FrontAd.this, getString(R.string.ad_guide_msg), Toast.LENGTH_SHORT).show();
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
