package qjizho.vrlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import qjizho.vrlauncher.wifi.WFSearchProcess;
import qjizho.vrlauncher.wifi.WTAdapter;
import qjizho.vrlauncher.wifi.WifiAdmin;


public class ExplorerActivity extends Activity{

    private View mDecorView;

    private static final int state_loading = 0;
    private static final int state_explorer = 1;
    private static final int state_dialog = 2;
    private static final int state_keyboard = 3;

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
    private LinearLayout mAlertDialogLeft;
    private LinearLayout mALertDialogRight;
    private TextView mAlertTextLeft;
    private TextView mAlertTextRight;
    private LinearLayout mPasswdLayoutRight;
    private TextView mWifiSSIDLeft;
    private TextView mWifiSSIDRight;
    private EditText mWifiPasswdLeft;
    private EditText mWifiPasswdRight;
    private TextView to_focus;
    private QueryRun mQueryRun = new QueryRun(null);
    private File mSDPath = null;
    private ArrayList<File> cFiles = null;
    private FilesAdapter mFilesAdapter;
    private int realFilesCount;
    private ArrayList<File> mControlPath = new ArrayList<File>();
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    mLoadingPBLeft.setVisibility(View.GONE);
                    mLoadingPBRight.setVisibility(View.GONE);
                    mCurState = state_explorer;
                    if(mFilesAdapter == null){
                        mFilesAdapter = new FilesAdapter(ExplorerActivity.this, cFiles);
                        explorer_left.setAdapter(mFilesAdapter);
                        explorer_right.setAdapter(mFilesAdapter);
                    }else{
                        mFilesAdapter.setData(cFiles);
                    }
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
        mPasswdLayoutRight = (LinearLayout) findViewById(R.id.passwd_layout_right);
        mWifiSSIDLeft = (TextView) findViewById(R.id.wifi_ssid_left);
        mWifiSSIDRight = (TextView) findViewById(R.id.wifi_ssid_right);
        mWifiPasswdLeft = (EditText) findViewById(R.id.wifi_passwd_left);
        mWifiPasswdRight = (EditText) findViewById(R.id.wifi_passwd_right);
        mAlertDialogLeft = (LinearLayout) findViewById(R.id.alert_layout_left);
        mALertDialogRight = (LinearLayout) findViewById(R.id.alert_layout_right);
        mAlertTextLeft = (TextView)findViewById(R.id.alert_txt_left);
        mAlertTextRight = (TextView) findViewById(R.id.alert_txt_right);
        selected_left.setImageResource(R.mipmap.movies);
        selected_right.setImageResource(R.mipmap.movies);
        //wifi管理类
        m_wiFiAdmin  = WifiAdmin.getInstance(this);
        explorer_left.setAdapter(m_wTAdapter);
        explorer_right.setAdapter(m_wTAdapter);

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            mSDPath = Environment.getExternalStorageDirectory();
            mSDPath = new File("/sdcard/");
            mControlPath.add(mSDPath);
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
                    if(cFiles.get(cur_selected_explorer).isDirectory()){
                        mControlPath.add(cFiles.get(cur_selected_explorer));
                        cur_selected_explorer = 0;
                        QueryRun mQueryRun = new QueryRun(cFiles.get(cur_selected_explorer));
                        mQueryRun.run();
                    }
                }
                break;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_BUTTON_B:
                switch(mCurState){
                    case state_explorer:
                        if(mControlPath.size() > 1){
                            mControlPath.remove(mControlPath.size() -1);
                            cur_selected_explorer = 0;
                            QueryRun mQueryRun = new QueryRun(mControlPath.get(mControlPath.size() -1));
                            mQueryRun.run();
                        }else{
                            this.finish();
                        }
                        break;
                }

                break;
            default:
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
            return getResources().getDrawable(R.drawable.easyicon_file_image);
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
            return getResources().getDrawable(R.drawable.easyicon_file_video);
        }else if (str.toLowerCase().endsWith("apk")){
            return getResources().getDrawable(R.drawable.easyicon_file_apk);
        }
        return getResources().getDrawable(R.drawable.easyicon_file);
    }
}
