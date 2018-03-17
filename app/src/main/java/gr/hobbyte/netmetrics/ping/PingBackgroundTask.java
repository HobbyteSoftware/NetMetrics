package gr.hobbyte.netmetrics.ping;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import gr.hobbyte.netmetrics.R;

class PingBackgroundTask extends AsyncTask<String, Integer, String[]> {
    private static final String LOGCAT_TAG = "PingBackgroundTask";
    private final PingResultsActivity pingResAct;
    private final String iperfLocation;
    private Context context;

    PingBackgroundTask(PingResultsActivity pingResAct, String path, Context context) {
        this.pingResAct = pingResAct;
        iperfLocation = path+"/iperf";
        this.context = context;
    }

    @Override
    protected String[] doInBackground(String[] objects) {
        if("ping".equals(objects[0]))
            return ping(objects);
        else
            return iperf(objects);
    }

    @Override
    protected void onPostExecute(String[] result) {
        pingResAct.finishedDialog(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        pingResAct.updateDialog(values[0]);
    }

    private String[] ping(String[] objects){
        long l = 0;
        int connections = Integer.parseInt(objects[1]);
        int wait = Integer.parseInt(objects[2]);
        int count = 0;
        while(count<connections) {
            if(Boolean.parseBoolean(objects[3]))
                justPing(objects[4]);
            else
                l = downloadHTML(objects[4]);
            count++;
            if(count%10==0 || count==connections){
                publishProgress(count);
            }
            try {
                Thread.sleep(wait);
            } catch (IllegalArgumentException | InterruptedException e) {
                Log.e(LOGCAT_TAG, "Interrupted", e);
                Logger.addRecordToLog(LOGCAT_TAG+" Interrupted "+e.toString(), pingResAct);
            }
            if(pingResAct.isCancelled()&&count<connections){
                cancel(true);
                count = connections;
            }
        }
        return new String[]{Long.toString(l)};
    }

    private void justPing(String url){
        Runtime runtime = Runtime.getRuntime();
        try {
            Process proc = runtime.exec("ping -c 1 " + url);
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            Log.e(LOGCAT_TAG, "IO-IllegalArgException", e);
            Logger.addRecordToLog(LOGCAT_TAG+" IO-IllegalArgException "+e.toString(), pingResAct);
        }
    }

    private long downloadHTML(String url){
        Connection.Response response;
        try {
            response = Jsoup.connect(url).execute();
        } catch (IOException e) {
            Log.e(LOGCAT_TAG, "IO", e);
            Logger.addRecordToLog(LOGCAT_TAG+" IO "+e.toString(), pingResAct);
            return 0;
        }
        long l = response.bodyAsBytes().length;
        for (Map.Entry<String, String> entry : response.headers().entrySet()) {
            l += entry.getValue().getBytes().length;
        }
        return l;
    }

    private String[] iperf(String[] objects){
        ConnectionHelperClass conHelper = new ConnectionHelperClass(context);
        try {
            initializeIPerf();
            Process process;
            //The user input for the parameters is parsed into a string list as required from the ProcessBuilder Class.
            String[] commands = objects[1].split(" ");
            List<String> commandList = new ArrayList<>(Arrays.asList(commands));

            //If the first parameter is "iperf", it is removed
            if ("iperf".equals(commandList.get(0))) {
                commandList.remove(0);
            }
            int extraTime = Integer.parseInt(commandList.get(5)) - 60;
            if(Integer.parseInt(commandList.get(5)) > 50){
                commandList.set(5, Integer.toString(60));
            }
            for(String s : commandList)
                System.err.println(s);
            //The execution command is added first in the list for the shell interface.
            commandList.add(0, iperfLocation);
            //The process is now being run with the verified parameters.

            process = new ProcessBuilder().command(commandList).redirectErrorStream(true).start();

            //Get process input stream.
            InputStream prInputStream = process.getInputStream();

            //A buffered output of the stdout is being initialized so the iperf output could be displayed on the screen.
            BufferedReader reader = new BufferedReader(new InputStreamReader(prInputStream));

            int read;
            //The output text is accumulated into a string buffer and published to the GUI
            char[] buffer = new char[10240];
            StringBuilder output = new StringBuilder();
            int count = 0;
            boolean isCounting = false;
            String lastOutput = "Err";
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
                //This is used to pass the output to the thread running the GUI, since this is separate thread.
                lastOutput = output.toString();
                Log.d("PingBGTask-iPerf", lastOutput);
                if(lastOutput.contains("Interval")){
                    if(isCounting)
                        break;
                    isCounting = true;
                }
                if(isCounting)
                    count++;
                publishProgress(count);
                output.delete(0, output.length());
            }
            if("err".equals(lastOutput))
                return new String[0];
            String[] values = getFinalResults(lastOutput);
            reader.close();
            process.destroy();

            rerunIperf(commandList, extraTime, count);

            return values;
        }
        catch (IOException e) {
            Log.e("IPerfTask", "Error: ", e);
            Logger.addRecordToLog("IPerfTask Error: "+e.toString(), pingResAct);
        }

        return new String[0];
    }

