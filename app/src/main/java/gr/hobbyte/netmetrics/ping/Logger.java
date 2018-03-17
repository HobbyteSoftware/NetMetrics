package gr.hobbyte.netmetrics.ping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;

import gr.hobbyte.netmetrics.R;

/**
 * Definition - Logger file use to keep Log info to external SD with the simple method
 */

class Logger {
    private static final String FILENAME = "ProjectName_Log";
    private static String state = Environment.getExternalStorageState();

    private Logger(){}

    static void addRecordToLog(String message, Context context) {
        File dir = new File("/sdcard/Files/NetMetrics");
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if(!dir.exists() && !dir.mkdirs())
                return;
            File logFile = new File("/sdcard/Files/NetMetrics/"+ FILENAME +".txt");
            try {
                if(logFile.exists() && !logFile.delete())
                    throw new IOException();
                Log.d("File created ", "File created ");
                if(!logFile.exists() && !logFile.createNewFile())
                    throw new IOException();
            } catch (IOException e) {
                Log.e("Logger", "Error creating file", e);
            }
            //BufferedWriter for performance, true to set append to file flag
            try(BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true))) {
                buf.write(message + "\r\n");
                buf.newLine();
                buf.flush();
                buf.close();
            } catch (IOException e) {
                Log.e("Logger", "Error writing file", e);
            }
        }
        errorNotify(context);
    }

    private static void errorNotify(Context context){
        final MediaPlayer mp = MediaPlayer.create(context, R.raw.err);
        mp.start();
        mp.start();
    }
}
