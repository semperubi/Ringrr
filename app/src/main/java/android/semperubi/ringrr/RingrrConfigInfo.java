package android.semperubi.ringrr;

/**
 * Created by Herb on 12/21/2015.
 */

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Locale;


public class RingrrConfigInfo {
    public static DeviceId deviceId;
    public static int nstats;
    public static int statPollIntervals[];
    public static int statDeltas[];
    public static boolean statUseFlags[];

    static boolean initFlag = true;
    static Context mainContext;
    static String configLineFormat = "%s  %s  %d";
    static String configFileHeader="STATISTIC_TYPE" + Utilities.hTab + "USE" + Utilities.hTab + "POLL_INTERVAL"+ Utilities.hTab +"ACCURACY";
    private static File configFile;

    private static RingrrConfigInfo ourInstance = new RingrrConfigInfo();

    public static RingrrConfigInfo getInstance(Context ctx) {
        if (initFlag) {
            mainContext = ctx;
            deviceId = DeviceId.findBest(mainContext);
            nstats = StatisticType.values().length;
            statPollIntervals = new int[nstats];
            statUseFlags = new boolean[nstats];
            statDeltas = new int[nstats];
            configFile = new File(Utilities.getConfigFilePath());
            if (configFile.exists()) {
                readConfigInfoFile();
            }
        }

        return ourInstance;
    }

    private RingrrConfigInfo() {
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
        boolean readHdr = false;
        boolean validType;

        String fline;
        String statisticType;
        String stName;
        String[] fparts;
        FileReader inFile;
        BufferedReader freader;
        try {
            inFile = new FileReader(configFile);
            freader = new BufferedReader(inFile);
            while (readFlag) {
                fline = freader.readLine();
                if (fline == null) {
                    readFlag = false;
                }
                else
                {
                    if (readHdr) {
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
                                            } catch (Exception e) {
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
                    else {
                        readHdr = true;
                    }
                }
            }
        }
        catch (Exception e) {
            rval = false;
        }

        return rval;
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
            writer.write(configFileHeader + "\n");

            for (i = 0; i < configLines.length; i++) {
                writer.write(configLines[i] + "\n");
            }
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            rval = false;
            Utilities.handleCatch("RingrrReadConfigFile", "writeConfigInfo", e);
        }

        return rval;
    }
}

