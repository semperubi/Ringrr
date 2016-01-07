package android.semperubi.ringrr;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    //final static String thePassword = "Nirvana2015";
    final static String thePassword = "xxx";
    final static int SETUP = 1;
    final static int TEST = 2;
    TextView lbServices;
    EditText etPassword;
    TableLayout tlPassword,tlServices;
    InputMethodManager imm;
    EditText.OnEditorActionListener etListener;
    View.OnFocusChangeListener focusListener;

    ActivityManager activityManager;
    Context appContext;
    Context baseContext;

    StatisticsLog statLogger;
    RingrrConfigInfo configInfo;
    ServiceMessage serviceMessage;
    String previousAppList="initial list";

    int bf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appContext = getApplicationContext();
        Utilities.setContext(appContext);
        configInfo = RingrrConfigInfo.getInstance();
        baseContext = getBaseContext();
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        serviceMessage = ServiceMessage.getInstance();
        previousAppList = "";
        getScreenObjects();
        setServiceObjects(false);
        setupListeners();
        etPassword = (EditText)findViewById(R.id.etPassword);
        setEtParams(etPassword,null,true);
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    private void getScreenObjects() {

        lbServices = (TextView)findViewById(R.id.lbServices);
        tlServices = (TableLayout)findViewById(R.id.tlServiceButtons);
        etPassword = (EditText)findViewById(R.id.etPassword);
    }

    private void setServiceObjects(boolean isVisible) {
        int visibilityCode;
        if (isVisible) {
            visibilityCode = View.VISIBLE;
        }
        else {
            visibilityCode = View.INVISIBLE;
        }
        lbServices.setVisibility(visibilityCode);
        tlServices.setVisibility(visibilityCode);
    }

    // Method to start the service
    public void startService(View view) {
        serviceMessage.startService();
        Intent i = new Intent(this, StatisticsService.class);
        startService(i);
    }

    // Method to stop the service
    public void stopService(View view) {
        serviceMessage.stopService();
        Intent i = new Intent(this, StatisticsService.class);
        stopService(i);
    }

    public void pauseService(View view) {
        serviceMessage.pauseService();
    }

    public void continueService(View view) {
        serviceMessage.continueService();
    }

    public void reloadService(View view) {
        serviceMessage.reloadService();
    }

    public void setup(View v) {
        try {
            Intent intent = new Intent(this,SetupActivity.class);
            startActivityForResult(intent,SETUP);
        }
        catch (Exception e) {
            Utilities.handleCatch("Main", "startSetup: ", e);
        }
    }
    public void testFunctions(View v) {
        try {
            Intent intent = new Intent(this,TestActivity.class);
            startActivityForResult(intent,TEST);
        }
        catch (Exception e) {
            Utilities.handleCatch("Main", "testFunctions", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        switch(requestCode) {
            case SETUP:
                if (resultCode == Activity.RESULT_OK) {
                    //showToast("Finished Setup");
                }
                else {
                    //showToast("Unable to complete Setup - Please restart app");
                    //this.finish();
                }
                break;
            default:
                break;
        }
    }
    private boolean checkEntry(String tag,String txt) {
        int pollInterval;
        if (txt == null) {
            return false;
        }
        if (txt.length() < 1) {
            return false;
        }
        switch(tag) {
            case "password":
                if (txt.equals(thePassword)) {
                    TableLayout tlPassword = (TableLayout)findViewById(R.id.tlPassword);
                    tlPassword.setVisibility(View.GONE);
                    setServiceObjects(true);
                }
                else {
                    Utilities.showToast(appContext,"Invalid password");
                }
                break;
            default: //
                try {
                    pollInterval = Integer.parseInt(txt);
                    if (pollInterval < 1) {
                        Utilities.showToast(appContext,"Value must be an integer 1 or greater");
                    }
                }
                catch (Exception e) {
                    Utilities.showToast(appContext,"Poll interval value must be integer");
                }
        }
        return true;
    }

    private void setEtParams(EditText et, String theText,boolean setListener) {
        String txt = "";
        if (theText != null) {
            if (theText.length() > 0) {
                txt = theText;
            }
        }
        et.setText(txt);
        et.setFocusable(true);
        if (setListener) {
            et.setOnEditorActionListener(etListener);
            et.setOnFocusChangeListener(focusListener);
        }
    }

    private void setupListeners() {

        etListener = new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,KeyEvent event) {
                boolean entryOK = false;
                String tag = v.getTag().toString();
                String txt = v.getText().toString();

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    entryOK = checkEntry(tag,txt);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return entryOK;
            }
        };

        focusListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                boolean entryOK;
                //boolean hideSoftKeyboard = false;
                String txt;
                String tag = v.getTag().toString();
                EditText et;
                try {
                    if (!hasFocus) {
                        et = (EditText)v;
                        txt = et.getText().toString();
                        entryOK = checkEntry(tag,txt);
                        if (!entryOK) {
                            Utilities.showToast(appContext,"Invalid entry");
                        }
                    }
                }
                catch (Exception e) {
                    // hide virtual keyboard
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    //Utilities.handleCatch("SetupActivity","SetFocusListener",e);
                }

            }
        };
    }

}
