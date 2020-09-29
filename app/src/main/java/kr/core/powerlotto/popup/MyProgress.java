package kr.core.powerlotto.popup;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import kr.core.powerlotto.R;

public class MyProgress extends Dialog {

    public MyProgress(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.progress_layout);
    }

}
