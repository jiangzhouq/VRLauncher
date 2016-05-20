package qjizho.vrlauncher.usb;

import android.content.Context;

import com.jiongbull.jlog.JLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
public class HandleInput implements Bluetooth.BluetoothListener, WIFI.WifiListener {

    /* Define public json key */
    public static final String KEY_COMMAND = "cmd";
    public static final String KEY_VALUE_OK = "ok";
    public static final String KEY_ID = "id";
    public static final String KEY_VALUE_VOL = "volume";
    public static final String KEY_VALUE_TIME = "time";
    public static final String KEY_VALUE_MODULE = "module";
    public static final String KEY_VALUE_SDK = "sdk";
    public static final String KEY_VALUE_SYSTEM = "system";
    public static final String KEY_VALUE_LANG = "language";
    public static final String KEY_VALUE_SUPPORTED_LANG = "supported";
    public static final String KEY_VALUE_FILES = "files";
    public static final String KEY_VALUE_QUERY_URL = "query";
    public static final String KEY_VALUE_WIFI_TURN = "turn";
    public static final String KEY_VALUE_WIFI_RESULT = "wifi";

    private ArrayList<MyFile> avFiles = new ArrayList<MyFile>();
    private static Context mContext;
    /*   Define CMDs from JSON     */
    //Volume
    public static final int CMD_GET_VOL = 0;
    public static final int CMD_SET_VOL = 1;
    //WIFI
    public static final int CMD_WIFI_TURN_ON = 10;
    public static final int CMD_WIFI_TURN_OFF = 11;
    public static final int CMD_WIFI_SCAN = 12;
    public static final int CMD_WIFI_CONNECT = 13;
    public static final int CMD_WIFI_FORGET= 14;
    public static final int CMD_WIFI_GET_TURN= 15;
    //BLUETOOTH
    public static final int CMD_BLUE_TURN_ON = 16;
    public static final int CMD_BLUE_TURN_OFF = 17;
    public static final int CMD_BLUE_SCAN = 18;
    public static final int CMD_BLUE_CONNECT = 19;
    public static final int CMD_BLUE_FORGET = 20;
    public static final int CMD_BLUE_GET_TURN =21;
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

    //Explorer
    public static final int CMD_GET_USBS = 8;
    public static final int CMD_LIST_PATH = 9;



    private Volume mVolume;
    private WIFI mWifi;
    private Time mTime;
    private About mAbout;
    private Language mLanguage;
    private ScreenOff mScreenOff;
    private Bluetooth mBluetooth;
    private JSONObject jsonObject;

    private static HandleInput instance;
    private HandleInput(){
        mWifi = WIFI.getInstance(mContext);
        mWifi.setWifiListener(this);
        mBluetooth = Bluetooth.getInstance(mContext);
        mBluetooth.setBluetoothListener(this);
    }
    public static HandleInput getInstance(Context context){
        mContext = context;
        if(instance == null)
            instance = new HandleInput();
        return instance;
    }

    public interface  HandleInputListener{
        void sendToClient(JSONObject jsonObject);
    }
    private HandleInputListener handleInputListener;
    public void setHandleInputListener(HandleInputListener handleInputListener){
        this.handleInputListener = handleInputListener;
    }

    @Override
    public void returnToClient(JSONObject wifiObject) {
        handleInputListener.sendToClient(wifiObject);
        JLog.json(wifiObject.toString());
    }

    @Override
    public void returnBlueToClient(JSONObject blueObject) {
        handleInputListener.sendToClient(blueObject);
        JLog.json(blueObject.toString());
    }

