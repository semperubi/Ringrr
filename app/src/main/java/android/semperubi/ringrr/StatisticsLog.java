package android.semperubi.ringrr;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Created by Herb on 12/17/2015.
 *
 * Name of logfile will be of form statsLogYYMMDD.txt and will be
 * incremented for each new day
 */
public class StatisticsLog {
        final String myLogTag = "RingrrStat";
        String appName;
        String[] logLines;
        String timeStamp;
        private String jClass;
        private boolean isWriteable = false;
        private int lineCount = 0;
        private int MAXLINES = 100;
        private FileWriter logFile;
        private BufferedWriter writer;
        private Long lastLogWriteTime = 0L;
        int bf;

    private static StatisticsLog ourInstance = new StatisticsLog();

    public static StatisticsLog getInstance() {
        return ourInstance;
    }

    private StatisticsLog() {
        appName = Utilities.getAppName();
        jClass = this.getClass().getName();
        //logLines = new String[MAXLINES];
        bf = 1;
    }

    public void addAdminMessage(String msg) {
        JSONObject jsonObject;
        String jStr = null;
        try {
            // Here we convert Java Object to JSON
            jsonObject = new JSONObject();
            jsonObject.put("MESSAGE",msg);
            jStr = jsonObject.toString();
            addStatLine(StatisticType.ADMIN,jStr);
        }
        catch(JSONException ex) {
            Utilities.handleCatch("E",jClass+":toJSON",ex);
        }
    }

    public void addStatLine(StatisticType sType,String statInfo) {
        Long timeSinceLastWrite;
        lineCount++;
        Log.d(myLogTag,sType.toString()+"|" +statInfo);
        timeSinceLastWrite = (new Date()).getTime() - lastLogWriteTime;
        if (lineCount > (MAXLINES-1)) {
            writeLog();
            lastLogWriteTime = (new Date()).getTime();
        }

    }

    public void writeLog() {
        int logLines=0;
        int statLines=0;
        String line;
        boolean readFlag = true;
        addAdminMessage("Dumping event log");
        openLog();
        if (isWriteable) {
            try {
                Process process = Runtime.getRuntime().exec("logcat -d -v long");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while (readFlag) {
                    line = bufferedReader.readLine();
                    if (line != null) {
                        logLines++;
                        if (line.contains(myLogTag)) {
                            writer.write(line + Utilities.newLine);
                            statLines++;
                        }
                        else {
                            bf = 1;
                        }
                    }
                    else {
                        readFlag = false;
                        closeLog();
                        Runtime.getRuntime().exec("logcat -c");
                        bf = 1;
                    }
                }
            } catch (IOException e) {
                bf = 1;
            }
        }
    }

    public void openLog() {
        String fpath;
        try {
            fpath = Utilities.getStatLogFilePath();
            logFile = new FileWriter(fpath,true);
            writer = new BufferedWriter(logFile);
            isWriteable = true;
        }
        catch (Exception e) {
            Utilities.handleCatch("E",jClass+"openLog",e);
        }
    }

    public void closeLog() {
        if (writer != null) {
            if (lineCount > 0) {
                writeLog();
            }
            try {
                writer.flush();
                writer.close();
                logFile.close();
            } catch (Exception e) {
                Utilities.handleCatch("E", jClass+"closeLog", e);
            }
            isWriteable = false;
        }
    }

}
