package gr.hobbyte.netmetrics.database;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DatabaseReference;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import gr.hobbyte.netmetrics.R;

/**
 * Story:
 * This is a REALTIME database, so we can get data when something
 * is changed. Not before. (For example get latest Metric id so we
 * can save on the next one)
 * We can solve this problem with the rest API.
 * This code gets a json file which contains only one integer
 * (aka latest id) because I like things to be simple! ;-P
 * The code takes that, increases it by one so the next one can
 * save immediately, and saves the data to the database.
 */
class DataSaveThread extends AsyncTask<String, Integer, Integer>{

    private static final String LOG_TAG = "DataSaveThread";
    private final DatabaseReference rootEl; //database ref

    //Objects to save.
    private final NetworkStats networkStats;
    private final BatteryStats batteryStats;
    private final DeviceStats deviceStats;

    //Needed to show dialog.
    private final Context context;

    private MaterialDialog dialog;

    //Constructor to pass needed data.
    DataSaveThread(Context context, DatabaseReference rootEl, NetworkStats networkStats, BatteryStats batteryStats, DeviceStats deviceStats){
        this.rootEl = rootEl;
        this.batteryStats = batteryStats;
        this.networkStats = networkStats;
        this.deviceStats = deviceStats;
        this.context = context;
    }

    /**
     * Runs before doInBackground() and shows progressDialog.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = spawnSaveDialog();
    }

    @Override
    protected Integer doInBackground(String[] objects) {
        int posToSave;
        String str="https://netmetrics-63675.firebaseio.com/Metrics/count.json";

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(str).openConnection().getInputStream()))){
            StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            posToSave = Integer.parseInt(stringBuffer.toString());
            Log.d(LOG_TAG, "Thread is going to save on id=" + posToSave);

            //increase counter so next one can save.
            rootEl.child("Metrics").child("count").setValue(posToSave+1);
            publishProgress(25);
            Log.d(LOG_TAG, "Thread increased id in database to id=" + (posToSave + 1));

            DatabaseReference saveLoc = rootEl.child("Metrics").child(Integer.toString(posToSave));
            saveLoc.child("DeviceStats").setValue(deviceStats);
            publishProgress(50);
            saveLoc.child("BatteryStats").setValue(batteryStats);
            publishProgress(75);
            saveLoc.child("NetworkStats").setValue(networkStats);
            publishProgress(100);

            Log.d(LOG_TAG, "Thread finished saving data. I hope...");
        }
        catch(IOException ex) {
            Log.e(LOG_TAG, "WTF has happened?! Attaching stacktrace: \n", ex);
            return -1;
        }
        return  0;
    }

    /**
     * Runs at the end of the AsyncTask and dismisses dialog.
     * @param o is object :P
     */
    @Override
    protected void onPostExecute(Integer o) {
        dialog.dismiss();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        dialog.setProgress(values[0]);
    }

    /**
     * Returns a nice looking dialog and shows it.
     * @return MaterialDialog.
     */
    private MaterialDialog spawnSaveDialog(){
        return new MaterialDialog.Builder(context)
                .title(R.string.progress_dialog_saving_data)
                .content(R.string.please_wait)
                .progress(false, 100, true)
                .backgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDialog))
                .contentColor(ContextCompat.getColor(context, R.color.colorTextSecondary))
                .titleColor(ContextCompat.getColor(context, R.color.colorText))
                .icon(ContextCompat.getDrawable(context, R.mipmap.ic_launcher))
                .show();
    }
}