package android.semperubi.ringrr;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;

import org.json.JSONException;

/**
 * Created by Herb on 12/20/2015.
 */
public class MemoryUsage extends StatisticObject {

    final long mgByte = 1048576L;
    Long threshold =0L;
    Long thresholdMeg=0L;
    boolean lowMemory = false;
    static ActivityManager activityManager;
    static MemoryInfo mi;
    Long totalMegs, availableMegs,previousAvailableMegs;
    boolean changeFlag = false;
    int bf;

    public MemoryUsage(Context ctx,int d) {
        super(ctx,StatisticType.MEMORY,d);
    }

    void initDetails() {
        try {
            activityManager = (ActivityManager)(mContext.getSystemService(Context.ACTIVITY_SERVICE));
            mi = new MemoryInfo();
            previousAvailableMegs = 0L;
        }
        catch (Exception e) {
            bf = 1;
        }
    }

    void getDetails() {
        activityManager.getMemoryInfo(mi);
        totalMegs = mi.totalMem / mgByte;
        threshold = mi.threshold;
        thresholdMeg = threshold / mgByte;
        availableMegs = mi.availMem / mgByte;
        lowMemory = mi.lowMemory;
    }

    public boolean hasChanged() {return changeFlag;}


    boolean checkDelta() {
        int memDiff;
        Long memDiffLong;
        boolean rval = false;
        memDiff = 100*(int)((availableMegs - previousAvailableMegs)/availableMegs);
        //memDiff is percentage change
        if (Math.abs(memDiff) > delta) {
            rval = true;
        }
        previousAvailableMegs = availableMegs;
        return rval;
    }


    void setJSONdetails() {
        try {
            jsonObject.put("AVAILABLE", availableMegs.toString());
            jsonObject.put("TOTAL", totalMegs.toString()); // Set the first name/pair
            jsonObject.put("THRESHOLD", thresholdMeg.toString());
            if (lowMemory) {
                jsonObject.put("LOWMEMORY", "true");
            }
        }
        catch (JSONException e) {
            bf = 1;
        }
    }

}
