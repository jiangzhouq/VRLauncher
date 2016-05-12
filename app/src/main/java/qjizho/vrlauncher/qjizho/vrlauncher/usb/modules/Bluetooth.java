package qjizho.vrlauncher.usb.modules;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;

import com.jiongbull.jlog.JLog;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by qjizho on 16/5/5.
 */
public class Bluetooth {
    private static Bluetooth tInstance;
    private static Context mContext;
    BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> mDevicesList = new ArrayList<>();
    private Bluetooth(Context context){
        mContext = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    public static synchronized Bluetooth gettInstance(Context context){
        if(null == tInstance){
            tInstance = new Bluetooth(context);
        }
        return tInstance;
    }

    public interface BluetoothListener{
        void returnSearchedBlue(BlueDevice blueDevice);
    }

    private BluetoothListener bluetoothListener;
    public void setBluetoothListener(BluetoothListener bluetoothListener){
        this.bluetoothListener = bluetoothListener;
    }

    public void setBluetoothTurn(boolean turn){
        if(turn){
            if (!bluetoothAdapter.isEnabled())
            {
                bluetoothAdapter.enable();
                startScan();
            }
        }else{
            if (bluetoothAdapter.isEnabled())
            {
                bluetoothAdapter.disable();
            }
        }

    }

    public boolean getBluetoothState(){
        return bluetoothAdapter.isEnabled();
    }

    public void startScan(){
        mDevicesList.clear();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(receiver, intentFilter);
        bluetoothAdapter.startDiscovery();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                JLog.v(device.getAddress());
                JLog.v(device.getName());
                JLog.v("" + device.getType());
                BlueDevice blueDevice = new BlueDevice();
                blueDevice.bName = device.getName();
                blueDevice.bAdress = device.getAddress();
                blueDevice.bType = device.getType();
                blueDevice.bUuid = new ArrayList<>();
                for (ParcelUuid uuid : device.getUuids()){
                    JLog.v(uuid.getUuid().toString());
                    blueDevice.bUuid.add(uuid.getUuid().toString());
                }
                bluetoothListener.returnSearchedBlue(blueDevice);
                mDevicesList.add(device);
            }
        }
    };

    public ArrayList getBondedBlues(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> pairedDevicesList = new ArrayList<>(pairedDevices);
        return pairedDevicesList;
    }

    public void bondBlue(String address){
        for(BluetoothDevice device : mDevicesList){
            if(device.getAddress().equals(address)){
                try {
                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                    createBondMethod.invoke(device);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void disBondBlue(){

    }

    public void connectBlue(String address){
        for(BluetoothDevice device : bluetoothAdapter.getBondedDevices()){
            if(device.getAddress().equals(address)){
                try {
                    connect(device);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void diconncetBlue(){

    }

    private void notifyBluetoothState(){

    }

    private void notifyBlueDeviceState(){

    }

    private boolean createBTConnection(BluetoothProfile proxy, BluetoothDevice btDevice)
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

    private void connect(final BluetoothDevice device) throws IOException {
        BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                //4 == BluetoothInputDevice
                Log.d("qiqi", "Bluetooth service connected");
                if (profile == 4) {
                    for(int i = 0; i< 10; i++){
                        boolean connected = createBTConnection(proxy, device);
                        Log.d("qiqi","for i try:" + i + "  boolean:" + connected);
                        if(!connected){
                            Log.d("qiqi","break the for");
                            continue;
                        }
                        break;
                    }
                    //doesnot connect ):
                    Log.d("qiqi", "Bluetooth service connected 444444");
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                Log.d("qiqi", "Bluetooth service disconnected");
            }
        };
        // 固定的UUID
        final String SPP_UUID = "00001124-0000-1000-8000-00805F9B34FB";
        bluetoothAdapter.getProfileProxy(mContext, mProfileListener, 4);
    }

    public class BlueDevice{
        public String bName;
        public String bAdress;
        public int bType;
        public ArrayList<String> bUuid;
    }
}
