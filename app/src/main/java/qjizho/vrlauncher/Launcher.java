package qjizho.vrlauncher;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jiongbull.jlog.JLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.Inflater;

import qjizho.vrlauncher.ExplorerActivity;
import qjizho.vrlauncher.usb.modules.Bluetooth;

public class Launcher extends AppCompatActivity implements qjizho.vrlauncher.BatteryReceiver.BatteryHandler{
    private View mDecorView;
    private List<Map<String, Object>> mapList;
    private List<Map<String, String>> lstPics = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> lstSettings;
    private GridView grid_left;
    private GridView grid_right;
    private ImageView battery_left;
    private ImageView battery_right;
    private TextView battery_text_left;
    private TextView battery_text_right;

    private ArrayList<RelativeLayout> homeListLeft = new ArrayList<>();
    private ArrayList<RelativeLayout> launchpadListLeft = new ArrayList<>();
    private ArrayList<RelativeLayout> homeListRight = new ArrayList<>();
    private ArrayList<RelativeLayout> launchpadListRight = new ArrayList<>();
    private int[] battery_list = new int[]{
            R.drawable.easyicon_battery_1,
            R.drawable.easyicon_battery_2,
            R.drawable.easyicon_battery_3,
            R.drawable.easyicon_battery_4,
            R.drawable.easyicon_battery_5,
            R.drawable.easyicon_battery_charging,
    };
    private int[] icon_bg_list = new int[]{
            R.drawable.bg_blue,
            R.drawable.bg_origin,
            R.drawable.bg_purple,
            R.drawable.bg_sky,
            R.drawable.bg_yellow
    };
    private ImageLoader imageLoader;
    private ImageLoaderConfiguration config ;
    private AppsAdapter mAppAdapter;
    private int LauncherState = 0;

    private RelativeLayout bg_home_left;

    private LinearLayout home_layout_left;
    private RelativeLayout layout_iplayer_left;
    private RelativeLayout layout_player_left;
    private RelativeLayout layout_file_explorer_left;
    private RelativeLayout layout_game1_left;
    private RelativeLayout layout_game2_left;
    private LinearLayout launchpad_layout_left;
    private RelativeLayout layout_icon1_left;
    private RelativeLayout layout_icon2_left;
    private RelativeLayout layout_icon3_left;
    private RelativeLayout layout_icon4_left;
    private RelativeLayout layout_icon5_left;
    private RelativeLayout layout_icon6_left;

    private LinearLayout uninstall_layout_left;
    private RelativeLayout uninstall_icon_left;
    private Button uninstall_cancel_left;
    private Button uninstall_confirm_left;
    private TextView apk_uninstall_alert1_left;


    private RelativeLayout bg_home_right;

    private LinearLayout home_layout_right;
    private RelativeLayout layout_iplayer_right;
    private RelativeLayout layout_player_right;
    private RelativeLayout layout_file_explorer_right;
    private RelativeLayout layout_game1_right;
    private RelativeLayout layout_game2_right;
    private LinearLayout launchpad_layout_right;
    private RelativeLayout layout_icon1_right;
    private RelativeLayout layout_icon2_right;
    private RelativeLayout layout_icon3_right;
    private RelativeLayout layout_icon4_right;
    private RelativeLayout layout_icon5_right;
    private RelativeLayout layout_icon6_right;

    private LinearLayout uninstall_layout_right;
    private RelativeLayout uninstall_icon_right;
    private Button uninstall_cancel_right;
    private Button uninstall_confirm_right;
    private TextView apk_uninstall_alert1_right;


    private int nowState = 0;//0-home,1-uninstall,2-uninstalling,3-uninstalled
    private DisplayImageOptions options;
    private ArrayList<AppInfo> appList;

