package qjizho.vrlauncher;
import IBluetoothListener;
interface IBluetooth
{
    boolean checkXIAOMIPaired();
    void startScan();
    void turnOnAndOffBluetooth();
    void setListener(IBluetoothListener listener);
}
