package qjizho.vrlauncher.usb.modules;

import android.content.Context;
import android.media.AudioManager;

import com.jiongbull.jlog.JLog;

/**
 * Created by qjizho on 16/4/22.
 */
public class Volume {
    private static Volume tInstance;
    private AudioManager tAm;
//    private int tMaxVolume;
    private static Context mContext;

    private Volume(Context context){
        mContext = context;
//        tMaxVolume = getMaxVolume();
    }

    public static synchronized Volume getInstance(Context context){
        if(null == tInstance){
            tInstance = new Volume(context);
        }
        return tInstance;
    }

    public void setVolume(int vol){
        if(null == tAm)
            tAm = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        tAm.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_PLAY_SOUND);
        tAm.setStreamVolume(AudioManager.STREAM_SYSTEM, vol, AudioManager.FLAG_PLAY_SOUND);
        tAm.setStreamVolume(AudioManager.STREAM_NOTIFICATION, vol, AudioManager.FLAG_PLAY_SOUND);
        JLog.d("Set = " + vol);
    }

    public int getVolume(){
        if(null == tAm)
            tAm = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int curVol = tAm.getStreamVolume(AudioManager.STREAM_MUSIC);
        JLog.d("Get = " + curVol);
        return curVol;
    }

//    private int getMaxVolume(){
//        if(null == tAm)
//            tAm = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        int returnVol = tAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        JLog.v("chl", "Get Max Volume = " + returnVol);
//        return returnVol;
//    }

}
