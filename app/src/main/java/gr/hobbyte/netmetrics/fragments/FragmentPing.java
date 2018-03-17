package gr.hobbyte.netmetrics.fragments;

import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import gr.hobbyte.netmetrics.R;
import gr.hobbyte.netmetrics.databinding.FragmentPingBinding;

public class FragmentPing extends Fragment{

    /**
     * MAX is the number of HTML downloads or Pings we do to a Website
     * URL_ADDRESS array to be used for quicker URL changes
     * MAX can be changed from the second EditText
     */
    private static final String DEFAULT_MAX = "10000";
    private static final String DEFAULT_URL = "http://en.wikipedia.org/";
    private static final String DEFAULT_WAIT = "100";
    private String type;

    private FragmentPingBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_ping, container, false);
        return binding.getRoot();
    }

    public void defaultValues(){
        binding.etTries.setText(DEFAULT_MAX);
        binding.etUrl.setText(DEFAULT_URL);
        binding.etWait.setText(DEFAULT_WAIT);
    }

    public void setType(String type){
        this.type = type;
    }

    public Bundle getAttributes (){
        Bundle bundle = new Bundle();
        String max = binding.etTries.getEditText().getText().toString().length() > 0 ? binding.etTries.getEditText().getText().toString() : DEFAULT_MAX;
        String url = binding.etUrl.getEditText().getText().toString().length()>0 ? binding.etUrl.getEditText().getText().toString() : DEFAULT_URL;
        String wait = binding.etWait.getEditText().getText().toString().length()>0 ? binding.etWait.getEditText().getText().toString() : DEFAULT_WAIT;
        bundle.putString("url", url);
        bundle.putString("max", max);
        bundle.putString("wait", wait);
        bundle.putString("Type", type);
        return bundle;
    }
}
