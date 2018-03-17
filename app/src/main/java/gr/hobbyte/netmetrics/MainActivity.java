package gr.hobbyte.netmetrics;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import gr.hobbyte.netmetrics.databinding.ActivityMainBinding;
import gr.hobbyte.netmetrics.ping.PingActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.buttonPing.setOnClickListener(this);
        binding.buttonMetrics.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_ping:
                ping();
                break;
            case R.id.button_metrics:
                showMeTheMetrics();
                break;
            default:
                break;
        }
    }

    private void showMeTheMetrics() {
        NetworkConnectivityUtils utils = new NetworkConnectivityUtils();
        if(utils.checkInternetConnection(this)) {
            Intent intent = new Intent(this, MetricViewer.class);
            startActivity(intent);
        }
    }

    private void ping(){
        Intent intent = new Intent(this, PingActivity.class);
        startActivity(intent);
    }
}