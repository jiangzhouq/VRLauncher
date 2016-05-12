package qjizho.vrlauncher;

import android.app.Application;

import com.jiongbull.jlog.JLog;

/**
 * Created by qjizho on 16/4/22.
 */
public class VRLauncherApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        JLog.init(this).setDebug(true);
    }

}
