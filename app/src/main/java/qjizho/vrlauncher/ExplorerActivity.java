package qjizho.vrlauncher;

import android.app.Activity;
import android.content.res.Configuration;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import qjizho.vrlauncher.wifi.WFSearchProcess;
import qjizho.vrlauncher.wifi.WTAdapter;
import qjizho.vrlauncher.wifi.WifiAdmin;


public class ExplorerActivity extends Activity{

    private View mDecorView;

    private static final int state_blocked = 0;
    private static final int state_wifilist = 1;
    private static final int state_dialog = 2;
    private static final int state_keyboard = 3;

    private int mCurState = 0;

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
            't','T','u','U','v','V','w','W','x','X','y','Y','z','Z','`','~','!','@','#','$','%','^','&','*','(',')','_','-','+',
            '=','{','}','[',']','<','>',',','|','\\',':',';','"','\'','.','?','/'};
    //The views
    private ProgressBar mLoadingPBLeft;
    private ProgressBar mLoadingPBRight;
    private LinearLayout mPasswdLayoutLeft;
    private LinearLayout mAlertDialogLeft;
    private LinearLayout mALertDialogRight;
    private TextView mAlertTextLeft;
    private TextView mAlertTextRight;
    private LinearLayout mPasswdLayoutRight;
    private TextView mWifiSSIDLeft;
    private TextView mWifiSSIDRight;
    private EditText mWifiPasswdLeft;
    private EditText mWifiPasswdRight;

    private QueryRun mQueryRun = new QueryRun(null);
    private File mSDPath = null;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
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
        mAlertDialogLeft = (LinearLayout) findViewById(R.id.alert_layout_left);
        mALertDialogRight = (LinearLayout) findViewById(R.id.alert_layout_right);
        mAlertTextLeft = (TextView)findViewById(R.id.alert_txt_left);
        mAlertTextRight = (TextView) findViewById(R.id.alert_txt_right);

        selected_left.setImageResource(R.mipmap.setting);
        selected_right.setImageResource(R.mipmap.setting);
        //wifi管理类
        m_wiFiAdmin  = WifiAdmin.getInstance(this);
        explorer_left.setAdapter(m_wTAdapter);
        explorer_right.setAdapter(m_wTAdapter);

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            mSDPath = Environment.getExternalStorageDirectory();
        }else{
            Toast.makeText(this, "没有SD卡", Toast.LENGTH_LONG).show();
            finish();
        }

        mQueryRun.setPath(mSDPath);
        mQueryRun.run();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                if(mCurState == state_keyboard){
                    if(mEditPaswdArray.size() > 1){
                        mEditPaswdArray.remove(mEditPaswdArray.size() -1);
                        mEditPaswdCPos.remove(mEditPaswdCPos.size() -1);
                    }
                    mEditPasswd = new StringBuilder();
                    for ( int i = 0; i < mEditPaswdArray.size(); i ++){
                        mEditPasswd.append(mEditPaswdArray.get(i)[mEditPaswdCPos.get(i)]);
                    }
                    mWifiPasswdLeft.setText(mEditPasswd);
                    mWifiPasswdRight.setText(mEditPasswd);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(mCurState == state_keyboard) {
                    if (mEditPaswdArray.get(mEditPaswdArray.size() - 1)[mEditPaswdCPos.get(mEditPaswdCPos.size() - 1)] == '*')
                        break;
                    mEditPaswdArray.add(mEditPaswdCHARS);
                    mEditPaswdCPos.add(0);
                    mEditPasswd = new StringBuilder();
                    for (int i = 0; i < mEditPaswdArray.size(); i++) {
                        mEditPasswd.append(mEditPaswdArray.get(i)[mEditPaswdCPos.get(i)]);
                    }
                    mWifiPasswdLeft.setText(mEditPasswd);
                    mWifiPasswdRight.setText(mEditPasswd);
                }
                break;
            case KeyEvent.KEYCODE_BUTTON_A:
                Log.d("qiqi", "mCurState:" + mCurState);
                if(mCurState == state_wifilist){
                    if(m_wiFiAdmin.getWifiInfo().getSSID().replace("\"", "").equals(m_listWifi.get(cur_selected_explorer).SSID)){
                        Log.d("qiqi", "connected");
                        mAlertDialogLeft.setVisibility(View.VISIBLE);
                        mALertDialogRight.setVisibility(View.VISIBLE);
                        mAlertTextRight.setText(R.string.alert_text);
                        mAlertTextLeft.setText(R.string.alert_text);
                        mCurState = state_dialog;
                        break;
                    }else{
                        Log.d("qiqi", "no connected");
                    }
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
                }else if (mCurState == state_keyboard){
                    if(mWifiPasswdLeft.getText().length() >= 8){
                        WifiConfiguration localWifiConfiguration = m_wiFiAdmin.createWifiInfo(m_listWifi.get(cur_selected_explorer).SSID, mWifiPasswdLeft.getText().toString(), 3, "wt");
                        //添加到网络
                        m_wiFiAdmin.addNetwork(localWifiConfiguration);
                        //"点击链接"消失，显示进度条，
                        //点击后3.5s发送消息
                        mPasswdLayoutLeft.setVisibility(View.GONE);
                        mPasswdLayoutRight.setVisibility(View.GONE);
                        explorer_left.getChildAt(cur_selected_explorer - explorer_left.getFirstVisiblePosition()).findViewById(R.id.connecting_progressBar_wtitem).setVisibility(View.VISIBLE);
                        explorer_right.getChildAt(cur_selected_explorer - explorer_left.getFirstVisiblePosition()).findViewById(R.id.connecting_progressBar_wtitem).setVisibility(View.VISIBLE);
                    }
                }else if (mCurState == state_dialog){
                        mAlertDialogLeft.setVisibility(View.GONE);
                        mALertDialogRight.setVisibility(View.GONE);
                        m_wiFiAdmin.disconnectWifi(m_wiFiAdmin.getNetworkId());
                        mCurState = state_wifilist;
                        break;
                }
                break;
            case KeyEvent.KEYCODE_BUTTON_B:
                switch(mCurState){
                    case state_wifilist:
                    case state_blocked:
                        this.finish();
                        break;
                    case state_dialog:
                        mAlertDialogLeft.setVisibility(View.GONE);
                        mALertDialogRight.setVisibility(View.GONE);
                        mCurState = state_wifilist;
                        break;
                    case state_keyboard:
                        mPasswdLayoutLeft.setVisibility(View.GONE);
                        mPasswdLayoutRight.setVisibility(View.GONE);
                        mCurState = state_wifilist;
                        break;
                }
                break;
            case KeyEvent.KEYCODE_BUTTON_Y:
                break;
        }
        return true;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_wtSearchProcess.stop();
    }

    class QueryRun implements  Runnable{
        private File queryPath = null;
        public void setPath(File path){
            this.queryPath = path;
        }
        public QueryRun(File path) {
            this.queryPath = path;
        }

        @Override
        public void run() {
            if(queryPath != null){
                File files[] = queryPath.listFiles();
            }
        }
    }
}
