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
    String appName;
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
        boolean startFlag,stopFlag;
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
            if (pname.equals("system")) {
                bf = 1;
            }
            stopFlag = true;
            try {
                for (j = 0; j < nRunning; j++) {
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
        String runningList="";
        String startList="";
        String stopList="";
        String jRunningList = null;
        String jStartList = null;
        String jStopList = null;
        JSONObject jObjRunning,jObjStart,jObjStop;
        for (i=0; i < nRunning; i++) {
            runningList = runningList + runningApps[i] + ":";
        }
        for (i=0; i < nStarted; i++) {
            startList = startList + startedApps[i] + ":";
        }
        for (i=0; i < nStopped; i++) {
            stopList = stopList  + stoppedApps[i]+ ":";
        }
        try {
            // Here we convert Java Object to JSON
            //jObjRunning = new JSONObject();
            //jObjRunning.put("RUNNING",runningList);
            //jRunningList = jObjRunning.toString();
           // statisticsLogger.addLogLine(LogMessageType.STATISTIC,StatisticType.APPS, jRunningList);
            if (nStarted > 0) {
                jObjStart = new JSONObject();
                jObjStart.put("STARTED", startList);
                jStartList = jObjStart.toString();
                statisticsLogger.addLogLine(LogMessageType.STATISTIC, StatisticType.APPS, jStartList,true);
            }
            if (nStopped > 0) {
                jObjStop = new JSONObject();
                jObjStop.put("STOPPED", stopList);
                jStopList = jObjStop.toString();
                statisticsLogger.addLogLine(LogMessageType.STATISTIC,StatisticType.APPS, jStopList,true);
            }
        }
        catch(JSONException ex) {
            Utilities.handleCatch("E","ProcessInfo:getJSON",ex);
        }
    }
}
