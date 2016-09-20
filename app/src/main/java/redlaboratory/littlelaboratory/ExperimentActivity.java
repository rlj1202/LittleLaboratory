package redlaboratory.littlelaboratory;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;

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

        private Measurement measurement;
        private int sensorType;
        private ArrayList<LineGraphSeries> series;

        public MeasurementsListViewItem(Measurement measurement, int sensorType, ArrayList<LineGraphSeries> series) {
            this.measurement = measurement;
            this.sensorType = sensorType;
            this.series = series;
        }

    }

    public static class MeasurementsListViewAdapter extends BaseAdapter {

        private ExperimentActivity context;
        private ArrayList<MeasurementsListViewItem> items;

        public MeasurementsListViewAdapter(ExperimentActivity context, ArrayList<MeasurementsListViewItem> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = context.inflater.inflate(R.layout.item_measurements, null);
            }

            final MeasurementsListViewItem item = items.get(i);

            GraphView graphView = (GraphView) view.findViewById(R.id.graph);
            graphView.removeAllSeries();
            for (LineGraphSeries series : item.series) graphView.addSeries(series);
            graphView.setBackgroundColor(0xff424242);
            graphView.getGridLabelRenderer().setHorizontalLabelsColor(0xffffffff);
            graphView.getGridLabelRenderer().setVerticalLabelsColor(0xffffffff);
            graphView.getGridLabelRenderer().setGridColor(0xff888888);
            graphView.getLegendRenderer().setTextColor(0xffffffff);
            graphView.setTitleColor(0xffffffff);
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

            Button viewButton = (Button) view.findViewById(R.id.view);
            Button deleteButton = (Button) view.findViewById(R.id.delete);

            viewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(context);
                    ab.setMessage(R.string.delete_confirm)
                            .setCancelable(true)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    context.deleteMeasurementItem(item);
                                }
                            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = ab.create();
                    alertDialog.setTitle(R.string.delete);
                    alertDialog.show();
                }
            });

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
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment);

        Log.i("LittleLaboratory", "ExperimentActivity onCreate");

        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        dbHelper = new LittleLaboratoryDbHelper(getApplicationContext());
        long experimentId = getIntent().getLongExtra("experimentId", -1);
        experiment = dbHelper.selectExperiment(experimentId);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(experiment.getTitle());
        setSupportActionBar(toolbar);

        TextView description = (TextView) findViewById(R.id.description);
        description.setText(experiment.getDescription());

        items = new ArrayList<>();

        MeasurementsListViewAdapter adapter = new MeasurementsListViewAdapter(this, items);

        ListView measurementListView = (ListView) findViewById(R.id.measurements);
        measurementListView.setAdapter(adapter);
//        measurementListView.setEmptyView(inflater.inflate(R.layout.item_measurements_empty, null));
        measurementListView.setEmptyView(findViewById(R.id.description));

        for (long measurementId : experiment.getMeasurements()) {
            Log.i("LittleLaboratory", "Load measurementListViewItem: " + measurementId);
            MeasurementsListViewItem item = getMeasurementsListViewItem(measurementId);
            addMeasurementItem(item);
        }

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

    private void addMeasurementItem(MeasurementsListViewItem item) {
        if (item == null) return;

        items.add(item);
        ListView listView = (ListView) findViewById(R.id.measurements);
        setListViewHeightBasedOnChildren(listView);
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    private void deleteMeasurementItem(MeasurementsListViewItem item) {
        items.remove(item);
        dbHelper.deleteMeasurementInExperiment(experiment.getId(), item.measurement.getId());
        ListView listView = (ListView) findViewById(R.id.measurements);
        setListViewHeightBasedOnChildren(listView);
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    /**
     *
     * @see <a href="http://stackoverflow.com/questions/1661293/why-do-listview-items-not-grow-to-wrap-their-content">StackOverflow original post</a>
     * @param listView listView
     */
    private static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private MeasurementsListViewItem getMeasurementsListViewItem(long measurementId) {
        Measurement measurement = dbHelper.selectMeasurement(measurementId);

        if (measurement == null) return null;

        int sensorType = measurement.getSensorType();
        ArrayList<Long> seriesIds = measurement.getSeriesIds();

        String[] valueNames = SensorInformation.fromSensorType(sensorType).getValueNames();
        int[] colors = SensorInformation.fromSensorType(sensorType).getColors();

        MeasurementsListViewItem item = new MeasurementsListViewItem(measurement, sensorType, new ArrayList<LineGraphSeries>());

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
                dbHelper.updateExperiment(experiment);

                MeasurementsListViewItem item = getMeasurementsListViewItem(l);
                addMeasurementItem(item);
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
