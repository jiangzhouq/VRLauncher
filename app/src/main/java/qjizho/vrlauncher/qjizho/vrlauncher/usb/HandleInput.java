package qjizho.vrlauncher.usb;

import android.content.Context;

import com.jiongbull.jlog.JLog;

import org.json.JSONArray;
import org.json.JSONObject;

import qjizho.vrlauncher.usb.modules.About;
import qjizho.vrlauncher.usb.modules.Bluetooth;
import qjizho.vrlauncher.usb.modules.Language;
import qjizho.vrlauncher.usb.modules.ScreenOff;
import qjizho.vrlauncher.usb.modules.Time;
import qjizho.vrlauncher.usb.modules.Volume;
import qjizho.vrlauncher.usb.modules.WIFI;

/**
 * Created by qjizho on 16/4/22.
 */
public class HandleInput implements Bluetooth.BluetoothListener {

    /* Define public json key */
    private String KEY_COMMAND = "cmd";
    private String KEY_VALUE_OK = "ok";
    private String KEY_ID = "id";
    private String KEY_VALUE_VOL = "volume";
    private String KEY_VALUE_TIME = "time";
    private String KEY_VALUE_MODULE = "module";
    private String KEY_VALUE_SDK = "sdk";
    private String KEY_VALUE_SYSTEM = "system";
    private String KEY_VALUE_LANG = "language";
    private String KEY_VALUE_SUPPORTED_LANG = "supported";

    private static Context mContext;
    /*   Define CMDs from JSON     */
    //Volume
    public static final int CMD_GET_VOL = 0;
    public static final int CMD_SET_VOL = 1;
    //WIFI
    //BLUETOOTH
    //time
    public static final int CMD_GET_TIME = 2;
    public static final int CMD_SET_TIME = 3;
    //sleep and wake up .. auto screem off time .. auto sleep time
    //language
    public static final int CMD_GET_LANG = 5;
    public static final int CMD_GET_SUPPORTED_LANG = 6;
    public static final int CMD_SET_LANG = 7;
    //about
    public static final int CMD_GET_INFO = 4;




    private Volume mVolume;
    private WIFI mWifi;
    private Time mTime;
    private About mAbout;
    private Language mLanguage;
    private ScreenOff mScreenOff;
    private Bluetooth mBluetooth;
    private JSONObject jsonObject;

    private static HandleInput instance;
    public static HandleInput getInstance(Context context){
        mContext = context;
        if(instance == null)
            instance = new HandleInput();
        return instance;
    }

    public interface  HandleInputListener{
        void sendToClient();
    }
    private HandleInputListener handleInputListener;
    public void setHandleInputListener(HandleInputListener handleInputListener){
        this.handleInputListener = handleInputListener;
    }


    @Override
    public void returnSearchedBlue(Bluetooth.BlueDevice blueDevice) {
        handleInputListener.sendToClient();
    }

    public JSONObject handleJSON(JSONObject json){
        jsonObject = new JSONObject();
        try{
            int cmd = json.getInt(KEY_COMMAND);
            JLog.v("chl", "cmd:" + cmd);
            switch (cmd){
                case CMD_GET_VOL:
                    mVolume = Volume.getInstance(mContext);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_VOL, mVolume.getVolume());
                    break;
                case CMD_SET_VOL:
                    mVolume = Volume.getInstance(mContext);
                    int vol = json.getInt(KEY_VALUE_VOL);
                    mVolume.setVolume(vol);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_OK, 1);
                    break;
                case CMD_GET_TIME:
                    mTime = Time.getInstance(mContext);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_TIME, mTime.getTime());
                    break;
                case CMD_SET_TIME:
                    mTime = Time.getInstance(mContext);
                    mTime.setTime(json.getLong(KEY_VALUE_TIME));
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_OK, 1);
                    break;
                case CMD_GET_INFO:
                    mAbout = About.getInstance(mContext);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_MODULE, mAbout.getModuleInfo());
                    jsonObject.put(KEY_VALUE_SDK, mAbout.getSDKInfo());
                    jsonObject.put(KEY_VALUE_SYSTEM, mAbout.getSystemInfo());
                    break;
                case CMD_GET_LANG:
                    mLanguage = Language.getInstance(mContext);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_LANG, mLanguage.getLanguage());
                    break;
                case CMD_SET_LANG:
                    mLanguage = Language.getInstance(mContext);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_OK, 1);
                    break;
                case CMD_GET_SUPPORTED_LANG:
                    mLanguage = Language.getInstance(mContext);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_SUPPORTED_LANG, new JSONArray(mLanguage.getSupportedLang()));
                    break;
            }
            JLog.json(jsonObject.toString());
            return jsonObject;
        }catch (Exception e){

        }

        return null;
    }
}
