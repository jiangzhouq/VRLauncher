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
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import qjizho.vrlauncher.wifi.WFSearchProcess;
import qjizho.vrlauncher.wifi.WTAdapter;
import qjizho.vrlauncher.wifi.WifiAdmin;
import qjizho.vrlauncher.wifi.WifiBroadcastReceiver;




public class WifiActivity extends Activity implements WifiBroadcastReceiver.EventHandler{

    private View mDecorView;

    private static final int state_blocked = 0;
    private static final int state_wifilist = 1;
    private static final int state_dialog = 2;
    private static final int state_keyboard = 3;

    private int mCurState = 0;

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

    private String mSSID = "";

    private int cur_selected_explorer = 0;
    private boolean scanResultReceived = false;

    private ArrayList<char[]> mEditPaswdArray;
    private ArrayList<Integer> mEditPaswdCPos;
    private StringBuilder mEditPasswd;
    private char[]  mEditPaswdCHARS = new char[]{'*','0','1','2','3','4','5','6','7','8','9','a','A',
            'b','B','c','C','d','D','e','E','f','F','g','G','h','H','i','I','j','J',
            'k','K','l','L','m','M','n','N','o','O','p','P','q','Q','r','R','s','S',
            't','T','u','U','v','V','w','W','x','X','y','Y','z','Z'};
    //The views
    private ProgressBar mLoadingPBLeft;
    private ProgressBar mLoadingPBRight;
    private LinearLayout mPasswdLayoutLeft;
    private LinearLayout mPasswdLayoutRight;
    private TextView mWifiSSIDLeft;
    private TextView mWifiSSIDRight;
    private EditText mWifiPasswdLeft;
    private EditText mWifiPasswdRight;

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
                    Log.d("qiqi", m_wiFiAdmin.mWifiManager.getScanResults().size() + " --- size");
                    if(m_wiFiAdmin.mWifiManager.getScanResults() != null) {
                        if(!scanResultReceived){
                            scanResultReceived = true;
                            mCurState = state_wifilist;
                            Log.d("qiqi", "set mCurState:" + state_wifilist);
                            mLoadingPBLeft.setVisibility(View.GONE);
                            mLoadingPBRight.setVisibility(View.GONE);
                            for (int i = 0; i < m_wiFiAdmin.mWifiManager.getScanResults().size(); i++) {
                                ScanResult scanResult = m_wiFiAdmin.mWifiManager.getScanResults().get(i);
                                //和指定连接热点比较，将其他的过滤掉！
//                            if(scanResult.SSID.startsWith(WIFI_AP_HEADER)) {
                                m_listWifi.add(scanResult);
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
        mLoadingPBLeft = (ProgressBar) findViewById(R.id.loading_progressbar_left);
        mLoadingPBRight = (ProgressBar) findViewById(R.id.loading_progressbar_right);
        mPasswdLayoutLeft = (LinearLayout) findViewById(R.id.passwd_layout_left);
        mPasswdLayoutRight = (LinearLayout) findViewById(R.id.passwd_layout_right);
        mWifiSSIDLeft = (TextView) findViewById(R.id.wifi_ssid_left);
        mWifiSSIDRight = (TextView) findViewById(R.id.wifi_ssid_right);
        mWifiPasswdLeft = (EditText) findViewById(R.id.wifi_passwd_left);
        mWifiPasswdRight = (EditText) findViewById(R.id.wifi_passwd_right);


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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_DPAD_UP:
                if(mCurState == state_wifilist){
                    if(cur_selected_explorer > 0){
                        cur_selected_explorer -- ;
                        m_wTAdapter.setCurPosition(cur_selected_explorer);
                        if(cur_selected_explorer < explorer_left.getSelectedItemPosition()){
                            explorer_left.setSelection(cur_selected_explorer );
                            explorer_right.setSelection(cur_selected_explorer );
                        }
                    }
                }else if (mCurState == state_keyboard){
                    if(mEditPaswdCPos.get(mEditPaswdCPos.size() -1) > 0){
                        mEditPaswdCPos.set(mEditPaswdCPos.size() -1, mEditPaswdCPos.get(mEditPaswdCPos.size() -1) - 1 );
                        mEditPasswd = new StringBuilder();
                        for ( int i = 0; i < mEditPaswdArray.size(); i ++){
                            mEditPasswd.append(mEditPaswdArray.get(i)[mEditPaswdCPos.get(i)]);
                        }
                        mWifiPasswdLeft.setText(mEditPasswd);
                        mWifiPasswdRight.setText(mEditPasswd);
                    }
                }


                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if(mCurState == state_wifilist){
                    if(cur_selected_explorer < m_wTAdapter.getCount() - 1){
                        cur_selected_explorer ++ ;
                        m_wTAdapter.setCurPosition(cur_selected_explorer);
                        if(cur_selected_explorer - explorer_left.getSelectedItemPosition() > 5){
                            explorer_left.setSelection(cur_selected_explorer - 5);
                            explorer_right.setSelection(cur_selected_explorer - 5);
                        }
                        Log.d("qiqi", cur_selected_explorer + " --- now " + " " + explorer_left.getSelectedItemPosition() + " --- selected");
                    }
                }else if (mCurState == state_keyboard){
                    if(mEditPaswdCPos.get(mEditPaswdCPos.size() -1) < mEditPaswdCHARS.length -1){
                        mEditPaswdCPos.set(mEditPaswdCPos.size() -1, mEditPaswdCPos.get(mEditPaswdCPos.size() -1) + 1 );
                        mEditPasswd = new StringBuilder();
                        for ( int i = 0; i < mEditPaswdArray.size(); i ++){
                            mEditPasswd.append(mEditPaswdArray.get(i)[mEditPaswdCPos.get(i)]);
                        }
                        mWifiPasswdLeft.setText(mEditPasswd);
                        mWifiPasswdRight.setText(mEditPasswd);
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(mEditPaswdArray.size() > 1)
                    mEditPaswdArray.remove(mEditPaswdArray.size() -1);
                mEditPasswd = new StringBuilder();
                for ( int i = 0; i < mEditPaswdArray.size(); i ++){
                    mEditPasswd.append(mEditPaswdArray.get(i)[mEditPaswdCPos.get(i)]);
                }
                mWifiPasswdLeft.setText(mEditPasswd);
                mWifiPasswdRight.setText(mEditPasswd);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(mEditPaswdArray.get(mEditPaswdArray.size() -1)[mEditPaswdCPos.get(mEditPaswdCPos.size() - 1)] == '*')
                    break;
                mEditPaswdArray.add(mEditPaswdCHARS);
                mEditPaswdCPos.add(0);
                mEditPasswd = new StringBuilder();
                for ( int i = 0; i < mEditPaswdArray.size(); i ++){
                    mEditPasswd.append(mEditPaswdArray.get(i)[mEditPaswdCPos.get(i)]);
                }
                mWifiPasswdLeft.setText(mEditPasswd);
                mWifiPasswdRight.setText(mEditPasswd);
                break;
            case KeyEvent.KEYCODE_BUTTON_A:

                if(mCurState == state_wifilist){
                    mCurState = state_keyboard;
                    mEditPasswd = new StringBuilder();
                    mEditPaswdArray = new ArrayList<char[]>();
                    mEditPaswdArray.add(mEditPaswdCHARS);
                    mEditPaswdCPos = new ArrayList<Integer>();
                    mEditPaswdCPos.add(0);

                    for ( int i = 0; i < mEditPaswdArray.size(); i ++){
                        mEditPasswd.append(mEditPaswdArray.get(i)[mEditPaswdCPos.get(i)]);
                    }
                    mWifiPasswdLeft.setText(mEditPasswd);
                    mWifiPasswdRight.setText(mEditPasswd);

                    Log.d("qiqi", "set mCurState:" + state_dialog);
                    mPasswdLayoutLeft.setVisibility(View.VISIBLE);
                    mPasswdLayoutRight.setVisibility(View.VISIBLE);
                    mWifiSSIDLeft.setText(m_listWifi.get(cur_selected_explorer).SSID);
                    mWifiSSIDRight.setText(m_listWifi.get(cur_selected_explorer).SSID);


//                WifiConfiguration localWifiConfiguration = wifiAdmin.createWifiInfo(localScanResult.SSID, WifiActivity.WIFI_AP_PASSWORD, 3,"wt");
//                //添加到网络
//                wifiAdmin.addNetwork(localWifiConfiguration);
//                //"点击链接"消失，显示进度条，
//                viewHolder.textConnect.setVisibility(View.GONE);
//                viewHolder.progressBConnecting.setVisibility(View.VISIBLE);
//                viewHolder.linearLConnectOk.setVisibility(View.GONE);
//                //点击后3.5s发送消息
//                mContext.mHandler.sendEmptyMessageDelayed(mContext.m_nWTConnected, 3500L);
                }
                break;
            case KeyEvent.KEYCODE_BUTTON_B:
                switch(mCurState){
                    case state_wifilist:
                    case state_blocked:
                        break;
                    case state_dialog:
                        break;
                    case state_keyboard:
                        mPasswdLayoutLeft.setVisibility(View.GONE);
                        mPasswdLayoutRight.setVisibility(View.GONE);
                        break;
                }
                break;
            case KeyEvent.KEYCODE_BUTTON_Y:
                break;
        }
        return true;
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
