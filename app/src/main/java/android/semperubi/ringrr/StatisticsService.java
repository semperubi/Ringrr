package android.semperubi.ringrr;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class StatisticsService extends IntentService {
    Context appContext;
    ActivityManager activityManager;
    StatisticsCollector statisticsCollector;
    StatisticsLog statLog;
    int bf;

    public StatisticsService() {
        super("statistics-service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        Utilities.setContext(appContext);
        bf = 1;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        statLog = StatisticsLog.getInstance();
        statLog.addLogLine(LogMessageType.ADMIN, null, "Statistics Collection Service Started", true);
        statisticsCollector = new StatisticsCollector(activityManager);
        statisticsCollector.getAllStats();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        statLog.addLogLine(LogMessageType.ADMIN, null, "Statistics Collection Service Stopped", true);
        statLog.closeLog();
    }
}
