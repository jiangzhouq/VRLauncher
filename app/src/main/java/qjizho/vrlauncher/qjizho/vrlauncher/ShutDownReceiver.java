package qjizho.vrlauncher;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by qjizho on 15/10/27.
 */
public class ShutDownReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("qiqi","shut down receiverd:" + intent.getAction());
        String rotation = runCmd("cat /sys/class/graphics/fb0/device/panel_rotation");
        Log.d("rotation", "rotation:"  + rotation);
        if(intent.getAction().equals("com.ut.action.power.off")){
            if(rotation.contains("90")){
                context.startActivity(new Intent(context, ShutDownActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }else{
                context.startActivity(new Intent(context, ShutDownActivity2.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
    }
    public static String runCmd(String command) {
        Runtime runtime = Runtime.getRuntime();
        DataOutputStream dataOut = null;
        DataInputStream datain = null;

        String string1 = command + "\n";
        Process process = null;
        String result = "";
        try {
            process = runtime.exec("sh ");
            dataOut = new DataOutputStream(process.getOutputStream());
            datain = new DataInputStream(process.getInputStream());
            dataOut.writeBytes(string1);
            dataOut.flush();
            dataOut.writeBytes("exit\n");
            dataOut.flush();
            String line = null;
            while ((line = datain.readLine()) != null) {
                Log.d("qiqi", "cat:" + line);
                result = line;
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (dataOut != null) {
                    dataOut.close();
                }
                if (datain != null) {
                    datain.close();
                }
//                if (successResult != null) {
//                    successResult.close();
//                }
//                if (errorResult != null) {
//                    errorResult.close();
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
}
