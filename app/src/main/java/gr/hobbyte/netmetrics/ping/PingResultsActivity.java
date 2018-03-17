package gr.hobbyte.netmetrics.ping;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import gr.hobbyte.netmetrics.ADBHelper;
import gr.hobbyte.netmetrics.AdminReceiver;
import gr.hobbyte.netmetrics.Constants;
import gr.hobbyte.netmetrics.ManualInput;
import gr.hobbyte.netmetrics.R;
import gr.hobbyte.netmetrics.databinding.ActivityPingResultsBinding;

import static android.net.TrafficStats.getUidRxBytes;
import static android.net.TrafficStats.getUidTxBytes;
import static android.os.Process.myUid;

public class PingResultsActivity extends AppCompatActivity
        implements View.OnClickListener, DialogInterface.OnShowListener{

    private ActivityPingResultsBinding binding;
    private boolean isLocked = false;
    private String max;
    private String wait;
    private boolean isIPerf;
    private MaterialDialog dialog;
    private long receivedBytesInitial = 0;
    private long sentBytesInitial = 0;
    private long totalBytes;
    private String connType;
    private String connSignal;
    private long startTime;
    private String timeTotal;
    private String batteryAh;
    private String isJustPing = "false";
    private String mode = "N/A";
    private String jitter = "N/A";
    private String lostPackets = "N/A";
    private String bandwidth = "N/A";

    private static final int RESULT_ENABLE = 1;
    private DevicePolicyManager deviceManger;
    private ComponentName compName;

    /**
     * Broadcast receiver for Screen ON/OFF
     * To be used for audio playback if it's OFF so that we know the Pinging
     * has finished and stop battery monitoring asap for more accurate results
     */
    private final BroadcastReceiver myBroadcast = new BroadcastReceiver() {
        //When Event is published, onReceive method is called
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                isLocked = false;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                isLocked = true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ping_results);

        binding.buttonSaveResults.setOnClickListener(this);

        registerReceiver(myBroadcast, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(myBroadcast, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        if("IPerf".equals(getIntent().getExtras().getString("Type", "N/A")))
            iperf();
        else
            ping();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcast);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_save_results)
            moveToSave();
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        dialog = (MaterialDialog) dialogInterface;
    }

    /**
     * Called when Ping Button is clicked
     * Checks for Internet Connectivity first of all. If it exists, shows a ProgressDialog
     * and executes an AsyncTask responsible for repeating HTML downloading/pinging
     */
    private void ping(){
        devicePolicyCreate();
        boolean active = deviceManger.isAdminActive(compName);
        if (active) {
            deviceManger.lockNow();
        } else {
            askDeviceAdmin();
        }

        new ADBHelper().resetStats();
        Bundle b = getIntent().getExtras();
        String url = b.getString("url");
        max = b.getString("max");
        wait = b.getString("wait");
        isJustPing = b.getString("isJustPing");

        receivedBytesInitial = getUidRxBytes(myUid());
        sentBytesInitial = getUidTxBytes(myUid());
        PingBackgroundTask getXML = new PingBackgroundTask(this, this.getFilesDir().getPath(), this);
        showDialog();
        isIPerf = false;
        startTime = System.nanoTime();
        getXML.execute("ping", max, wait, isJustPing, url);
    }

    private void iperf(){
        devicePolicyCreate();
        boolean active = deviceManger.isAdminActive(compName);
        if (active)
            deviceManger.lockNow();
        else
            askDeviceAdmin();
        new ADBHelper().resetStats();
        receivedBytesInitial = getUidRxBytes(myUid());
        sentBytesInitial = getUidTxBytes(myUid());

        Bundle bundle = getIntent().getExtras();
        String url = bundle.getString("url");
        String bandwidthAv = bundle.getString("bandwidth");
        String time = bundle.getString("time");
        String array = bundle.getString("array");
        mode = bundle.getString("mode", "");
        String command = getResources().getString(R.string.iperf_command, url, bandwidthAv, time, array, mode);
        max = time;
        wait = "No Wait";
        isIPerf = true;
        PingBackgroundTask getXML = new PingBackgroundTask(this, this.getApplicationInfo().dataDir, this);
        showDialog();
        getXML.execute("iperf", command);
    }

    /**
     * This method creates a new ProgressDialog (using MaterialDialog library)
     * with a title, content, icon, progress bar, percentage and absolute number
     * and customised colours to go along with the rest of the app
     */
    private void showDialog(){
        // Create and show a non-indeterminate dialog with a max value of MAX (defined above)
        dialog = new MaterialDialog.Builder(this)
                .title(R.string.progress_dialog)
                .content(R.string.please_wait)
                .progress(false, Integer.parseInt(max), true)
                .backgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDialog))
                .contentColor(ContextCompat.getColor(this, R.color.colorTextSecondary))
                .titleColor(ContextCompat.getColor(this, R.color.colorText))
                .icon(ContextCompat.getDrawable(this, R.mipmap.ic_launcher))
                .show();
    }

    /**
     * This method receives an Integer from the AsyncTask (PingBackgroundTask)
     * and updates the ProgressDialog's progress (absolute number, not percentage!)
     * @param next is the receiving Integer
     */
    void updateDialog(int next){
        dialog.setProgress(next);
    }

    /**
     * This method is called from the AsyncTask to check if the user has
     * cancelled the ProgressDialog so that it stops the background process
     * @return if the ProgressDialog is cancelled
     */
    boolean isCancelled(){
        return dialog.isCancelled();
    }

    /**
     * This method is called after the AsyncTask has finished its work
     * Receives a long which represents byte size (nominal data usage)
     * of AsyncTask's executions. Sets some TextViews with Android's recorded
     * data usage and then calls a method to play a sound to alert the user
     * @param finished the number of bytes received
     */
    void finishedDialog(String[] finished){
        if(!isIPerf)
            timeTotal = nanoToMinutes(System.nanoTime() - startTime, true);
        else
            timeTotal = nanoToMinutes(Long.parseLong(max)*1000, false);

        batteryAh = new BatteryStatsReader().bufferToString(new ADBHelper().getStats(this));

        dialog.setContent(R.string.done);
        dialog.setProgress(dialog.getMaxProgress());

        long receivedBytes = getUidRxBytes(myUid()) - receivedBytesInitial;
        long sentBytes = getUidTxBytes(myUid()) - sentBytesInitial;
        totalBytes = sentBytes + receivedBytes;
        connType = new ConnectionHelperClass(this).getNetworkType();
        connSignal = new ConnectionHelperClass(this).getNetworkSignal(this);
        binding.tv1.setText(getResources().getString(R.string.usage_nominal, finished[0]));
        binding.tv2.setText(getResources().getString(R.string.usage_received, longToStringBytes(receivedBytes)));
        binding.tv3.setText(getResources().getString(R.string.usage_sent, longToStringBytes(sentBytes)));
        binding.tv4.setText(getResources().getString(R.string.usage_total, longToStringBytes(totalBytes)));//
        binding.tv5.setText(getResources().getString(R.string.connection_type, connType));//
        binding.tv6.setText(getResources().getString(R.string.signal_strength, connSignal));//
        binding.tv7.setText(getResources().getString(R.string.time_total, timeTotal));//
        binding.tv8.setText(getResources().getString(R.string.battery_mah, batteryAh));//
        if(isIPerf){
            binding.tv9.setText(getResources().getString(R.string.type, "iPerf"));
            binding.tv10.setText(getResources().getString(R.string.mode, mode.isEmpty()? "TCP" : "UDP"));
            binding.tv11.setText(getResources().getString(R.string.bandwidth, finished[1]));
            bandwidth = finished[1];
            binding.tv10.setVisibility(View.VISIBLE);
            binding.tv11.setVisibility(View.VISIBLE);
            if(!mode.isEmpty()){
                binding.tv12.setText(getResources().getString(R.string.lost_packets, finished[3]));
                binding.tv13.setText(getResources().getString(R.string.jitter, finished[2]));
                jitter = finished[2];
                lostPackets = finished[3];
                binding.tv12.setVisibility(View.VISIBLE);
                binding.tv13.setVisibility(View.VISIBLE);
            }
        } else
            binding.tv9.setText(getResources().getString(R.string.type, "Ping/HTML"));
        binding.buttonSaveResults.setVisibility(View.VISIBLE);
        pingingFinished();
    }

    /**
     * This method receives a number (supposedly of bytes) and changes it to Kilobytes,
     * Megabytes etc. Returns a formatted String with the appropriate suffix at the end
     * @param l is the number we want formatted
     * @return the aforementioned formatted String
     */
    private String longToStringBytes(Long l){
        String bytes;
        int unit = 1024;
        if (l < unit)
            bytes = l + " B";
        else {
            int exp = (int) (Math.log(l) / Math.log(unit));
            String pre = "KMGTPE".charAt(exp - 1) + "i";
            bytes = String.format(Locale.GERMAN, "%.1f %sB", l / Math.pow(unit, exp), pre);
        }
        return bytes;
    }

    private String nanoToMinutes(Long nano, boolean nanoToMilli) {
        long milli = nano;
        if(nanoToMilli)
           milli = TimeUnit.MILLISECONDS.convert(nano, TimeUnit.NANOSECONDS);
        String milliString = Long.toString(milli);
        if(milli<1000)
            return Long.toString(milli)+"ms";
        long secs = milli/1000;
        if (secs<60)
            return Long.toString(secs)+"s"+milliString.substring(milliString.length()-3, milliString.length())+"ms";
        long mins = TimeUnit.MINUTES.convert(secs, TimeUnit.SECONDS);
        secs = secs - (mins*60);
        return Long.toString(mins)+"m"+Long.toString(secs)+"s"+milliString.substring(milliString.length()-3, milliString.length())+"ms";
    }

    /**
     * A simple method playing a sound file if the screen is off (and until it's on)
     * so that the user is alerted of the finished process and can stop the battery monitor
     */
    private void pingingFinished(){
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.noti);
        if (isLocked) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(500);
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    pingingFinished(); // finish current activity
                }
            });
        }
    }

    void setSignalStrength(String signal){
        binding.tv6.setText(getResources().getString(R.string.signal_strength, signal));
        connSignal = signal;
    }

    private void moveToSave(){

        Bundle bundle = new Bundle();
        bundle.putString(Constants.CONN_TYPE, connType);
        bundle.putString(Constants.CONN_SIGNAL, connSignal);
        bundle.putString(Constants.PING_NUM, max);
        bundle.putString(Constants.PING_BYTES, longToStringBytes(totalBytes/Long.parseLong(max)));
        bundle.putString(Constants.PING_WAIT, wait);
        bundle.putString(Constants.TOTAL_BYTES, longToStringBytes(totalBytes));
        bundle.putString(Constants.TOTAL_TIME, timeTotal);
        bundle.putString(Constants.BATTERY_USED, batteryAh);
        bundle.putString(Constants.IPERF_BANDWIDTH, bandwidth);
        bundle.putString(Constants.IPERF_JITTER, jitter);
        bundle.putString(Constants.IPERF_LOST_PACKETS, lostPackets);
        if(isIPerf) {
            bundle.putString(Constants.USAGE_MODE, "iPerf");
            if (!mode.isEmpty()) {
                bundle.putString(Constants.IPERF_MODE, "UDP");
            } else
                bundle.putString(Constants.IPERF_MODE, "TCP");
        } else {
            bundle.putString(Constants.IPERF_MODE, "N/A");
            bundle.putString(Constants.USAGE_MODE, "true".equals(isJustPing) ? "Ping" : "HTML");
        }
        Intent intent = new Intent(this, ManualInput.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void devicePolicyCreate() {
        deviceManger = (DevicePolicyManager)getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, AdminReceiver.class);
    }

    private void askDeviceAdmin() {
        Intent intent = new Intent(DevicePolicyManager
                .ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Additional text explaining why this needs to be added.");
        startActivityForResult(intent, RESULT_ENABLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_ENABLE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i("DeviceAdminSample", "Admin enabled!");
                deviceManger.lockNow();
            } else {
                Log.i("DeviceAdminSample", "Admin enable FAILED!");
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
