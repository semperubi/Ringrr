package android.semperubi.ringrr;

/**
 * Created by Herb on 12/21/2015.
 */

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.Locale;


public final class RingrrConfigInfo {
    public static boolean debugFlag=false;
    public static String deviceID = null;
    public static int pollTime = 1;
    public static long threadSleepTime;
    public static int nstats;
    public static int statPollIntervals[];
    public static int statDeltas[];
    public static boolean statUseFlags[];
    private static String[] httpPathSegments;
    private static String httpScheme,encodedAuthority,httpPath,logType;
    private static Boolean dryRunFlag = true;

    static boolean initFlag = true;
    static Context mainContext=null;
    static String configLineFormat = "%s  %s  %d";
    private static File configFile;

    private static int bf;

    private static RingrrConfigInfo ourInstance = new RingrrConfigInfo();

    public static RingrrConfigInfo getInstance() {
        return ourInstance;
    }

    public static String getDeviceID() {
        if (deviceID == null) {
            DeviceId devId = DeviceId.findBest(Utilities.appContext);
        }
        return deviceID;
    }

    private RingrrConfigInfo() {
        nstats = StatisticType.values().length;
        statPollIntervals = new int[nstats];
        statUseFlags = new boolean[nstats];
        statDeltas = new int[nstats];
        configFile = new File(Utilities.getConfigFilePath());
        if (configFile.exists()) {
            readConfigInfoFile();
        }
        else {
            File sysFolder = new File(Utilities.getSystemFolder());
            if (!sysFolder.exists()) {
                sysFolder.mkdir();
            }
        }
        bf = 1;
    }

    public StatisticType getStatType(String stName) {
        StatisticType sType = null;
        for (StatisticType st : StatisticType.values()) {
            if (stName.toUpperCase().equals(st.toString().toUpperCase())) {
                sType = st;
                break;
            }
        }
        return sType;
    }
    public StatisticType getStatTypeByIndex(int index) {
        StatisticType rval = null;
        for (StatisticType st : StatisticType.values()) {
            if (index == st.getIntValue()) {
                rval = st;
                break;
            }
        }
        return rval;
    }

    public boolean getStatUseFlag(String statName) {
        boolean rval = false;
        StatisticType st = getStatType(statName);
        if (st != null) {
            rval = statUseFlags[st.getIntValue()];
        }
        return rval;
    }

    public int getStatPollInterval(String statName) {
        int rval = -1;
        StatisticType st = getStatType(statName);
        if (st != null) {
            rval = statPollIntervals[st.getIntValue()];
        }
        return rval;
    }

    public void setStatInfo(String statName,boolean useFlag,int pollInterval,int deltaValue) {
        int index;
        StatisticType st = getStatType(statName);
        if (st != null) {
            index = st.getIntValue();
            statUseFlags[index] = useFlag;
            statPollIntervals[index] = pollInterval;
            statDeltas[index] = deltaValue;
        }
    }

    public static boolean readConfigInfoFile() {
        int stIndex;
        boolean rval = true;
        boolean readFlag = true;
        ConfigSection section;
        String line;
        FileReader inFile;
        BufferedReader freader;
        try {
            inFile = new FileReader(configFile);
            freader = new BufferedReader(inFile);
            section = ConfigSection.NONE;
            while (readFlag) {
                line = freader.readLine();
                if (line == null) {
                    readFlag = false;
                } else {
                    if (line.startsWith("[")) {
                        for (ConfigSection cs : ConfigSection.values()) {
                            if (cs.toString().equals(line)) {
                                section = cs;
                                break;
                            }
                        }
                    } else {
                        switch (section) {
                            case STATISTICS:
                                parseStatisticConfigLine(line);
                                break;
                            default:
                                parseOtherConfigLine(line);
                                break;
                        }
                    }
                }
            }
        }
        catch(Exception e){
            bf = 1;
        }
            return rval;
    }

    private static void parseOtherConfigLine(String fline) {
        String fparts[];
        fparts = fline.split(Utilities.hTab);
        switch (fparts[0]) {
            case "DEVICEID":
                deviceID = fparts[1];
            case "POLLTIME":
                try {
                    pollTime = Integer.parseInt(fparts[1]);
                }
                catch (Exception e) {
                    bf = 1;
                }
                break;
            case "SLEEPTIME":
                try {
                    int p = Integer.parseInt(fparts[1]);
                    threadSleepTime = Utilities.MILLISECONDS_PER_MINUTE *p;
                }
                catch (Exception e) {
                    bf = 1;
                }
                break;
            case "PATH":
                try {
                    if (!parseWebPath(fparts[1])) {
                        bf = 1;
                    }
                }
                catch (Exception e) {
                    bf = 1;
                }
                break;
            case "DRYRUN":
                try {
                    if (fparts[1].equals("true")) {
                        dryRunFlag = true;
                    }
                }
                catch (Exception e) {
                    bf = 1;
                }
                break;
            case "LOGTYPE":
                logType = fparts[1];
                break;
            default:
                break;
        }
    }

