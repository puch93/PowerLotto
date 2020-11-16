package kr.core.powerlotto.util;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {

    public static final String TAG = "Lotto";
    public static final String STATUS = "status";
    public static final String SUCCESS = "success";
    public static final String FILE = "file";
    public static final String BANNER = "B";
    public static final String ADMOB = "A";
    public static final String NONE = "N";

    public static final String KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCZ7FCZUCKDFk5N+LttCUrqvzZ57bX3zRjCQA0XQz3Diue6/lQKGaesc45g51U74X8NKtdUlbrLMh0bw9QAM4xYKh5VAKovC2f7SxgCjrrOpVbJl9aSXqasSvoU1xFllI8lI83IiUYej7LCZmLYaUhQAsb7ArLlmbu0vjfOCcoMRwIDAQAB";
    public static final String DEVELOPERPAYLOAD = "powerlotto";
    public static final int IAP_API_VERSION = 5;

    public static final String URL_ONE_STORE_WEB = "https://m.onestore.co.kr/mobilepoc/apps/appsDetail.omp?prodId=0000751716";
    public static final String URL_ONE_STORE_APP = "onestore://common/product/0000751716";
    public static final String URL_PLAY_STORE = "https://play.google.com/store/apps/details?id=kr.core.powerlotto";

    public static final boolean isGoogle = true;

    public static String getDeviceId(Context context) {
        String newId = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            newId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            newId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

            if (StringUtil.isNull(newId)) {
                newId = "35" +
                        Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                        Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                        Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                        Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                        Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                        Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                        Build.USER.length() % 10;
            }
        }
        return newId;
    }
    // get phone num
    public static String getPhoneNumber(Activity act) {
        TelephonyManager tm = (TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNum = tm.getLine1Number();
        if (StringUtil.isNull(phoneNum)) {
            return null;
        } else {
            if (phoneNum.startsWith("+82")) {
                phoneNum = phoneNum.replace("+82", "0");
            }
            return phoneNum;
        }
    }


    public static String getTelecom(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getNetworkOperatorName();
    }

    public static boolean isNull(String str) {
        if (str == null || str.length() == 0 || str.equals("null")) {
            return true;
        } else {
            return false;
        }
    }

    public static String setNumComma(long price) {
        DecimalFormat format = new DecimalFormat("###,###");
        return format.format(price);
    }

    public static String getStr(JSONObject jo, String key) {
        String s = null;
        try {
            s = jo.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public static boolean isImage(String msg){
        String reg = "^([\\S]+(\\.(?i)(jpg|png|jpeg))$)";

        return msg.matches(reg);
    }

    /**
     * 밀리초에서 시/분/초를 계산하여 지정된 포맷으로 출력함.
     * -> 0:00 / 1:00 / 10:00 / 1:00:00
     *
     * @param duration
     * @return
     */

    public static String getTime(String duration) {

        long milliSeconds = Long.parseLong(duration);
        int totalSeconds = (int) (milliSeconds / 1000);

        int hour = totalSeconds / 3600;
        int minute = (totalSeconds - (hour * 3600)) / 60;
        int second = (totalSeconds - ((hour * 3600) + (minute * 60)));


        return formattedTime(hour, minute, second);
    }

    /**
     * 계산된 시/분/초 를 지정한 형태의 문자열로 반환함.
     *
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    private static String formattedTime(int hour, int minute, int second) {
        String result = "";

        if (hour > 0) {
            result = hour + ":";
        }

        if (minute >= 10) {
            result = result + minute + ":";
        } else {
            result = result + "0" + minute + ":";
        }

        if (second >= 10) {
            result = result + second;
        } else {
            result = result + "0" + second;
        }

        return result;
    }

   /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context. * @param uri The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isToday(String idate){
        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Log.i(StringUtil.TAG,"today: "+sdf.format(date) + " idate: "+idate);

        if (idate.equalsIgnoreCase(sdf.format(date))){
            return true;
        }else{
            return false;
        }
    }

    public static String changeDate(long timeMillis){
        Date date = new Date(timeMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.i(StringUtil.TAG,"changeDate: "+sdf.format(date));
        return sdf.format(date);

    }

}
