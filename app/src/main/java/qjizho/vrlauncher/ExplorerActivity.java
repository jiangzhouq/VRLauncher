package qjizho.vrlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import qjizho.vrlauncher.wifi.WFSearchProcess;
import qjizho.vrlauncher.wifi.WTAdapter;
import qjizho.vrlauncher.wifi.WifiAdmin;


public class ExplorerActivity extends Activity implements BatteryReceiver.BatteryHandler{

    private View mDecorView;

    private static final int state_loading = 0;
    private static final int state_explorer = 1;
    private static final int state_dialog_apk = 2;
    private static final int state_dialog_apk_installing = 3;
    private static final int state_dialog_apk_installed = 4;
    private static final int state_dialog_video = 5;
    private static final int state_dialog_pic = 6;
    private static final int state_dialog_delete = 7;
    private static final int state_dialog_deleting = 8;
    private static final int state_dialog_deleted = 9;

    private int mCurState = 0;

    private ImageView selected_left;
    private ImageView selected_right;
    private GridView explorer_left;
    private GridView explorer_right;

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
    private int cur_page_explorer = 0;
    private boolean scanResultReceived = false;
    //The views
    private ProgressBar mLoadingPBLeft;
    private ProgressBar mLoadingPBRight;
    private LinearLayout mPasswdLayoutLeft;
    private RelativeLayout mAlertDialogLeft;
    private RelativeLayout mALertDialogRight;
    private ImageView battery_left;
    private ImageView battery_right;
    private int[] battery_list = new int[]{
            R.drawable.easyicon_battery_1,
            R.drawable.easyicon_battery_2,
            R.drawable.easyicon_battery_3,
            R.drawable.easyicon_battery_4,
            R.drawable.easyicon_battery_5,
            R.drawable.easyicon_battery_charging,
    };

