package android.semperubi.ringrr;

/**
 * Created by Herb on 1/1/2016.
 */
public enum LogMessageType {
    ADMIN("ADMIN",0),
    STATISTIC("STATISTIC",1),
    DEBUG("DEBUG",2);

    private String stringValue;
    private int intValue;

    LogMessageType(String toString, int value) {
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
