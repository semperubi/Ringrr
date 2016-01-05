package android.semperubi.ringrr;

import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Herb on 1/1/2016.
 */
public class TransmissionList {
    String thisDevice;
    ArrayList<String> transmitList;
    int bf;

    public boolean filesPending() {

        if (transmitList.size() > 0) {
            return true;
        }
        else {
            return false;
        }

    }

    private static TransmissionList ourInstance = new TransmissionList();

    public static TransmissionList getInstance() {
        return ourInstance;
    }


    private TransmissionList() {
        thisDevice = RingrrConfigInfo.deviceId.toString();
        transmitList = new ArrayList<String>();
        refresh();
    }

    public void transmitData() {        //cleanup of transmitList will be done in doInBackground of DataPoster
        int nFiles,i;
        String[] fPath;
        nFiles = transmitList.size();
        if (nFiles > 0)  {
            if (wifiConnected()) {
                fPath = new String[nFiles];
                for (i=0;i<nFiles;i++) {
                    fPath[i] = transmitList.get(i);
                }
                for (i=0;i<nFiles;i++) {
                    transmitFile(fPath[i]);
                }
            }
        }
        refresh();
    }

    private boolean transmitFile(String transmitFilePath) {
        boolean rval = false;
        Long logTime,startTime,endTime;
        Date statFileDate = Utilities.getStatLogFileDate(transmitFilePath);
        startTime = statFileDate.getTime();
        endTime = startTime + Utilities.MILLISECONDS_PER_DAY - Utilities.MILLISECONDS_PER_MINUTE;
        logTime = (new Date()).getTime();

        try {
            //String urlParameters = "?log=gps&device=" + thisDevice + "&now=" + logTime.toString() + "&start=" + startTime.toString() + "&stop=" + endTime.toString() + "&dry-run=true";
            Uri uri = new Uri.Builder()
                    .scheme( "http" )
                    .encodedAuthority( "test.hop.video:7770" )
                    .appendPath("device")
                    .appendPath("upload-log")
                    .appendQueryParameter("log", "test")
                    .appendQueryParameter("device", thisDevice)
                    .appendQueryParameter("now", logTime.toString())
                    .appendQueryParameter("start", startTime.toString())
                    .appendQueryParameter("stop", endTime.toString())
                    .appendQueryParameter( "dry-run", "true" )
                    .build();
           DataPoster dataPoster = new DataPoster(transmitList);
           dataPoster.execute(transmitFilePath,uri.toString());
           rval = true;
        }
        catch (Exception e) {
            bf = 1;
        }
        return rval;
    }

    private boolean wifiConnected() {
        boolean rval = false;
        NetworkDetails netDetails = new NetworkDetails();
        if (netDetails.isConnected && netDetails.transmitFlag) {
            rval = true;
        }
        return rval;
    }



    public void refresh() {
        String fname,fpath,currentLogFilePath;
        boolean addFlag;
        File statFolder;
        File[] statFiles;
        try {
            currentLogFilePath = Utilities.getStatLogFilePath();
            statFolder = new File(Utilities.getSystemFolder());
            statFiles = statFolder.listFiles();
            for (int i = 0; i < statFiles.length; i++) {
                fpath = statFiles[i].getPath();
                fname = statFiles[i].getName();
                if (fname.startsWith(Utilities.statFilePrefix)) {
                    if (!fpath.equals(currentLogFilePath)) {
                        addFlag = true;
                        for (int j = 0; j < transmitList.size(); j++) {
                            if (transmitList.get(j).equals(fpath)) {
                                addFlag = false;
                                break;
                            }
                        }
                        if (addFlag) {
                            transmitList.add(fpath);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            bf = 1;
        }
    }
}
