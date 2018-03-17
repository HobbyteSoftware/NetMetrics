package gr.hobbyte.netmetrics.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;

import org.json.*;
import gr.hobbyte.netmetrics.adapters.RecyclerInfoAdapter;

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
public class DataFetchThread extends AsyncTask<String, String, Integer>{

    private static final String LOG_TAG = "DataFetchThread";
    private static final String BATTERY = "BatteryStats";
    private static final String DEVICE = "DeviceStats";
    private static final String NETWORK = "NetworkStats";

    //Needed to show dialog.
    private final Context context;

    private ArrayList<String[]> data; //data to be swapped to adapter
    private final RecyclerInfoAdapter arrayAdapter; //array adapter

    //Constructor to pass array adapter and context.
    public DataFetchThread(Context c, RecyclerInfoAdapter adapter){
        this.context = c;
        this.arrayAdapter = adapter;
    }

    @Override
    protected Integer doInBackground(String[] objects) {

        String str="https://netmetrics-63675.firebaseio.com/Metrics.json"; //use Rest API to fetch all data/

        //Prepare to download
        URLConnection urlConn;
        BufferedReader bufferedReader;
        try
        {
            URL url = new URL(str);
            urlConn = url.openConnection();
            bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            String line; //read file line by line
            StringBuilder fileContents = new StringBuilder(); //concat everything to this
            while ((line = bufferedReader.readLine()) != null) {
                fileContents.append(line);
            }
            bufferedReader.close();
            //No internet connectivity needed beyond this point.

            //Create the JSON object
            JSONObject obj = new JSONObject(fileContents.toString());
            int count = Integer.parseInt(obj.getString("count"));//get number of metrics

            data = new ArrayList<>(count); //init arraylist
            String[] temp;
            //Start adding data to ArrayList
            for(int metrics = 0; metrics < count; metrics++){
                temp = new String[16];
                temp[0] = overrideStr(obj, BATTERY, "batteryUsed", metrics);
                temp[1] = overrideStr(obj, DEVICE, "deviceBrand", metrics);
                temp[2] = overrideStr(obj, DEVICE, "deviceID", metrics);
                temp[3] = overrideStr(obj, DEVICE, "deviceModel", metrics);
                temp[4] = overrideStr(obj, DEVICE, "deviceVersion", metrics);
                temp[5] = overrideStr(obj, NETWORK, "connType", metrics);
                temp[6] = overrideStr(obj, NETWORK, "mbsused", metrics);
                temp[7] = overrideStr(obj, NETWORK, "signalStrength", metrics);
                temp[8] = overrideStr(obj, NETWORK, "timeBetweenPings", metrics);
                temp[9] = overrideStr(obj, NETWORK, "usageType", metrics);
                temp[10] = overrideStr(obj, NETWORK, "numberOfPings", metrics);
                temp[11] = overrideStr(obj, NETWORK, "time", metrics);
                temp[12] = overrideStr(obj, NETWORK, "mode", metrics);
                temp[13] = overrideStr(obj, NETWORK, "jitter", metrics);
                temp[14] = overrideStr(obj, NETWORK, "lostPackets", metrics);
                temp[15] = overrideStr(obj, NETWORK, "bandwidth", metrics);
                data.add(temp);
            }
            Collections.reverse(data); //reverse data
        //Catch some errors :-(
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Could not create JSON object OR some value not found while reading the file", e);
            Toast.makeText(context, "Could not read data from server", Toast.LENGTH_LONG).show();
            return -1;
        } catch(IOException a){
            Log.e(LOG_TAG, "Cound not download json file from firebase server.", a);
            Toast.makeText(context, "Could not read data from server", Toast.LENGTH_LONG).show();
            return -2;
        }
        return  0;
    }

    /**
     * Runs at the end of the AsyncTask and dismisses dialog.
     * @param i an object
     * (do we REALLY need a comment explaining what onPostExecute does here? :P )
     */
    @Override
    protected void onPostExecute(Integer i) {
        arrayAdapter.changeData(data); //add new data to adapter
    }

    /**
     * Fetches data from JSON. Prevent app crash when String not found! :-)
     * @param json JSONObject type
     * @param type BATTERY, DEVICE, NETWORK ONLY.
     * @param value name of value saved in db.
     * @param counter used in doInBackground()
     * @return string result.
     */
    private String overrideStr(JSONObject json, String type, String value, int counter){
        try{
            return json.getJSONObject(Integer.toString(counter)).getJSONObject(type).getString(value);
        }catch(JSONException exception){
            Log.e(LOG_TAG, "JSON object '" + type + "." + value + "' not found.", exception);
            return "Not exist";
        }
    }
}