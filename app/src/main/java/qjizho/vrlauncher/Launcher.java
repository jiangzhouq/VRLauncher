package qjizho.vrlauncher;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Launcher extends AppCompatActivity implements qjizho.vrlauncher.BatteryReceiver.BatteryHandler{
    private View mDecorView;
    private List<Map<String, Object>> mapList;
    private List<Map<String, String>> lstPics = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> lstSettings;
    private GridView grid_left;
    private GridView grid_right;
    private ImageView selected_left;
    private ImageView selected_right;
    private ImageView arrow_left;
    private ImageView arrow_right;
    private GridView explorer_left;
    private GridView explorer_right;
    private ImageView battery_left;
    private ImageView battery_right;
    private TextView battery_text_left;
    private TextView battery_text_right;
    private int[] battery_list = new int[]{
            R.drawable.easyicon_battery_1,
            R.drawable.easyicon_battery_2,
            R.drawable.easyicon_battery_3,
            R.drawable.easyicon_battery_4,
            R.drawable.easyicon_battery_5,
            R.drawable.easyicon_battery_charging,
    };
    private ImageLoader imageLoader;
    private ImageLoaderConfiguration config ;
    private PicsAdapter mPicAdapter;
    private AppsAdapter mAppAdapter;
    private PicsAdapter settingAdapter;
    private int[] imageResources = new int[]{R.mipmap.setting, R.drawable.easyicon_sd,  R.mipmap.games};
    private int[] imageResources_focus = new int[]{R.mipmap.setting_focus, R.drawable.easyicon_sd_pressed, R.mipmap.games_focus};
    private int cur_selected_launcher = 1;
    //3 = lancuer mode ; 0 - 2 explorer mode ,4 for uninstall app, 5 for uninstalling, 6 for uninstaled
    private int cur_mode = 3;
    private int cur_selected_explorer = 0;
    private int cur_page_explorer = 0;
    private DisplayImageOptions options;
    private int realListPicCount = 0;
    private int realListAppCount = 0;
    private ArrayList<AppInfo> appList;

//    private qjizho.vrlauncher.BluetoothService.BlueBinder mBlueBinder;
    private IBluetooth mBlueService;
    private ServiceConnection mBlueConn ;

    private boolean isCharging = false;
    private int capacity = -1;
    private BroadcastReceiver batteryReceiver = null;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    ((ImageView) ((ViewGroup) grid_left.getChildAt(cur_selected_launcher)).getChildAt(0)).setImageResource(imageResources_focus[cur_selected_launcher]);
                    ((ImageView)((ViewGroup) grid_right.getChildAt(cur_selected_launcher)).getChildAt(0)).setImageResource(imageResources_focus[cur_selected_launcher]);
                    break;
                case 1:
                    mAlertConfirmLeft.setVisibility(View.VISIBLE);
                    mAlertConfirmRight.setVisibility(View.VISIBLE);
                    mAlertConfirmLeft.setText(R.string.apk_uninstalled);
                    mAlertConfirmRight.setText(R.string.apk_uninstalled);
                    break;
                case 2:
                    mAlertConfirmLeft.setText(R.string.apk_uninstalling);
                    mAlertConfirmRight.setText(R.string.apk_uninstalling);
                    break;
                case 3:
                    Log.d("qiqi","disable dialog");
                    disableDialog();
                    break;

            }
        }
    };
    private RelativeLayout mAlertDialogLeft;
    private RelativeLayout mALertDialogRight;
    private TextView mAlertTextLeft;
    private TextView mAlertTextRight;
    private TextView mAlertConfirmLeft;
    private TextView mAlertConfirmRight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }else{
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        setContentView(R.layout.activity_launcher);
        mDecorView = getWindow().getDecorView();
        hideSystemUI();
        grid_left = (GridView) findViewById(R.id.list_left);
        grid_right = (GridView) findViewById(R.id.list_right);
        selected_left = (ImageView) findViewById(R.id.selected_left);
        selected_right = (ImageView) findViewById(R.id.selected_right);
        explorer_left = (GridView) findViewById(R.id.explorer_left);
        explorer_right = (GridView) findViewById(R.id.explorer_right);
        arrow_left = (ImageView) findViewById(R.id.arrow_left);
        arrow_right = (ImageView) findViewById(R.id.arrow_right);
        battery_left = (ImageView) findViewById(R.id.battery_left);
        battery_right = (ImageView) findViewById(R.id.battery_right);
        mAlertDialogLeft = (RelativeLayout) findViewById(R.id.alert_layout_left);
        mALertDialogRight = (RelativeLayout) findViewById(R.id.alert_layout_right);
        mAlertTextLeft = (TextView)findViewById(R.id.alert_txt_left);
        mAlertTextRight = (TextView) findViewById(R.id.alert_txt_right);
        mAlertConfirmLeft = (TextView) findViewById(R.id.alert_confirm_left);
        mAlertConfirmRight = (TextView) findViewById(R.id.alert_confirm_right);
        battery_text_left = (TextView) findViewById(R.id.battery_text_left);
        battery_text_right = (TextView) findViewById(R.id.battery_text_right);
        showLauncher();
        config = ImageLoaderConfiguration.createDefault(this);
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(null)  // 设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(null)       // 设置图片加载或解码过程中发生错误显示的图片
                .cacheInMemory(true)                        // 设置下载的图片是否缓存在内存中
                .cacheOnDisc(true)                          // 设置下载的图片是否缓存在SD卡中
                .build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

        mapList = getLauncherList();
        final SimpleAdapter adapter = new SimpleAdapter(this, mapList,R.layout.list_item,new String[]{"img","title"}, new int[]{R.id.img, R.id.txt});
        grid_left.setAdapter(adapter);
        grid_right.setAdapter(adapter);
        TextView to_launcher = (TextView) findViewById(R.id.to_launcher);
        to_launcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(mIntent);
            }
        });
        requestPermission();
        qjizho.vrlauncher.BatteryReceiver.ehList.add(this);

        Intent intent = new Intent("com.pascalwelsch.circularprogressbarsample.BLUE_SERVICE");
        mBlueConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//                mBlueBinder = (qjizho.vrlauncher.BluetoothService.BlueBinder)iBinder;
