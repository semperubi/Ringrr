package android.semperubi.ringrr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

public class SetupActivity extends Activity {

	boolean savedFlag = false;
	Context appContext;
	InputMethodManager imm;
	EditText.OnEditorActionListener etListener;
	OnFocusChangeListener focusListener;

	TableLayout tlInfoTypes,tlButtons;
	CheckBox useGPS,useBattery,useNetwork,useMemory,useApps;
	EditText etPollGPS,etPollNetwork,etPollBattery,etPollMemory,etPollApps;
	EditText etDeltaGPS,etDeltaBattery,etDeltaMemory;

	Intent intent;
	Bundle bundle;

	private RingrrConfigInfo configInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);
		appContext = getApplicationContext();
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		intent = getIntent();
		bundle = intent.getExtras();
		configInfo = RingrrConfigInfo.getInstance(appContext);
		getScreenObjects();
		setupListeners();
		setScreenObjects();
	}
	    public void done(View v) {

			if (savedFlag) {
				setResult(Activity.RESULT_OK, intent);
				this.finish();
			} else {
				setResult(Activity.RESULT_CANCELED, intent);
				Utilities.showToast(appContext, "Please press Save before finishing");
			}
		}
	    
	    public void saveSetupInfo(View v) {
			boolean sFlag;
			getAllParams();
			sFlag = configInfo.writeConfigInfo();
			if (!sFlag) {
				Utilities.showToast(appContext, "Unable to save Setup info");
			} else {
				Utilities.showToast(appContext, "Setup info saved");
				savedFlag = true;
			}
		}

	public void cancel(View v) {
    	this.finish();
    }
    
	private void getScreenObjects() {
		tlInfoTypes = (TableLayout)findViewById(R.id.tlInfoTypes);
		tlButtons = (TableLayout)findViewById(R.id.tlButtons);

		etPollGPS = (EditText)findViewById(R.id.etGPSInterval);
		etPollNetwork = (EditText)findViewById(R.id.etNetworkInterval);
		etPollBattery = (EditText)findViewById(R.id.etBatteryInterval);
		etPollMemory = (EditText)findViewById(R.id.etMemoryInterval);
		etPollApps = (EditText)findViewById(R.id.etAppsInterval);
		etDeltaGPS = (EditText)findViewById(R.id.etGPSDelta);
		etDeltaBattery = (EditText)findViewById(R.id.etBatteryDelta);
		etDeltaMemory = (EditText)findViewById(R.id.etMemoryDelta);

		useGPS = (CheckBox)findViewById(R.id.checkBoxGPS);
		useBattery = (CheckBox)findViewById(R.id.checkBoxBattery);
		useNetwork = (CheckBox)findViewById(R.id.checkBoxNetwork);
		useMemory = (CheckBox)findViewById(R.id.checkBoxMemory);
		useApps = (CheckBox)findViewById(R.id.checkBoxApps);
	}
	
	private void setScreenObjects() {
		setEtParams(etPollNetwork,null,true);
		setEtParams(etPollGPS,null,true);
		setEtParams(etPollBattery, null, true);
		setEtParams(etPollMemory, null, true);
		setEtParams(etPollApps, null, true);
		setEtParams(etDeltaGPS,null,true);
		setEtParams(etDeltaBattery,null,true);
		setEtParams(etDeltaMemory,null,true);
		
		//setTextItems(true);
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
	
	focusListener = new OnFocusChangeListener() {
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
						//TODO
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

	private void getAllParams() {

		boolean useFlag = false;
		if (useGPS.isChecked()) {
			useFlag = true;
		}
		configInfo.setStatInfo("Location",useFlag,getIntegerValue(etPollGPS),getIntegerValue(etDeltaGPS));


		useFlag = false;
		if (useNetwork.isChecked()) {
			useFlag = true;
		}
		configInfo.setStatInfo("Network",useFlag,getIntegerValue(etPollNetwork),0);


		useFlag = false;
		if (useBattery.isChecked()) {
			useFlag = true;
		}
		configInfo.setStatInfo("Battery",useFlag,getIntegerValue(etPollBattery),getIntegerValue(etDeltaBattery));


		useFlag = false;
		if (useMemory.isChecked()) {
			useFlag = true;
		}
		configInfo.setStatInfo("Memory",useFlag,getIntegerValue(etPollMemory),getIntegerValue(etDeltaMemory));


		useFlag = false;
		if (useApps.isChecked()) {
			useFlag = true;
		}
		configInfo.setStatInfo("Apps",useFlag,getIntegerValue(etPollApps),0);

	}

	private int getIntegerValue(EditText et) {
		int rval=0;
		try {
			rval = Integer.parseInt(et.getText().toString());
		}
		catch (Exception e) {
			Utilities.showToast(appContext,"Value must be integer");
		}

		return rval;
	}
}


