package android.semperubi.ringrr;

import android.content.Context;

import org.json.JSONObject;
/**
 * Created by Herb on 12/20/2015.
 */

abstract class StatisticObject {
    String appName;
    String jClass;
    Context mContext;
    StatisticType statType;
    StatisticsLog statLogger;
    boolean logFlag = true;
    boolean changeFlag;
    String previousInfo,newInfo;
    int delta;
    JSONObject jsonObject;
    abstract void initDetails();
    abstract void getDetails();
    abstract boolean checkDelta();
    abstract void setJSONdetails();

    public StatisticObject(Context ctx,StatisticType stype,int d) {
        mContext = ctx;
        statType = stype;
        appName = Utilities.getAppName();
        jClass = this.getClass().getName();
        delta = d;
        statLogger = StatisticsLog.getInstance();
        if (statLogger == null) {
            logFlag = false;
        }
        previousInfo = null;
        initDetails();
    }

    public boolean hasChanged(){return changeFlag;}

    public void update() {
        getDetails();
        newInfo = getJSON();
        changeFlag = true;
        if (newInfo.equals(previousInfo)) {
            changeFlag = false;
        }
        else {
            changeFlag = checkDelta();
        }
        if (changeFlag && logFlag) {
                statLogger.addLogLine(LogMessageType.STATISTIC,statType,getJSON(),true);
        }
        previousInfo = newInfo;
    }



    public String getJSON() {
        String jStr = null;
        try {
            // Here we convert Java Object to JSON
            jsonObject = new JSONObject();
            setJSONdetails();
            jStr = jsonObject.toString();
        }
        catch(Exception ex) {
            Utilities.handleCatch("E",jClass+":toJSON",ex);
        }

        return jStr;
    }

}
