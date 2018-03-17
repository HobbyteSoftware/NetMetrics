package gr.hobbyte.netmetrics.database;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseInterface {
    private final DatabaseReference rootEl;
    private final Context context; //used to get ANDROIDID.

    public DatabaseInterface(Context context) {
        this.context = context;
        this.rootEl = FirebaseDatabase.getInstance().getReference();
    }

    /***
     * Uses DataSaveThread to save statistics
     * @param networkStats net stats to be saved
     * @param batteryStats battery stats to be saved.
     */
    public void saveMetric(NetworkStats networkStats, BatteryStats batteryStats){

        DataSaveThread dct = new DataSaveThread(context, rootEl, networkStats, batteryStats, getDeviceStats());
        dct.execute();
    }

    /***
     * Uses DataSaveThread to save statistics
     * @param networkStats net stats to be saved
     * @param batteryStats battery stats to be saved.
     * @param deviceStats device info to be saved
     */
    public void saveMetric(NetworkStats networkStats, BatteryStats batteryStats, DeviceStats deviceStats){

        DataSaveThread dct = new DataSaveThread(context, rootEl, networkStats, batteryStats, deviceStats);
        dct.execute();
    }

    /**
     * Gets deviceStats object.
     * @return DeviceStats.
     */
    private DeviceStats getDeviceStats(){

        DeviceStats stats = new DeviceStats();
        stats.setDeviceBrand(Build.MANUFACTURER);
        stats.setDeviceID(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        stats.setDeviceModel(Build.MODEL);
        stats.setDeviceVersion(Build.VERSION.RELEASE + Build.VERSION.SDK_INT);
        return stats;
    }
}
