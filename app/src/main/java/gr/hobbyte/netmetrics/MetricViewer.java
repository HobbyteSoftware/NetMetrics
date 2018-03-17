package gr.hobbyte.netmetrics;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import gr.hobbyte.netmetrics.adapters.RecyclerInfoAdapter;
import gr.hobbyte.netmetrics.database.DataFetchThread;

/**
 * This Activity shows the metrics saved in the database.
 */
public class MetricViewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metric_viewer); // connect to xml

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv);

        //recycler views need a layout manager for reasons!
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(false);

        //Create temporary card to let the user know that it is working
        ArrayList<String[]> tempData = new ArrayList<>(1);
        String[] fetchingArray= new String[16];
        for(int t = 0; t < fetchingArray.length; t++){
            fetchingArray[t] = "Fetching...";
        }
        tempData.add(fetchingArray); //add to ArrayList

        //Create and bind adapter to recycler view
        RecyclerInfoAdapter adapter = new RecyclerInfoAdapter(tempData);
        recyclerView.setAdapter(adapter);

        //init and fetch thread.
        DataFetchThread fetchThread = new DataFetchThread(this,adapter);
        fetchThread.execute();
    }
}