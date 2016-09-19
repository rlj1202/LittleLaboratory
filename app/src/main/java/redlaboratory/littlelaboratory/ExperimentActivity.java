package redlaboratory.littlelaboratory;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ExperimentActivity extends AppCompatActivity {
//
//    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
//
//        private String[] dataset;
//
//        public static class ViewHolder extends RecyclerView.ViewHolder {
//
//            public TextView textView;
//
//            public ViewHolder(TextView view) {
//                super(view);
//                textView = view;
//            }
//
//        }
//
//        public MyAdapter(String[] dataset) {
//            this.dataset = dataset;
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            TextView v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view, parent, false);
//
//            ViewHolder vh = new ViewHolder(v);
//            return vh;
//        }
//
//        @Override
//        public void onBindViewHolder(ViewHolder holder, int position) {
//            holder.textView.setText(dataset[position]);
//        }
//
//        @Override
//        public int getItemCount() {
//            return dataset.length;
//        }
//    }

    public static class MeasurementsListViewItem {

        private int sensorType;
        private ArrayList<LineGraphSeries> series;

        public MeasurementsListViewItem(int sensorType, ArrayList<LineGraphSeries> series) {
            this.sensorType = sensorType;
            this.series = series;
        }

    }

    public static class MeasurementsListViewAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<MeasurementsListViewItem> items;

        private LayoutInflater inflater;

        public MeasurementsListViewAdapter(Context context, ArrayList<MeasurementsListViewItem> items) {
            this.context = context;
            this.items = items;

            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = inflater.inflate(R.layout.item_measurements, null);
            }

            MeasurementsListViewItem item = items.get(i);

            GraphView graphView = (GraphView) view.findViewById(R.id.graph);
            graphView.removeAllSeries();
            for (LineGraphSeries series : item.series) graphView.addSeries(series);
            graphView.setBackgroundColor(0xff424242);
            graphView.getViewport().setXAxisBoundsManual(true);
            graphView.getViewport().setYAxisBoundsManual(true);
            graphView.getViewport().setMinX(0);
            graphView.getViewport().setMaxX(10);
            graphView.getViewport().setScrollable(true);
            graphView.getViewport().setScalable(true);
            graphView.getLegendRenderer().setVisible(true);
            graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            graphView.getGridLabelRenderer().setLabelVerticalWidth(40);
            graphView.setTitle(context.getString(SensorInformation.fromSensorType(item.sensorType).getTitleStringId()));

            return view;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

    }

    private Experiment experiment;
    private LittleLaboratoryDbHelper dbHelper;
    private ArrayList<MeasurementsListViewItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment);

        Log.i("LittleLaboratory", "ExperimentActivity onCreate");

        dbHelper = new LittleLaboratoryDbHelper(getApplicationContext());
        long experimentId = getIntent().getLongExtra("experimentId", -1);
        experiment = dbHelper.selectExperiment(experimentId);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(experiment.getTitle());
        setSupportActionBar(toolbar);

        TextView description = (TextView) findViewById(R.id.description);
        description.setText(experiment.getDescription());

        items = new ArrayList<MeasurementsListViewItem>();
        for (long measurementId : experiment.getMeasurements()) {
            Log.i("LittleLaboratory", "Load measurementListViewItem: " + measurementId);
            items.add(getMeasurementsListViewItem(measurementId));
        }

        MeasurementsListViewAdapter adapter = new MeasurementsListViewAdapter(getApplicationContext(), items);

        ListView measurementListView = (ListView) findViewById(R.id.measurements);
        measurementListView.setAdapter(adapter);

        FloatingActionButton newMeasurementFab = (FloatingActionButton) findViewById(R.id.newmeasurementfab);
        newMeasurementFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExperimentActivity.this, NewMeasurementActivity.class);
                startActivityForResult(intent, 0);
            }
        });

//        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager((this)));
//        recyclerView.setAdapter(new MyAdapter(new String[] {"test", "test2"}));
    }

    private MeasurementsListViewItem getMeasurementsListViewItem(long measurementId) {
        Measurement measurement = dbHelper.selectMeasurement(measurementId);

        int sensorType = measurement.getSensorType();
        ArrayList<Long> seriesIds = measurement.getSeriesIds();

        String[] valueNames = SensorInformation.fromSensorType(sensorType).getValueNames();
        int[] colors = SensorInformation.fromSensorType(sensorType).getColors();

        MeasurementsListViewItem item = new MeasurementsListViewItem(sensorType, new ArrayList<LineGraphSeries>());

        for (int i = 0; i < seriesIds.size(); i++) {
            long seriesId = seriesIds.get(i);

            LineGraphSeries<DataPoint> series = dbHelper.selectSeries(seriesId);

            if (series == null) continue;

            series.setTitle(valueNames[i]);
            series.setColor(colors[i]);

            item.series.add(series);
        }

        return item;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            long[] measurementIds = data.getLongArrayExtra("measurementIds");

            Log.i("LittleLaboratory", "Get measurement ids: " + Arrays.toString(measurementIds));

            for (long l : measurementIds) {
                experiment.getMeasurements().add(l);
                items.add(getMeasurementsListViewItem(l));
                ListView measurementListView = (ListView) findViewById(R.id.measurements);
                ((BaseAdapter) measurementListView.getAdapter()).notifyDataSetChanged();
                dbHelper.updateExperiment(experiment.getId(), experiment.getTitle(), experiment.getDescription(), experiment.getAddedDate(), experiment.getMeasurements());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_experiment, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                AlertDialog.Builder ab = new AlertDialog.Builder(this);
                ab.setMessage(R.string.delete_confirm)
                        .setCancelable(true)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int id) {
                                dbHelper.deleteExperiment(experiment.getId());
                                finish();
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int id) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alert = ab.create();
                alert.setTitle(R.string.delete);
                alert.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
