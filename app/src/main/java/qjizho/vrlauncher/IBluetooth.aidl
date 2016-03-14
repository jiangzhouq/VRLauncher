package qjizho.vrlauncher;
import IBluetoothListener;
interface IBluetooth
{
    boolean checkXIAOMIPaired();
    void startScan();
    void setListener(IBluetoothListener listener);
}
