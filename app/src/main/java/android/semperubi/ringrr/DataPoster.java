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
    private ArrayList<String> transmitList;
    private String dataFilePath;
    private String uriString;
    private HttpURLConnection httpConnection;
    private final int sleepTime = 10000;
    int bf;


    public DataPoster(ArrayList<String> tList) {
        transmitList = tList;
    }

    @Override
    protected String doInBackground(String... params) {
        dataFilePath = params[0];
        uriString = params[1];
        URL url;
        // taskStart = new Date();
        try {
            url = new URL(uriString);
            httpConnection = (HttpURLConnection)url.openConnection();
            postCode();
            Thread.sleep(sleepTime);
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
            bf = 1;
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
                    dataOutputStream.writeBytes(logLine);
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
                Log.d("DataPoster", "Http Post:" + dataFilePath + " Succeeded");
            }
            else {
                Log.d("DataPoster","Http Post:" + dataFilePath + " Failed:" + HttpResponse(responseCode));
            }
        }
        catch (Exception e) {
            bf = 1;
        }
    }

    private void renameFile(String fPath) {
        boolean result;
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
            bf = 1;
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
                    stVal = "Authentication failure - the organization ID does not match Reporting Services account.";
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

