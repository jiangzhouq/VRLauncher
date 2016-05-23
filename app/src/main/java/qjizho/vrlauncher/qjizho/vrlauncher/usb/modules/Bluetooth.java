package qjizho.vrlauncher.usb.modules;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.jiongbull.jlog.JLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import qjizho.vrlauncher.usb.HandleInput;

/**
 * Created by qjizho on 16/5/5.
 */
public class Bluetooth {
    private static Bluetooth tInstance;
    private static Context mContext;
    BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> mDevicesList = new ArrayList<>();
    private ArrayList<BlueDevice> mBlueList = new ArrayList<>();
    private Bluetooth(Context context){
        mContext = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectedBlues = new ArrayList<>();
    }
    private ArrayList<String> connectedBlues;
    public static synchronized Bluetooth getInstance(Context context){
        if(null == tInstance){
            tInstance = new Bluetooth(context);
        }
        return tInstance;
    }

    public interface BluetoothListener{
        void returnBlueToClient(JSONObject blueObject);
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
                try{
                    Thread.sleep(2000);
                }catch (Exception e){

                }
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
        mBlueList.clear();
        getBondedBlues();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mContext.registerReceiver(receiver, intentFilter);
        bluetoothAdapter.startDiscovery();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            JLog.d("action:" + action);
            out:if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                JSONObject blueObject = new JSONObject();

