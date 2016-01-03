package android.semperubi.ringrr;

/**
 * Created by Herb on 12/22/2015.
 */
public enum ServiceMessageType {
    STOP("STOP",0),
    START("START",1),
    PAUSE("PAUSE",2),
    CONTINUE("CONTINUE",3),
    RELOAD("RELOAD",4),
    HEARTBEAT("HEARTBEAT",5),
    NONE("NONE",6);

    private String stringValue;
    private int intValue;

    ServiceMessageType(String toString, int value) {
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