    private TextView mAlertTextLeft;
    private TextView mAlertTextRight;
    private TextView mAlertConfirmLeft;
    private TextView mAlertConfirmRight;
    private QueryRun mQueryRun = new QueryRun(null);
    private File mSDPath = null;
    private ArrayList<File> cFiles = null;
    private FilesAdapter mFilesAdapter;
    private int realFilesCount;
    private ArrayList<File> mControlPath = new ArrayList<File>();
    private ArrayList<Integer> mControlPosition = new ArrayList<Integer>();
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    mLoadingPBLeft.setVisibility(View.GONE);
                    mLoadingPBRight.setVisibility(View.GONE);
                    mCurState = state_explorer;
                    Log.d("qiqi", "cFiles.length:" + cFiles.size());
                    if(mFilesAdapter == null){
                        mFilesAdapter = new FilesAdapter(ExplorerActivity.this, cFiles);
                        explorer_left.setAdapter(mFilesAdapter);
                        explorer_right.setAdapter(mFilesAdapter);
                    }else{
                        mFilesAdapter.setData(cFiles);
                        mFilesAdapter.notifyDataSetChanged();
                    }
                    break;
                case 1:
                    mAlertConfirmLeft.setVisibility(View.VISIBLE);
                    mAlertConfirmRight.setVisibility(View.VISIBLE);
                    mAlertConfirmLeft.setText(R.string.apk_installed);
                    mAlertConfirmRight.setText(R.string.apk_installed);
                    break;
                case 2:
                    mAlertConfirmLeft.setText(R.string.apk_installing);
                    mAlertConfirmRight.setText(R.string.apk_installing);
                    break;
                case 3:
                    mAlertConfirmLeft.setVisibility(View.VISIBLE);
                    mAlertConfirmRight.setVisibility(View.VISIBLE);
                    mAlertConfirmLeft.setText(R.string.deleted);
                    mAlertConfirmRight.setText(R.string.deleted);
                    break;
                case 4:
                    mAlertConfirmLeft.setText(R.string.deleting);
                    mAlertConfirmRight.setText(R.string.deleting);
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
        setContentView(R.layout.explorer_layout);
        mDecorView = getWindow().getDecorView();
        hideSystemUI();
        selected_left = (ImageView) findViewById(R.id.selected_left);
        selected_right = (ImageView) findViewById(R.id.selected_right);
        explorer_left = (GridView) findViewById(R.id.explorer_left);
        explorer_right = (GridView) findViewById(R.id.explorer_right);
        mLoadingPBLeft = (ProgressBar) findViewById(R.id.loading_progressbar_left);
        mLoadingPBRight = (ProgressBar) findViewById(R.id.loading_progressbar_right);
        mPasswdLayoutLeft = (LinearLayout) findViewById(R.id.passwd_layout_left);
        mAlertDialogLeft = (RelativeLayout) findViewById(R.id.alert_layout_left);
        mALertDialogRight = (RelativeLayout) findViewById(R.id.alert_layout_right);
        mAlertTextLeft = (TextView)findViewById(R.id.alert_txt_left);
        mAlertTextRight = (TextView) findViewById(R.id.alert_txt_right);
        mAlertConfirmLeft = (TextView) findViewById(R.id.alert_confirm_left);
        mAlertConfirmRight = (TextView) findViewById(R.id.alert_confirm_right);
        selected_left.setImageResource(R.drawable.easyicon_sd);
        selected_right.setImageResource(R.drawable.easyicon_sd);
        battery_left = (ImageView) findViewById(R.id.battery_left);
        battery_right = (ImageView) findViewById(R.id.battery_right);
        //wifi管理类
        m_wiFiAdmin  = WifiAdmin.getInstance(this);
        explorer_left.setAdapter(m_wTAdapter);
        explorer_right.setAdapter(m_wTAdapter);
        BatteryReceiver.ehList.add(this);
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            mSDPath = Environment.getExternalStorageDirectory();
            mSDPath = new File("/sdcard/");
            mControlPath.add(mSDPath);
            mControlPosition.add(0);
            QueryRun queryRun = new QueryRun(mSDPath);
            queryRun.run();
            Log.d("qiqi","mSDPath:" + mSDPath);
        }else{
            Log.d("qiqi","sdcard null");
            Toast.makeText(this, "没有SD卡", Toast.LENGTH_LONG).show();
            enableDialog("未检测到SD卡");
            finish();
        }

        mQueryRun.setPath(mSDPath);
        mQueryRun.run();
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(mCurState == state_explorer){
                    if((cur_selected_explorer > 0 && cur_selected_explorer < cFiles.size())){
//                        (explorer_left.getChildAt(cur_selected_explorer)).setBackgroundColor(getResources().getColor(android.R.color.black));
                        cur_selected_explorer -- ;
                        mControlPosition.set(mControlPosition.size()-1, cur_selected_explorer);
//                        (explorer_left.getChildAt(cur_selected_explorer)).setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                        Log.d("qiqi", "cur_selected_explorer :" + cur_selected_explorer);
                        if((cur_selected_explorer)/9 != cur_page_explorer ){
                            cur_page_explorer = (cur_selected_explorer)/9;
                            explorer_left.setSelection(cur_page_explorer*9);
                            explorer_right.setSelection(cur_page_explorer*9);
                        }
                        mFilesAdapter.notifyDataSetChanged();
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(mCurState == state_explorer){
                    if((cur_selected_explorer >= 0 && cur_selected_explorer < cFiles.size())){
//                        (explorer_left.getChildAt(cur_selected_explorer)).setBackgroundColor(getResources().getColor(android.R.color.black));
                        cur_selected_explorer ++ ;
                        mControlPosition.set(mControlPosition.size()-1, cur_selected_explorer);
                        if(cur_selected_explorer >= realFilesCount)
                            cur_selected_explorer = realFilesCount -1;
//                        (explorer_left.getChildAt(cur_selected_explorer)).setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                        Log.d("qiqi", "cur_selected_explorer :" + cur_selected_explorer);
                        if((cur_selected_explorer)/9 != cur_page_explorer ){
                            cur_page_explorer = (cur_selected_explorer)/9;
                            explorer_left.setSelection(cur_page_explorer*9);
                            explorer_right.setSelection(cur_page_explorer*9);
                        }
                        mFilesAdapter.notifyDataSetChanged();
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if(mCurState == state_explorer) {
                    if (cur_selected_explorer / 3 > 0) {
                        cur_selected_explorer = cur_selected_explorer - 3;
                        mControlPosition.set(mControlPosition.size()-1, cur_selected_explorer);
                        Log.d("qiqi", "cur_selected_explorer :" + cur_selected_explorer);
                        if ((cur_selected_explorer) / 9 != cur_page_explorer) {
                            cur_page_explorer = (cur_selected_explorer) / 9;
                            explorer_left.setSelection(cur_page_explorer * 9);
                            explorer_right.setSelection(cur_page_explorer * 9);
                            Log.d("qiqi", "setSelection:" + cur_page_explorer * 9);
                        }
                        mFilesAdapter.notifyDataSetChanged();
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if(mCurState == state_explorer) {
                    if (cur_selected_explorer / 3 < cFiles.size()) {
                        cur_selected_explorer = cur_selected_explorer + 3;
                        mControlPosition.set(mControlPosition.size()-1, cur_selected_explorer);
                        if (cur_selected_explorer >= realFilesCount)
                            cur_selected_explorer = realFilesCount - 1;

                        Log.d("qiqi", "cur_selected_explorer :" + cur_selected_explorer);
                        if ((cur_selected_explorer) / 9 != cur_page_explorer) {
                            cur_page_explorer = (cur_selected_explorer) / 9;
                            explorer_left.setSelection(cur_page_explorer * 9);
                            explorer_right.setSelection(cur_page_explorer * 9);
                            Log.d("qiqi", "setSelection:" + cur_page_explorer * 9);
                        }
                        mFilesAdapter.notifyDataSetChanged();
                    }
                }

                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                if(mCurState == state_explorer) {
                    if(cFiles.size() <= cur_selected_explorer){
                        break;
                    }
                    if(cFiles.get(cur_selected_explorer).isDirectory()){
                        mControlPath.add(cFiles.get(cur_selected_explorer));

                        Log.d("qiqi", "cfile:" + cFiles.get(cur_selected_explorer));
                        QueryRun mQueryRun = new QueryRun(cFiles.get(cur_selected_explorer));
                        mQueryRun.run();
                        cur_selected_explorer = 0;
                        mControlPosition.add(0);

                        if((cur_selected_explorer >= 0 && cur_selected_explorer < cFiles.size())) {
//                        (explorer_left.getChildAt(cur_selected_explorer)).setBackgroundColor(getResources().getColor(android.R.color.black));
                            cur_selected_explorer++;
                            mControlPosition.set(mControlPosition.size() - 1, cur_selected_explorer);
                            if (cur_selected_explorer >= realFilesCount)
                                cur_selected_explorer = realFilesCount - 1;
//                        (explorer_left.getChildAt(cur_selected_explorer)).setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                            Log.d("qiqi", "cur_selected_explorer :" + cur_selected_explorer);
                            if ((cur_selected_explorer) / 9 != cur_page_explorer) {
                                cur_page_explorer = (cur_selected_explorer) / 9;
                                explorer_left.setSelection(cur_page_explorer * 9);
                                explorer_right.setSelection(cur_page_explorer * 9);
                            }
                            mFilesAdapter.notifyDataSetChanged();
                        }

                        if((cur_selected_explorer > 0 && cur_selected_explorer < cFiles.size())){
//                        (explorer_left.getChildAt(cur_selected_explorer)).setBackgroundColor(getResources().getColor(android.R.color.black));
                            cur_selected_explorer -- ;
                            mControlPosition.set(mControlPosition.size()-1, cur_selected_explorer);
//                        (explorer_left.getChildAt(cur_selected_explorer)).setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                            Log.d("qiqi", "cur_selected_explorer :" + cur_selected_explorer);
                            if((cur_selected_explorer)/9 != cur_page_explorer ){
                                cur_page_explorer = (cur_selected_explorer)/9;
                                explorer_left.setSelection(cur_page_explorer*9);
                                explorer_right.setSelection(cur_page_explorer*9);
                            }
                            mFilesAdapter.notifyDataSetChanged();
                        }

                    }else{
                        switch (indentifyFileType(cFiles.get(cur_selected_explorer).getName())){
                            //image
                            case 1:
                                Intent iintent = new Intent(ExplorerActivity.this, SimplePicPlayerActivity.class);
                                iintent.putExtra("url", cFiles.get(cur_selected_explorer).getAbsolutePath());
                                startActivity(iintent);
                                break;
                            //video
                            case 2:
                                Intent intent = new Intent(ExplorerActivity.this, SimpleStreamPlayerActivity.class);
                                intent.putExtra("url", cFiles.get(cur_selected_explorer).getAbsolutePath());
                                startActivity(intent);
                                break;
                            case 3:
                                mCurState = state_dialog_apk;
                                enableDialog(String.format(getResources().getString(R.string.apk_confirm),cFiles.get(cur_selected_explorer).getName()));
                                mAlertConfirmLeft.setText(R.string.confirm);
                                mAlertConfirmRight.setText(R.string.confirm);
                                break;
                            case 4:
                                break;
                        }
                    }
                }else if (mCurState == state_dialog_apk){
                    mCurState = state_dialog_apk_installing;
                    handler.sendEmptyMessage(2);
//                    InstallRun install = new InstallRun(cFiles.get(cur_selected_explorer).getAbsolutePath());
//                    install.run();
                    Thread thread = new Thread(new InstallRun(cFiles.get(cur_selected_explorer).getAbsolutePath()) {
                    });
                    thread.start();
//                    int result = installSlient(this, cFiles.get(cur_selected_explorer).getAbsolutePath());
//                    int result = 0;
//                    try{
//                        Thread.sleep(5000);
//                    }catch(Exception e){
//
//                    }
//                    if(result == 0){
//                        mCurState = state_dialog_apk_installed;
//                        handler.sendEmptyMessage(1);
//                    }
//                    Log.d("qiqi", "result:" + result);
                }else if (mCurState == state_dialog_apk_installed){
                    disableDialog();
                    mCurState = state_explorer;
                }else if (mCurState == state_dialog_delete){
                    mCurState = state_dialog_deleting;
                    handler.sendEmptyMessage(4);
                    Thread thread = new Thread(new DeleteRun(cFiles.get(cur_selected_explorer).getAbsoluteFile()) {
                    });
                    thread.start();

//                    if(cFiles.get(cur_selected_explorer).isDirectory()){
//                        deleteMyDirectory(cFiles.get(cur_selected_explorer).getAbsoluteFile());
//                    }else{
//                        deleteMyFile(cFiles.get(cur_selected_explorer).getAbsolutePath());
//                    }
//                    mCurState = state_explorer;
                }else if (mCurState == state_dialog_deleted){
                    disableDialog();
                    mCurState = state_explorer;
                    QueryRun mQueryRun = new QueryRun(mControlPath.get(mControlPath.size() -1));
                    mQueryRun.run();
                }
                break;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_BUTTON_B:
                switch(mCurState){
                    case state_explorer:
                        if(mControlPath.size() > 1){
                            mControlPath.remove(mControlPath.size() - 1);
                            mControlPosition.remove(mControlPosition.size() - 1);

                            QueryRun mQueryRun = new QueryRun(mControlPath.get(mControlPath.size() -1));
                            mQueryRun.run();

                            cur_selected_explorer = mControlPosition.get(mControlPosition.size()-1);
                            cur_page_explorer = 0;

//                            if(mFilesAdapter == null){
//                                mFilesAdapter = new FilesAdapter(ExplorerActivity.this, cFiles);
//                                explorer_left.setAdapter(mFilesAdapter);
//                                explorer_right.setAdapter(mFilesAdapter);
//                            }else{
//                                mFilesAdapter.setData(cFiles);
//                                mFilesAdapter.notifyDataSetChanged();
//                            }

                            if ((cur_selected_explorer) / 9 != cur_page_explorer) {
                                cur_page_explorer = (cur_selected_explorer) / 9;
                                explorer_left.requestFocus();
                                explorer_left.setSelection(cur_page_explorer * 9);
                                explorer_right.setSelection(cur_page_explorer * 9);
                            }
                            mFilesAdapter.notifyDataSetChanged();
                        }else{
                            this.finish();
                        }
                        break;
                    case state_dialog_apk:
                    case state_dialog_apk_installed:
                    case state_dialog_delete:
                    case state_dialog_pic:
                    case state_dialog_video:
                        disableDialog();
                        mCurState = state_explorer;
                        break;
                    case state_dialog_deleted:
                        disableDialog();
                        QueryRun mQueryRun = new QueryRun(mControlPath.get(mControlPath.size() -1));
                        mQueryRun.run();
                        break;
                }

                break;
            case KeyEvent.KEYCODE_BUTTON_Y:
                switch(mCurState) {
                    case state_explorer:
                        Log.d("qiqi", "start to delet pos:" + cur_selected_explorer);
                        if(cFiles.size() <= cur_selected_explorer){
                            break;
                        }
                        mCurState = state_dialog_delete;
                        if(cFiles.get(cur_selected_explorer).isDirectory()){
                            enableDialog(String.format(getResources().getString(R.string.apk_delete_dir),cFiles.get(cur_selected_explorer).getName()));
                        }else{
                            enableDialog(String.format(getResources().getString(R.string.apk_delete_file),cFiles.get(cur_selected_explorer).getName()));
                        }
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

    private boolean deleteMyDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteMyDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }
    private boolean deleteMyFile(String selectedFilePath){
        File file = new File(selectedFilePath);
        boolean deleted = file.delete();
        return deleted;
    }
    /**
     * install slient
     *
     * @param context
     * @param filePath
     * @return 0 means normal, 1 means file not exist, 2 means other exception error
     */
    public static int installSlient(Context context, String filePath) {
        File file = new File(filePath);
        if (filePath == null || filePath.length() == 0 || (file = new File(filePath)) == null || file.length() <= 0
                || !file.exists() || !file.isFile()) {
            return 1;
        }

        String[] args = { "pm", "install", "-r", filePath };
        ProcessBuilder processBuilder = new ProcessBuilder(args);

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;

            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }

            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = 2;
        } catch (Exception e) {
            e.printStackTrace();
            result = 2;
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        // TODO should add memory is not enough here
        if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
            result = 0;
        } else {
            result = 2;
        }
        Log.d("installSlient", "successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
        return result;
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
            Log.d("qiqi","queryPath:" + queryPath);
            if(queryPath != null){
                File[] files = queryPath.listFiles();
                cFiles = new ArrayList<File>(Arrays.asList(files));
                realFilesCount = cFiles.size();
                if(cFiles.size()%9 > 0){
                    while (true){
                        cFiles.add(new File(""));
                        if(cFiles.size()%9 == 0){
                            break;
                        }
                    }
                }
                Message msg = new Message();
                msg.what = 0;
                handler.sendMessage(msg);
            }
        }
    }
    class InstallRun implements  Runnable {
        private String installPath = null;

        public InstallRun(String path) {
            this.installPath = path;
        }

        @Override
        public void run() {
            if (installPath != null) {
                int result = installSlient(ExplorerActivity.this, installPath);
                if (result == 0) {
                    mCurState = state_dialog_apk_installed;
                    handler.sendEmptyMessage(1);
                }
            }
        }
    }

    class DeleteRun implements  Runnable {
        private File toDelete = null;

        public DeleteRun(File toDelete) {
            this.toDelete = toDelete;
        }

        @Override
        public void run() {
                    if(toDelete.isDirectory()){
                        deleteMyDirectory(toDelete);
                    }else{
                        deleteMyFile(toDelete.getAbsolutePath());
                    }
                    mCurState = state_dialog_deleted;
                    handler.sendEmptyMessage(3);
        }
    }

    public class FilesAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<File> mFiles ;
        private ImageLoaderConfiguration config;
        private ImageLoader imageLoader;
        private boolean mUserLoader;
        public FilesAdapter(Context context,ArrayList<File> data){
            mContext = context;
            mFiles = data;
            config = ImageLoaderConfiguration.createDefault(mContext);
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
            Log.d("qiqi", mFiles.size() + "");
            for(File f : mFiles){
                Log.d("qiqi",f.getName());
            }
        }
        public void setData(ArrayList<File> data){
            mFiles = data;
            this.notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return mFiles.size();
        }

        @Override
        public Object getItem(int position) {
            return mFiles.get(position);
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
            if(mFiles.get(position).isDirectory()){
                holder.image.setImageDrawable(getResources().getDrawable(R.drawable.easyicon_folder));
            }else if (mFiles.get(position).isFile()){
                holder.image.setImageDrawable(calFileType(mFiles.get(position).getName()));
            }else{
                holder.image.setImageDrawable(null);
            }
            holder.text.setText(mFiles.get(position).getName());
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
    private Drawable calFileType(String str){
        switch(indentifyFileType(str)){
            case 1:
                return getResources().getDrawable(R.drawable.easyicon_file_image);
            case 2:
                return getResources().getDrawable(R.drawable.easyicon_file_video);
            case 3:
                return getResources().getDrawable(R.drawable.easyicon_file_apk);
            case 4:
                return getResources().getDrawable(R.drawable.easyicon_file);
            default:
                return getResources().getDrawable(R.drawable.easyicon_file);
        }
    }

    //1 for iamge , 2 for video , 3 for apk ,4 for others.
    private int indentifyFileType(String str){
        if(str.toLowerCase().endsWith("bmp")
                || str.toLowerCase().endsWith("dib")
                || str.toLowerCase().endsWith("emf")
                || str.toLowerCase().endsWith("gif")
                || str.toLowerCase().endsWith("icb")
                || str.toLowerCase().endsWith("ico")
                || str.toLowerCase().endsWith("jpg")
                || str.toLowerCase().endsWith("jpeg")
                || str.toLowerCase().endsWith("pbm")
                || str.toLowerCase().endsWith("pcd")
                || str.toLowerCase().endsWith("pcx")
                || str.toLowerCase().endsWith("pgm")
                || str.toLowerCase().endsWith("png")
                || str.toLowerCase().endsWith("ppm")
                || str.toLowerCase().endsWith("psp")
                || str.toLowerCase().endsWith("tif")
                || str.toLowerCase().endsWith("sgi")){
            return 1;
        }else if (str.toLowerCase().endsWith("avi")
                || str.toLowerCase().endsWith("rmvb")
                || str.toLowerCase().endsWith("rm")
                || str.toLowerCase().endsWith("asf")
                || str.toLowerCase().endsWith("divx")
                || str.toLowerCase().endsWith("mpg")
                || str.toLowerCase().endsWith("mpeg")
                || str.toLowerCase().endsWith("mpe")
                || str.toLowerCase().endsWith("wmv")
                || str.toLowerCase().endsWith("mp4")
                || str.toLowerCase().endsWith("mkv")
                || str.toLowerCase().endsWith("vob")) {
            return 2;
        }else if (str.toLowerCase().endsWith("apk")){
            return 3;
        }
        return 4;
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
