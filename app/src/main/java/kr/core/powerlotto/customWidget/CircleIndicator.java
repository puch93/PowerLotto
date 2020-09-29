package kr.core.powerlotto.customWidget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import kr.core.powerlotto.util.ConvertPxDp;

public class CircleIndicator extends LinearLayout {
    private Context mContext;

    private int itemMargin = 10;

    private int animDuration = 250;

    private int mDefaultCircle;

    private int mSelectCircle;

    private ImageView[] imageDot;

    public void setAnimDuration(int animDuration){
        this.animDuration = animDuration;
    }

    public void setItemMargin(int itemMargin){
        this.itemMargin = itemMargin;
    }

    public CircleIndicator(Context context){
        super(context);
        mContext = context;
    }

    public CircleIndicator(Context context, AttributeSet attrs){
        super(context,attrs);
        mContext = context;
    }

    public int getDotCount(){
        if (imageDot != null) {
            return imageDot.length;
        }else{
            return 0;
        }
    }

    public void createDotPanel(int count, int defaultCircle, int selectCircle,int size){
        mDefaultCircle = defaultCircle;
        mSelectCircle = selectCircle;

        imageDot = new ImageView[count];

        for(int i = 0; i < count; i++){
            imageDot[i] = new ImageView(mContext);
            LayoutParams params = new LayoutParams(
                    (int) ConvertPxDp.convertDpToPixel((float) size,getContext()), (int)ConvertPxDp.convertDpToPixel((float) size,getContext()));
            params.topMargin = itemMargin;
            params.bottomMargin = itemMargin;
            params.leftMargin = itemMargin;
            params.rightMargin = itemMargin;
            params.gravity = Gravity.CENTER;

            imageDot[i].setLayoutParams(params);
            imageDot[i].setImageResource(defaultCircle);
            imageDot[i].setTag(imageDot[i].getId(), false);
            this.addView(imageDot[i]);
        }

        selectDot(0);
    }

    public void selectDot(int position){
        if (imageDot != null) {
            for (int i = 0; i < imageDot.length; i++) {
                if (i == position) {
                    imageDot[i].setImageResource(mSelectCircle);
                } else {
                    imageDot[i].setImageResource(mDefaultCircle);
                }
            }
        }
    }

    public void selectScaleAnim(View view, float startScale, float endScale){
        Animation anim = new ScaleAnimation(
                startScale, endScale,
                startScale, endScale,
                Animation.RELATIVE_TO_SELF,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f);
        anim.setFillAfter(true);
        anim.setDuration(animDuration);
        view.startAnimation(anim);
        view.setTag(view.getId(), true);
    }

    public void defaultScaleAnim(View view, float startScale, float endScale){
        Animation anim = new ScaleAnimation(
                startScale, endScale,
                startScale, endScale,
                Animation.RELATIVE_TO_SELF,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f);
        anim.setFillAfter(true);
        anim.setDuration(animDuration);
        view.startAnimation(anim);
        view.setTag(view.getId(), true);
    }
}