    /**
     * Checks if Iperf binary file exists is app's private space. If it does not
     * it copies the file, gives correct permissions and runs, else it just runs!
     */
    private void initializeIPerf () {
        pingResAct.getDir("abigjugfolderth", Context.MODE_PRIVATE);
        InputStream in;
        //The asset "iperf" (from assets folder) inside the activity is opened for reading.
        in = pingResAct.getResources().openRawResource(R.raw.iperf3);
        try {
            //Checks if the file already exists, if not copies it.
            FileInputStream test = new FileInputStream(iperfLocation); //if exception is thrown go to catch and copy!
            test.close();
        } catch (FileNotFoundException fnf) {
            Log.e("PBT", "File not found...", fnf);
            Logger.addRecordToLog("PBT File not found: "+fnf.toString(), pingResAct);
            try(FileOutputStream out = new FileOutputStream(iperfLocation, false)) {
                // Transfer bytes from "in" to "out"
                byte[] buf = new byte[2048];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                //After the copy operation is finished, we give execute permissions to the "iperf" executable using shell commands.
                Process processChmod = Runtime.getRuntime().exec("/system/bin/chmod 744 " + iperfLocation);
                // Executes the command and waits untill it finishes.
                processChmod.waitFor();
                in.close();
            } catch (IOException | InterruptedException e) {
                Log.e("PBT", "Errooooor", e);
                Logger.addRecordToLog("PBT Errooooor "+e.toString(), pingResAct);
            }
        } catch (IOException e){
            Log.e("IO", "Error in closing", e);
            Logger.addRecordToLog("IO Error in closing "+e.toString(), pingResAct);
        }
    }

    private String[] getFinalResults(String lastOutput){
        String[] values = new String[4];
        String[] lines = lastOutput.split("\n");
        int j = 0;
        for(String s:lines){
            j++;
            if(s.contains("Interval"))
                break;
        }
        try {
            String received = lines[j].split("sec")[1].trim().split("Bytes")[0];
            values[0] = received + "iB";
            values[1] = lines[j].split("Bytes")[1].trim().split("bits/sec")[0];
            if (lastOutput.contains("Jitter")) {
                values[2] = lines[j].split("bits/sec")[1].trim().split("ms")[0] + "ms";
                values[3] = lines[j].split("ms")[1].trim();
            }
        } catch (ArrayIndexOutOfBoundsException ex){
            Log.e("PBT", "Array out of bounds, huh?", ex);
            Log.d("PBT", "Hmm "+lastOutput);
            Logger.addRecordToLog("Array out of bounds: "+ex.toString()+"\n\n AND: "+lastOutput, pingResAct);
        }
        return values;
    }

    /**
     * This function restarts IPerf, taking into account
     * time elapsed.
     * @param commands iperf command
     *
     */
    private void rerunIperf(List<String> commands, int extraTime, int count){
        int runCount = 0;
        int newCount = count;
        Process process;
        long newProcTime = (long) 0.0;//will be used in while loop.
        long timeLeft = extraTime * 1000;
        Log.d("IPerfRestart", "Time left: " + timeLeft);

        if(timeLeft > 0) { //just a failsafe. U never know.
            while (timeLeft > 0) { //if positive re-execute.
                    runCount++;
                    commands.set(6, Integer.toString((int)timeLeft/1000)); //change seconds in command. Fuck maths.
                //Start new iperf process with same variables except time.
                try {
                    Log.d("IPerfRestart", "Restarting IPerf. Run: " + runCount + ".");
                    for(String s : commands)
                        System.out.println(s);
                    newProcTime = System.currentTimeMillis(); //start counting how many seconds this run will be
                    process = new ProcessBuilder().command(commands).redirectErrorStream(true).start();//execute
                    //Get process input stream.
                    InputStream prInputStream = process.getInputStream();

                    //Progress Bar hotfix
                    //A buffered output of the stdout is being initialized so the iperf output could be displayed on the screen.
                    BufferedReader reader = new BufferedReader(new InputStreamReader(prInputStream));

                    int read;
                    //The output text is accumulated into a string buffer and published to the GUI
                    char[] buffer = new char[10240];
                    StringBuilder output = new StringBuilder();

                    boolean isCounting = false;
                    String lastOutput = "Err";

                    while ((read = reader.read(buffer)) > 0) {
                        output.append(buffer, 0, read);
                        //This is used to pass the output to the thread running the GUI, since this is separate thread.
                        lastOutput = output.toString();
                        Log.d("PingBGTask-iPerf", lastOutput);
                        if(lastOutput.contains("Interval")){
                            if(isCounting)
                                break;
                            isCounting = true;
                        }
                        if(isCounting)
                            newCount++;
                        publishProgress(newCount);
                        output.delete(0, output.length());
                    }
                    //end progress bar hotfix
                    process.waitFor(); //wait for it to complete
                    newProcTime = System.currentTimeMillis() - newProcTime; //refresh time

                    Log.d("IPerfRestart", "Run " + runCount + " finished at " + newProcTime + "ms.");
                } catch (IOException e) {
                    Log.e("IPerfRestart", "An IOException occurred", e);
                } catch (InterruptedException e) {
                    Log.e("IPerfRestart", "An InterruptedException occurred", e);
                }

                timeLeft = timeLeft - newProcTime; //find new time left.
                Log.d("IPerfRestart", "Time left after run " + runCount + "ms is " + timeLeft + "ms");
            }
        }
    }
}
