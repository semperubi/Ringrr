package android.semperubi.ringrr;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by Herb on 1/2/2016.
 */
public class NetworkDetails {
    int mNetType;
    boolean isConnected = false;
    boolean transmitFlag = false;
    String netType;
    String wifiName;
    Context mContext;
    ConnectivityManager connManager;

    android.net.NetworkInfo mNetInfo;

    public NetworkDetails() {
        netType = null;
        wifiName = null;
        try {
            isConnected = false;
            mContext = Utilities.appContext;
            connManager = (ConnectivityManager)(mContext.getSystemService(Context.CONNECTIVITY_SERVICE));
            mNetInfo = connManager.getActiveNetworkInfo();
            if (mNetInfo != null) {
                if (mNetInfo.isConnected()) {
                    isConnected = true;
                    mNetType = mNetInfo.getType();
                    //Should be one of these: TYPE_MOBILE, TYPE_WIFI, TYPE_WIMAX, TYPE_ETHERNET, TYPE_BLUETOOTH
                    switch (mNetType) {
                        case ConnectivityManager.TYPE_MOBILE:
                            netType = "Mobile";
                            transmitFlag = true;
                            break;
                        case ConnectivityManager.TYPE_WIFI:
                            netType = "Wifi";
                            transmitFlag = true;
                            WifiManager wifiMgr = (WifiManager) (mContext.getSystemService(Context.WIFI_SERVICE));
                            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                            wifiName = wifiInfo.getSSID();
                            break;
                        case ConnectivityManager.TYPE_WIMAX:
                            netType = "Wimax";
                            break;
                        case ConnectivityManager.TYPE_ETHERNET:
                            netType = "Ethernet";
                            transmitFlag = true;
                            break;
                        case ConnectivityManager.TYPE_BLUETOOTH:
                            netType = "Bluetooth";
                            BluetoothManager btMgr = (BluetoothManager)(mContext.getSystemService(Context.BLUETOOTH_SERVICE));
                            //btDevices = btMgr.getConnectedDevices(profile);
                            //TODO need more information about profiles
                            break;
                    }
                }
            }
        }
        catch (Exception e) {
            //Utilities.exceptionHandler(ex, "MainActivity:CheckWiFiStatus");
            //evLogger.writeEntry(ex,"MainActivity:CheckWiFiStatus");
            Utilities.handleCatch("Ringrr","NetworkInfo",e);
        }
    }
}
