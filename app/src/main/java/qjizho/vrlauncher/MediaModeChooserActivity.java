package qjizho.vrlauncher;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class MediaModeChooserActivity extends AppCompatActivity{
    private int cur_pos = 0;
    private View mDecorView;
    private ImageView image2d_left;
    private ImageView image3d_left;
    private ImageView imagepano_left;

    private ImageView image2d_right;
    private ImageView image3d_right;
    private ImageView imagepano_right;
    private ArrayList<ImageView> imagesListLeft = new ArrayList<>();
    private ArrayList<ImageView> imagesListRight = new ArrayList<>();
    private int[] images = new int[]{R.drawable.icon_2d, R.drawable.icon_3d, R.drawable.icon_pano};
    private int[] images_p = new int[]{R.drawable.icon_2d_p, R.drawable.icon_3d_p, R.drawable.icon_pano_p};
    private String url ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }else{
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        setContentView(R.layout.activity_media_mode_chooser);

        mDecorView = getWindow().getDecorView();
        hideSystemUI();
        image2d_left = (ImageView) findViewById(R.id.twod_left);
        image3d_left = (ImageView) findViewById(R.id.threed_left);
        imagepano_left = (ImageView) findViewById(R.id.pano_left);
        imagesListLeft.add(image2d_left);
        imagesListLeft.add(image3d_left);
        imagesListLeft.add(imagepano_left);
        image2d_right = (ImageView) findViewById(R.id.twod_right);
        image3d_right = (ImageView) findViewById(R.id.threed_right);
        imagepano_right = (ImageView) findViewById(R.id.pano_right);
        imagesListRight.add(image2d_right);
        imagesListRight.add(image3d_right);
        imagesListRight.add(imagepano_right);

        updateBackGround();
        url = getIntent().getStringExtra("url");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(cur_pos > 0)
                    cur_pos --;
                updateBackGround();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(cur_pos < 2)
                    cur_pos ++;
                updateBackGround();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                if (cur_pos == 0){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("co.mobius.vrcinema");
                    Uri data = Uri.parse(url);
                    intent.setDataAndType(data, "video/*");
                    startActivity(intent);
                }else if(cur_pos == 1){
                    Intent internt = new Intent(Intent.ACTION_VIEW);
                    internt.setDataAndType(Uri.parse(url), "video/*");
                    internt.setComponent(new ComponentName("com.android.gallery3d","com.android.gallery3d.app.MovieActivity"));
                    startActivity(internt);
                }else{
                    Intent intent = new Intent(MediaModeChooserActivity.this, qjizho.vrlauncher.SimpleStreamPlayerActivity.class);
                    intent.putExtra("url", url);
                    startActivity(intent);
                }
                break;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_BUTTON_B:
                this.finish();
                break;
            case KeyEvent.KEYCODE_BUTTON_Y:
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
    private void updateBackGround(){
        for(int i = 0; i < 3; i++){
            if(i == cur_pos){
                imagesListLeft.get(i).setImageResource(images_p[i]);
                imagesListRight.get(i).setImageResource(images_p[i]);
            }else{
                imagesListLeft.get(i).setImageResource(images[i]);
                imagesListRight.get(i).setImageResource(images[i]);
            }
        }
    }
}
