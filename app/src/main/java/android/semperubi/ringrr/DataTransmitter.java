package android.semperubi.ringrr;

import java.io.File;

/**
 * Created by Herb on 1/1/2016.
 */
public class DataTransmitter {
    File statFolder;
    File[] statFiles;
    private static DataTransmitter ourInstance = new DataTransmitter();

    public static DataTransmitter getInstance() {

        return ourInstance;
    }

    private DataTransmitter() {
        String fname;
        statFolder = new File(Utilities.getSystemFolder());
        statFiles = statFolder.listFiles();
        for (int i=0; i < statFiles.length; i++) {
            fname = statFiles[i].getName();
            if (fname.startsWith(Utilities.statFilePrefix)) {

            }
        }
    }
}
