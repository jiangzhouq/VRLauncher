package qjizho.vrlauncher;

import android.app.Activity;
import android.content.res.Configuration;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import qjizho.vrlauncher.wifi.WFSearchProcess;
import qjizho.vrlauncher.wifi.WTAdapter;
import qjizho.vrlauncher.wifi.WifiAdmin;
import qjizho.vrlauncher.wifi.WifiBroadcastReceiver;




public class WifiActivity extends Activity implements WifiBroadcastReceiver.EventHandler{

    private View mDecorView;
    //消息事件
    public static final int m_nWifiSearchTimeOut = 0;// 搜索超时
    public static final int m_nWTScanResult = 1;// 搜索到wifi返回结果
    public static final int m_nWTConnectResult = 2;// 连接上wifi热点
    public static final int m_nCreateAPResult = 3;// 创建热点结果
    public static final int m_nUserResult = 4;// 用户上线人数更新命令(待定)
    public static final int m_nWTConnected = 5;// 点击连接后断开wifi，3.5秒后刷新adapter

    public static final String WIFI_AP_HEADER = "zhf_";
    public static final String WIFI_AP_PASSWORD ="zhf12345";

    private ImageView selected_left;
    private ImageView selected_right;
    private ListView explorer_left;
    private ListView explorer_right;

    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private List<ScanResult> mWifiList;
    private List<WifiConfiguration> mWifiConfiguration;

    //三个重要的类
    public WFSearchProcess m_wtSearchProcess; //WiFi搜索进度条线程
    public WifiAdmin m_wiFiAdmin; //Wifi管理类
    private WifiManager.WifiLock mWifiLock;
    ArrayList<ScanResult> m_listWifi = new ArrayList();//检测到热点信息列表
    private WTAdapter m_wTAdapter; //网络列表适配器
    private String mPasswd = "";
    private String mSSID = "";
    public  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case m_nWifiSearchTimeOut: // 搜索超时
                    m_wtSearchProcess.stop();
//                    m_FrameLWTSearchAnimation.stopAnimation();
                    m_listWifi.clear();  //网络列表
                    //设置控件
//                    m_textVWTPrompt.setVisibility(View.VISIBLE);
//                    m_textVWTPrompt.setText("需要重新搜索，点右上角重新搜索或创建新的热点...");
                    break;

                case m_nWTScanResult:  //扫描到结果
                    m_listWifi.clear();
                    Log.d("qiqi",m_wiFiAdmin.mWifiManager.getScanResults().size() + " --- size");
                    if(m_wiFiAdmin.mWifiManager.getScanResults() != null) {
                        for (int i = 0; i < m_wiFiAdmin.mWifiManager.getScanResults().size(); i++) {
                            ScanResult scanResult = m_wiFiAdmin.mWifiManager.getScanResults().get(i);
                            //和指定连接热点比较，将其他的过滤掉！
//                            if(scanResult.SSID.startsWith(WIFI_AP_HEADER)) {
                            m_listWifi.add(scanResult);
                            Log.d("qiqi", "wifi:" + scanResult.SSID);
//                            }
                        }
                        if(m_listWifi.size() > 0) {
                            m_wtSearchProcess.stop();
//                            m_FrameLWTSearchAnimation.stopAnimation();
//                            m_textVWTPrompt.setVisibility(View.GONE);
                            //更新列表，显示出搜索到的热点
                            m_wTAdapter.setData(m_listWifi);
                            m_wTAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }else{
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        setContentView(R.layout.wifi_layout);
        mDecorView = getWindow().getDecorView();
        hideSystemUI();
        selected_left = (ImageView) findViewById(R.id.selected_left);
        selected_right = (ImageView) findViewById(R.id.selected_right);
        explorer_left = (ListView) findViewById(R.id.explorer_left);
        explorer_right = (ListView) findViewById(R.id.explorer_right);
        WifiBroadcastReceiver.ehList.add(this);
       m_wtSearchProcess = new WFSearchProcess(this);
        //wifi管理类
        m_wiFiAdmin  = WifiAdmin.getInstance(this);
        if(!m_wtSearchProcess.running) { //搜索线程没有开启
            //1.当前热点或wifi连接着    WIFI_STATE_ENABLED 3 //WIFI_AP_STATE_ENABLED  13
//            if(m_wiFiAdmin.getWifiApState() == 3 || m_wiFiAdmin.getWifiApState() == 13) {
//                wFOperateEnum = WFOperateEnum.SEARCH; //搜索wifi事件
//                m_LinearLDialog.setVisibility(View.VISIBLE); ///wifi提示对话框显示
//                m_textVContentDialog.setText("是否关闭当前热点去搜索其他热点？");
//                return;  //跳出此方法，交由对话框来处理事件
//            }
            //2.当前没有热点或wifi连接着
            if(!m_wiFiAdmin.mWifiManager.isWifiEnabled()) { //如果wifi没打开
                m_wiFiAdmin.OpenWifi();
            }
//            m_textVWTPrompt.setVisibility(View.VISIBLE); //中间提示文字
//            m_textVWTPrompt.setText("正在搜索附近的热点...");
//            m_linearLCreateAP.setVisibility(View.GONE); //创建wifi热点布局消失
//            m_gifRadar.setVisibility(View.GONE); //热点连接动画消失
//            m_btnCreateWF.setBackgroundResource(R.drawable.x_wt_create); //更改按钮文字“创建”
            //开始搜索wifi
            m_wiFiAdmin.startScan();
            m_wtSearchProcess.start(); //开启搜索线程
//            m_FrameLWTSearchAnimation.startAnimation(); //开启波纹动画
        }else {//搜索线程开启着，再次点击按钮
            //重新启动
            m_wtSearchProcess.stop();
            m_wiFiAdmin.startScan(); 	//开始搜索wifi
            m_wtSearchProcess.start();
        }
        m_wTAdapter = new WTAdapter(this, m_listWifi);
        explorer_left.setAdapter(m_wTAdapter);
        explorer_right.setAdapter(m_wTAdapter);
    }

    private void enableWifi(){
        if(!mWifiManager.isWifiEnabled()){
            mWifiManager.setWifiEnabled(true);
        }
    }

    @Override
    public void handleConnectChange() {
        Message msg = mHandler.obtainMessage(m_nWTConnectResult);
        mHandler.sendMessage(msg);
    }

    @Override
    public void scanResultsAvaiable() {
        Message msg = mHandler.obtainMessage(m_nWTScanResult);
        mHandler.sendMessage(msg);
    }

    @Override
    public void wifiStatusNotification() {
        m_wiFiAdmin.mWifiManager.getWifiState(); //获取当前wifi状态
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        Log.d("qiqi", "hideSystemUI");
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        Log.d("qiqi", "showSystemUI");
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

    }
    Runnable hideUIRun = new Runnable() {

        @Override
        public void run() {
            hideSystemUI();
        }
    };
}
