package qjizho.vrlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by qjizho on 15/10/27.
 */
public class ShutDownReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("qiqi","shut down receiverd:" + intent.getAction());
        if(intent.getAction().equals("com.ut.action.power.off")){
            context.startActivity(new Intent(context, ShutDownActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

}
