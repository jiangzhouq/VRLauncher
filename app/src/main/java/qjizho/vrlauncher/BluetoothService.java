package qjizho.vrlauncher;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

public class BluetoothService extends Service {

    public static final int STATE_BT_ON = 0;
    public static final int STATE_BT_OFF = 1;
    public static final int STATE_CONNECTED =2;
    public static final int STATE_CONNECTING = 3;
    public static final int STATE_DISCONNECTED = 4;
    private int mState = STATE_BT_OFF;
    public interface OnBTStateListener{
        void onStateChanged(int state);
    }
    private BluetoothDevice mDevice = null;
    private OnBTStateListener onBTStateListener = null;
    private BluetoothSocket mSocket = null;
    public BluetoothService() {
    }
    public void setOnBTStateListener( OnBTStateListener listener){
        Log.d("qiqi", "setlitener completed");
        onBTStateListener = listener;
        notifyState(mState);
    }
    private void notifyState(int i){
        mState = i;
        if(onBTStateListener != null){
            onBTStateListener.onStateChanged(i);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("qiqi","service binded");
        // TODO: Return the communication channel to the service.
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null){
            if(bluetoothAdapter.isEnabled()){
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        Log.d("qiqi", device.getName() + " " + device.getAddress());
                        if(device.getName().equals("RG-BLE-12")){
                            mDevice = device;
                        }
                    }
                    if(mDevice!=null){
                        notifyState(STATE_CONNECTING);
                        if(createSocket()){
                            notifyState(STATE_CONNECTED);
                        }else{
                            notifyState(STATE_DISCONNECTED);
                        }
                    }else{
                        notifyState(STATE_DISCONNECTED);
                    }
                }
            }else{
                notifyState(STATE_DISCONNECTED);
            }
        }else{
            notifyState(STATE_DISCONNECTED);
        }
        return new BlueBinder();
    }
    private boolean createSocket(){
        Method method;
        if(mDevice == null)
            return false;
        try {
            method = mDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            mSocket = (BluetoothSocket) method.invoke(mDevice, 1);
        } catch (Exception e) {
            Log.e("TAG", e.toString());
        }
        if(mSocket == null){
            Log.d("qiqi","create socket false");
            return false;
        }else{
            Log.d("qiqi","create socket true");
            try{
                mSocket.connect();
            }catch (Exception e){
            }
            return true;
        }
    }
    public class BlueBinder extends Binder{
        public void start(int time){
            setDeviceOn(true, time);
        }
        public void stop(){
            setDeviceOn(false, 0);
        }
        public void connect(){
            createSocket();
        }
        public BluetoothService getService(){
            return BluetoothService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            mSocket.close();
        }catch (Exception e){

        }
        return super.onUnbind(intent);
    }
    private void setDeviceOn(boolean isOn, int time){

        try {
//            if(!mSocket.isConnected()){
//                createSocket();
//            }
            OutputStream outStream = mSocket.getOutputStream();
            if(isOn){
                outStream.write(getHexBytes("AA000100045502100" + Integer.toHexString(time) +"0000CC33C33C"));
                outStream.write(getHexBytes("AA000100045502100" + Integer.toHexString(time) +"0000CC33C33C"));
            }else{
                outStream.write(getHexBytes("AA0201000355011F0000CC33C33C"));
                outStream.write(getHexBytes("AA0201000355011F0000CC33C33C"));
            }
        } catch (Exception e) {
            Log.e("qiqi", e.toString());
            notifyState(STATE_DISCONNECTED);
        }
    }
    private byte[] getHexBytes(String message) {
        int len = message.length() / 2;
        char[] chars = message.toCharArray();
        String[] hexStr = new String[len];
        byte[] bytes = new byte[len];
        for (int i = 0, j = 0; j < len; i += 2, j++) {
            hexStr[j] = "" + chars[i] + chars[i + 1];
            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
        }
        return bytes;
    }
}