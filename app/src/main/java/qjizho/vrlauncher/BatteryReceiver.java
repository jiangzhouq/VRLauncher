package qjizho.vrlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by qjizho on 15/10/27.
 */
public class BatteryReceiver extends BroadcastReceiver {
    public static ArrayList<BatteryHandler> ehList = new ArrayList<BatteryHandler>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("qiqi","battery changed");
        if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            //获取当前电量
            int level = intent.getIntExtra("level", 0);
            Log.d("qiqi","level:" + level);
            //电量的总刻度
            int scale = intent.getIntExtra("scale", 100);
            Log.d("qiqi","scale:" + scale);
            //把它转成百分比
            int charging = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_CHARGING);

            for(int i = 0; i < ehList.size(); i++) {
                ((BatteryHandler)ehList.get(i)).handleBatteryChanged(charging, level*100/scale);
            }

            //wifi打开或关闭状态变化   "android.net.wifi.WIFI_STATE_CHANGED"
        }

    }

    public static abstract interface BatteryHandler {
        /**处理连接变化事件**/
        public abstract void handleBatteryChanged(int charging, int level);
    }
}