    private int cur_selected_pos = 0;
    private int old_selected_pos = 0;
    private boolean isCharging = false;
    private DigitalClock mDigitalClockLeft;
    private DigitalClock mDigitalClockRight;
    private int capacity = -1;
    private BroadcastReceiver batteryReceiver = null;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 1:
                    nowState = 3;
                    apk_uninstall_alert1_left.setText(R.string.apk_uninstalled);
                    apk_uninstall_alert1_right.setText(R.string.apk_uninstalled);
                    break;
                case 2:
                    break;
                case 3:
                    Log.d("qiqi","disable dialog");
                    disableDialog();
                    break;

            }
        }
    };

    BroadcastReceiver installReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            JLog.d(intent.getAction() + " received");
            prepareApps();
            updateLauncher(0, 0);
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
        setContentView(R.layout.activity_launcher);
        mDecorView = getWindow().getDecorView();
        hideSystemUI();
        grid_left = (GridView) findViewById(R.id.list_left);
        grid_right = (GridView) findViewById(R.id.list_right);
        battery_left = (ImageView) findViewById(R.id.battery_left);
        battery_right = (ImageView) findViewById(R.id.battery_right);
        battery_text_left = (TextView) findViewById(R.id.battery_text_left);
        battery_text_right = (TextView) findViewById(R.id.battery_text_right);

        home_layout_left = (LinearLayout) findViewById(R.id.home_left);
        layout_iplayer_left = (RelativeLayout) findViewById(R.id.iPlayer_left);
        layout_player_left = (RelativeLayout) findViewById(R.id.player_left);

        layout_file_explorer_left = (RelativeLayout) findViewById(R.id.file_explorer_left);
        layout_game1_left = (RelativeLayout) findViewById(R.id.game1_left);
        layout_game2_left = (RelativeLayout) findViewById(R.id.game2_left);
        homeListLeft.add(layout_iplayer_left);
        homeListLeft.add(layout_player_left);
        homeListLeft.add(layout_file_explorer_left);
        homeListLeft.add(layout_game1_left);
        homeListLeft.add(layout_game2_left);

        launchpad_layout_left = (LinearLayout) findViewById(R.id.launchpad_left);
        layout_icon1_left = (RelativeLayout) findViewById(R.id.icon1_left);
        layout_icon2_left = (RelativeLayout) findViewById(R.id.icon2_left);
        layout_icon3_left = (RelativeLayout) findViewById(R.id.icon3_left);
        layout_icon4_left = (RelativeLayout) findViewById(R.id.icon4_left);
        layout_icon5_left = (RelativeLayout) findViewById(R.id.icon5_left);
        layout_icon6_left = (RelativeLayout) findViewById(R.id.icon6_left);
        launchpadListLeft.add(layout_icon1_left);
        launchpadListLeft.add(layout_icon2_left);
        launchpadListLeft.add(layout_icon3_left);
        launchpadListLeft.add(layout_icon4_left);
        launchpadListLeft.add(layout_icon5_left);
        launchpadListLeft.add(layout_icon6_left);

        uninstall_layout_left = (LinearLayout) findViewById(R.id.uninstall_alert_left);
        uninstall_icon_left = (RelativeLayout) findViewById(R.id.uninstall_alert_left_icon);
//        uninstall_cancel_left = (Button) findViewById(R.id.uninstall_alert_left_cancel);
        uninstall_confirm_left = (Button) findViewById(R.id.uninstall_alert_left_confirm);
        apk_uninstall_alert1_left = (TextView) findViewById(R.id.apk_uninstall_alert1_left);

        home_layout_right = (LinearLayout) findViewById(R.id.home_right);
        layout_iplayer_right = (RelativeLayout) findViewById(R.id.iPlayer_right);
        layout_player_right = (RelativeLayout) findViewById(R.id.player_right);

        layout_file_explorer_right = (RelativeLayout) findViewById(R.id.file_explorer_right);
        layout_game1_right = (RelativeLayout) findViewById(R.id.game1__right);
        layout_game2_right = (RelativeLayout) findViewById(R.id.game2__right);
        homeListRight.add(layout_iplayer_right);
        homeListRight.add(layout_player_right);
        homeListRight.add(layout_file_explorer_right);
        homeListRight.add(layout_game1_right);
        homeListRight.add(layout_game2_right);

        launchpad_layout_right = (LinearLayout) findViewById(R.id.launchpad__right);
        layout_icon1_right = (RelativeLayout) findViewById(R.id.icon1_right);
        layout_icon2_right = (RelativeLayout) findViewById(R.id.icon2_right);
        layout_icon3_right = (RelativeLayout) findViewById(R.id.icon3_right);
        layout_icon4_right = (RelativeLayout) findViewById(R.id.icon4_right);
        layout_icon5_right = (RelativeLayout) findViewById(R.id.icon5_right);
        layout_icon6_right = (RelativeLayout) findViewById(R.id.icon6_right);
        launchpadListRight.add(layout_icon1_right);
        launchpadListRight.add(layout_icon2_right);
        launchpadListRight.add(layout_icon3_right);
        launchpadListRight.add(layout_icon4_right);
        launchpadListRight.add(layout_icon5_right);
        launchpadListRight.add(layout_icon6_right);

        uninstall_layout_right = (LinearLayout) findViewById(R.id.uninstall_alert_right);
        uninstall_icon_right = (RelativeLayout) findViewById(R.id.uninstall_alert_right_icon);
