package  qjizho.vrlauncher;


import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class KillProgramService extends Service {

    @Override
    public void onCreate() {
        Log.d("qiqi", "FanService oncreated");
        super.onCreate();
    }
    private ArrayList<String> apps = new ArrayList<String>();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("qiqi", "FanService onstarted");
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < apps.size() ; i++){

                    if(printIfLive(apps.get(i))){
                        killIfLive(apps.get(i));
                    }
                }
            }
        }).start();
        getApps();
        return Service.START_STICKY;
    }
    public void getApps(){
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        for(int i=0;i<packages.size();i++) {
            PackageInfo packageInfo = packages.get(i);
            String appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            String packageName = packageInfo.packageName;
            //Only display the non-system app info
            if((packageInfo.applicationInfo.flags& ApplicationInfo.FLAG_SYSTEM)==0) {
                if(!appName.equals(getApplicationInfo().loadLabel(this.getPackageManager()).toString())){
                    apps.add(packageName);//如果非系统应用，则添加至appList
                    Log.d("qiqi", "killservice add :" + packageName);
                }
            }

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("qiqi", "service binded");
        return new UpdateBinder();
    }

    public class UpdateBinder extends Binder{

        public KillProgramService getService(){
            return KillProgramService.this;
        }
    }

    @Override
    public void onDestroy() {
        Log.d("qiqi","FanService killed");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }




    public static boolean printIfLive(String packageName) {
        Runtime runtime = Runtime.getRuntime();
        DataOutputStream dataOut = null;
        DataInputStream datain = null;

        String string1 = "ps|grep " + packageName + "\n";
        Process process = null;
        boolean result = false;
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
                result = true;
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

    public static String killIfLive(String packageName) {
        Runtime runtime = Runtime.getRuntime();
        DataOutputStream dataOut = null;
        Log.d("qiqi", "start to kill : "+ packageName);
        String string1 = "am force-stop " + packageName + "\n";
        Process process = null;
        String result = "";
        try {
            process = runtime.exec("sh ");
            dataOut = new DataOutputStream(process.getOutputStream());
            dataOut.writeBytes(string1);
            dataOut.flush();
            dataOut.close();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (dataOut != null) {
                    dataOut.close();
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