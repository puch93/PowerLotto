package kr.core.powerlotto.util;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.zip.Inflater;

import kr.core.powerlotto.R;

public class LottoUtil {

    public static void animationPriceText(int startval, int endval, final TextView tv){
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startval,endval);
        valueAnimator.setDuration(2000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                tv.setText(StringUtil.setNumComma(Long.parseLong(valueAnimator.getAnimatedValue().toString())));
            }
        });

        valueAnimator.start();
    }

    public static void animationPriceTextfloat(float startval, float endval, final TextView tv, final long origval){
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(startval,endval);
        valueAnimator.setDuration(2000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                long val = (long)Float.parseFloat(valueAnimator.getAnimatedValue().toString());
                tv.setText(StringUtil.setNumComma(val));
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                tv.setText(StringUtil.setNumComma(origval));
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        valueAnimator.start();
    }


    /**
     * 1~10 : 노랑
     * 11~20 : 파랑
     * 21~30 : 빨강
     * 31~40 : 보라
     * 41~45 : 녹색
     * @param num 공 숫자
     * @param isBig 큰이미지 : true / 작은이미지 : false
     * @return resId
     */
    public static int getBallResource(int num,boolean isBig){
        int resId = -1;
        switch (num){
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                if (isBig) {
                    resId = R.drawable.b_ball_yellow;
                }else{
                    resId = R.drawable.s_ball_yellow;
                }
                break;
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
                if (isBig) {
                    resId = R.drawable.b_ball_green;
                }else{
                    resId = R.drawable.s_ball_green;
                }
                break;
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
                if (isBig) {
                    resId = R.drawable.b_ball_red;
                }else{
                    resId = R.drawable.s_ball_red;
                }
                break;
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
                if (isBig) {
                    resId = R.drawable.b_ball_blue;
                }else{
                    resId = R.drawable.s_ball_blue;
                }
                break;
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
                if (isBig) {
                    resId = R.drawable.b_ball_violet;
                }else{
                    resId = R.drawable.s_ball_violet;
                }
                break;
            default:
                if (isBig) {
                    resId = R.drawable.b_ball_grey;
                } else {
                    resId = R.drawable.s_ball_grey;
                }
                break;
        }
        return resId;
    }

    public static int getPopBallResource(int num){
        int resId = -1;
        switch (num){
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                resId = R.drawable.bb_ball_yellow;
                break;
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
                resId = R.drawable.bb_ball_green;
                break;
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
                resId = R.drawable.bb_ball_red;
                break;
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
                resId = R.drawable.bb_ball_blue;
                break;
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
                resId = R.drawable.bb_ball_purple;
                break;
            default:
                resId = R.drawable.bb_ball_yellow;
                break;
        }
        return resId;
    }

    public static int getRandnum(){
        int temp;
        temp = (int)((Math.random()*45)+1);
        return temp;
    }

    public static int getRand(int total){
        int temp;
        temp = (int)((Math.random()*total));
        return temp;
    }

    public static String resultStr(JSONArray ja){
        String res = "";
        try{
            for (int i = 0; i < ja.length(); i++){
                if (i == 0) {
                    res = ja.getString(i);
                }else{
                    res += "\n" + ja.getString(i);
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return res;
    }

    public static void resultAdd(JSONArray ja, LinearLayout linearLayout, LayoutInflater inflater){
        linearLayout.removeAllViews();
        try{
            for (int i = 0; i < ja.length(); i++){

                LinearLayout child = (LinearLayout) inflater.inflate(R.layout.layout_resultitem,null,false);
                TextView tv_episode = child.findViewById(R.id.tv_episode);
                TextView tv_rank = child.findViewById(R.id.tv_rank);
                TextView tv_price = child.findViewById(R.id.tv_price);
                TextView tv_date = child.findViewById(R.id.tv_date);

                String[] preText = ja.getString(i).split("당첨금액 : ")[0].split(" : ");
                String price = StringUtil.setNumComma(Long.parseLong(ja.getString(i).split("당첨금액 : ")[1])) + "원";

                String pre[] = preText[0].split("\\[");
                String ep = pre[0].replaceAll("회차","");
                tv_episode.setText("제 "+ep.trim()+"회");

                String date = pre[1].replace("]", "");
                tv_date.setText(date);

                tv_rank.setText(preText[1].trim());
                tv_price.setText(price);
                linearLayout.addView(child);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    public static String getResultType(String type) {
        switch (type) {
            case "rand":
                return "랜덤";
            case "custom":
                return "수동";
            case "adv":
                return "고급";
            default:
                return "";
        }
    }

    public static int getResultTypeResource(String type) {
        switch (type) {
            case "rand":
                return R.drawable.r_blue;
            case "custom":
                return R.drawable.r_red;
            case "adv":
                return R.drawable.r_purple;
            default:
                return R.drawable.r_blue;
        }
    }


//    public static SpannableStringBuilder resultSpanStr(JSONArray ja){
//        String res = "";
//        try{
//            for (int i = 0; i < ja.length(); i++){
//                if (i == 0) {
//                    res = ja.getString(i);
//                }else{
//                    res += "\n" + ja.getString(i);
//                }
//            }
//        }catch (JSONException e){
//            e.printStackTrace();
//        }
//        return res;
//    }

//    public static int getRandnum(int length){
//        int temp;
//        int no[] = {0,0,0,0};
//        for (int i = 0; i < length; i++){
//            temp = (int)((Math.random()*45)+1);
//
//        }
//    }

}
