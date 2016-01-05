package android.semperubi.ringrr;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Created by Herb on 12/22/2015.
 */
public class ServiceMessage {
    FileWriter serviceFile;
    BufferedWriter writer;
    int bf;

    private static ServiceMessage ourInstance = new ServiceMessage();

    public static ServiceMessage getInstance() {
        return ourInstance;
    }

    private ServiceMessage() {
    }

    public void stopService() {sendMessage(ServiceMessageType.STOP);
    }

    public void startService() {
        sendMessage(ServiceMessageType.START);
    }

    public void pauseService() {sendMessage(ServiceMessageType.PAUSE);}

    public void continueService() {sendMessage(ServiceMessageType.CONTINUE);}

    public void reloadService() {sendMessage(ServiceMessageType.RELOAD);}

    private boolean sendMessage(ServiceMessageType sType) {
        boolean rval;
        try {
            rval = openFile();
            if (rval) {
                writer.write(sType.toString() + Utilities.newLine);
                closeFile();
            }
        }
        catch (Exception e) {
            rval = false;
        }
        return rval;
    }

    private boolean openFile() {
        boolean rval = true;
        try {
            serviceFile = new FileWriter(Utilities.getServiceFilePath(), true);
            writer = new BufferedWriter(serviceFile);
        }
        catch (Exception e) {
            rval = false;
            Utilities.handleCatch("Ringrr", "ServiceMessage:openFile", e);
        }
        return rval;
    }

    private void closeFile() {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
                serviceFile.close();
            }
            catch (Exception e) {
                bf = 1;
            }
        }
    }

}
