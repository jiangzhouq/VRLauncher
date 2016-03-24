package qjizho.vrlauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;


public class ExplorerChooseActivity extends Activity{

    private View mDecorView;
    private Button btn1_left;
    private Button btn2_left;
    private Button btn3_left;
    private Button btn1_right;
    private Button btn2_right;
    private Button btn3_right;
    private Button btn4_left;
    private Button btn4_right;

    private Button[] btns_left ;
    private Button[] btns_right ;
    private int curBtn = 1;

    private ArrayList<MyFile> avFiles = new ArrayList<MyFile>();

    private boolean isCharging = false;
    private int capacity = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }else{
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        setContentView(R.layout.activity_explorer_choose);
        mDecorView = getWindow().getDecorView();
        btn1_left = (Button) findViewById(R.id.btn1_left);
        btn2_left = (Button) findViewById(R.id.btn2_left);
        btn3_left = (Button) findViewById(R.id.btn3_left);
        btn1_right = (Button) findViewById(R.id.btn1_right);
        btn2_right = (Button) findViewById(R.id.btn2_right);
        btn3_right = (Button) findViewById(R.id.btn3_right);

        btn4_left = (Button) findViewById(R.id.btn4_left);
        btn4_right = (Button) findViewById(R.id.btn4_right);

        btns_left = new Button[]{btn1_left, btn2_left, btn3_left, btn4_left};
        btns_right = new Button[]{btn1_right, btn2_right, btn3_right, btn4_right};
        hideSystemUI();

        avFiles = new ArrayList<MyFile>();

        isCharging = getIntent().getBooleanExtra("isCharging", false);
        capacity = getIntent().getIntExtra("capacity", 0);

        File storageFile = new File("/storage/");
        File[] storageFiles = storageFile.listFiles();
        for(int i = 0; i < storageFiles.length; i ++){
            File curFile = storageFiles[i];
            if(curFile.isDirectory() ){
                if(curFile != null && curFile.listFiles() != null && curFile.listFiles().length > 0){
                    if(curFile.getName().contains("usb") || curFile.getName().contains("sdcard")){
                        Log.d("qiqi", storageFiles[i].getName() + " is Diractory");
                        MyFile myFile = new MyFile();
                        myFile.fName = storageFiles[i].getName();
                        myFile.fUrl = storageFiles[i].getAbsolutePath();
                        avFiles.add(myFile);
                    }
                }

            }
        }
        for(int i =0; i< avFiles.size(); i++){
            btns_left[i].setText(avFiles.get(i).fName);
            btns_right[i].setText(avFiles.get(i).fName);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_DPAD_UP:
                curBtn --;
                if(curBtn == 0)
                    curBtn = avFiles.size();
                setBtnFocused(curBtn);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                curBtn ++;
                if(curBtn == avFiles.size() + 1)
                    curBtn = 1;
                setBtnFocused(curBtn);
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                Intent explorerIntent = new Intent(this, qjizho.vrlauncher.ExplorerActivity.class);
                explorerIntent.putExtra("isCharging", isCharging);
                explorerIntent.putExtra("capacity", capacity);
                explorerIntent.putExtra("startUrl", avFiles.get(curBtn - 1).fUrl);
                Log.d("qiqi","send isCharging:" + isCharging + " capacity:" + capacity);
                startActivity(explorerIntent);
                break;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_BUTTON_B:
                this.finish();
                break;
            default:
                break;
        }
        return true;
    }

    private void setBtnFocused(int i){
        btn1_left.setBackgroundColor(getResources().getColor(R.color.white));
        btn1_right.setBackgroundColor(getResources().getColor(R.color.white));
        btn1_left.setTextColor(getResources().getColor(R.color.black));
        btn1_right.setTextColor(getResources().getColor(R.color.black));
        btn2_left.setBackgroundColor(getResources().getColor(R.color.white));
        btn2_left.setTextColor(getResources().getColor(R.color.black));
        btn2_right.setBackgroundColor(getResources().getColor(R.color.white));
        btn2_right.setTextColor(getResources().getColor(R.color.black));
        btn3_left.setBackgroundColor(getResources().getColor(R.color.white));
        btn3_right.setBackgroundColor(getResources().getColor(R.color.white));
        btn3_left.setTextColor(getResources().getColor(R.color.black));
        btn3_right.setTextColor(getResources().getColor(R.color.black));
        btn4_left.setBackgroundColor(getResources().getColor(R.color.white));
        btn4_right.setBackgroundColor(getResources().getColor(R.color.white));
        btn4_left.setTextColor(getResources().getColor(R.color.black));
        btn4_right.setTextColor(getResources().getColor(R.color.black));
        switch (i){
            case 1:
                btn1_left.setBackgroundColor(getResources().getColor(R.color.red));
                btn1_right.setBackgroundColor(getResources().getColor(R.color.red));
                btn1_left.setTextColor(getResources().getColor(R.color.white));
                btn1_right.setTextColor(getResources().getColor(R.color.white));
                break;
            case 2:
                btn2_left.setBackgroundColor(getResources().getColor(R.color.red));
                btn2_right.setBackgroundColor(getResources().getColor(R.color.red));
                btn2_left.setTextColor(getResources().getColor(R.color.white));
                btn2_right.setTextColor(getResources().getColor(R.color.white));
                break;
            case 3:
                btn3_left.setBackgroundColor(getResources().getColor(R.color.red));
                btn3_right.setBackgroundColor(getResources().getColor(R.color.red));
                btn3_left.setTextColor(getResources().getColor(R.color.white));
                btn3_right.setTextColor(getResources().getColor(R.color.white));
                break;
            case 4:
                btn4_left.setBackgroundColor(getResources().getColor(R.color.red));
                btn4_right.setBackgroundColor(getResources().getColor(R.color.red));
                btn4_left.setTextColor(getResources().getColor(R.color.white));
                btn4_right.setTextColor(getResources().getColor(R.color.white));
                break;
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

    private class MyFile{
        public String fName = "";
        public String fUrl = "";
    }

}
