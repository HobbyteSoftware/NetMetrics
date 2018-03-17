package gr.hobbyte.netmetrics.ping;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

class ConnectionHelperClass {

    private static final int UNKNOW_CODE = 99;

    private final Context context;
    private final NetworkInfo info;
    private PingResultsActivity act;
    private MyPhoneStateListener myPhoneStateListener;
    private TelephonyManager telephonyManager;

    ConnectionHelperClass(Context context){
        this.context = context;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        info = cm.getActiveNetworkInfo();
    }

    /**
     * Find and return the type of connection the phone has
     * Taken from StackOverflow as-is
     * @return the type in String format
     */
    String getNetworkType() {
        if(info==null || !info.isConnected())
            return "-"; //not connected
        if(info.getType() == ConnectivityManager.TYPE_WIFI)
            return "WIFI";
        if(info.getType() == ConnectivityManager.TYPE_MOBILE){
            int networkType = info.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "4G";
                default:
                    return "?";
            }
        }
        return "?";
    }

    String getNetworkSignal(PingResultsActivity act) {
        this.act = act;
        int signamDBm;
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            signamDBm = wifiManager.getConnectionInfo().getRssi();
        } else {
            telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

            signamDBm = 0;
            myPhoneStateListener = new MyPhoneStateListener();
            telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
        return Integer.toString(signamDBm);
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        /* Get the Signal strength from the provider, each time there is an update */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            if (null != signalStrength && signalStrength.getGsmSignalStrength() != UNKNOW_CODE) {
                int tempSignal = signalStrength.getGsmSignalStrength();
                act.setSignalStrength(Integer.toString(tempSignal*2-113));
                telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            }
        }
    }
}
