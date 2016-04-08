package qjizho.vrlauncher;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
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
    public static final int STATE_XIAOMI_CONNECTED = 5;
    public static final int STATE_XIAOMI_PAIRED = 6;
    public static final int STATE_XIAOMI_FINDING = 7;
    private int mState = STATE_BT_OFF;

    public interface OnBTStateListener{
        void onStateChanged(int state);
    }

    private IBluetoothListener onBTStateListener = null;
    private BluetoothSocket mSocket = null;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice mmmDevice;
    private BlueBinder blueBinder;
    public BluetoothService() {
    }
    private void notifyState(int i){
        mState = i;
        Log.d("qiqi", "notify state:" + i);
        if(onBTStateListener != null){
            try{
                onBTStateListener.onStateChanged(i);
            }catch(Exception e){
                Log.d("qiqi","e:" + e.toString());
            }
        }else{
            Log.d("qiqi", "listener == null");
        }
    }

    public class BlueBinder extends IBluetooth.Stub{

        @Override
        public void turnOnAndOffBluetooth() throws RemoteException {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    privateTurnOffAndOnBluetooth();
                }
            }).start();

        }

        @Override
        public boolean checkXIAOMIPaired() throws RemoteException {
            return privateCheckXIAOMIPaired();
        }

        @Override
        public void startScan() throws RemoteException {
            privateStartScan();
        }

        @Override
        public void setListener(IBluetoothListener listener) throws RemoteException {
            onBTStateListener = listener;
        }
    }

    @Override
    public void onCreate() {
        blueBinder = new BlueBinder();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("qiqi","service binded");
        // TODO: Return the communication channel to the service.

        return blueBinder;
    }
    private  void privateTurnOffAndOnBluetooth(){
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (bluetoothAdapter == null)
//        {
//            // 设备不支持蓝牙
//            Log.d("qiqi","not support bluetooth");
//        }
//        //关闭蓝牙
//        if (bluetoothAdapter.isEnabled())
//        {
////            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
////            // 设置蓝牙可见性，最多300秒
////            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
////            context.startActivity(intent);
//            Log.d("qiqi","start to stop bluetooth");
//            bluetoothAdapter.disable();
//        }
//        try{
//            Thread.sleep(3000);
//        }catch (Exception e){
//
//        }

//        privateStartScan();
    }
    public boolean privateCheckXIAOMIPaired (){
        if (bluetoothAdapter == null)
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 打开蓝牙
        if (!bluetoothAdapter.isEnabled())
        {
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            // 设置蓝牙可见性，最多300秒
//            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//            context.startActivity(intent);
            Log.d("qiqi","start to enable bluetooth");
            bluetoothAdapter.enable();
            try{
                Thread.sleep(3000);
            }catch (Exception e){
            }
        }

        if(bluetoothAdapter != null){
//            if(bluetoothAdapter.isEnabled()){
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            Log.d("qiqi","bonded size:" + pairedDevices.size());
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                a:for (BluetoothDevice device : pairedDevices) {
                    Log.d("qiqi", "bonded:" + device.getName());
                    if(device.getName().contentEquals("小米蓝牙手柄")){
                        return true;
                    }
                }
            }

        }else{
            notifyState(STATE_DISCONNECTED);
        }
        return false;
    }

    public void privateStartScan(){

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("qiqi","start 10 000 delay");
                try{
                    Thread.sleep(10000);
                }catch(Exception e){

                }
                Log.d("qiqi","stop 10 000 delay");

                bluetoothAdapter.startDiscovery();
            }
        }).start();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("qiqi","action:" + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 获取查找到的蓝牙设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                System.out.println(device.getName());
                // 如果查找到的设备符合要连接的设备，处理
                if (device !=null && device.getName() != null && device.getName().contains("小米蓝牙手柄")) {
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
//                        case BluetoothDevice.BOND_BONDED:
//                            try {
//                                // 连接
//                                connect(device);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            break;
                    }
                }else{
                    Log.d("qiqi","device == null!!!!!!!!!!");
                }
            }
            else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                // 状态改变的广播
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName().contains("小米蓝牙手柄")) {
                    switch (device.getBondState()) {
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
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                try{
                    Thread.sleep(5000);

                }catch(Exception e){

                }
                if(!privateCheckXIAOMIPaired()){
                    bluetoothAdapter.startDiscovery();
                }
            }
            else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)){
                if(privateCheckXIAOMIPaired()){
                    notifyState(STATE_XIAOMI_PAIRED);
                }else{
                    notifyState(STATE_DISCONNECTED);
                }
            }
        }
    };

    BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            //4 == BluetoothInputDevice
            Log.d("qiqi", "Bluetooth service connected");
            if (profile == 4) {
                    for(int i = 0; i< 10; i++){
                        boolean connected = createBTConnection(proxy, mmmDevice);
                        Log.d("qiqi","for i try:" + i + "  boolean:" + connected);
                        if(!connected){
                            Log.d("qiqi","break the for");
                            continue;
                        }
                        break;
                    }

                notifyState(STATE_XIAOMI_CONNECTED);
                //doesnot connect ):
                Log.d("qiqi", "Bluetooth service connected 444444");
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.d("qiqi", "Bluetooth service disconnected");
        }
    };
    public boolean createBTConnection(BluetoothProfile proxy, BluetoothDevice btDevice)
    {
        Boolean returnValue = false;
        Class class1 = null;
        try {
            class1 = Class.forName("android.bluetooth.BluetoothInputDevice");
            Method createConnectionMethod = class1.getMethod("connect", new Class[] {BluetoothDevice.class});
            returnValue = (Boolean) createConnectionMethod.invoke(proxy, btDevice);
        } catch (Exception e) {
            Log.d("qiqi", "createBTConnection error:" + e.toString());
        }
        Log.d("qiqi", "returnValue:" + returnValue.booleanValue());
        return returnValue.booleanValue();
    }

    private void connect(BluetoothDevice device) throws IOException {
        // 固定的UUID
        final String SPP_UUID = "00001124-0000-1000-8000-00805F9B34FB";
        UUID uuid = UUID.fromString(SPP_UUID);
        mmmDevice = device;
        bluetoothAdapter.getProfileProxy(this, mProfileListener, 4);

    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            mSocket.close();
        }catch (Exception e){

        }
        return super.onUnbind(intent);
    }
}