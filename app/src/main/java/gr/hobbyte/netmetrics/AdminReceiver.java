package gr.hobbyte.netmetrics;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AdminReceiver extends DeviceAdminReceiver {
    private void showToast(Context context, CharSequence msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, "Device Admin: enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "Don't. Please.";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, "Device Admin: disabled");
    }
}
