package android.semperubi.ringrr;
/**
 * Created by Herb on 12/22/2015.
 */

import android.app.ActivityManager;
import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

public class StatisticsCollector {
    final int MAX_STATUS_COUNT = 50;
    private Context appContext;
    private ActivityManager activityManager;
    private static final int SLEEPTIME = 60000; //milliseconds
    private RingrrConfigInfo configInfo;
    private LocationInfo locationInfo;
    private GPSInfo gpsInfo;
    private BatteryInfo batteryInfo;
    private MemoryUsage memoryUsage;
    private NetworkInfo networkInfo;
    private ProcessInfo processInfo;
    private int waitIntervals[];
    private static StatisticsLog statLog;
    private boolean loadConfigFlag = true;
    private boolean pollFlag = true;
    private boolean runFlag = true;
    private int statusCount=0;
    private int intervalCount = 0;
    private ServiceMessageType previousMessageType = null;
    int bf,index;

    public StatisticsCollector(Context ctx,ActivityManager am) {
        appContext = ctx;
        activityManager = am;
        statLog = StatisticsLog.getInstance();
    }
    public void getAllStats() {
            while (pollFlag) {
                checkServiceStatus();
                if (loadConfigFlag) {
                    loadConfig();
                }
                try {
                    Thread.sleep(SLEEPTIME);
                    intervalCount++;
                    if (runFlag) {
                        for (index = 0; index < waitIntervals.length; index++) {
                            if (RingrrConfigInfo.statPollIntervals[index] > 0) {  //pollInterval < 1 means not used
                                waitIntervals[index] = waitIntervals[index] - 1;
                                if (waitIntervals[index] < 1) {
                                    getInfo(index);
                                    waitIntervals[index] = RingrrConfigInfo.statPollIntervals[index];
                                }
                            }
                        }
                    }
                }
                catch (Exception e) {
                    Utilities.handleCatch("SystemStatsService","poll",e);
                }
            }
        }

    private void loadConfig() {
        configInfo = null;
        configInfo = RingrrConfigInfo.getInstance(appContext);
        waitIntervals = null;
        waitIntervals = new int[RingrrConfigInfo.statPollIntervals.length];
        try {
            for (int i = 0; i < RingrrConfigInfo.nstats; i++) {
                waitIntervals[i] = RingrrConfigInfo.statPollIntervals[i];
            }
        } catch (Exception e) {
            bf = 1;
        }
        loadConfigFlag = false;
    }

    private void checkServiceStatus() {
        boolean addFlag = false;
        boolean dumpFlag = false;

        ServiceMessageType serviceMessageType = readServiceMessage();
        switch (serviceMessageType) {
            case STOP:
                pollFlag = false;
                break;
            case PAUSE:
                runFlag = false;
                statLog.closeLog();
                break;
            case CONTINUE:
                statLog.openLog();
                runFlag = true;
                break;
            case RELOAD:
                loadConfigFlag = true;
                break;
            default:    //used for ADMIN and STATUS types
                break;
        }
        if (previousMessageType == null) {
            addFlag = true;
        }
        else {
            if (serviceMessageType.equals(previousMessageType)) {
                statusCount++;
                if (statusCount > MAX_STATUS_COUNT) {
                    statLog.addAdminMessage("Heartbeat");
                    addFlag = true;
                    statusCount = 0;
                }
            }
        }
        if (addFlag) {
            statLog.addStatLine(StatisticType.STATUS, serviceMessageType.toString());
        }
        previousMessageType = serviceMessageType;
    }

    private void getInfo(int statIndex) {
        int delta;
        StatisticType st = configInfo.getStatTypeByIndex(statIndex);
        boolean initFlag;
        try {
            delta = RingrrConfigInfo.statDeltas[statIndex];
            if (st != null) {
                switch (st) {
                    case LOCATION:
                        if (locationInfo == null) {
                            int pollInterval = configInfo.getStatPollInterval(StatisticType.LOCATION.toString());
                            locationInfo = new LocationInfo(appContext, pollInterval, delta);
                        }
                        break;
                    case BATTERY:
                        if (batteryInfo == null) {
                            batteryInfo = new BatteryInfo(appContext, delta);
                        }
                        batteryInfo.update();
                        break;
                    case MEMORY:
                        if (memoryUsage == null) {
                            memoryUsage = new MemoryUsage(appContext, delta);
                        }
                        memoryUsage.update();
                        break;
                    case NETWORK:
                        if (networkInfo == null) {
                            networkInfo = new NetworkInfo(appContext, delta);
                        }
                        networkInfo.update();
                        break;
                    case APPS:
                        if (processInfo == null) {
                            processInfo = new ProcessInfo(activityManager);
                        }
                        processInfo.update();
                    default:
                        break;
                }
            }
        }
        catch (Exception e) {
            bf = 1;
        }
    }


    private ServiceMessageType readServiceMessage() {
        ServiceMessageType smType = ServiceMessageType.NONE;
        String message;
        FileReader serviceFileReader;
        BufferedReader bufferedServiceReader;

        try {
            serviceFileReader = new FileReader(Utilities.getServiceFilePath());
            bufferedServiceReader = new BufferedReader(serviceFileReader);
            message = bufferedServiceReader.readLine();
            bufferedServiceReader.close();
            serviceFileReader.close();
            if (message != null) {
                for (ServiceMessageType smt : ServiceMessageType.values()) {
                    if (message.equals(smt.toString())) {
                        smType = smt;
                        break;
                    }
                }
            }
        }
        catch (FileNotFoundException e) {
            // initialize the file
            try {
                FileWriter fWriter = new FileWriter(Utilities.getServiceFilePath());
                BufferedWriter buffWriter = new BufferedWriter(fWriter);
                buffWriter.write(ServiceMessageType.NONE.toString()+Utilities.newLine);
                buffWriter.flush();
                buffWriter.close();
                fWriter.close();
            }
            catch (Exception e2) {
                Utilities.handleCatch("Ringrr","StatisticsLog",e);
            }
        }
        catch (Exception e3) {
            Utilities.handleCatch("Ringrr","StatisticsCollector",e3);
        }

        return smType;
    }

}

