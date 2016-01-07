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
    public final static int MILLISECONDS_PER_MINUTE = 60000;
    public final static int MILLISECONDS_PER_DAY = MILLISECONDS_PER_MINUTE * (24 * 60);

    protected final static String sysFolderRoot = Environment.getExternalStorageDirectory().getPath() + "/DCIM/";
    public final static String setupInfoFile="setupinfo.txt";
    public final static String serviceFile="service.txt";
    public final static String statFilePrefix="statsLog_";
    public final static String statFileSuffix=".txt";
    public final static String statFilefmt= statFilePrefix + "%s" + statFileSuffix;

    public final static SimpleDateFormat timeStampFmt = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss",Locale.getDefault());
    public final static SimpleDateFormat startDateTimeFmt = new SimpleDateFormat("MM/dd/yy hh:mma",Locale.getDefault());
    public final static SimpleDateFormat Datefmt = new SimpleDateFormat("mm/dd/yyyy hh:mm",Locale.getDefault());
    public final static SimpleDateFormat DayOnlyfmt = new SimpleDateFormat("MM/dd/yy",Locale.getDefault());
    public final static SimpleDateFormat LogFileDatefmt = new SimpleDateFormat("MMddyy",Locale.getDefault());

    public static Context appContext;
    public static String appName;
    public static String sysFolder;
    public static String statLogFolder;
    //probably should be singleton
    public static StatisticsLog statisticsLogger = null;

    int bf;

    public Utilities() {}

    public static void setContext(Context ctx) {
        appContext = ctx;
        appName = appContext.getResources().getString(R.string.app_name);
    }

    public static String getAppName() {
        return appName;
    }

    public static String getSystemFolder() {
        sysFolder = sysFolderRoot + appName + "/";
        return sysFolder;
    }

    public static String getConfigFilePath() {

        return getSystemFolder() + setupInfoFile;
    }


    public static String getStatLogFilePath() {
        String dPart = LogFileDatefmt.format(new Date());
        String fname = String.format(statFilefmt,dPart);
        return(getSystemFolder() + fname);
    }

    public static Date getStatLogFileDate(String statLogFilePath) {
        int startPoint,endPoint;
        Date rval=null;
        String fname,dateString;
        String fparts[] = statLogFilePath.split("/");
        fname = fparts[fparts.length-1];
        startPoint = statFilePrefix.length();
        endPoint = fname.length() - statFileSuffix.length();
        dateString = fname.substring(startPoint,endPoint);
        try {
            rval = LogFileDatefmt.parse(dateString);
        }
        catch (Exception e) {
            handleCatch("E","Utilities:getStatLogFileDate",e);
        }
        return rval;

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