//        uninstall_cancel_left = (Button) findViewById(R.id.uninstall_alert_left_cancel);
        uninstall_confirm_right = (Button) findViewById(R.id.uninstall_alert_right_confirm);
        apk_uninstall_alert1_right = (TextView) findViewById(R.id.apk_uninstall_alert1_right);


        bg_home_left = (RelativeLayout)findViewById(R.id.bg_home_left);
        bg_home_right = (RelativeLayout)findViewById(R.id.bg_home_right);

        config = ImageLoaderConfiguration.createDefault(this);
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(null)  // 设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(null)       // 设置图片加载或解码过程中发生错误显示的图片
                .cacheInMemory(true)                        // 设置下载的图片是否缓存在内存中
                .cacheOnDisc(true)                          // 设置下载的图片是否缓存在SD卡中
                .build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

        mDigitalClockLeft = (DigitalClock) findViewById(R.id.clock_left);
        mDigitalClockRight = (DigitalClock) findViewById(R.id.clock_left);

//        showHome();
//        final AppsAdapter adapter = new AppsAdapter(this, appList);
//        grid_left.setAdapter(adapter);
//        grid_right.setAdapter(adapter);
        requestPermission();
        qjizho.vrlauncher.BatteryReceiver.ehList.add(this);
        prepareApps();
        updateLauncher(0, 0);
        try {
            String path = Environment.getExternalStorageDirectory() + "/Cardboard";
            File dir = new File(path);
            if (dir.mkdirs() || dir.isDirectory()) {
                CopyRAWtoSDCard(R.raw.current_device_params, path + File.separator + "current_device_params");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Settings.Global.putInt(getContentResolver(), Settings.Global.INSTALL_NON_MARKET_APPS,1);

        Bluetooth bluetooth = Bluetooth.getInstance(this);

    }
    private void showHome(){
        JLog.d("showHome");
        bg_home_left.setBackgroundResource(R.drawable.bg_home_b);
        bg_home_right.setBackgroundResource(R.drawable.bg_home_b);
        home_layout_left.setVisibility(View.VISIBLE);
        launchpad_layout_left.setVisibility(View.GONE);
        uninstall_layout_left.setVisibility(View.GONE);
        home_layout_right.setVisibility(View.VISIBLE);
        launchpad_layout_right.setVisibility(View.GONE);
        uninstall_layout_right.setVisibility(View.GONE);

        LayoutInflater inflater = getLayoutInflater();
        for (int i = 0; i < 5 ; i ++){
            if(i >= appList.size()){
                JLog.d("remove all views for:" + i);
                homeListLeft.get(i).removeAllViews();
                homeListRight.get(i).removeAllViews();
                homeListLeft.get(i).setBackground(null);
                homeListRight.get(i).setBackground(null);
                return;
            }
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params1.addRule(RelativeLayout.CENTER_IN_PARENT);
            View view1 = inflater.inflate(R.layout.explorer_list_item_with_name, null);
            ((ImageView)view1.findViewById(R.id.img)).setImageDrawable(appList.get(i).appIcon);
//            ((ImageView)view1.findViewById(R.id.img)).setBackgroundResource(R.drawable.icon_circle);
            ((TextView)view1.findViewById(R.id.txt)).setText(appList.get(i).appName);
            view1.setLayoutParams(params1);
            homeListLeft.get(i).removeAllViews();
            homeListLeft.get(i).addView(view1);
            if(i == 0){
                homeListLeft.get(i).setBackgroundResource(R.drawable.icon_circle_long);
            }else{
                homeListLeft.get(i).setBackgroundResource(R.drawable.icon_circle);
            }

            View view2 = inflater.inflate(R.layout.explorer_list_item_with_name, null);
            ((ImageView)view2.findViewById(R.id.img)).setImageDrawable(appList.get(i).appIcon);
//            ((ImageView)view1.findViewById(R.id.img)).setBackgroundResource(R.drawable.icon_circle);
            ((TextView)view2.findViewById(R.id.txt)).setText(appList.get(i).appName);
            view2.setLayoutParams(params1);
            homeListRight.get(i).removeAllViews();
            homeListRight.get(i).addView(view2);
            if(i == 0){
                homeListRight.get(i).setBackgroundResource(R.drawable.icon_circle_long);
            }else{
                homeListRight.get(i).setBackgroundResource(R.drawable.icon_circle);
            }
//            updateBack(0,0);
            if(appList.get(i).appName.isEmpty()){
                launchpadListLeft.get(i).setBackground(null);
                launchpadListRight.get(i).setBackground(null);
            }
        }
    }
    private void showLaunchPad(){
        JLog.d("showLaunchPad");

        bg_home_left.setBackgroundResource(R.drawable.bg_home_6);
        bg_home_right.setBackgroundResource(R.drawable.bg_home_6);
        home_layout_left.setVisibility(View.GONE);
        launchpad_layout_left.setVisibility(View.VISIBLE);
        uninstall_layout_left.setVisibility(View.GONE);
        home_layout_right.setVisibility(View.GONE);
        launchpad_layout_right.setVisibility(View.VISIBLE);
        uninstall_layout_right.setVisibility(View.GONE);

        int page = (cur_selected_pos - 5) / 6;
        int startPos = 5 + (page*6);
        LayoutInflater inflater = getLayoutInflater();
        for (int i = startPos , j = 0; i < startPos + 6; i++, j++){
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params1.addRule(RelativeLayout.CENTER_IN_PARENT);
            View view1 = inflater.inflate(R.layout.explorer_list_item_with_name, null);
            if(i < appList.size()){
                ((ImageView)view1.findViewById(R.id.img)).setImageDrawable(appList.get(i).appIcon);
//            ((ImageView)view1.findViewById(R.id.img)).setBackgroundResource(R.drawable.icon_circle);
                ((TextView)view1.findViewById(R.id.txt)).setText(appList.get(i).appName);
            }

            view1.setLayoutParams(params1);
            launchpadListLeft.get(j).removeAllViews();
            launchpadListLeft.get(j).addView(view1);
            if(i == 0){
                launchpadListLeft.get(j).setBackgroundResource(R.drawable.icon_circle_long);
            }else{
                launchpadListLeft.get(j).setBackgroundResource(R.drawable.icon_circle);
            }

            View view2 = inflater.inflate(R.layout.explorer_list_item_with_name, null);
            if(i < appList.size()){
                ((ImageView)view2.findViewById(R.id.img)).setImageDrawable(appList.get(i).appIcon);
//            ((ImageView)view1.findViewById(R.id.img)).setBackgroundResource(R.drawable.icon_circle);
                ((TextView)view2.findViewById(R.id.txt)).setText(appList.get(i).appName);
            }

            view2.setLayoutParams(params1);
            launchpadListRight.get(j).removeAllViews();
            launchpadListRight.get(j).addView(view2);
            if(i == 0){
                launchpadListRight.get(j).setBackgroundResource(R.drawable.icon_circle_long);
            }else{
                launchpadListRight.get(j).setBackgroundResource(R.drawable.icon_circle);
            }
            if(i >= appList.size() || appList.get(i).appName.isEmpty()){
                    launchpadListLeft.get(j).setBackground(null);
                    launchpadListRight.get(j).setBackground(null);
            }

        }
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        try{
            this.unregisterReceiver(installReceiver);
        }catch (Exception e){

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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
//        Intent intent = new Intent(this, qjizho.vrlauncher.KillProgramService.class);
//        startService(intent);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("qjizho.vrlauncher.action.battery_changed");
        batteryReceiver = new BattReceiver();
        registerReceiver(batteryReceiver, intentFilter);
        Intent batteryBoradcast = new Intent();
        batteryBoradcast.setAction("com.3dinlife.FAN_RECEIVER");
        sendBroadcast(batteryBoradcast);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
//        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        this.registerReceiver(installReceiver, filter);
    }

    private void updateState(int i){
        nowState = i;
        switch(i){
            //Launcher
            case 0:
                if(cur_selected_pos <=4){
                    uninstall_layout_left.setVisibility(View.GONE);
                    launchpad_layout_left.setVisibility(View.GONE);
                    home_layout_left.setVisibility(View.VISIBLE);

                    uninstall_layout_right.setVisibility(View.GONE);
                    launchpad_layout_right.setVisibility(View.GONE);
                    home_layout_right.setVisibility(View.VISIBLE);
                }else{
                    uninstall_layout_left.setVisibility(View.GONE);
                    launchpad_layout_left.setVisibility(View.VISIBLE);
                    home_layout_left.setVisibility(View.GONE);

                    uninstall_layout_right.setVisibility(View.GONE);
                    launchpad_layout_right.setVisibility(View.VISIBLE);
                    home_layout_right.setVisibility(View.GONE);
                }
                break;
            //uninstalling
            case 1:
                enableUninstallDialog(cur_selected_pos);
                break;
        }
    }
    private void enableUninstallDialog(int i){
        bg_home_left.setBackgroundResource(R.drawable.bg_home_uninstall);
        bg_home_right.setBackgroundResource(R.drawable.bg_home_uninstall);
        uninstall_layout_left.setVisibility(View.VISIBLE);
        launchpad_layout_left.setVisibility(View.GONE);
        home_layout_left.setVisibility(View.GONE);
        uninstall_layout_right.setVisibility(View.VISIBLE);
        launchpad_layout_right.setVisibility(View.GONE);
        home_layout_right.setVisibility(View.GONE);
        LayoutInflater inflater = getLayoutInflater();
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params1.addRule(RelativeLayout.CENTER_IN_PARENT);

        View view1 = inflater.inflate(R.layout.explorer_list_item_with_name, null);
        ((ImageView)view1.findViewById(R.id.img)).setImageDrawable(appList.get(i).appIcon);
//            ((ImageView)view1.findViewById(R.id.img)).setBackgroundResource(R.drawable.icon_circle);
        ((TextView)view1.findViewById(R.id.txt)).setText(appList.get(i).appName);
        view1.setLayoutParams(params1);

        View view2 = inflater.inflate(R.layout.explorer_list_item_with_name, null);
        ((ImageView)view2.findViewById(R.id.img)).setImageDrawable(appList.get(i).appIcon);
//            ((ImageView)view1.findViewById(R.id.img)).setBackgroundResource(R.drawable.icon_circle);
        ((TextView)view2.findViewById(R.id.txt)).setText(appList.get(i).appName);
        view2.setLayoutParams(params1);
        uninstall_icon_left.removeAllViews();
        uninstall_icon_left.addView(view1);
        uninstall_icon_right.removeAllViews();
        uninstall_icon_right.addView(view2);
    }
    private void disableDialog(){
    }

    @Override
    public boolean
    onKeyDown(int keyCode, KeyEvent event) {
        JLog.d("nowState:" + nowState + " current pos:" + cur_selected_pos);
        switch(keyCode){
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(nowState == 0 && cur_selected_pos > 0){
                    old_selected_pos = cur_selected_pos;
                    cur_selected_pos = cur_selected_pos - 1;
                    JLog.d("old_selected_pos:" + old_selected_pos + " cur_selected_pos:" + cur_selected_pos);
                    updateLauncher(old_selected_pos, cur_selected_pos);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(nowState == 0 && cur_selected_pos < appList.size() - 1){
                    old_selected_pos = cur_selected_pos;
                    cur_selected_pos = cur_selected_pos + 1;
                    JLog.d("old_selected_pos:" + old_selected_pos + " cur_selected_pos:" + cur_selected_pos);
                    updateLauncher(old_selected_pos, cur_selected_pos);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if(nowState == 0){
                    if((cur_selected_pos + 1)%6 <=2)
                        break;
                    if(cur_selected_pos - 3 >= 0){
                        old_selected_pos = cur_selected_pos;
                        cur_selected_pos = cur_selected_pos - 3;
                        JLog.d("old_selected_pos:" + old_selected_pos + " cur_selected_pos:" + cur_selected_pos);
                        updateLauncher(old_selected_pos, cur_selected_pos);
                    }else if(cur_selected_pos - 3 >= -1){
                        old_selected_pos = cur_selected_pos;
                        cur_selected_pos = 0;
                        JLog.d("old_selected_pos:" + old_selected_pos + " cur_selected_pos:" + cur_selected_pos);
                        updateLauncher(old_selected_pos, cur_selected_pos);
                    }
                }

                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if(nowState == 0){
                    if((cur_selected_pos + 1)%6 >= 3)
                        break;
                    if(cur_selected_pos + 3 <= appList.size() - 1){
                        old_selected_pos = cur_selected_pos;
                        cur_selected_pos = cur_selected_pos + 3;
                        JLog.d("old_selected_pos:" + old_selected_pos + " cur_selected_pos:" + cur_selected_pos);
                        updateLauncher(old_selected_pos, cur_selected_pos);
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                switch(nowState){
                    case 0:
//                        if(cur_selected_pos == 0){
//                            Intent intent = new Intent();
//                            ComponentName componentName = new ComponentName("com.hotcast.vr","com.qihoo.util.StartActivity");
//                            intent.setComponent(componentName);
//                            startActivity(intent);
//                        }else
                        if(cur_selected_pos == 0){
                            Intent launchIntent = new Intent(this, qjizho.vrlauncher.MoviesActivity.class);
                            launchIntent.putExtra("startUrl","");
                            startActivity(launchIntent);
                        }else if(cur_selected_pos == 1){
                            Intent launchIntent = new Intent(this, ExplorerActivity.class);
                            launchIntent.putExtra("startUrl","");
                            startActivity(launchIntent);
                        }else{
                            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(appList.get(cur_selected_pos).packageName);
                            startActivity(launchIntent);
                        }
                        break;
                    case 1:
                        nowState = 2;
                        apk_uninstall_alert1_left.setText(R.string.apk_uninstalling);
                        apk_uninstall_alert1_right.setText(R.string.apk_uninstalling);
                        UnInstallRun unInstallRun = new UnInstallRun(appList.get(cur_selected_pos).packageName);
                        unInstallRun.run();
                        break;
                    case 3:
                        apk_uninstall_alert1_left.setText(R.string.apk_uninstall_alert1);
                        apk_uninstall_alert1_right.setText(R.string.apk_uninstall_alert1);
                        nowState = 0;
                        old_selected_pos = cur_selected_pos;
                        cur_selected_pos = 0;
                        updateState(0);
                        prepareApps();
                        updateLauncher(0, 0);
                        break;
                }

                break;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_BUTTON_B:
                switch(nowState){
                    case 1:
                        updateState(0);
                        break;
                    case 3:
                        apk_uninstall_alert1_left.setText(R.string.apk_uninstall_alert1);
                        apk_uninstall_alert1_right.setText(R.string.apk_uninstall_alert1);
                        nowState = 0;
                        updateState(0);
                        prepareApps();
                        updateLauncher(0, 0);
                        break;
                }
                break;
            case KeyEvent.KEYCODE_BUTTON_Y:
//                enableUninstallDialog(cur_selected_pos);
                if(nowState == 0 && cur_selected_pos > 2)
                    updateState(1);
                break;
            default:
                break;
        }
        return true;
    }

    private void updateLauncher(int oldPos, int newPos){
        if(oldPos == 5 && newPos == 4){
            LauncherState = 0;
        }else if(oldPos == 4 && newPos == 5){
            LauncherState = 1;
        }
        if(LauncherState == 0){
            showHome();
        }else{
            showLaunchPad();
        }
        updateBack(old_selected_pos, cur_selected_pos);
    }

    private void updateBack(int oldPos, int newPos){
        JLog.d("launcherstate:" + LauncherState + " oldPos:" + oldPos + " newPos:" + newPos);
        if(LauncherState == 0){
            if(oldPos >=0 && oldPos <=4){
                if(oldPos == 0){
                    homeListLeft.get(oldPos).setBackgroundResource(R.drawable.icon_circle_long);
                    homeListRight.get(oldPos).setBackgroundResource(R.drawable.icon_circle_long);
                }else{
                    JLog.d("update back circle:" + oldPos);
                    homeListLeft.get(oldPos).setBackgroundResource(R.drawable.icon_circle);
                    homeListRight.get(oldPos).setBackgroundResource(R.drawable.icon_circle);
                }
            }
            if(newPos >= 0 && newPos <=4){
                if(newPos == 0){
                    homeListLeft.get(newPos).setBackgroundResource(R.drawable.bg_blue_long);
                    homeListRight.get(newPos).setBackgroundResource(R.drawable.bg_blue_long);
                }else{
                    int i = new Random().nextInt(5);
                    homeListLeft.get(newPos).setBackgroundResource(icon_bg_list[i]);
                    homeListRight.get(newPos).setBackgroundResource(icon_bg_list[i]);
                }
            }
        }else if(LauncherState == 1){
            JLog.d("(newPos - 5)%6:" + (newPos - 5)%6);
            if((newPos - 5)%6 <=5){
                int i = new Random().nextInt(5);
                launchpadListLeft.get((newPos - 5)%6).setBackgroundResource(icon_bg_list[i]);
                launchpadListRight.get((newPos - 5)%6).setBackgroundResource(icon_bg_list[i]);
            }
            if((oldPos - 5)%6 >=0){
                launchpadListLeft.get((oldPos - 5)%6).setBackgroundResource(R.drawable.icon_circle);
                launchpadListRight.get((oldPos - 5)%6).setBackgroundResource(R.drawable.icon_circle);
            }

        }



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

    public void prepareApps(){
        cur_selected_pos = 0;
        appList = new ArrayList<AppInfo>(); //用来存储获取的应用信息数据
//        AppInfo iPlayerTmpInfo =new AppInfo();
//        iPlayerTmpInfo.appName = "VR热播";
//        iPlayerTmpInfo.packageName = "";
//        iPlayerTmpInfo.appIcon = getResources().getDrawable(R.drawable.icon_iplayer);
//        appList.add(iPlayerTmpInfo);
        AppInfo playerTmpInfo =new AppInfo();
        playerTmpInfo.appName = "Player";
        playerTmpInfo.packageName = "";
        playerTmpInfo.appIcon = getResources().getDrawable(R.drawable.icon_player);
        appList.add(playerTmpInfo);
        AppInfo explorerTmpInfo =new AppInfo();
        explorerTmpInfo.appName = "Explorer";
        explorerTmpInfo.packageName = "";
        explorerTmpInfo.appIcon = getResources().getDrawable(R.drawable.icon_file_explorer);
        appList.add(explorerTmpInfo);
        getApps();
        for(AppInfo appInfo : appList){
            JLog.d(appInfo.appName);
        }
    }

    public void getApps(){
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
            if((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)==0)
            {
                if(!tmpInfo.appName.equals(getApplicationInfo().loadLabel(this.getPackageManager()).toString())){
                    if(!tmpInfo.packageName.equals("co.mobius.vrcinema") && !tmpInfo.packageName.equals("be.ppareit.swiftp") && !tmpInfo.packageName.equals("com.hotcast.vr")){
                        appList.add(tmpInfo);//如果非系统应用，则添加至appList
                        tmpInfo.print();
                        Log.d("qiqi", tmpInfo.appName + " " + getApplicationInfo().loadLabel(this.getPackageManager()).toString());
                    }
                }
            }

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
//            if(position == cur_selected_explorer){
//                convertView.setBackgroundResource(R.drawable.explorer_item_background);
//            }else{
//                convertView.setBackground(null);
//            }
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
//            if(position == cur_selected_explorer){
//                convertView.setBackgroundResource(R.drawable.explorer_item_background);
//            }else{
//                convertView.setBackground(null);
//            }
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
        public String appName = "";
        public String packageName = "";
        public String versionName = "";
        public int versionCode = 0;
        public Drawable appIcon = null;
        public Drawable appBack = null;
        public void print()
        {
            Log.d("qiqi", "Name:" + appName + " Package:" + packageName);
            Log.d("qiqi", "Name:" + appName + " versionName:" + versionName);
            Log.d("qiqi", "Name:" + appName + " versionCode:" + versionCode);
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
    private void CopyRAWtoSDCard(int id, String path) throws IOException {
        InputStream in = getResources().openRawResource(id);
        FileOutputStream out = new FileOutputStream(path);
        byte[] buff = new byte[1024];
        int read = 0;
        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } finally {
            in.close();
            out.close();
        }
    }
}
