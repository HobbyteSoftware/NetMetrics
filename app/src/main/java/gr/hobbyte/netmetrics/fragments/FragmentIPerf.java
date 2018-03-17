package gr.hobbyte.netmetrics.fragments;

import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import gr.hobbyte.netmetrics.R;
import gr.hobbyte.netmetrics.databinding.FragmentIperfBinding;

public class FragmentIPerf extends Fragment implements Switch.OnCheckedChangeListener{

    /**
     * MAX is the number of HTML downloads or Pings we do to a Website
     * URL_ADDRESS array to be used for quicker URL changes
     * MAX can be changed from the second EditText
     */
    private static final String DEFAULT_URL = "83.212.84.149";
    private static final String DEFAULT_TIME = "60";
    private static final String DEFAULT_BANDWIDTH = "1M";
    private static final String DEFAULT_LENGTH = "4096";
    private static final String TYPE = "IPerf";

    private FragmentIperfBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_iperf, container, false);
        binding.switchUDP.setOnCheckedChangeListener(this);
        return binding.getRoot();
    }

    public void defaultValues(){
        binding.etUrl.setText(DEFAULT_URL);
        binding.etBand.setText(DEFAULT_BANDWIDTH);
        binding.etTime.setText(DEFAULT_TIME);
        binding.etArray.setText(DEFAULT_LENGTH);
        binding.switchUDP.setChecked(false);
    }

    public Bundle getAttributes (){
        Bundle bundle = new Bundle();
        String url = binding.etUrl.getEditText().getText().toString().length()>0 ? binding.etUrl.getEditText().getText().toString() : DEFAULT_URL;
        String bandwidth = binding.etBand.getEditText().getText().toString().length() > 0 ? binding.etBand.getEditText().getText().toString() : DEFAULT_BANDWIDTH;
        String time = binding.etTime.getEditText().getText().toString().length()>0 ? binding.etTime.getEditText().getText().toString() : DEFAULT_TIME;
        String array = binding.etArray.getEditText().getText().toString().length()>0 ? binding.etArray.getEditText().getText().toString() : DEFAULT_LENGTH;
        bundle.putString("url", url);
        bundle.putString("bandwidth", bandwidth);
        bundle.putString("time", time);
        bundle.putString("array", array);
        if(binding.switchUDP.isChecked())
            bundle.putString("mode", "-u");
        bundle.putString("Type", TYPE);
        return bundle;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(compoundButton == binding.switchUDP){
            if(b)
                binding.switchUDP.setText(getResources().getString(R.string.iperf_mode_udp));
            else
                binding.switchUDP.setText(getResources().getString(R.string.iperf_mode));
        }
    }
}
