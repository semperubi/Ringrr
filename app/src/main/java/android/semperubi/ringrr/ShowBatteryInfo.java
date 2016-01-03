package android.semperubi.ringrr;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ShowBatteryInfo extends Activity {
    Context appContext;
    BatteryInfo batteryInfo;
    TextView tvBatteryStatus,tvBatteryLevel,tvBatteryChargeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_battery_info);
        appContext = getApplicationContext();
        tvBatteryStatus = (TextView)findViewById(R.id.tvBatteryStatus);
        tvBatteryLevel = (TextView)findViewById(R.id.tvBatteryLevel);

        tvBatteryChargeType = (TextView)findViewById(R.id.tvBatteryChargeType);
        batteryInfo = new BatteryInfo(appContext,1);
        update();
    }

    public void refresh(View v) {
        update();
    }
    private void update() {
        batteryInfo.update();
        tvBatteryStatus.setText(batteryInfo.batteryState);
        tvBatteryLevel.setText(batteryInfo.batteryPercent .toString()+ "%");
        tvBatteryChargeType.setText(batteryInfo.batteryCable);
    }
}
