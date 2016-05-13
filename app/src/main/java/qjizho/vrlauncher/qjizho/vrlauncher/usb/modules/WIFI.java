package qjizho.vrlauncher.usb.modules;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;

import com.jiongbull.jlog.JLog;

import java.util.ArrayList;

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
    private WIFI(Context context){
        mContext = context;
        mWifiAdmin = WifiAdmin.getInstance(mContext);
        mWFSearchProcess = new WFSearchProcess(this);
    }


    public interface WifiListener{
        void returnSearchedWifi(ArrayList<ScanResult> results);
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

    }
    @Override
    public void handleConnectChange() {

    }

    @Override
    public void wifiStatusNotification() {
        JLog.d("get results: " + mWifiAdmin.mWifiManager.getScanResults().size());
        for(ScanResult result : mWifiAdmin.mWifiManager.getScanResults()){
            JLog.d("result: " + result.SSID);
        }
    }

    @Override
    public void scanResultsAvaiable() {

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
            mWifiAdmin.closeWifi();
        }else{
            mWifiAdmin.OpenWifi();
        }
        JLog.d("Set qjizho.vrlauncher.wifi turn to :" + on);
    }

    public void startScan(){
        if(!mWFSearchProcess.running){
            mWifiAdmin.startScan();
            mWFSearchProcess.start();
        }else{
            mWFSearchProcess.stop();
            mWifiAdmin.startScan();
            mWFSearchProcess.start();
        }
    }

    public void addNetWork(String uuid, String passwd){
        WifiConfiguration localWifiConfiguration = mWifiAdmin.createWifiInfo(uuid, passwd, 3, "wt");
        //添加到网络
        m_wiFiAdmin.addNetwork(localWifiConfiguration);
    }
}
