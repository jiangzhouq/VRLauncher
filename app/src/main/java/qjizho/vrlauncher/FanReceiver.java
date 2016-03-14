package qjizho.vrlauncher;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by qjizho on 15/10/27.
 */
public class FanReceiver extends BroadcastReceiver {

    private Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if(!this.isWorked("qjizho.vrlauncher.FanService")){
            Intent sIntent = new Intent();
            sIntent.setAction("com.pascalwelsch.circularprogressbarsample.FAN_SERVICE");
            Log.d("qiqi", "开始启动服务！！");
            // 启动Service
            mContext.startService(new Intent(mContext, FanService.class));
        }
        else{
            Log.d("qiqi", "服务已经启动了！！");
        }
    }

    private boolean isWorked(String className) {
        ActivityManager myManager = (ActivityManager) mContext.getSystemService(
                        Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(className)) {
                return true;
            }
        }
        return false;
    }
}