    private static boolean parseWebPath(String basePath) {
        boolean rval = false;
        String path;
        URL testURL;
        try {
            testURL = new URL(basePath);
            httpScheme = testURL.getProtocol();
            encodedAuthority = testURL.getAuthority();
            path = testURL.getPath();
            httpPathSegments = path.split("/");
            rval = true;

        } catch (Exception e) {
            bf = 1;
        }
        return rval;
    }

    private static void parseStatisticConfigLine(String fline) {
        int stIndex;
        String stName,statisticType;
        String fparts[];
        boolean validType;
        fparts = fline.split(Utilities.hTab);
        if (fparts.length > 1) {
            statisticType = fparts[0];
            validType = false;
            stIndex = -1;
            for (StatisticType st : StatisticType.values()) {
                stName = st.toString();
                if (statisticType.toUpperCase().equals(stName.toUpperCase())) {
                    stIndex = st.getIntValue();
                    validType = true;
                    break;
                }
            }
            if (validType) {
                statUseFlags[stIndex] = true;
                statPollIntervals[stIndex] = Utilities.DEFAULT_POLL_INTERVAL;
                statDeltas[stIndex] = Utilities.DEFAULT_STAT_DELTA;
                if (fparts.length > 1) {
                    if (fparts[1].equals("N")) {
                        statUseFlags[stIndex] = false;
                    } else {
                        if (fparts.length > 2) {
                            try {
                                statPollIntervals[stIndex] = Integer.parseInt(fparts[2]);
                            } catch (Exception e) {
                                Log.d("Ringrr", "RingrrConfigInfo: Invalid polling interval: " + fparts[2]);
                                            }
                        }
                        if (fparts.length > 3) {
                            try {
                                statDeltas[stIndex] = Integer.parseInt(fparts[2]);
                            }
                            catch (Exception e) {
                                Log.d("Ringrr", "RingrrConfigInfo: Invalid delta: " + fparts[3]);
                            }
                        }
                    }
                }
            } else {
                Log.d("Ringrr", "RingrrConfigInfo: Invalid statistic type: " + fparts[0]);
            }
        }
    }


    public boolean writeConfigInfo() {
        int i,index;
        boolean rval = true;
        String useFlag;
        String[] configLines = new String[nstats];
        Integer pollInterval,deltaValue;
        FileWriter outFile;
        BufferedWriter writer;

        for (StatisticType st: StatisticType.values()) {
            index = st.getIntValue();
            useFlag = "N";
            if (statUseFlags[index]) {
                useFlag = "Y";
            }
            pollInterval = statPollIntervals[index];
            deltaValue = statDeltas[index];
            configLines[index] = String.format(Locale.getDefault(),configLineFormat,st.toString(),useFlag,pollInterval.toString(),deltaValue.toString());
        }

        try {
            outFile = new FileWriter(configFile);
            writer = new BufferedWriter(outFile);

            for (i = 0; i < configLines.length; i++) {
                writer.write(configLines[i] + "\n");
            }
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            rval = false;
            Utilities.handleCatch("RingrrConfigInfo", "writeConfigInfo", e);
        }
        return rval;
    }


    //final; URI string should look like following example:
    //http://test.hop.video:7770/device/upload-log?log=test&device=wm9x53sdRnW--Qy82LxFLtDg&now=1451931876822&start=1451624400000&stop=1451710740000&dry-run=true
    //which can be created as follows provided the string components are supplied
    public static Uri getHttpUri(Long logTime,Long startTime,Long endTime) {
        Uri uri;
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme(httpScheme) ;  //either http or https
        uriBuilder.encodedAuthority(encodedAuthority);
        for (int i=0; i < httpPathSegments.length; i++) {
            uriBuilder.appendPath(httpPathSegments[i]);
        }
        uriBuilder.appendQueryParameter("log",logType);
        uriBuilder.appendQueryParameter("device", deviceID);
        uriBuilder.appendQueryParameter("now", logTime.toString());
        uriBuilder.appendQueryParameter("start", startTime.toString());
        uriBuilder.appendQueryParameter("stop", endTime.toString());
        uriBuilder.appendQueryParameter("dry-run", dryRunFlag.toString());
        uri = uriBuilder.build();
        return uri;
    }

    private enum ConfigSection {
        NONE("NONE",0),
        STATISTICS("[STATISTICS]",1),
        TIME("[TIME]",2),
        WEB("[WEB]",3),
        OTHER("[OTHER]",4);

        private String stringValue;
        private int intValue;

        ConfigSection(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue;
        }
        public int getIntValue() {
            return intValue;
        }
    }
}

