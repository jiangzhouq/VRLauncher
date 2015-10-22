package qjizho.vrlauncher;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Launcher extends AppCompatActivity {
    private View mDecorView;
    private List<Map<String, Object>> mapList;
    private GridView grid_left;
    private GridView grid_right;
    private ImageView selected_left;
    private ImageView selected_right;
    private ListView explorer_left;
    private ListView explorer_right;
    private ImageLoader imageLoader;
    private ImageLoaderConfiguration config ;
    private int[] imageResources = new int[]{R.mipmap.setting, R.mipmap.store, R.mipmap.movies, R.mipmap.pictures, R.mipmap.games};
    private int[] imageResources_focus = new int[]{R.mipmap.setting_focus, R.mipmap.store_focus, R.mipmap.movies_focus, R.mipmap.pictures_focus, R.mipmap.games_focus};
    private int cur_selected = 2;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    ((ImageView) ((ViewGroup) grid_left.getChildAt(cur_selected)).getChildAt(0)).setImageResource(imageResources_focus[cur_selected]);
                    ((ImageView)((ViewGroup) grid_right.getChildAt(cur_selected)).getChildAt(0)).setImageResource(imageResources_focus[cur_selected]);
                    break;
            }
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
        TextView to_focus = (TextView) findViewById(R.id.to_focus);
        to_focus.requestFocus();
        hideSystemUI();
        grid_left = (GridView) findViewById(R.id.list_left);
        grid_right = (GridView) findViewById(R.id.list_right);
        selected_left = (ImageView) findViewById(R.id.selected_left);
        selected_right = (ImageView) findViewById(R.id.selected_right);
        explorer_left = (ListView) findViewById(R.id.explorer_left);
        explorer_right = (ListView) findViewById(R.id.explorer_right);
        showLauncher();
        config = ImageLoaderConfiguration.createDefault(this);
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

        mapList = getData();
        final SimpleAdapter adapter = new SimpleAdapter(this, mapList,R.layout.list_item,new String[]{"img","title"}, new int[]{R.id.img, R.id.txt});
        grid_left.setAdapter(adapter);
        grid_right.setAdapter(adapter);
        grid_left.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showExplorer(position);
                GetFiles("/mnt/sdcard/DCIM/Camera/", "jpg", true);
                for (int j = 0; j < lstPics.size(); j++) {
                    ImageSimpleAdater explorerAdapter = new ImageSimpleAdater(Launcher.this, lstPics);
                    explorer_left.setAdapter(explorerAdapter);
                    explorer_right.setAdapter(explorerAdapter);
                    Log.d("qiqi", lstPics.get(j).get("img") + " " + lstPics.get(j).get("name"));
                }
            }
        });
        handler.sendEmptyMessageDelayed(0, 1000);

        requestPermission();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_DPAD_LEFT:
                ((ImageView)((ViewGroup) grid_left.getChildAt(cur_selected)).getChildAt(0)).setImageResource(imageResources[cur_selected]);
                ((ImageView)((ViewGroup) grid_right.getChildAt(cur_selected)).getChildAt(0)).setImageResource(imageResources[cur_selected]);
                cur_selected --;
                if(cur_selected == -1){
                    cur_selected = 4;
                }
                Log.d("qiqi", "" + cur_selected);
                ((ImageView)((ViewGroup) grid_left.getChildAt(cur_selected)).getChildAt(0)).setImageResource(imageResources_focus[cur_selected]);
                ((ImageView)((ViewGroup) grid_right.getChildAt(cur_selected)).getChildAt(0)).setImageResource(imageResources_focus[cur_selected]);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                ((ImageView)((ViewGroup) grid_left.getChildAt(cur_selected)).getChildAt(0)).setImageResource(imageResources[cur_selected]);
                ((ImageView)((ViewGroup) grid_right.getChildAt(cur_selected)).getChildAt(0)).setImageResource(imageResources[cur_selected]);
                cur_selected ++ ;
                if(cur_selected == 5){
                    cur_selected = 0;
                }
                ((ImageView)((ViewGroup) grid_left.getChildAt(cur_selected)).getChildAt(0)).setImageResource(imageResources_focus[cur_selected]);
                ((ImageView)((ViewGroup) grid_right.getChildAt(cur_selected)).getChildAt(0)).setImageResource(imageResources_focus[cur_selected]);
                Log.d("qiqi", "" + cur_selected);
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                break;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_BUTTON_B:
                break;
            default:
                break;
        }
        return true;
    }

    private void showLauncher(){
        grid_left.setVisibility(View.VISIBLE);
        selected_left.setVisibility(View.GONE);
        explorer_left.setVisibility(View.GONE);
        grid_right.setVisibility(View.VISIBLE);
        selected_right.setVisibility(View.GONE);
        explorer_right.setVisibility(View.GONE);
    }

    private void showExplorer(int pos){
        grid_left.setVisibility(View.GONE);
        selected_left.setVisibility(View.VISIBLE);
        selected_left.setImageResource((int) mapList.get(pos).get("img"));
        explorer_left.setVisibility(View.VISIBLE);
        grid_right.setVisibility(View.GONE);
        selected_right.setVisibility(View.VISIBLE);
        selected_right.setImageResource((int) mapList.get(pos).get("img"));
        explorer_right.setVisibility(View.VISIBLE);
    }
    private List<Map<String, Object>> moveListLeft(List<Map<String, Object>> mList){
        mList.add(mList.get(0));
        mList.remove(0);
        return mList;
    }

    private List<Map<String, Object>> moveListRight(List<Map<String, Object>> mList){
        mList.add(0,mList.get(4));
        mList.remove(5);
        return mList;
    }
    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("img", R.mipmap.setting);
        map.put("title", "Setting");
        map.put("action", "");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("img", R.mipmap.store);
        map.put("title", "Store");
        map.put("info", "");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("img", R.mipmap.movies);
        map.put("title", "Movie");
        map.put("info", "");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("img", R.mipmap.pictures);
        map.put("title", "Picture");
        map.put("info", "");
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("img", R.mipmap.games);
        map.put("title", "Games");
        map.put("info", "");
        list.add(map);

        return list;
    }
    private List<Map<String, String>> lstPics = new ArrayList<Map<String, String>>();

    public void GetFiles(String Path, String Extension, boolean IsIterative)
    {
        File file1 = new File(Path);
        boolean isD = file1.isDirectory();
        Log.d("qiqi", Path + " is Directory :" + isD + " contains item:" + file1.listFiles().length);

        File[] files = new File(Path).listFiles();
        Map<String , String> map ;
        for (int i = 0; i < files.length; i++)
        {
            File f = files[i];
            if (f.isFile())
            {
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
    public class ImageSimpleAdater extends BaseAdapter{

        private Context mContext;
        private List<Map<String, String>> mDataList;
        private ImageLoaderConfiguration config;
        private ImageLoader imageLoader;
        public ImageSimpleAdater(Context context, List<Map<String, String>> dataList){
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
            ViewHolder holder = null;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.explorer_list_item, parent, false);
                holder.image = (ImageView) convertView.findViewById(R.id.img);
                holder.name = (TextView) convertView.findViewById(R.id.txt);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            imageLoader.displayImage(mDataList.get(position).get("img"),  holder.image);
            holder.name.setText(mDataList.get(position).get("name"));
            return convertView;
        }

    }
    final class ViewHolder{
        ImageView image;
        TextView name;
    }

}
