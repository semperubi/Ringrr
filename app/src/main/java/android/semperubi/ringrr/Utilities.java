package android.semperubi.ringrr;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Herb on 12/14/2015.
 */
public class Utilities {

    public final static String hTab = "\t";
    public final static String comma = ",";
    public final static char newLine = '\n';
    public final static String quoteStr = "\"";

    public final static int DEFAULT_POLL_INTERVAL = 5;
    public final static int DEFAULT_STAT_DELTA = 5;
    public final static int SLEEP_MINUTES = 1;
    public final static int MILLISECONDS_PER_MINUTE = 60000;

    protected final static String sysFolderRoot = Environment.getExternalStorageDirectory().getPath() + "/DCIM/";
    public final static String setupInfoFile="setupinfo.txt";
    public final static String serviceFile="service.txt";
    public final static String statFilefmt="statsLog_%s.txt";

    public final static SimpleDateFormat timeStampFmt = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss",Locale.getDefault());
    public final static SimpleDateFormat startDateTimeFmt = new SimpleDateFormat("MM/dd/yy hh:mma",Locale.getDefault());
    public final static SimpleDateFormat Datefmt = new SimpleDateFormat("mm/dd/yyyy hh:mm",Locale.getDefault());
    public final static SimpleDateFormat DayOnlyfmt = new SimpleDateFormat("MM/dd/yy",Locale.getDefault());
    public final static SimpleDateFormat LogFilefmt = new SimpleDateFormat("MMddyy",Locale.getDefault());

    public static String appName;
    public static String sysFolder;
    //probably should be singleton
    public static StatisticsLog statisticsLogger = null;

    public Utilities() {}

    public static void setAppName(String aName) {
        appName = aName;
        sysFolder = sysFolderRoot + appName + "/";
    }

    public static String getAppName() {
        return appName;
    }

    public static String getSystemFolder() {
        return sysFolder;
    }

    public static String getConfigFilePath() {

        return getSystemFolder() + setupInfoFile;
    }


    public static String getStatLogFilePath() {
        String dPart = LogFilefmt.format(new Date());
        String fname = String.format(statFilefmt,dPart);
        return(getSystemFolder() + fname);
    }

    public static String getServiceFilePath() {
        return getSystemFolder() + serviceFile;
    }

    public static void handleCatch(String logType,String routine,Exception e) {
        String logTag = appName + ":" + routine;
        switch (logType) {
            case "E":
                Log.e(logTag,e.getMessage());
                break;
            case "I":
                Log.i(logTag,e.getMessage());
                break;
            case "D":
                Log.d(logTag,e.getMessage());
                break;
        }
    }

    public static void showToast(Context ctx,String msg){
        Toast toast = Toast.makeText(ctx, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
