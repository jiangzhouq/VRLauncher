package qjizho.vrlauncher;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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

        explorer_left = (ListView) findViewById(R.id.explorer_left);

        showLauncher();

        mapList = getData();
        final SimpleAdapter adapter = new SimpleAdapter(this, mapList,R.layout.list_item,new String[]{"img","title"}, new int[]{R.id.img, R.id.txt});
        grid_left.setAdapter(adapter);
        grid_right.setAdapter(adapter);
        grid_left.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showExplorer(position);
                GetFiles("/sdcard/DCIM/Camera/IMG_20151021_113627.jpg", "jpg", true);
                for(int j = 0; j < lstPics.size() ; j++){
                    Log.d("qiqi", lstPics.get(j).get("img") + " " + lstPics.get(j).get("name"));
                }
            }
        });
        Button hello = (Button) findViewById(R.id.hello);
        hello.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapList = moveListLeft(mapList);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showLauncher(){
        grid_left.setVisibility(View.VISIBLE);
        selected_left.setVisibility(View.GONE);
        explorer_left.setVisibility(View.GONE);
    }

    private void showExplorer(int pos){
        grid_left.setVisibility(View.GONE);
        selected_left.setVisibility(View.VISIBLE);
        selected_left.setImageResource((int) mapList.get(pos).get("img"));
        explorer_left.setVisibility(View.VISIBLE);
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
        Log.d("qiqi" , Path + " is Directory :" + isD + " contains item:" + file1.list());
//        File[] files = new File(Path).listFiles();
//        Map<String , String> map ;
//        for (int i = 0; i < files.length; i++)
//        {
//            File f = files[i];
//            if (f.isFile())
//            {
//                if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension))
//                {
//                    map = new HashMap<String, String>();
//                    map.put("img", f.getPath());
//                    map.put("name", f.getName());
//                    lstPics.add(map);
//                }
//                if (!IsIterative)
//                    break;
//            }
//            else if (f.isDirectory() && f.getPath().indexOf("/.") == -1)
//                GetFiles(f.getPath(), Extension, IsIterative);
//        }
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
