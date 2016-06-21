package qjizho.vrlauncher.usb.modules;

import android.content.Context;
import android.os.Build;

/**
 * Created by qjizho on 16/5/9.
 */
public class About {
    private static About tInstance;
    private static Context mContext;

    private About(Context context){
        mContext = context;
    }

    public static synchronized About getInstance(Context context){
        if(null == tInstance){
            tInstance = new About(context);
        }
        return tInstance;
    }

    public String getModuleInfo(){
        return Build.MODEL;
    }

    public int getSDKInfo(){
        return Build.VERSION.SDK_INT;
    }

    public String getSystemInfo(){
        return "0.0.1";
    }
}
