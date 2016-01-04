package android.semperubi.ringrr;

/**
 * Created by Herb on 12/14/2015.
 */
public enum StatisticType {
    APPS("APPS",0),
    LOCATION("LOCATION",1),
    BATTERY("BATTERY",2),
    MEMORY("MEMORY",3),
    NETWORK("NETWORK",4);


    private String stringValue;
    private int intValue;

    StatisticType(String toString, int value) {
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
