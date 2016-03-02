package qjizho.vrlauncher;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class UpdateService extends Service {

    public interface OnUpdateStateChanged{
        void onStateChanged();
    }

    private OnUpdateStateChanged onUpdateStateChanged;
    public UpdateService() {
    }
    public void setOnBTStateListener( OnUpdateStateChanged listener){
        Log.d("qiqi", "setlitener completed");
        onUpdateStateChanged = listener;
        notifyState();
    }
    private void notifyState(){
        if(onUpdateStateChanged != null){
            onUpdateStateChanged.onStateChanged();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("qiqi","service binded");
        return new UpdateBinder();
    }

    public class UpdateBinder extends Binder{

        public UpdateService getService(){
            return UpdateService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}