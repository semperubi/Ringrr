package android.semperubi.ringrr;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.io.File;

public class TestActivity extends Activity {

    Context appContext;
    ActivityManager activityManager;
    BatteryInfo batteryInfo;
    MemoryUsage memoryInfo;
    NetworkInfo networkInfo;
    LocationInfo locationInfo;
    Intent intent;
    Bundle bundle;
    RingrrConfigInfo configInfo;
    StatisticsLog statLogger;

    int bf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        intent = getIntent();
        bundle = intent.getExtras();
        appContext = getApplicationContext();
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        configInfo = RingrrConfigInfo.getInstance(appContext);
        statLogger = StatisticsLog.getInstance();
        statLogger.addAdminMessage("Test Functions Started",true);
    }


    public void transmit(View v) {
       TransmissionList tmList = TransmissionList.getInstance();
    }

    private void sendStatFile(File statFile) {
        String oldName,newName,newPath;
        try {
            newPath = statFile.getPath();
            oldName = statFile.getName();
            newName = "Saved_" + oldName;
            newPath = newPath.replaceFirst(oldName,newName);
            File newFile = new File(newPath);
            statFile.renameTo(newFile);
            bf = 1;
        }
        catch (Exception e) {
            bf = 1;
        }
    }

    public void dumpLog(View v) {
        statLogger.addAdminMessage("Write log requested",true);
        statLogger.writeLog();
    }

    public void getBatteryInfo(View v) {
        statLogger.addAdminMessage("Battery info requested",true);
        try {
            Intent intent = new Intent(this,ShowBatteryInfo.class);
            startActivityForResult(intent,StatisticType.BATTERY.getIntValue());
        }
        catch (Exception e)
        {
            Utilities.handleCatch("Ringrr", "getLocation: ", e);
        }

        bf = 1;
    }

    public void getMemoryInfo(View v) {
        if (memoryInfo == null) {
            memoryInfo = new MemoryUsage(appContext,1);
        }
        memoryInfo.update();

    }
    public void getAppInfo(View v) {
        statLogger.addAdminMessage("App info requested",true);
        ProcessInfo processInfo = new ProcessInfo(activityManager);
    }

    public void getNetworkInfo(View v) {
        statLogger.addAdminMessage("Network info requested",true);
    }
}
