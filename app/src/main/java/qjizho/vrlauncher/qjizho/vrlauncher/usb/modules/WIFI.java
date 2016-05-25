package qjizho.vrlauncher.usb.modules;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

import com.jiongbull.jlog.JLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import qjizho.vrlauncher.usb.HandleInput;
import qjizho.vrlauncher.wifi.WFSearchProcess;
import qjizho.vrlauncher.wifi.WifiAdmin;
import qjizho.vrlauncher.wifi.WifiBroadcastReceiver;

/**
 * Created by qjizho on 16/5/9.
 */
public class WIFI implements WifiBroadcastReceiver.EventHandler {
    private static WIFI tInstance;
    private static Context mContext;
    public WifiAdmin mWifiAdmin;
    private WFSearchProcess mWFSearchProcess;
    public static final int m_nWifiSearchTimeOut = 0;// 搜索超时
    private boolean mWifiCanReturn = false;
    private WIFI(Context context){
        mContext = context;
        mWifiAdmin = WifiAdmin.getInstance(mContext);
        mWFSearchProcess = new WFSearchProcess(this);
        WifiBroadcastReceiver.ehList.add(this);
    }


    public interface WifiListener{
        void returnToClient(JSONObject jsonObject);
    }
    private WifiListener wifiListener;
    public void setWifiListener(WifiListener wifiListener){
        this.wifiListener = wifiListener;
    }


//    public Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//        }
//    };

    public void notifyTimeOut(){
        startScan();
    }
    @Override
    public void handleConnectChange(String str) {
//        JLog.d("connection changed: " + str);
//        JSONObject json = new JSONObject();
//        try{
//            json.put("id",9999);
//            json.put("cmd", -1);
//            json.put("connection", str);
//        }catch (Exception e){
//        }
//        wifiListener.returnToClient(json);
    }

    @Override
    public void wifiStatusNotification() {

    }

    @Override
    public void supplicantStateChanged() {
//        JLog.d("supplicantStateChanged:state ---" + mWifiAdmin.getWifiInfo().getSupplicantState().toString());
//        JLog.d("supplicantStateChanged:SSID  ---" + mWifiAdmin.getWifiInfo().getSSID());
//        JSONObject json = new JSONObject();
//        try{
//            json.put("id",9998);
//            json.put("cmd", -1);
//            json.put("ssid", mWifiAdmin.getWifiInfo().getSSID());
//            json.put("supplicant", mWifiAdmin.getWifiInfo().getSupplicantState().toString());
//        }catch (Exception e){
//        }
//        wifiListener.returnToClient(json);
    }

    @Override
    public void scanResultsAvaiable() {
        mWifiAdmin.setWifiList();
        List<ScanResult> wifiResults = mWifiAdmin.getWifiList();
        JSONObject wifiObject = new JSONObject();
        try{
            wifiObject.put(HandleInput.KEY_COMMAND, HandleInput.CMD_WIFI_SCAN);
            wifiObject.put(HandleInput.KEY_ID, 9997);
            JSONArray resultsJsonArray = new JSONArray();
            for(ScanResult scanResult : wifiResults){
                JSONObject object = new JSONObject();
                object.put("ssid", scanResult.SSID);
                JLog.d("SSID:" + scanResult.SSID);
                object.put("capabilities", scanResult.capabilities);
                object.put("level", scanResult.level);
                object.put("configuration", mWifiAdmin.isConfigExsits(scanResult.SSID));
                object.put("connection", mWifiAdmin.getWifiInfo().getSSID().equals("\"" + scanResult.SSID + "\"") ? true : false);
                resultsJsonArray.put(object);
            }
            wifiObject.put(HandleInput.KEY_VALUE_WIFI_RESULT, resultsJsonArray);
        }catch(Exception e){

        }
        if(mWifiCanReturn){
            wifiListener.returnToClient(wifiObject);
            mWifiCanReturn = false;
        }

    }

    public static synchronized WIFI getInstance(Context context){
        if(null == tInstance){
            tInstance = new WIFI(context);
        }
        return tInstance;
    }

    public boolean getTurn(){
        return mWifiAdmin.getWifiTurn();
    }

    public void setTurn(boolean on){
        if(on){
            mWifiAdmin.OpenWifi();
//            startScan();
        }else{
            mWifiAdmin.closeWifi();
        }
        JLog.d("Set qjizho.vrlauncher.wifi turn to :" + on);
    }

    public void startScan(){
//        if(!mWFSearchProcess.running){
            mWifiAdmin.startScan();
            JLog.d("WIFI startScan");
//            mWFSearchProcess.start();
            mWifiCanReturn = true;
//        }else{
//            mWFSearchProcess.stop();
//            mWifiAdmin.startScan();
//            JLog.d("WIFI startScan");
//            mWFSearchProcess.start();
//            mWifiCanReturn = true;
//        }
    }

    public void addNetWork(String uuid, String passwd){
        JLog.d("start to connect network uuid:" + uuid + " passwd:" + passwd);
        WifiConfiguration localWifiConfiguration = mWifiAdmin.createWifiInfo(uuid, passwd, 3, "wt");
        //添加到网络
        mWifiAdmin.addNetwork(localWifiConfiguration);
    }
    public void removeNetWork(String uuid){
        mWifiAdmin.forgetWifi(uuid);
    }
    public void connectConfiguration(String uuid){
        mWifiAdmin.connectConfiguration(uuid);
    }
}