//                mBlueService = mBlueBinder.getService();
                mBlueService = IBluetooth.Stub.asInterface(iBinder);
                if(null != mBlueService){
                    try{
                        mBlueService.setListener(new IBluetoothListener.Stub() {
                            @Override
                            public void onStateChanged(int state) throws RemoteException {
                                Log.d("qiqi", "state:" + state);
                                switch (state) {
                                    case qjizho.vrlauncher.BluetoothService.STATE_BT_OFF:
                                        break;
                                    case qjizho.vrlauncher.BluetoothService.STATE_BT_ON:
                                        break;
                                    case qjizho.vrlauncher.BluetoothService.STATE_DISCONNECTED:
                                        break;
                                    case qjizho.vrlauncher.BluetoothService.STATE_CONNECTING:
                                        break;
                                    case qjizho.vrlauncher.BluetoothService.STATE_CONNECTED:
                                        break;
                                    case qjizho.vrlauncher.BluetoothService.STATE_XIAOMI_PAIRED:
                                    case qjizho.vrlauncher.BluetoothService.STATE_XIAOMI_CONNECTED:
                                        handler.sendEmptyMessage(3);
                                        break;
                                }
                            }
                        });

                        if(!mBlueService.checkXIAOMIPaired()){
                            Log.d("qiqi", "xiaomi no");
                            enableDialog("Cannot connect to Bluetooth Gamepad!!!");
                            mBlueService.startScan();
                        }else{
                            Log.d("qiqi","xiaomi is");
                        }
                    }catch (Exception e){
                        Log.d("qiqi", "service connected error:" + e.toString());
                    }
                }else{
                    Log.d("qiqi","mBlueService null");
                }


            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(intent, mBlueConn, Context.BIND_AUTO_CREATE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("qjizho.vrlauncher.action.battery_changed");
        batteryReceiver = new BattReceiver();
        registerReceiver(batteryReceiver, intentFilter);
    }

    public class BattReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("qjizho.vrlauncher.action.battery_changed")) {
                isCharging = intent.getBooleanExtra("status", false);
                capacity = intent.getIntExtra("capacity", -1);

                if(isCharging){
                    battery_left.setImageResource(battery_list[5]);
                    battery_right.setImageResource(battery_list[5]);
                }else{
                    if(capacity == 100){
                        battery_left.setImageResource(battery_list[4]);
                        battery_right.setImageResource(battery_list[4]);
                    }else if(capacity < 100 && capacity >= 70){
                        battery_left.setImageResource(battery_list[3]);
                        battery_right.setImageResource(battery_list[3]);
                    }else if(capacity < 70 && capacity >= 40){
                        battery_left.setImageResource(battery_list[2]);
                        battery_right.setImageResource(battery_list[2]);
                    }else if(capacity < 40 && capacity > 10){
                        battery_left.setImageResource(battery_list[1]);
                        battery_right.setImageResource(battery_list[1]);
                    }else if(capacity <= 10){
                        battery_left.setImageResource(battery_list[0]);
                        battery_right.setImageResource(battery_list[0]);
                    }
                }
                battery_text_left.setText(capacity + "%");
                battery_text_right.setText(capacity + "%");
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mBlueConn);
        if(batteryReceiver != null)
            unregisterReceiver(batteryReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(hideUIRun, 1000);
        handler.sendEmptyMessageDelayed(0, 1000);
        Log.d("qiqi", "getRequestedOrientation():" + getRequestedOrientation());
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }
    private void enableDialog(String str){
        mAlertTextLeft.setText(str);
        mAlertTextRight.setText(str);
        mAlertDialogLeft.setVisibility(View.VISIBLE);
        mALertDialogRight.setVisibility(View.VISIBLE);
    }
    private void disableDialog(){
        mAlertDialogLeft.setVisibility(View.GONE);
        mALertDialogRight.setVisibility(View.GONE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(cur_mode == 3){
                    ((ImageView)((ViewGroup) grid_left.getChildAt(cur_selected_launcher)).getChildAt(0)).setImageResource(imageResources[cur_selected_launcher]);
                    ((ImageView)((ViewGroup) grid_right.getChildAt(cur_selected_launcher)).getChildAt(0)).setImageResource(imageResources[cur_selected_launcher]);
                    cur_selected_launcher --;
                    if(cur_selected_launcher == -1){
                        cur_selected_launcher = 2;
                    }
                    Log.d("qiqi", "" + cur_selected_launcher);
                    ((ImageView)((ViewGroup) grid_left.getChildAt(cur_selected_launcher)).getChildAt(0)).setImageResource(imageResources_focus[cur_selected_launcher]);
                    ((ImageView)((ViewGroup) grid_right.getChildAt(cur_selected_launcher)).getChildAt(0)).setImageResource(imageResources_focus[cur_selected_launcher]);
                }else if (cur_mode <= 2){
                    if((cur_selected_explorer > 0 && cur_selected_explorer < appList.size())){
//                        (explorer_left.getChildAt(cur_selected_explorer)).setBackgroundColor(getResources().getColor(android.R.color.black));
                        cur_selected_explorer -- ;
//                        (explorer_left.getChildAt(cur_selected_explorer)).setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                        Log.d("qiqi", "cur_selected_explorer :" + cur_selected_explorer);
                        if((cur_selected_explorer)/9 != cur_page_explorer ){
                            cur_page_explorer = (cur_selected_explorer)/9;
                            explorer_left.setSelection(cur_page_explorer*9);
                            explorer_right.setSelection(cur_page_explorer*9);
                        }
                        switch(cur_mode){
                            case 2:
                                mAppAdapter.notifyDataSetChanged();
                                break;
                        }
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(cur_mode == 3){
                    ((ImageView)((ViewGroup) grid_left.getChildAt(cur_selected_launcher)).getChildAt(0)).setImageResource(imageResources[cur_selected_launcher]);
                    ((ImageView)((ViewGroup) grid_right.getChildAt(cur_selected_launcher)).getChildAt(0)).setImageResource(imageResources[cur_selected_launcher]);
                    cur_selected_launcher ++ ;
                    if(cur_selected_launcher == 3){
                        cur_selected_launcher = 0;
                    }
                    ((ImageView)((ViewGroup) grid_left.getChildAt(cur_selected_launcher)).getChildAt(0)).setImageResource(imageResources_focus[cur_selected_launcher]);
                    ((ImageView)((ViewGroup) grid_right.getChildAt(cur_selected_launcher)).getChildAt(0)).setImageResource(imageResources_focus[cur_selected_launcher]);
                    Log.d("qiqi", "" + cur_selected_launcher);

                }else if (cur_mode <= 2){
                    if((cur_selected_explorer >= 0 && cur_selected_explorer < appList.size())){
//                        (explorer_left.getChildAt(cur_selected_explorer)).setBackgroundColor(getResources().getColor(android.R.color.black));
                        cur_selected_explorer ++ ;
                        if(cur_selected_explorer >= realListAppCount)
                            cur_selected_explorer = realListAppCount -1;
//                        (explorer_left.getChildAt(cur_selected_explorer)).setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                        Log.d("qiqi", "cur_selected_explorer :" + cur_selected_explorer);
                        if((cur_selected_explorer)/9 != cur_page_explorer ){
                            cur_page_explorer = (cur_selected_explorer)/9;
                            explorer_left.setSelection(cur_page_explorer*9);
                            explorer_right.setSelection(cur_page_explorer*9);
                        }
                        switch(cur_mode){
                            case 2:
                                mAppAdapter.notifyDataSetChanged();
                                Log.d("qiqi","app adapter notified cur_selected_explorer:" + cur_selected_explorer);
                                break;
                        }
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if(cur_mode <= 2){
                    if(cur_selected_explorer/3 > 0){
//                        (explorer_left.getChildAt(cur_selected_explorer)).setBackgroundColor(getResources().getColor(android.R.color.black));
                        cur_selected_explorer = cur_selected_explorer - 3 ;
                        Log.d("qiqi", "cur_selected_explorer :" + cur_selected_explorer);
                        if((cur_selected_explorer)/9 != cur_page_explorer ){
                            cur_page_explorer = (cur_selected_explorer)/9;
                            explorer_left.setSelection(cur_page_explorer*9);
                            explorer_right.setSelection(cur_page_explorer*9);
                            Log.d("qiqi", "setSelection:" + cur_page_explorer * 9);
                        }

                        switch(cur_mode){
                            case 2:
                                mAppAdapter.notifyDataSetChanged();
                                break;
                        }
//                        int aaa = cur_selected_explorer - cur_visible_explorer*3;
//                        Log.d("qiqi", "explorer_left.getchildcount:" + explorer_left.getChildCount() + "  cur_selected_explorer - cur_visible_explorer*3 :" + aaa );
//                        ((RelativeLayout)explorer_left.getChildAt(cur_selected_explorer - cur_visible_explorer*3)).setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                    }


//                    if(cur_selected_explorer > 0){
//                        cur_selected_explorer -- ;
//                        explorer_left.smoothScrollBy(-dp2px(this, 40), 10);
//                        Log.d("qiqi", "cur_selected_explorer :" + cur_selected_explorer );
//                    }


                }else if (cur_mode ==3){

                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if(cur_mode <= 2){
                    if(cur_selected_explorer/3 < appList.size()/3){
                        cur_selected_explorer = cur_selected_explorer + 3;
                        if(cur_selected_explorer >= realListAppCount)
                            cur_selected_explorer = realListAppCount -1;

                        Log.d("qiqi", "cur_selected_explorer :" + cur_selected_explorer);
                        if((cur_selected_explorer)/9 != cur_page_explorer ){
                            cur_page_explorer = (cur_selected_explorer)/9;
                            explorer_left.setSelection(cur_page_explorer*9);
                            explorer_right.setSelection(cur_page_explorer*9);
                            Log.d("qiqi", "setSelection:" +cur_page_explorer*9);
                        }

                        switch(cur_mode){
                            case 2:
                                mAppAdapter.notifyDataSetChanged();
                                break;
                        }
                    }


                }else if (cur_mode ==3){

                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                switch(cur_mode){
                    case 0:
                        startActivity(new Intent( lstSettings.get(cur_selected_explorer).get("action")));
                        break;
                    case 3:
                        cur_mode = cur_selected_launcher;
                        Log.d("qiqi","set cur_mode:" + cur_mode);
                        controlMode(cur_mode);
                        break;
                    case 2:
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(appList.get(cur_selected_explorer).packageName);
                        startActivity(launchIntent);
                        break;
                    case 4:
//                        disableDialog();
                        handler.sendEmptyMessage(2);
                        Thread uninstallThread = new Thread(new UnInstallRun(appList.get(cur_selected_explorer).packageName));
                        uninstallThread.start();
                        cur_mode = 5;
                        break;
                    case 6:
                        cur_mode = 2;
                        disableDialog();
                        getApps();
                        realListAppCount = appList.size();
                        if(appList.size()%9 > 0){
                            while (true){
                                AppInfo info = new AppInfo();
                                appList.add(info);
                                if(appList.size()%9 == 0){
                                    break;
                                }
                            }
                        }
                        mAppAdapter = new AppsAdapter(Launcher.this, appList);
                        if (cur_selected_explorer > 0){
                            cur_selected_explorer = cur_selected_explorer -1;
                        }
                        explorer_left.setAdapter(mAppAdapter);
                        explorer_right.setAdapter(mAppAdapter);
                        break;
//                    case 3:
//                        Intent intent = new Intent("com.qjizho.pps.VIEW");
//                        intent.putExtra("url", lstPics.get(cur_selected_explorer).get("img"));
//                        startActivity(intent);
                }
                break;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_BUTTON_B:
                if(cur_mode <= 2){
                    cur_mode = 3;
                    controlMode(cur_mode);
                }else if(cur_mode == 3){

                }else if(cur_mode == 4){
                    disableDialog();
                    cur_mode = 2;
                }else if(cur_mode == 6){
                    disableDialog();
                    getApps();
                    realListAppCount = appList.size();
                    if(appList.size()%9 > 0){
                        while (true){
                            AppInfo info = new AppInfo();
                            appList.add(info);
                            if(appList.size()%9 == 0){
                                break;
                            }
                        }
                    }
                    mAppAdapter = new AppsAdapter(Launcher.this, appList);
                    if (cur_selected_explorer > 0){
                        cur_selected_explorer = cur_selected_explorer -1;
                    }
                    explorer_left.setAdapter(mAppAdapter);
                    explorer_right.setAdapter(mAppAdapter);
                    cur_mode = 2;
                }

                break;
            case KeyEvent.KEYCODE_BUTTON_Y:
                switch(cur_mode) {
                    case 2:
                        cur_mode = 4;
                        Log.d("qiqi","delete,app");
                        enableDialog(String.format(getResources().getString(R.string.apk_uninstall_confirm),appList.get(cur_selected_explorer).appName));
                        mAlertConfirmLeft.setText(R.string.confirm);
                        mAlertConfirmRight.setText(R.string.confirm);
                        break;
                }
                break;
            default:
                break;
        }
        return true;
    }
    private void controlMode(int mode){
        switch(mode){
            case 3:
                showLauncher();
                cur_selected_explorer = 0;
                break;
            case 1:
                cur_mode = 3;
                Intent explorerIntent = new Intent("com.qjizho.vrlauncher.EXPLORERACTIVITY");
                explorerIntent.putExtra("isCharging", isCharging);
                explorerIntent.putExtra("capacity", capacity);
                Log.d("qiqi","send isCharging:" + isCharging + " capacity:" + capacity);
                startActivity(explorerIntent);
                break;
            default:
                showExplorer(mode);
                break;
        }

    }
    private void showLauncher(){
        explorer_left.setAdapter(null);
        explorer_right.setAdapter(null);
        grid_left.setVisibility(View.VISIBLE);
        selected_left.setVisibility(View.GONE);
        explorer_left.setVisibility(View.GONE);
        arrow_left.setVisibility(View.GONE);
        grid_right.setVisibility(View.VISIBLE);
        selected_right.setVisibility(View.GONE);
        explorer_right.setVisibility(View.GONE);
        arrow_right.setVisibility(View.GONE);
    }

    private void showExplorer(int pos){
        grid_left.setVisibility(View.GONE);
        selected_left.setVisibility(View.VISIBLE);
        selected_left.setImageResource((int) mapList.get(pos).get("img"));
        explorer_left.setVisibility(View.VISIBLE);
        arrow_left.setVisibility(View.VISIBLE);
        grid_right.setVisibility(View.GONE);
        selected_right.setVisibility(View.VISIBLE);
        selected_right.setImageResource((int) mapList.get(pos).get("img"));
        explorer_right.setVisibility(View.VISIBLE);
        arrow_right.setVisibility(View.VISIBLE);
        switch (pos) {
//            case 3:
//                GetFiles("/mnt/sdcard/vrpics/", "jpg", true);
//                realListPicCount = lstPics.size();
//                if(lstPics.size()%9 > 0){
//                    while (true){
//                        Map<String , String> map = new HashMap<String, String>();
//                        map.put("img", "");
//                        map.put("name", "");
//                        lstPics.add(map);
//                        if(lstPics.size()%9 == 0){
//                            break;
//                        }
//                    }
//                }
//                mPicAdapter = new PicsAdapter(Launcher.this, lstPics, true);
//                explorer_left.setAdapter(mPicAdapter);
//                explorer_right.setAdapter(mPicAdapter);
//                break;
            case 2:
                getApps();
                realListAppCount = appList.size();
                if(appList.size()%9 > 0){
                    while (true){
                        AppInfo info = new AppInfo();
                        appList.add(info);
                        if(appList.size()%9 == 0){
                            break;
                        }
                    }
                }
                mAppAdapter = new AppsAdapter(Launcher.this, appList);
                explorer_left.setAdapter(mAppAdapter);
                explorer_right.setAdapter(mAppAdapter);
                break;
            case 0:
                lstSettings = getSettingList();
                settingAdapter = new PicsAdapter(Launcher.this, lstSettings, false);
                explorer_left.setAdapter(settingAdapter);
                explorer_right.setAdapter(settingAdapter);
                break;
        }
    }
    private List<Map<String, Object>> moveListLeft(List<Map<String, Object>> mList){
        mList.add(mList.get(0));
        mList.remove(0);
        return mList;
    }

    private List<Map<String, Object>> moveListRight(List<Map<String, Object>> mList){
        mList.add(0, mList.get(4));
        mList.remove(5);
        return mList;
    }
    private List<Map<String, String>> getSettingList() {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Map<String, String> map = new HashMap<String, String>();
        map.put("img", "" + R.mipmap.wifi_setting);
        map.put("title", "Wifi");
        map.put("action", "com.qjizho.vrlauncher.WIFIACTIVITY");
        list.add(map);

        return list;
    }
    private List<Map<String, Object>> getLauncherList() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("img", R.mipmap.setting);
        map.put("title", "设置");
        map.put("action", "");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("img", R.drawable.easyicon_sd);
        map.put("title", "文件夹");
        map.put("info", "");
        list.add(map);

//        map = new HashMap<String, Object>();
//        map.put("img", R.mipmap.movies);
//        map.put("title", "Movie");
//        map.put("info", "");
//        list.add(map);
//
//        map = new HashMap<String, Object>();
//        map.put("img", R.mipmap.pictures);
//        map.put("title", "Picture");
//        map.put("info", "");
//        list.add(map);

        map = new HashMap<String, Object>();
        map.put("img", R.mipmap.games);
        map.put("title", "应用");
        map.put("info", "");
        list.add(map);

        return list;
    }

    class UnInstallRun implements  Runnable {
        private String packageName = null;

        public UnInstallRun(String packageName) {
            this.packageName = packageName;
        }

        @Override
        public void run() {
            if (packageName != null) {
                int result = uninstallSlient(Launcher.this, packageName);
                cur_mode = 6;
                handler.sendEmptyMessage(1);
            }
        }
    }

    /**
     * install slient
     *
     * @param context
     * @param packageName
     * @return 0 means normal, 1 means file not exist, 2 means other exception error
     */
    public static int uninstallSlient(Context context, String packageName) {
//      public static String uninstall(String pakage) {
        Log.d("qiqi","start to uninstall " + packageName);
        String[] args = { "pm", "uninstall", packageName };
        String result = null;
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (Exception e) {
            Log.d("qiqi","error:" + e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        Log.d("qiqi", result);
        if (result.contains("Success")){
            return 0;
        }
        return 1;
    }

    public void getApps(){
        appList = new ArrayList<AppInfo>(); //用来存储获取的应用信息数据
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        for(int i=0;i<packages.size();i++) {
            PackageInfo packageInfo = packages.get(i);
            AppInfo tmpInfo =new AppInfo();
            tmpInfo.appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            tmpInfo.packageName = packageInfo.packageName;
            tmpInfo.versionName = packageInfo.versionName;
            tmpInfo.versionCode = packageInfo.versionCode;
            tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(getPackageManager());
            //Only display the non-system app info
            if((packageInfo.applicationInfo.flags& ApplicationInfo.FLAG_SYSTEM)==0)
            {
                if(!tmpInfo.appName.equals(getApplicationInfo().loadLabel(this.getPackageManager()).toString())){
                    appList.add(tmpInfo);//如果非系统应用，则添加至appList
                    Log.d("qiqi", tmpInfo.appName + " " + getApplicationInfo().loadLabel(this.getPackageManager()).toString());
                }
            }

        }
    }
    public void GetFiles(String Path, String Extension, boolean IsIterative)
    {
        File file1 = new File(Path);
        if (file1 == null || file1.listFiles() == null)
            return ;
        boolean isD = file1.isDirectory();
        Log.d("qiqi", Path + " is Directory :" + isD + " contains item:" + file1.listFiles().length);

        File[] files = new File(Path).listFiles();
        Map<String , String> map ;
        for (int i = 0; i < files.length; i++)
        {
            File f = files[i];
            if (f.isFile())
            {
                Log.d("qiqi" , i + " " + f.getPath());
                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension))
                {
                    map = new HashMap<String, String>();
                    map.put("img", "file://" + f.getPath());
                    map.put("name", f.getName());
                    lstPics.add(map);
                }
                if (!IsIterative)
                    break;
            }
            else if (f.isDirectory() && f.getPath().indexOf("/.") == -1)
                GetFiles(f.getPath(), Extension, IsIterative);
        }
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

    public static final int EXTERNAL_STORAGE_REQ_CODE = 10 ;

    public void requestPermission(){
        //判断当前Activity是否已经获得了该权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            //如果App的权限申请曾经被用户拒绝过，就需要在这里跟用户做出解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "please give me the permission", Toast.LENGTH_SHORT).show();
            } else {
                //进行权限请求
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_REQ_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_REQ_CODE: {
                // 如果请求被拒绝，那么通常grantResults数组为空
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //申请成功，进行相应操作

                } else {
                    //申请失败，可以继续向用户解释。
                }
                return;
            }
        }
    }
    public class PicsAdapter extends BaseAdapter{

        private Context mContext;
        private List<Map<String, String>> mDataList;
        private ImageLoaderConfiguration config;
        private ImageLoader imageLoader;
        private boolean mUserLoader;
        public PicsAdapter(Context context, List<Map<String, String>> dataList, boolean useLoader){
            mContext = context;
            mDataList = dataList;
            config = ImageLoaderConfiguration.createDefault(mContext);
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
            mUserLoader = useLoader;
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            Log.d("qiqi", "position:" + position + " " + convertView);
            ViewHolder holder = null;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.explorer_list_item_with_name, parent, false);
                holder.image = (ImageView) convertView.findViewById(R.id.img);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            if(mUserLoader){
                imageLoader.displayImage(mDataList.get(position).get("img"),  holder.image ,options);
            }else{
                holder.image.setImageResource(Integer.parseInt(mDataList.get(position).get("img")));
            }
            if(position == cur_selected_explorer){
                convertView.setBackgroundResource(R.drawable.explorer_item_background);
            }else{
                convertView.setBackground(null);
            }
            return convertView;
        }

    }

    public class AppsAdapter extends BaseAdapter{

        private Context mContext;
        private ArrayList<AppInfo> mDataList;
        private ImageLoaderConfiguration config;
        private ImageLoader imageLoader;
        private boolean mUserLoader;
        public AppsAdapter(Context context,ArrayList<AppInfo> dataList){
            mContext = context;
            mDataList = dataList;
            config = ImageLoaderConfiguration.createDefault(mContext);
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            Log.d("qiqi", "position:" + position + " " + convertView);
            ViewHolder holder = null;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.explorer_list_item_with_name, parent, false);
                holder.image = (ImageView) convertView.findViewById(R.id.img);
                holder.text = (TextView) convertView.findViewById(R.id.txt);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
                holder.image.setImageDrawable(mDataList.get(position).appIcon);
                holder.text.setText(mDataList.get(position).appName);
            if(position == cur_selected_explorer){
                convertView.setBackgroundResource(R.drawable.explorer_item_background);
            }else{
                convertView.setBackground(null);
            }
            return convertView;
        }

    }
    
    final class ViewHolder{
        ImageView image;
        TextView text;
    }

    public static int dp2px(Context context, float dpVal)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
    public class AppInfo {
        public String appName="";
        public String packageName="";
        public String versionName="";
        public int versionCode=0;
        public Drawable appIcon=null;
        public void print()
        {
            Log.v("app","Name:"+appName+" Package:"+packageName);
            Log.v("app","Name:"+appName+" versionName:"+versionName);
            Log.v("app","Name:"+appName+" versionCode:"+versionCode);
        }
    }

    @Override
    public void handleBatteryChanged(int charging, int level) {
        if(charging == BatteryManager.BATTERY_STATUS_CHARGING){
            battery_left.setImageResource(R.drawable.easyicon_battery_charging);
            battery_right.setImageResource(R.drawable.easyicon_battery_charging);
        }else{
            if(level >= 90 && level <= 100){
                battery_left.setImageResource(R.drawable.easyicon_battery_5);
                battery_right.setImageResource(R.drawable.easyicon_battery_5);
            }else if (level >= 50 && level < 90){
                battery_left.setImageResource(R.drawable.easyicon_battery_4);
                battery_right.setImageResource(R.drawable.easyicon_battery_4);
            }else if (level >= 25 && level < 50){
                battery_left.setImageResource(R.drawable.easyicon_battery_3);
                battery_right.setImageResource(R.drawable.easyicon_battery_3);
            }else if (level >= 10 && level < 50){
                battery_left.setImageResource(R.drawable.easyicon_battery_2);
                battery_right.setImageResource(R.drawable.easyicon_battery_2);
            }else if (level >= 0 && level < 10){
                battery_left.setImageResource(R.drawable.easyicon_battery_1);
                battery_right.setImageResource(R.drawable.easyicon_battery_1);
            }
        }
    }
}
