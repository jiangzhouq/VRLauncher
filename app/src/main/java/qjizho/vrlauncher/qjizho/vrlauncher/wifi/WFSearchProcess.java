package qjizho.vrlauncher.wifi;

import qjizho.vrlauncher.usb.modules.WIFI;

/**
 * Created by qjizho on 15/10/27.
 */
public class WFSearchProcess implements Runnable {

    public WIFI wifi;
    public WFSearchProcess(WIFI wifi) {
        this.wifi = wifi;
    }

    public boolean running = false;
    private long startTime = 0L;
    private Thread thread  = null;

    @Override
    public void run() {
        while(true) {
            //是否
            if(!running) return;
            if(System.currentTimeMillis() - startTime >= 30000L) {
                //发送（搜索超时）消息
                wifi.notifyTimeOut();
            }
            try {
                Thread.sleep(10L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        try {
            thread = new Thread(this);
            running = true;
            startTime = System.currentTimeMillis();
            thread.start(); //开启线程
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void stop() {
        try {
            running = false;
            thread = null;
            startTime = 0L;
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}