package android.semperubi.ringrr;

/**
 * Created by Herb on 1/1/2016.
 */
public class TransmissionList {
    private static TransmissionList ourInstance = new TransmissionList();

    public static TransmissionList getInstance() {
        return ourInstance;
    }

    private TransmissionList() {
    }
}
