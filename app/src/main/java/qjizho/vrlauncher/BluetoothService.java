package qjizho.vrlauncher;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

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
    private BluetoothAdapter bluetoothAdapter;
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
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null){
            // 设置广播信息过滤
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            intentFilter.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);
            intentFilter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
            intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
            intentFilter.addAction(BluetoothDevice.ACTION_UUID);
            intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intentFilter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);

            // 注册广播接收器，接收并处理搜索结果
            registerReceiver(receiver, intentFilter);
            // 寻找蓝牙设备，android会将查找到的设备以广播形式发出去
            bluetoothAdapter.startDiscovery();
//            if(bluetoothAdapter.isEnabled()){
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        Log.d("qiqi", device.getName() + " " + device.getAddress());
                        for (ParcelUuid uuu : device.getUuids()) {
                            Log.d("qiqi", "supported:" + uuu.getUuid());
                        }
                        try {
                            // 连接
                            connect(device);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        if(device.getName().equals("RG-BLE-12")){
//                            mDevice = device;
//                        }

                    }
                }
//                    if(mDevice!=null){
//                        notifyState(STATE_CONNECTING);
//                        if(createSocket()){
//                            notifyState(STATE_CONNECTED);
//                        }else{
//                            notifyState(STATE_DISCONNECTED);
//                        }
//                    }else{
//                        notifyState(STATE_DISCONNECTED);
//                    }
//                }
//            }else{
//                notifyState(STATE_DISCONNECTED);
//            }
        }else{
            notifyState(STATE_DISCONNECTED);
        }
        return new BlueBinder();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("qiqi","action:" + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 获取查找到的蓝牙设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println(device.getName());
                // 如果查找到的设备符合要连接的设备，处理
                if (device.getName().contains("ATET")) {
                    // 搜索蓝牙设备的过程占用资源比较多，一旦找到需要连接的设备后需要及时关闭搜索
                    bluetoothAdapter.cancelDiscovery();
                    // 获取蓝牙设备的连接状态
                    switch (device.getBondState()) {
                        // 未配对
                        case BluetoothDevice.BOND_NONE:
                            // 配对
                            try {
                                Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                                createBondMethod.invoke(device);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        // 已配对
                        case BluetoothDevice.BOND_BONDED:
                            try {
                                // 连接
                                connect(device);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            } else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                // 状态改变的广播
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName().contains("ATET")) {
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_NONE:
                            break;
                        case BluetoothDevice.BOND_BONDING:
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            try {
                                // 连接
                                Thread.sleep(3000);
                                connect(device);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            }
        }
    };
    private void connect(BluetoothDevice device) throws IOException {

        Log.d("qiqi","connecting:" + device.getName());
//        for(ParcelUuid uuu : device.getUuids()){
//            Log.d("qiqi","supported:" + uuu.getUuid());
//        }
        // 固定的UUID
        final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        UUID uuid = UUID.fromString(SPP_UUID);
        try{
            BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
//            Method method = device.getClass().getMethod("createL2capSocket", new Class[]{int.class});
//            mSocket = (BluetoothSocket) method.invoke(device, 1);

            socket.connect();
        }catch (Exception e){
            Log.d("qiqi","connect error:" + e.toString());
        }

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