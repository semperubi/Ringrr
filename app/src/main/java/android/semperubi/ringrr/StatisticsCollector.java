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
    final int MAX_STATUS_COUNT = 5;
    final int MAX_SLEEP_COUNT = 5;
    private final int SLEEPTIME = 10000; //milliseconds
    private int sleepCount = 0;
    private Context appContext;
    private ActivityManager activityManager;
    private RingrrConfigInfo configInfo;
    private LocationInfo locationInfo;
    private BatteryInfo batteryInfo;
    private MemoryUsage memoryUsage;
    private NetworkInfo networkInfo;
    private ProcessInfo processInfo;
    private TransmissionList transmitList;
    private int waitIntervals[];
    private static StatisticsLog statLog;
    private boolean loadConfigFlag = true;
    private boolean pollFlag = true;
    private boolean runFlag = true;
    private boolean xmitFlag = true;
    private int statusCount=0;

    private ServiceMessageType previousMessageType = null;
    int bf,index;

    public StatisticsCollector(ActivityManager am) {
        appContext = Utilities.appContext;
        activityManager = am;
        statLog = StatisticsLog.getInstance();
        transmitList = TransmissionList.getInstance();

    }
    public void getAllStats() {
        Long pollCycles = 0L;
            while (pollFlag) {
                pollCycles++;
                if (loadConfigFlag) {
                    loadConfig();
                }
                checkServiceStatus();
                checkXmitData();
                try {
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
                    Thread.sleep(SLEEPTIME);
                    sleepCount++;
                    if (sleepCount > MAX_SLEEP_COUNT) {
                        statLog.writeLog();
                        sleepCount = 0;
                    }
                }
                catch (Exception e) {
                   e.printStackTrace();
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

    try {
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
    }
    catch (Exception e) {
        bf = 1;
    }
    }

    private void getInfo(int statIndex) {
        int delta;
        StatisticType st;
        try {
            st = configInfo.getStatTypeByIndex(statIndex);
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

    private void checkXmitData() {
        if (transmitList.filesPending()) {
            transmitList.transmitData();
        }
    }
}

