package gr.hobbyte.netmetrics.adapters;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

import gr.hobbyte.netmetrics.R;

/**
 * This adapter holds info for the MetricViewer Activity.
 */
public class RecyclerInfoAdapter extends RecyclerView.Adapter<RecyclerInfoAdapter.MetricViewHolder> {

    /**
     * This ViewHolder binds the card elements into java objects
     */
    static class MetricViewHolder extends RecyclerView.ViewHolder {

        final CardView cv;
        final TextView model;
        final TextView manu;
        final TextView andid;
        final TextView andver;
        final TextView conntype;
        final TextView dataused;
        final TextView time;
        final TextView batBef;
        final TextView signalStrength;
        final TextView timeBetweenPings;
        final TextView pingBytes;
        final TextView jitter;
        final TextView mode;
        final TextView packetsLost;
        final TextView bandwidth;

        MetricViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            model = (TextView) itemView.findViewById(R.id.device_model_textview);
            manu = (TextView) itemView.findViewById(R.id.device_manufacturer_textview);
            andid = (TextView) itemView.findViewById(R.id.device_android_id);
            conntype = (TextView) itemView.findViewById(R.id.conn_type_textview);
            dataused = (TextView) itemView.findViewById(R.id.data_used_textview);
            time = (TextView) itemView.findViewById(R.id.time_textview);
            batBef = (TextView) itemView.findViewById(R.id.battery_before_textview);
            signalStrength = (TextView) itemView.findViewById(R.id.signal_strength_textview);
            andver = (TextView) itemView.findViewById(R.id.device_android_version);
            timeBetweenPings = (TextView) itemView.findViewById(R.id.timePings_textview);
            pingBytes = (TextView) itemView.findViewById(R.id.pingBytes_textview);
            jitter = (TextView) itemView.findViewById(R.id.jitter_textview);
            mode = (TextView) itemView.findViewById(R.id.mode_textview);
            packetsLost = (TextView) itemView.findViewById(R.id.lostpackets_textview);
            bandwidth = (TextView) itemView.findViewById(R.id.bandwidth_textview);
        }
    }

    private List<String[]> metrics; //data to be viewed

    public RecyclerInfoAdapter(List<String[]> metrics){
        this.metrics = metrics;
    }

    @Override
    public MetricViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        return new MetricViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MetricViewHolder metricViewHolder, int i) {
        metricViewHolder.model.setText(metrics.get(i)[3]); //model
        metricViewHolder.manu.setText(metrics.get(i)[1]); //manufacturer
        metricViewHolder.andid.setText(metrics.get(i)[2]); //android id
        metricViewHolder.conntype.setText(metrics.get(i)[5]); // connection type
        metricViewHolder.dataused.setText(metrics.get(i)[6]); // data used
        metricViewHolder.time.setText(metrics.get(i)[11]); //time spent
        metricViewHolder.batBef.setText(metrics.get(i)[0]);// battery used
        metricViewHolder.signalStrength.setText(metrics.get(i)[7]); //signal strength
        metricViewHolder.timeBetweenPings.setText(metrics.get(i)[8]); // time between pings
        metricViewHolder.pingBytes.setText(metrics.get(i)[9]); // ping bytes
        metricViewHolder.andver.setText(metrics.get(i)[4]); // android version.
        metricViewHolder.jitter.setText(metrics.get(i)[13]); // jitter.
        metricViewHolder.mode.setText(metrics.get(i)[12]); // mode.
        metricViewHolder.packetsLost.setText(metrics.get(i)[14]); // packets lost.
        metricViewHolder.bandwidth.setText(metrics.get(i)[15]); // bandwidth.
    }

    /**
     * Swipes the old data, adds new and refreshes adapter.
     * @param data data to be used.
     */
    public void changeData(List<String[]> data){
        this.metrics = data;
        notifyDataSetChanged(); //efficient since we fetch new data once and never cache it somewhere
    }

    @Override
    public int getItemCount() {
        return metrics.size();
    }
}