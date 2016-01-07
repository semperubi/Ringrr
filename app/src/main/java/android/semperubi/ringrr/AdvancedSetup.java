package android.semperubi.ringrr;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.net.URL;

public class AdvancedSetup extends Activity {
    boolean savedFlag = false;
    Context appContext;
    InputMethodManager imm;
    EditText.OnEditorActionListener etListener;
    View.OnFocusChangeListener focusListener;
    String basePath;
    int bf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_setup);
    }

    public void save(View v) {

    }

    public void cancel(View v) {

    }

    public void done(View v) {

    }

    private boolean parseBasePath() {
        boolean rval = false;
        String scheme, authority, path;
        URL testURL;
        try {
            testURL = new URL(basePath);
            scheme = testURL.getProtocol();
            authority = testURL.getAuthority();
            path = testURL.getPath();
            rval = true;

        } catch (Exception e) {
            bf = 1;
        }
        return rval;
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

    private boolean checkEntry(String tag,String txt) {
        int pollInterval;
        if (txt == null) {
            return false;
        }
        if (txt.length() < 1) {
            return false;
        }
        try {
            pollInterval = Integer.parseInt(txt);
            if (pollInterval < 1) {
                Utilities.showToast(appContext,"Value must be an integer 1 or greater");
            }
        }
        catch (Exception e) {
            Utilities.showToast(appContext,"Poll interval value must be integer");
        }

        return true;
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
