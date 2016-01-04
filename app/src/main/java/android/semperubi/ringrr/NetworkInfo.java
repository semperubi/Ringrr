package android.semperubi.ringrr;

/**
 * Created by Herb on 12/19/2015.
 */

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.json.JSONException;

public class NetworkInfo extends StatisticObject {
    ConnectivityManager connManager;
    boolean isConnected = false;
    String netType;
    String wifiName;
    android.net.NetworkInfo mNetInfo;
    BluetoothDevice[] btDevices;

    public NetworkInfo(Context ctx,int d) {
        super(ctx,StatisticType.NETWORK,d);
    }

    void initDetails() {
        connManager = (ConnectivityManager)(mContext.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    void getDetails()
    {
        int mNetType;
        netType = null;
        wifiName = null;
        try {
            isConnected = false;
            mNetInfo = connManager.getActiveNetworkInfo();
            if (mNetInfo != null) {
                if (mNetInfo.isConnected()) {
                    isConnected = true;
                    mNetType = mNetInfo.getType();
                    //Should be one of these: TYPE_MOBILE, TYPE_WIFI, TYPE_WIMAX, TYPE_ETHERNET, TYPE_BLUETOOTH
                    switch (mNetType) {
                        case ConnectivityManager.TYPE_MOBILE:
                            netType = "Mobile";
                            break;
                        case ConnectivityManager.TYPE_WIFI:
                            netType = "Wifi";
                            WifiManager wifiMgr = (WifiManager) (mContext.getSystemService(Context.WIFI_SERVICE));
                            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                            wifiName = wifiInfo.getSSID();
                            break;
                        case ConnectivityManager.TYPE_WIMAX:
                            netType = "Wimax";
                            break;
                        case ConnectivityManager.TYPE_ETHERNET:
                            netType = "Ethernet";
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

    public boolean checkDelta() {
        return true;
    }

    void setJSONdetails() {
        try {
            jsonObject.put("CONNECTED",isConnected);
            if (netType != null) {
                jsonObject.put("TYPE", netType);
            }
            if (wifiName != null) {
                jsonObject.put("SSID",wifiName);
            }
        }
        catch (JSONException ex) {
            Utilities.handleCatch("Ringrr","NetworkInfo",ex);
        }
    }
}
