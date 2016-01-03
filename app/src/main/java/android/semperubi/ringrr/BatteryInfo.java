package android.semperubi.ringrr;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

/**
 * Created by Herb on 12/19/2015.
 */
public class BatteryInfo extends StatisticObject {
    public Integer batteryLevel,previousBatteryLevel,scale;
    public Float batteryPercent;
    public String batteryState = null;
    public String batteryCable = null;
    public Intent batteryStatus = null;
    int bf;

    public BatteryInfo(Context ctx,int d) {
        super(ctx,StatisticType.BATTERY,d);
        previousBatteryLevel=0;
    }

    void initDetails() {
        // nothing to do here, but must implement to satisfy abstract class requirements
    }

    public boolean hasChanged() {return changeFlag;}

    void getDetails() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, ifilter);
        int status;

        try {

            status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        }
        catch (Exception e) {

            return;
        }


        switch(status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                batteryState = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                batteryState = "Not Charging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                batteryState = "Not Charging";
                batteryLevel = 100;
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                batteryState = "Discharging";
                break;
        }

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        switch (chargePlug) {
            case BatteryManager.BATTERY_PLUGGED_USB:
                batteryCable = "USB";
                break;
            case BatteryManager.BATTERY_PLUGGED_AC:
                batteryCable = "AC Cable";
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                batteryCable = "Wireless";
                break;
            default:
                batteryCable = "None";
        }

        batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        batteryPercent = 100 * batteryLevel / (float)scale;
    }


    boolean checkDelta() {
        int levelDiff;
        boolean rval = false;
        levelDiff = Math.abs(batteryLevel - previousBatteryLevel);
        if (levelDiff > delta) {
            rval = true;
        }
        return rval;
    }

    void setJSONdetails() {
       try {
           jsonObject.put("STATUS", batteryState);
           jsonObject.put("CABLE", batteryCable);
           jsonObject.put("PERCENT",batteryLevel);
       }
        catch (Exception e) {
            Utilities.handleCatch("E","BatteryInfo:setJSONdetails",e);
        }
    }


}