    public JSONObject handleJSON(JSONObject json){
        jsonObject = new JSONObject();
        JLog.json(json.toString());
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
                case CMD_GET_USBS:
                    avFiles = new ArrayList<MyFile>();
                    for(String point : mMountPoints){
                        JLog.d("isMountPoints:" + point);
                        if(isMounted(point)){
                            MyFile myFile = new MyFile();
                            myFile.type = 1;
                            myFile.fName = point.replace("/storage/","");
                            myFile.fUrl = point;
                            avFiles.add(myFile);
                            JLog.d(point + " is added");
                        }
                    }
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    JSONArray filesArray = new JSONArray();
                    for(MyFile myFile : avFiles){
                        JSONObject object = new JSONObject();
                        object.put("type", myFile.type);
                        object.put("name", myFile.fName);
                        object.put("url", myFile.fUrl);
                        filesArray.put(object);
                    }
                    jsonObject.put(KEY_VALUE_FILES, filesArray);
                    break;
                case CMD_LIST_PATH:
                    JLog.d("cmd:" + CMD_LIST_PATH);
                    String path = json.getString(KEY_VALUE_QUERY_URL);
                    JLog.d("query path:" + path);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    JSONArray queryFilesArray = new JSONArray();
                    for(File file : queryPath(path)){
                        JSONObject object = new JSONObject();
                        if(file.isDirectory()){
                            object.put("type", 1);
                        }else{
                            object.put("type", 0);
                        }
                        object.put("name", file.getName());
                        object.put("url",  file.getAbsolutePath());
                        queryFilesArray.put(object);
                    }
                    jsonObject.put(KEY_VALUE_FILES, queryFilesArray);
                    break;
                case CMD_WIFI_GET_TURN:
                    mWifi = WIFI.getInstance(mContext);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_WIFI_TURN, mWifi.getTurn());
                    break;
                case CMD_WIFI_TURN_ON:
                    mWifi = WIFI.getInstance(mContext);
                    mWifi.setTurn(true);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_OK, 1);
                    break;
                case CMD_WIFI_TURN_OFF:
                    mWifi = WIFI.getInstance(mContext);
                    mWifi.setTurn(false);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_OK, 1);
                    break;
                case CMD_WIFI_SCAN:
                    mWifi = WIFI.getInstance(mContext);
                    mWifi.startScan();
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_OK, 1);
                    break;
                case CMD_WIFI_CONNECT:
                    mWifi = WIFI.getInstance(mContext);
                    if(json.getString("passwd").isEmpty()){
                        mWifi.connectConfiguration(json.getString("uuid"));
                    }else{
                        mWifi.addNetWork(json.getString("uuid"), json.getString("passwd"));
                    }
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_OK, 1);
                    break;
                case CMD_WIFI_FORGET:
                    mWifi = WIFI.getInstance(mContext);
                    mWifi.removeNetWork(json.getString("uuid"));
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_OK, 1);
                    break;
                case CMD_BLUE_TURN_ON:
                    mBluetooth = Bluetooth.getInstance(mContext);
                    mBluetooth.setBluetoothTurn(true);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_OK, 1);
                    break;
                case CMD_BLUE_TURN_OFF:
                    mBluetooth = Bluetooth.getInstance(mContext);
                    mBluetooth.setBluetoothTurn(false);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_OK, 1);
                    break;
                case CMD_BLUE_SCAN:
                    mBluetooth = Bluetooth.getInstance(mContext);
                    mBluetooth.startScan();
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_OK, 1);
                    break;
                case CMD_BLUE_CONNECT:
                    mBluetooth = Bluetooth.getInstance(mContext);
                    mBluetooth.bondBlue(json.getString("address"));
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_OK, 1);
                    break;
                case CMD_BLUE_FORGET:
                    mBluetooth = Bluetooth.getInstance(mContext);
                    mBluetooth.diconncetBlue(json.getString("address"));
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put(KEY_VALUE_OK, 1);
                    break;
                case CMD_BLUE_GET_TURN:
                    mBluetooth = Bluetooth.getInstance(mContext);
                    jsonObject.put(KEY_ID, json.getInt(KEY_ID));
                    jsonObject.put(KEY_COMMAND, json.getInt(KEY_COMMAND));
                    jsonObject.put("turn", mBluetooth.getBluetoothState());
                    break;
            }
            JLog.json(jsonObject.toString());
            return jsonObject;
        }catch (Exception e){
        }

        return null;
    }
    private static final String MOUNTS_FILE = "/proc/mounts";

    private String[] mMountPoints=new String[]{
            "/storage/sdcard0",
            "/storage/sdcard1",
            "/storage/usbdrive1",
            "/storage/usbdrive2",
            "/storage/usbdrive3",
            "/storage/usbdrive4"
            };

    public static boolean isMounted(String path) {
        boolean blnRet = false;
        String strLine = null;
        BufferedReader reader = null;
        if(path.equals("/storage/sdcard0")){
            return true;
        }
        try {
            reader = new BufferedReader(new FileReader(MOUNTS_FILE));

            while ((strLine = reader.readLine()) != null) {
                if (strLine.contains(path)) {
                    blnRet = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reader = null;
            }
        }
        return blnRet;
    }

    private File[] queryPath(String path){
        File query = new File(path);
        File[] files = query.listFiles();
        return files;
    }

    private class MyFile{
        public int type = 0;
        public String fName = "";
        public String fUrl = "";
    }
}
