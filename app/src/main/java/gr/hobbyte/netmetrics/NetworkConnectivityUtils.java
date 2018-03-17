package gr.hobbyte.netmetrics;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * This class will be used as a placeholder for various utils we might need.
 */
public class NetworkConnectivityUtils {

    /**
     * This method check for existent Internet Connectivity using ConnectivityManager
     * Then it creates and shows a Toast with the result and returns a boolean with existence
     * @return if there is a Connection or not
     */
    public boolean checkInternetConnection(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (null == ni) {
            Toast.makeText(c, "No internet connection detected", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }
}
