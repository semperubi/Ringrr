package android.semperubi.ringrr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Herb on 12/29/2015.
 */
public class BatteryInfoReceiver extends BroadcastReceiver {

    Context mContext;
    Intent mIntent;

    @Override
    public void onReceive(Context ctx, Intent i) {
        mContext = ctx;
        mIntent = i;
        Log.d("RECEIVER", "Battery event received: " + i.getAction().toString());
    }
}
