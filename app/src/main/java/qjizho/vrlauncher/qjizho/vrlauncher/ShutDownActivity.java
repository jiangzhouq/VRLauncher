package qjizho.vrlauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;


public class ShutDownActivity extends Activity{

    private LinearLayout mAlertShutDowningLeft;
    private LinearLayout mAlertShutDowningRight;
    private View mDecorView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }else{
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        setContentView(R.layout.activity_shutdown);
        mDecorView = getWindow().getDecorView();
        mAlertShutDowningLeft = (LinearLayout) findViewById(R.id.alert_shutdowning_left);
        mAlertShutDowningRight = (LinearLayout) findViewById(R.id.alert_shutdowning_right);
        hideSystemUI();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                mAlertShutDowningLeft.setVisibility(View.VISIBLE);
                mAlertShutDowningRight.setVisibility(View.VISIBLE);
                Intent intent = new Intent();
                intent.setAction("com.ut.action.shut.down");
                sendBroadcast(intent);
                break;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_BUTTON_B:
                if(!(mAlertShutDowningLeft.getVisibility() == View.VISIBLE)){
                    this.finish();
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
}
