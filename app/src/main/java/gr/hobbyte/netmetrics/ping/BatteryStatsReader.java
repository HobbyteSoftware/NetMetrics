package gr.hobbyte.netmetrics.ping;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;

class BatteryStatsReader {

    String bufferToString(BufferedReader br){
        String line;
        try {
            while ((line = br.readLine()) != null){
                if(!line.contains("Computed drain"))
                    continue;
                String[] ar = line.split(":");
                if(ar.length>1){
                    return ar[2].substring(1,ar[2].length()-14);
                } else {
                    return ar[0];
                }
            }
            br.close();
        } catch (IOException e) {
            Log.e("IO", "Battery reading failed", e);
        }
        return "Small Sample - N/A";
    }
}
