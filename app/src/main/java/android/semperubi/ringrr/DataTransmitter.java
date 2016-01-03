package android.semperubi.ringrr;

/**
 * Created by Herb on 1/1/2016.
 */
public class DataTransmitter {
    private static DataTransmitter ourInstance = new DataTransmitter();

    public static DataTransmitter getInstance() {
        return ourInstance;
    }

    private DataTransmitter() {
    }
}
