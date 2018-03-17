package gr.hobbyte.netmetrics.ping;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;

import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;
import gr.hobbyte.netmetrics.MetricViewer;
import gr.hobbyte.netmetrics.NetworkConnectivityUtils;
import gr.hobbyte.netmetrics.R;
import gr.hobbyte.netmetrics.databinding.ActivityPingBinding;
import gr.hobbyte.netmetrics.fragments.FragmentIPerf;
import gr.hobbyte.netmetrics.fragments.FragmentPing;

public class PingActivity extends AppCompatActivity
        implements View.OnClickListener, RadioRealButtonGroup.OnClickedButtonPosition, Animator.AnimatorListener{

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 2;

    private boolean permissionLoc = false;
    private boolean permissionRead = false;
    private Fragment ping;
    private Fragment html;
    private Fragment iperf;
    private int active;
    private Techniques direction;
    private ActivityPingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ping);

        ping = new FragmentPing();
        html = new FragmentPing();
        iperf = new FragmentIPerf();

        ((FragmentPing)ping).setType("Ping");
        ((FragmentPing)html).setType("HTML");
        binding.buttonPing.setOnClickListener(this);
        binding.buttonDefault.setOnClickListener(this);

        binding.radioGroup.setOnClickedButtonPosition(this);
        getFragmentManager().beginTransaction().replace(R.id.FragmentContainer, ping).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_ping:
                checkInternetConnection();
                break;
            case R.id.button_default:
                defaultValues();
                break;
            default:
                break;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_showmetrics:
                Intent i = new Intent(this, MetricViewer.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickedButtonPosition(final int position) {
        if(active==position)
            return;
        direction = active < position ? Techniques.SlideInRight : Techniques.SlideInLeft;
        Techniques directionOut = active < position ? Techniques.SlideOutLeft : Techniques.SlideOutRight;
        active = position;

        YoYo.with(directionOut)
                .duration(250)
                .interpolate(new AccelerateDecelerateInterpolator())
                .withListener(this)
                .playOn(binding.FragmentContainer);
    }

    /**
     * Called when Ping Button is clicked
     * Checks for Internet Connectivity first of all. If it exists, shows a ProgressDialog
     * and executes an AsyncTask responsible for repeating HTML downloading/pinging
     */
    private void ping(){
        Bundle bundle = new Bundle();
        Fragment f = getFragmentManager().findFragmentById(R.id.FragmentContainer);
        if(f instanceof FragmentPing)
            bundle = ((FragmentPing)f).getAttributes();
        else if(f instanceof FragmentIPerf)
            bundle = ((FragmentIPerf)f).getAttributes();
        Intent intent = new Intent(this, PingResultsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void defaultValues(){
        Fragment f = getFragmentManager().findFragmentById(R.id.FragmentContainer);
        if(f instanceof FragmentPing)
            ((FragmentPing)f).defaultValues();
        else if(f instanceof FragmentIPerf)
            ((FragmentIPerf)f).defaultValues();
    }

    /**
     * This method check for existent Internet Connectivity using NetworkConnectivityUtils Class
     * and if there is, it proceeds as nessesary
     */
    private void checkInternetConnection() {
        if(new NetworkConnectivityUtils().checkInternetConnection(this)){
            if ( Build.VERSION.SDK_INT >= 23)
                requestPermission();
            else
                ping();
        }
    }

    /**
     * All the next methods are for Permission requirement and handling
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                permissionLoc = true;
                checkPermissions();
            }
            else
                Snackbar.make(findViewById(android.R.id.content),"Need your location to find the Signal Strength!", Snackbar.LENGTH_LONG).show();
        }
        if (requestCode == MY_PERMISSIONS_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                permissionRead = true;
                checkPermissions();
            }
            else
                Snackbar.make(findViewById(android.R.id.content),"Need your storage to access Battery Stats!", Snackbar.LENGTH_LONG).show();
        }
    }

    private void checkPermissions(){
        if(permissionLoc&&permissionRead)
            ping();
    }

    private void requestPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else
            permissionLoc = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE },
                    MY_PERMISSIONS_REQUEST_STORAGE);
        } else
            permissionRead = true;
        checkPermissions();
    }

    /**
     * Listeners for the animation when changing fragments
     * @param animation we are listening to
     */
    @Override
    public void onAnimationStart(Animator animation) {
        //No need for implementation
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        YoYo.with(direction)
                .duration(250)
                .interpolate(new AccelerateDecelerateInterpolator())
                .playOn(binding.FragmentContainer);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if(active == 0){
            ft.replace(R.id.FragmentContainer, ping).commit();
        } else if(active == 1){
            ft.replace(R.id.FragmentContainer, html).commit();
        } else if(active == 2){
            ft.replace(R.id.FragmentContainer, iperf).commit();
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        //No need for implementation
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        //No need for implementation
    }
}
