package android.semperubi.ringrr;

import android.util.Log;

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
        public static Long logWriteTime = 0L;
        final String myLogTag = "RINGRR:";
        private String appName;
        private String jClass;
        private boolean isWriteable = false;
        private static int lineCount = 0;
        private int MAXLINES = 100;
        private String currentLogFilePath;
        private String lastLogFilePath="";
        private FileWriter logFile;
        private BufferedWriter writer;
        int bf;

    private static StatisticsLog ourInstance = new StatisticsLog();

    public static StatisticsLog getInstance() {
        return ourInstance;
    }

    private StatisticsLog() {
        appName = Utilities.getAppName();
        jClass = this.getClass().getName();
        bf = 1;
    }

    public void addAdminMessage(String message,boolean writeflag) {
        addLogLine(LogMessageType.ADMIN,null,message,writeflag);
    }

    public void addLogLine(LogMessageType logType,StatisticType sType, String message,boolean writeFlag) {
        String msgBody;
        String logTag = myLogTag + logType.toString();
        switch (logType) {
            case STATISTIC:
                msgBody = sType.toString() + ":" + message;
                Log.i(logTag,msgBody);
                break;
            case ADMIN:
            case DEBUG:
                msgBody = message;
                if (msgBody == null) {
                    msgBody = "No Other Information Supplied";
                }
                Log.d(logTag,msgBody); //prevents exception in Log
                break;
        }
        lineCount++;
        if (lineCount > (MAXLINES-1)) {
            if (writeFlag) {
                writeLog();
            }
        }
    }

    public void writeLog() {
        int logLines=0;
        int statLines=0;
        String line;
        boolean readFlag = true;
        if (lineCount < 1) {
            addAdminMessage("HEARTBEAT",false);
            return;
        }
        if (!isWriteable) {
            openLog();
        }
        if (isWriteable) {
            try {
                addAdminMessage("WRITE LOG",false);
                Process process = Runtime.getRuntime().exec("logcat -d -v time");  //sends log to standard out
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
                        lineCount = 0;
                        logWriteTime = (new Date()).getTime();
                        Runtime.getRuntime().exec("logcat -c");  //clears log
                        closeCurrentLog();
                        bf = 1;
                    }
                }
            } catch (IOException e) {
                bf = 1;
            }
        }
    }

    public void openLog() {
        String fPath;
        try {
            fPath = Utilities.getStatLogFilePath();
            if (!fPath.equals(lastLogFilePath)) {  //this should be different with each new day
                closeLog();
                currentLogFilePath = fPath;
                TransmissionList trList = TransmissionList.getInstance();
                trList.transmitData();
                lastLogFilePath = currentLogFilePath;
            }
            logFile = new FileWriter(currentLogFilePath,true);
            writer = new BufferedWriter(logFile);
            isWriteable = true;
        }
        catch (Exception e) {
            Utilities.handleCatch("E",jClass+":openLog",e);
        }
    }

    public void closeLog() {
        if (writer != null) {
            if (lineCount > 0) {
                writeLog();
            }
            closeCurrentLog();

        }
    }

    private void closeCurrentLog() {
        try {
            writer.flush();
            writer.close();
            logFile.close();
            writer = null;
            logFile = null;
        } catch (Exception e) {
            Utilities.handleCatch("E", jClass+"closeCurrentLog", e);
        }
        isWriteable = false;
    }


}
