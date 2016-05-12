package qjizho.vrlauncher.usb.modules;

import android.app.AlarmManager;
import android.content.Context;

/**
 * Created by qjizho on 16/5/9.
 */
public class Time {
    private static Time tInstance;
    private static Context mContext;

    private Time(Context context){
        mContext = context;
    }

    public static synchronized Time getInstance(Context context){
        if(null == tInstance){
            tInstance = new Time(context);
        }
        return tInstance;
    }

    public long getTime(){
        return System.currentTimeMillis();
    }

    public void setTime(long time){
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.setTime(time);
    }
}
