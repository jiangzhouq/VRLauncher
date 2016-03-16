package qjizho.vrlauncher;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class FanService extends Service {

    @Override
    public void onCreate() {
        Log.d("qiqi","FanService oncreated");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("qiqi","FanService onstarted");
        //和上一笔记中创建通知的步骤一样，只是不需要通过通知管理器进行触发，而是用startForeground(ID,notify)来处理
        //步骤1：和上一笔记一样，通过Notification.Builder( )来创建通知
        //FakePlayer就是两个大button的activity，也即服务的界面，见最左图
        Intent i = new Intent("android.intent.category.HOME");
        //注意Intent的flag设置：FLAG_ACTIVITY_CLEAR_TOP: 如果activity已在当前任务中运行，在它前端的activity都会被关闭，它就成了最前端的activity。FLAG_ACTIVITY_SINGLE_TOP: 如果activity已经在最前端运行，则不需要再加载。设置这两个flag，就是让一个且唯一的一个activity（服务界面）运行在最前端。
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        Notification myNotify = new Notification.Builder(this)
                .setContentTitle("Test")
                .setContentText("Now Playing: \"Ummmm, Nothing\"")
                .setContentIntent(pi)
                .build();
        //设置notification的flag，表明在点击通知后，通知并不会消失，也在最右图上仍在通知栏显示图标。这是确保在activity中退出后，状态栏仍有图标可提下拉、点击，再次进入activity。
        myNotify.flags |= Notification.FLAG_NO_CLEAR;

        // 步骤 2：startForeground( int, Notification)将服务设置为foreground状态，使系统知道该服务是用户关注，低内存情况下不会killed，并提供通知向用户表明处于foreground状态。
        startForeground(1339,myNotify);
        startFanListener();
        return Service.START_STICKY;
    }

    private void startFanListener(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(3000);
                        getTemp(FanService.this);
                        Log.d("qiqi","sleep 3000 ,");
                    }catch(Exception e){
                        Log.d("qiqi", "e:" + e.toString());
                    }
                }
            }
        }).start();


    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("qiqi", "service binded");
        return new UpdateBinder();
    }

    public class UpdateBinder extends Binder{

        public FanService getService(){
            return FanService.this;
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


    public static int getTemp(Context context) {
        String[] args = { "cat", "/sys/devices/platform/tegra12-i2c.0/i2c-0/0-004c/ext_temperature"};
        ProcessBuilder processBuilder = new ProcessBuilder(args);

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        String result = "";

        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s ;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
                result = s;
            }

            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        Log.d("qiqi", "result:" + result);
        // TODO should add memory is not enough here

        if (!result.isEmpty() && ((int)Double.parseDouble(result)) > 0) {
            return ((int)Double.parseDouble(result));
        } else {
            return 0;
        }
    }
}