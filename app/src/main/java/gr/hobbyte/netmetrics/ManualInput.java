package gr.hobbyte.netmetrics;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import gr.hobbyte.netmetrics.database.BatteryStats;
import gr.hobbyte.netmetrics.database.DatabaseInterface;
import gr.hobbyte.netmetrics.database.NetworkStats;
import gr.hobbyte.netmetrics.database.DeviceStats;

import gr.hobbyte.netmetrics.databinding.ActivityManualinputBinding;

public class ManualInput extends AppCompatActivity
        implements View.OnClickListener {

    private ActivityManualinputBinding binding;
    private DatabaseInterface dbInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manualinput);

        dbInt = new DatabaseInterface(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manualinput);

        binding.buttonSaveManual.setOnClickListener(this);

        Bundle b = getIntent().getExtras();
        if(b != null){
            binding.connectionManualEdittext.setText(b.getString(Constants.CONN_TYPE));
            binding.signalManualEdittext.setText(b.getString(Constants.CONN_SIGNAL));
            binding.bandwidth.setText(b.getString(Constants.IPERF_BANDWIDTH));
            binding.timePingsManualEdittext.setText(b.getString(Constants.PING_WAIT));
            binding.timeManualEdittext.setText(b.getString(Constants.TOTAL_TIME));
            binding.dataManualEdittext.setText(b.getString(Constants.TOTAL_BYTES));
            binding.batteryManualEdittext.setText(b.getString(Constants.BATTERY_USED));
            binding.type.setText(b.getString(Constants.USAGE_MODE));
            binding.iperfMode.setText(b.getString(Constants.IPERF_MODE));
            binding.datagrams.setText(b.getString(Constants.IPERF_LOST_PACKETS));
            binding.jitter.setText(b.getString(Constants.IPERF_JITTER));
        }
        binding.modelManualEdittext.setText(Build.MODEL);
        binding.deviceBrandManualEdittext.setText(Build.MANUFACTURER);
        binding.deviceidManualEdittext.setText(Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));
        binding.versionManualEdittext.setText(Build.VERSION.RELEASE);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() != R.id.button_save_manual)
            return;

        String connection = binding.connectionManualEdittext.getText().toString();
        String data = binding.dataManualEdittext.getText().toString();
        String time = binding.timeManualEdittext.getText().toString();
        String battery = binding.batteryManualEdittext.getText().toString();
        String signal = binding.signalManualEdittext.getText().toString();
        String timePings = binding.timePingsManualEdittext.getText().toString();
        String bandwidth = binding.bandwidth.getText().toString();
        String usageType = binding.type.getText().toString();
        String iperfMode = binding.iperfMode.getText().toString();
        String datagrams = binding.datagrams.getText().toString();
        String jitter = binding.jitter.getText().toString();

        NetworkStats m = new NetworkStats();
        m.setConnType(connection);
        m.setMbsused(data);
        m.setTime(time);
        m.setSignalStrength(signal);
        m.setTimeBetweenPings(timePings);
        m.setBandwidth(bandwidth);
        m.setUsageType(usageType);
        m.setMode(iperfMode);
        m.setLostPackets(datagrams);
        m.setJitter(jitter);


        BatteryStats bs = new BatteryStats();
        bs.setBatteryUsed(battery);

        NetworkConnectivityUtils utils = new NetworkConnectivityUtils();
        deviceInfo(utils, m, bs);
        Snackbar bar = Snackbar.make(findViewById(android.R.id.content), "Saved", Snackbar.LENGTH_LONG).setAction("Show", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(), MetricViewer.class);
                startActivity(intent);
            }
        });

        bar.setActionTextColor(Color.GREEN).show();
    }

    private void deviceInfo(NetworkConnectivityUtils utils, NetworkStats m, BatteryStats bs){
        DeviceStats devstats = new DeviceStats();
        devstats.setDeviceModel(binding.modelManualEdittext.getText().toString());
        devstats.setDeviceID(binding.deviceidManualEdittext.getText().toString());
        devstats.setDeviceVersion(binding.versionManualEdittext.getText().toString());
        devstats.setDeviceBrand(binding.deviceBrandManualEdittext.getText().toString());

        if(utils.checkInternetConnection(this)){
            dbInt.saveMetric(m, bs, devstats);
        }
    }
}
