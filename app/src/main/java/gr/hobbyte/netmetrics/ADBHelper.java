package gr.hobbyte.netmetrics;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class ADBHelper {

    public void resetStats(){
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes("dumpsys batterystats --reset\n");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();
            outputStream.close();
        } catch (IOException | InterruptedException e) {
            Log.e("IO", "Failed reseting stats", e);
        }
    }

    public BufferedReader getStats(Context context){
        try {
            String loc = context.getResources().getString(R.string.stats_file);
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes("dumpsys batterystats > "+loc+"\n");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();
            outputStream.close();
            return new BufferedReader(new FileReader(loc));
        } catch (IOException | InterruptedException e) {
            Log.e("IO", "Err", e);
        }
        return null;
    }
}