                try{
//                    blueObject.put(HandleInput.KEY_ID, 10007);
//                    blueObject.put(HandleInput.KEY_COMMAND, HandleInput.CMD_BLUE_SCAN);
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(device != null){
                        if(mBlueList.size()>0){
                            JLog.d("mBlueList.size()" + mBlueList.size());
                            for( BlueDevice blueDevice : mBlueList){
                                JLog.d(blueDevice.bAdress);
                                JLog.d(device.getAddress());
                                if(blueDevice.bAdress.equals(device.getAddress())){
                                    break out;
                                }
                            }
                        }
//                        JLog.d(device.getAddress().toString());
//                        JLog.d(device.getName().toString());
//                        JLog.d("" + device.getType());
//                        JLog.d("" + device.getUuids().toString());

//                        blueObject.put("name", device.getName().toString());
//                        blueObject.put("address", device.getAddress().toString());
//                        blueObject.put("type", device.getType());
//                        JSONArray uuidArray = new JSONArray();
//                        for (ParcelUuid uuid : device.getUuids()){
//                            JSONObject uuidObject = new JSONObject();
//                            uuidObject.put("uuid", uuid.getUuid().toString());
//                            uuidArray.put(uuidObject);
//                        }
//                        blueObject.put("uuid", uuidArray);
                        mDevicesList.add(device);

                        BlueDevice blueDevice = new BlueDevice();
                        blueDevice.bName = device.getName();
                        blueDevice.bAdress = device.getAddress();
                        blueDevice.bType = device.getType();
                        blueDevice.bBond = device.getBondState();
                        if(connectedBlues.contains(device.getAddress())){
                            blueDevice.bConnect = true;
                        }else{
                            blueDevice.bConnect = false;
                        }
                        mBlueList.add(blueDevice);
                    }else{
                        JLog.d("device == null");
                    }
                }catch (Exception e){
                    JLog.d(e.toString());
                }
//                JLog.json(blueObject.toString());
//                bluetoothListener.returnBlueToClient(blueObject);
            }else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
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
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
//                JSONObject jsonObject = new JSONObject();
//                try{
//                    jsonObject.put(HandleInput.KEY_ID, 10007);
//                    jsonObject.put(HandleInput.KEY_COMMAND, HandleInput.CMD_BLUE_SCAN);
//                    jsonObject.put("discovery", true);
//                }catch (Exception e){
//
//                }
//                JLog.json(jsonObject.toString());
//                bluetoothListener.returnBlueToClient(jsonObject);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
//                JSONObject jsonObject = new JSONObject();
//                try{
//                    jsonObject.put(HandleInput.KEY_ID, 10007);
//                    jsonObject.put(HandleInput.KEY_COMMAND, HandleInput.CMD_BLUE_SCAN);
//                    jsonObject.put("discovery", false);
//                }catch (Exception e){
//
//                }
//                JLog.json(jsonObject.toString());
//                bluetoothListener.returnBlueToClient(jsonObject);
//                getBondedBlues();
                returnBlues();
            }
            else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)){

            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                JSONObject jsonObject = new JSONObject();
//                try{
//                    jsonObject.put(HandleInput.KEY_ID, 10007);
//                    jsonObject.put(HandleInput.KEY_COMMAND, HandleInput.CMD_BLUE_CONNECT);
//                    jsonObject.put("bluetooth", device.getName());
//                    jsonObject.put("connected", true);
//                }catch (Exception e){
//
//                }
//                JLog.json(jsonObject.toString());
//                bluetoothListener.returnBlueToClient(jsonObject);
                for(BlueDevice blueDevice : mBlueList){
                    if(blueDevice.bAdress.equals(device.getAddress())){
                        blueDevice.bConnect = true;
                    }
                }
                if(!connectedBlues.contains(device.getAddress())){
                    connectedBlues.add(device.getAddress());
                }
                returnBlues();
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                JSONObject jsonObject = new JSONObject();
//                try{
//                    jsonObject.put(HandleInput.KEY_ID, 10007);
//                    jsonObject.put(HandleInput.KEY_COMMAND, HandleInput.CMD_BLUE_CONNECT);
//                    jsonObject.put("bluetooth", device.getName());
//                    jsonObject.put("connected", false);
//                }catch (Exception e){
//
//                }
//                JLog.json(jsonObject.toString());
//                bluetoothListener.returnBlueToClient(jsonObject);

                for(BlueDevice blueDevice : mBlueList){
                    if(blueDevice.bAdress.equals(device.getAddress())){
                        blueDevice.bConnect = false;
                    }
                }
                if(connectedBlues.contains(device.getAddress())){
                    connectedBlues.remove(device.getAddress());
                }
                returnBlues();
            }
        }
    };

    private void returnBlues(){

        JSONObject blueObject = new JSONObject();
        try{
            JSONArray blueArray = new JSONArray();
            blueObject.put(HandleInput.KEY_ID, 10008);
            blueObject.put(HandleInput.KEY_COMMAND, HandleInput.CMD_BLUE_SCAN);
            JLog.d("mBlueList.size():" + mBlueList.size());
            for(BlueDevice blueDevice : mBlueList){
                JSONObject object = new JSONObject();
                object.put("name", blueDevice.bName);
                object.put("address", blueDevice.bAdress);
                object.put("type", blueDevice.bType);
                object.put("bond", blueDevice.bBond);
                object.put("connnect", blueDevice.bConnect);
                blueArray.put(object);
            }
            blueObject.put("bluetooth", blueArray);
        }catch (Exception e){

        }
        JLog.json(blueObject.toString());
        bluetoothListener.returnBlueToClient(blueObject);
    }
    public void getBondedBlues(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> pairedDevicesList = new ArrayList<>(pairedDevices);
        JLog.d("paired size:" + pairedDevicesList.size());
//        JSONObject bondObject = new JSONObject();
        try{
//            bondObject.put(HandleInput.KEY_ID, 10008);
//            bondObject.put(HandleInput.KEY_COMMAND, HandleInput.CMD_BLUE_SCAN);
//            JSONArray blueArray = new JSONArray();
            for(BluetoothDevice bluetoothDevice : pairedDevicesList){
//                JSONObject blueObject = new JSONObject();
//                blueObject.put("name", bluetoothDevice.getName());
//                blueObject.put("address", bluetoothDevice.getAddress());
//                blueObject.put("type", bluetoothDevice.getType());
//                blueObject.put("bond", bluetoothDevice.getBondState());
//                blueArray.put(blueObject);
                BlueDevice blueDevice = new BlueDevice();
                blueDevice.bName = bluetoothDevice.getName();
                blueDevice.bAdress = bluetoothDevice.getAddress();
                blueDevice.bType = bluetoothDevice.getType();
                blueDevice.bBond = bluetoothDevice.getBondState();
                if(connectedBlues.contains(bluetoothDevice.getAddress())){
                    blueDevice.bConnect = true;
                }else{
                    blueDevice.bConnect = false;
                }
                mBlueList.add(blueDevice);
//                JSONArray uuidArray = new JSONArray();
//                for (ParcelUuid uuid : bluetoothDevice.getUuids()){
//                    JSONObject uuidObject = new JSONObject();
//                    uuidObject.put("uuid", uuid.getUuid().toString());
//                    uuidArray.put(uuidObject);
//                }
//                blueObject.put("uuid", uuidArray);
            }
        }catch ( Exception e){

        }
//        JLog.json(bondObject.toString());
//        bluetoothListener.returnBlueToClient(bondObject);
    }

    public void bondBlue(String address){
        for(BluetoothDevice device : mDevicesList){
            if(device.getAddress().equals(address)){
                try {
                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                    createBondMethod.invoke(device);
                } catch (Exception e) {
                    JLog.d(e.toString());
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

    public void diconncetBlue(String address){
        for(BluetoothDevice device : bluetoothAdapter.getBondedDevices()){
            if(device.getAddress().equals(address)){
                try {
                    Method m = device.getClass()
                            .getMethod("removeBond", (Class[]) null);
                    m.invoke(device, (Object[]) null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
        public int bBond;
        public boolean bConnect;
    }
}
