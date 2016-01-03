package android.semperubi.ringrr;

import android.app.ActivityManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
/**
 * Created by Herb on 12/22/2015.
 */
public class ProcessInfo {
    final int MAX_APPS = 100;
    int i,j;
    String appName,startList,stopList,jsonString;
    static int nRunning,nPrevious,nStarted,nStopped;
    static String[] runningApps;
    static String[] previousApps;
    static String[] startedApps;
    static String[] stoppedApps;
    ActivityManager activityManager;
    StatisticsLog statisticsLogger;

    List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo;
    List<ActivityManager.RunningAppProcessInfo> previousAppProcessInfo=null;

    int bf;

    public ProcessInfo(ActivityManager am) {
        runningApps = new String[MAX_APPS];
        previousApps = new String[MAX_APPS];
        startedApps = new String[MAX_APPS];
        stoppedApps = new String[MAX_APPS];
        nRunning = 0;
        nPrevious = 0;
        nStarted = 0;
        nStopped = 0;
        appName = Utilities.getAppName();
        activityManager = am;
        statisticsLogger = StatisticsLog.getInstance();
    }

    public void update() {
        int nApps;
        boolean startFlag,stopFlag,changeFlag;
        String pname;
        ActivityManager.RunningAppProcessInfo ri;
        try {
            for (i = 0; i < nRunning; i++) {
                previousApps[i] = runningApps[i];
            }
        }
        catch (Exception e) {
            bf = 1;
        }
        nPrevious = nRunning;
        nStarted = 0;
        nStopped = 0;
        runningAppProcessInfo = activityManager.getRunningAppProcesses();
        changeFlag = false;
        nRunning = runningAppProcessInfo.size();

        for (i=0; i < nRunning; i++) {
            ri = runningAppProcessInfo.get(i);
            pname = ri.processName;
            runningApps[i] = pname;
            startFlag = true;
            for (j=0;j < nPrevious; j++) {
                try {
                    if (pname.equals(previousApps[j])) {
                        startFlag = false;
                        break;
                    }
                }
                catch (Exception e) {
                    bf = 1;
                }
            }
            if (startFlag) {
                startedApps[nStarted] = pname;
                nStarted++;
            }
        }

        for (i=0; i < nPrevious; i++) {
            pname = previousApps[i];
            stopFlag = true;
            try {
                for (j = 0; j < nPrevious; j++) {
                    if (pname.equals(runningApps[j])) {
                        stopFlag = false;
                        break;
                    }
                }
            }
            catch (Exception e) {
                bf = 1;
            }
            if (stopFlag) {
                stoppedApps[nStopped] = pname;
                nStopped++;
            }
        }
        if ((nStarted+nStopped) > 0) {
            logJSON();
        }
    }

    public void logJSON() {
        int i;
        String startList="";
        String stopList="";
        JSONObject jObj;
        jsonString = null;
        for (i=0; i < nStarted; i++) {
            startList = startList + startedApps[i] + ":";
        }
        for (i=0; i < nStopped; i++) {
            stopList = stopList  + stoppedApps[i]+ ":";
        }
        try {
            // Here we convert Java Object to JSON
            jObj = new JSONObject();
            jObj.put("TYPE",StatisticType.APPS.toString());
            if (nStarted > 0) {
                jObj.put("APPS_STARTED", startList);
            }
            if (nStopped > 0) {
                jObj.put("APPS_STOPPED", stopList);
            }
            jsonString = jObj.toString();
            if (jsonString != null) {
                statisticsLogger.addStatLine(StatisticType.APPS, jsonString);
            }
        }
        catch(JSONException ex) {
            Utilities.handleCatch("E","ProcessInfo:getJSON",ex);
        }
    }
}
