package qjizho.vrlauncher;

import android.util.Log;

/**
 * Created by qjizho on 16/2/26.
 */
public class MyMethod {
    private static boolean DEBUG_DRAGER = true;
    public static void msg(String str){
        if(DEBUG_DRAGER)
            Log.d("qiqi","INFO: " + str);
    }
}
