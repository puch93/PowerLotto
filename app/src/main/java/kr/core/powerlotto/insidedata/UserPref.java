package kr.core.powerlotto.insidedata;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPref {

    public static String getIdx(Context context){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        return pref.getString("idx","");
    }

    public static void setIdx(Context context,String idx){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("idx",idx);
        editor.commit();
    }

    public static boolean isAlarmState(Context context){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        return pref.getBoolean("alarm",true);
    }

    public static void setAlarmState(Context context,boolean alarm){
        SharedPreferences pref = context.getSharedPreferences("user",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("alarm",alarm);
        editor.commit();
    }










    //phons sim operator
    public static void saveFCheck(Context ctx, String value) {
        SharedPreferences pref = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("fcheck", value);
        editor.commit();
    }

    public static String getFCheck(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        return pref.getString("fcheck", null);
    }

    //fcm
    public static void saveFcmToken(Context ctx, String value) {
        SharedPreferences pref = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("fcm", value);
        editor.commit();
    }

    public static String getFcmToken(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        return pref.getString("fcm", null);
    }

    //coupa alarm
    public static void saveCoupaAlarmState(Context ctx, String value) {
        SharedPreferences pref = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("coupa_alarm", value);
        editor.commit();
    }

    public static String getCoupaAlarmState(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        return pref.getString("coupa_alarm", null);
    }
}
