package android.semperubi.ringrr;

/**
 * Created by Herb on 12/20/2015.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class StatsReceiver extends BroadcastReceiver {
    final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";

    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action;
        try {
            mContext = context;
            Log.d("Ringrr:ADMIN", "Received intent: " + intent.getAction());
            action = intent.getAction();
            if (action.equals(BOOT_ACTION)) {
                startService();
            }
        } catch (Exception e) {
            Log.e("StatsReceiver", "onReceive",e);
        }
    }

    private void startService() {
        Intent in = new Intent(mContext, StatisticsService.class);
        mContext.startService(in);
    }
}

