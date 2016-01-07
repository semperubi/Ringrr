package android.semperubi.ringrr;
/**
 * Created by Herb on 12/21/2015.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DataPoster extends AsyncTask<String,Integer,String> {
    final String transmitMarker = "I/RINGRR";
    RingrrConfigInfo configInfo;
    @SuppressWarnings("CanBeFinal")
    private ArrayList<String> transmitList;
    private String dataFilePath;
    String uriString;
    private HttpURLConnection httpConnection;
    int bf;

    public DataPoster(ArrayList<String> tList) {
        transmitList = tList;
    }

    @Override
    protected String doInBackground(String... params) {
        dataFilePath = params[0];
        uriString = params[1];
        configInfo = RingrrConfigInfo.getInstance();
        URL url;
        // taskStart = new Date();
        try {
            Log.d("DataPoster","Transmit file:" + dataFilePath);
            Log.d("DataPoster","URI String = " + uriString);
            url = new URL(uriString);

            httpConnection = (HttpURLConnection)url.openConnection();
            postCode();
            Thread.sleep(configInfo.threadSleepTime);
        }
        catch (InterruptedException e)
        {
            Utilities.handleCatch("HttpSendTime", "constructor", e);
        }
        catch (Exception e) {
            Utilities.handleCatch("HttpSendTime", "constructor", e);
        }
        return "DONE";
    }

    private void postCode() {
        int responseCode;

        DataOutputStream dataOutputStream;

        try {
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            //httpConnection.setDoInput(true);
            //httpConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            //httpConnection.setRequestProperty("Content-Type", "text/plain");
        }
        catch (Exception e) {
            Utilities.handleCatch("DataPoster","postCode",e);
        }

        try {
            String logLine;
            boolean readFlag= true;
            FileReader logFile;
            BufferedReader logFilereader;
            dataOutputStream = new DataOutputStream(httpConnection.getOutputStream());
            logFile = new FileReader(dataFilePath);
            logFilereader = new BufferedReader(logFile);
            while(readFlag) {
                logLine = logFilereader.readLine();
                if (logLine == null) {
                    readFlag = false;
                }
                else {
                    if (logLine.contains(transmitMarker)) {
                        dataOutputStream.writeBytes(logLine);
                    }
                }
            }
            dataOutputStream.flush();
            dataOutputStream.close();
            responseCode = httpConnection.getResponseCode();
            for (int i=0; i < transmitList.size(); i++) {
                if (transmitList.get(i).equals(dataFilePath)) {
                    renameFile(dataFilePath);
                    transmitList.remove(i);
                }
            }
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("DataPoster", "Http Post Succeeded");
            }
            else {
                Log.d("DataPoster","Http Post Failed:" + HttpResponse(responseCode));
            }
        }
        catch (Exception e) {
            Utilities.handleCatch("DataPoster","postCode",e);
        }
    }

    private void renameFile(String fPath) {
        String oldName,newName,filePath;
        File statFile,newFile;
        try {
            filePath = fPath;
            statFile = new File(filePath);
            oldName = statFile.getName();
            newName = "Saved_" + oldName;
            filePath = filePath.replaceFirst(oldName,newName);
            newFile = new File(filePath);
            statFile.renameTo(newFile);
        }
        catch (Exception e) {
            Utilities.handleCatch("DataPoster","renameFile",e);
        }
    }

    protected void onProgressUpdate(Integer... progress) {
        bf = 1;
    }

    protected void onPostExecute(String result) {
       bf = 1;
    }

    public String HttpResponse(int rCode) {
        //returns a text value for the passed return code
        String stVal;
        try {
            switch (rCode) {
                case 0:
                case 200://OK
                    stVal = "OK";
                    break;
                case 1: //auth failure
                    stVal = "Authentication failure - invalid organization ID";
                    break;
                case 2: //activation failure
                    stVal = "Activation key does not match Organization ID.";
                    break;
                default:
                    stVal = "Unknown error code: " + rCode;
                    break;
            }
        }
        catch (Exception e) {
            stVal = "Unknown response code: " + rCode;
        }
        return stVal;
    }

}

