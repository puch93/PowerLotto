package kr.core.powerlotto.reciever;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

import kr.core.powerlotto.R;
import kr.core.powerlotto.insidedata.UserPref;
import kr.core.powerlotto.popup.PopupAlarmMsg;
import kr.core.powerlotto.util.PushWakeLock;
import kr.core.powerlotto.util.StringUtil;

import static androidx.legacy.content.WakefulBroadcastReceiver.startWakefulService;

public class AlarmReceiver extends BroadcastReceiver {

    private Context ctx;

    private static final int TYPE_ALARM_01 = 1001;
    private static final int TYPE_ALARM_02 = 1002;
    private static final int TYPE_ALARM_03 = 1003;

    private static final int TYPE_ALARM_CODE_01 = 0;
    private static final int TYPE_ALARM_CODE_02 = 1;
    private static final int TYPE_ALARM_CODE_03 = 2;

    String title, contents;

    Intent intent;

    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context;
        this.intent = intent;

        checkType(intent.getIntExtra("type", 10));
    }

    private void checkType(int type) {
        Log.e(StringUtil.TAG, "type: " + type);
        if (UserPref.isAlarmState(ctx)) {
            switch (type) {
                case TYPE_ALARM_01:
                    title = "오늘의 행운번호";
                    contents = ctx.getString(R.string.alarm_contents_03);

                    sendNotiAndPopup(TYPE_ALARM_CODE_01);
                    break;

                case TYPE_ALARM_02:
                    title = "이번주 로또 당첨 금액";
//                    contents = ctx.getString(R.string.alarm_contents_02);

                    sendNotiAndPopup(TYPE_ALARM_CODE_02);
                    break;

                case TYPE_ALARM_03:
                    title = "이번주 로또 마감시간!";

                    sendNotiAndPopup(TYPE_ALARM_CODE_03);
                    break;
            }
        }

        setAlarm();
    }

    private void sendNotiAndPopup(int type) {
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        if(!pm.isScreenOn()) {
            //화면 깨우기
            PushWakeLock.acquireCpuWakeLock(ctx);
            PushWakeLock.releaseCpuLock();
        }

        /* send noti and popup */
        sendNotification(title, contents, type);
        sendPopUp(type);
    }

    /* 푸시보내기 기본*/
    public void sendNotification(String title, String message, int type) {
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        //If on Oreo then notification required a notification channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, "default")
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher);


        Intent intent = new Intent(ctx, PopupAlarmMsg.class);
        intent.putExtra("title", title);
        intent.putExtra("contents", contents);
        intent.putExtra("type", type);
        intent.putExtra("sendType", "front");

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Notification notification = builder.build();

        notification.flags = notification.flags | notification.FLAG_AUTO_CANCEL;

        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        notification = builder.build();
        notificationManager.notify(1, notification);
    }

    private void sendPopUp(int type) {
        Log.d(StringUtil.TAG, "sendPopUp: "+type);
        ComponentName comp = new ComponentName(ctx.getPackageName(),
                AlarmReceiver.class.getName());
        startWakefulService(ctx, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);

        /* intent */
        Intent intent = new Intent(ctx, PopupAlarmMsg.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra("title", title);
        intent.putExtra("contents", contents);
        intent.putExtra("type", type);

        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }


    private void setAlarm() {
        Log.e(StringUtil.TAG, "setAlarm");

        /* initialize alarm service */
        Calendar mCalendar = Calendar.getInstance();


        /* set compare data */
        int LAST_DAY_OF_YEAR = mCalendar.getMaximum(Calendar.DAY_OF_YEAR);
        int NOW_DAY_OF_YEAR = mCalendar.get(Calendar.DAY_OF_YEAR);


        /* set calendar */
        Calendar calendar01 = setCalendar(TYPE_ALARM_01, NOW_DAY_OF_YEAR, LAST_DAY_OF_YEAR);
        Calendar calendar02 = setCalendar(TYPE_ALARM_02, NOW_DAY_OF_YEAR, LAST_DAY_OF_YEAR);
        Calendar calendar03 = setCalendar(TYPE_ALARM_03, NOW_DAY_OF_YEAR, LAST_DAY_OF_YEAR);


        /* set alarm manager */
        AlarmManager manager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);


        /* set pending intent */
        PendingIntent pendingIntent01 = PendingIntent.getBroadcast(ctx, TYPE_ALARM_CODE_01, setIntent(TYPE_ALARM_01), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent02 = PendingIntent.getBroadcast(ctx, TYPE_ALARM_CODE_02, setIntent(TYPE_ALARM_02), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent03 = PendingIntent.getBroadcast(ctx, TYPE_ALARM_CODE_03, setIntent(TYPE_ALARM_03), PendingIntent.FLAG_UPDATE_CURRENT);


        /* register alarm (버전별로 따로) */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e(StringUtil.TAG, "first");
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar01.getTimeInMillis(), pendingIntent01); //10초뒤 알람
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar02.getTimeInMillis(), pendingIntent02); //10초뒤 알람
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar03.getTimeInMillis(), pendingIntent03); //10초뒤 알람
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.e(StringUtil.TAG, "second");
            manager.setExact(AlarmManager.RTC_WAKEUP, calendar01.getTimeInMillis(), pendingIntent01);
            manager.setExact(AlarmManager.RTC_WAKEUP, calendar02.getTimeInMillis(), pendingIntent02);
            manager.setExact(AlarmManager.RTC_WAKEUP, calendar03.getTimeInMillis(), pendingIntent03);
        } else {
            Log.e(StringUtil.TAG, "third");
            manager.set(AlarmManager.RTC_WAKEUP, calendar01.getTimeInMillis(), pendingIntent01);
            manager.set(AlarmManager.RTC_WAKEUP, calendar02.getTimeInMillis(), pendingIntent02);
            manager.set(AlarmManager.RTC_WAKEUP, calendar03.getTimeInMillis(), pendingIntent03);
        }
    }

    private Calendar setCalendar(int type, int NOW_DAY_OF_YEAR, int LAST_DAY_OF_YEAR) {
        Calendar calendar = Calendar.getInstance();
        switch (type) {
            case TYPE_ALARM_01:
                calendar.set(Calendar.HOUR_OF_DAY, 11);
                calendar.set(Calendar.MINUTE, 10);
                calendar.set(Calendar.SECOND, 0);
                break;

            case TYPE_ALARM_02:
                calendar.set(Calendar.HOUR_OF_DAY, 11);
                calendar.set(Calendar.MINUTE, 20);
                calendar.set(Calendar.SECOND, 0);
                break;

            case TYPE_ALARM_03:
                calendar.set(Calendar.HOUR_OF_DAY, 11);
                calendar.set(Calendar.MINUTE, 30);
                calendar.set(Calendar.SECOND, 0);
                break;
        }

        // manager.set 할때 현재시간보다 이전 시간대면, 리시버가 바로 실행됨, 다음날로 지정해줘야 함 => ex) 세팅시간 8:00, 현재시간 9:20
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            Log.e(StringUtil.TAG, "현재시간보다 작음");

            // 올해의 마지막 날일 경우
            if (NOW_DAY_OF_YEAR == LAST_DAY_OF_YEAR) {
                NOW_DAY_OF_YEAR = 1;
            } else {
                NOW_DAY_OF_YEAR++;
            }
            calendar.set(Calendar.DAY_OF_YEAR, NOW_DAY_OF_YEAR);
        }

        return calendar;
    }

    private Intent setIntent(int type) {
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.putExtra("type", type);
        return intent;
    }
}
