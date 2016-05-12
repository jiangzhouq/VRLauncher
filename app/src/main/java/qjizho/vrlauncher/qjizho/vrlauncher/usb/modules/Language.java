package qjizho.vrlauncher.usb.modules;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by qjizho on 16/5/9.
 */
public class Language {
    private static Language tInstance;
    private static Context mContext;

    private Language(Context context){
        mContext = context;
    }

    public static synchronized Language getInstance(Context context){
        if(null == tInstance){
            tInstance = new Language(context);
        }
        return tInstance;
    }

    public void setLanguage(String language){
        Resources res = mContext.getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.locale = new Locale(language.toLowerCase());
        res.updateConfiguration(conf, dm);
    }

    public String getLanguage(){
        return Locale.getDefault().getDisplayLanguage();
    }

    public ArrayList<String> getSupportedLang(){
        ArrayList<String> supported = new ArrayList<>();
        supported.add("en");
        supported.add("zh");
        return supported;
    }
}
