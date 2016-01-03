package android.semperubi.ringrr;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StatisticsService extends IntentService {
    Context appContext;
    ActivityManager activityManager;
    StatisticsCollector statisticsCollector;
    int bf;

    public StatisticsService() {
        super("statistics-service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Ringrr:ADMIN","StatisticsService started");
        appContext = getApplicationContext();
        bf = 1;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        statisticsCollector = new StatisticsCollector(appContext,activityManager);
        statisticsCollector.getAllStats();
    }
}
