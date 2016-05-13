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
        int mVol = vol;
        if(null == tAm)
            tAm = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        tAm.setStreamVolume(AudioManager.STREAM_MUSIC, mVol, AudioManager.FLAG_PLAY_SOUND);
        JLog.v("chl", "Set = " + vol + " final /100 * max = " + mVol);
    }

    public int getVolume(){
        if(null == tAm)
            tAm = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int curVol = tAm.getStreamVolume(AudioManager.STREAM_MUSIC);
        int returnVol = curVol;
        JLog.v("chl", "Get = " + curVol + " final /max * 100 = " + returnVol  );
        return returnVol;
    }

//    private int getMaxVolume(){
//        if(null == tAm)
//            tAm = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//        int returnVol = tAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        JLog.v("chl", "Get Max Volume = " + returnVol);
//        return returnVol;
//    }

}
