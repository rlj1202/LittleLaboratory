package redlaboratory.littlelaboratory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import redlaboratory.littlelaboratory.db.DataType;
import redlaboratory.littlelaboratory.db.LittleLaboratoryDbHelper;

public class MeasurementActivity extends Activity {

    public static class MeasurementGraphListViewAdapter extends BaseAdapter {

        private Context context;
        private List<MeasurementActivity.SensorListener> handlers;

        private LayoutInflater inflater;

        public MeasurementGraphListViewAdapter(Context context, List<MeasurementActivity.SensorListener> handlers) {
            this.context = context;
            this.handlers = handlers;

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            Log.i("LittleLaboratory", "Adapter load: " + handlers.size());
            for (MeasurementActivity.SensorListener handler : handlers) Log.i("LittleLaboratory", "" + handler.sensorType);
        }

        @Override
        public int getCount() {
            return handlers.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_graph, null);
            }

            MeasurementActivity.SensorListener sensorListener = handlers.get(position);

            GraphView graphView = (GraphView) convertView.findViewById(R.id.graph);
            graphView.removeAllSeries();
            for (LineGraphSeries series : sensorListener.series) graphView.addSeries(series);
            graphView.setBackgroundColor(0xff424242);
            graphView.getGridLabelRenderer().setHorizontalLabelsColor(0xffffffff);
            graphView.getGridLabelRenderer().setVerticalLabelsColor(0xffffffff);
            graphView.getGridLabelRenderer().setGridColor(0xff888888);
            graphView.getLegendRenderer().setTextColor(0xffffffff);
            graphView.setTitleColor(0xffffffff);
//        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
//        staticLabelsFormatter.setHorizontalLabels(new String[] {"test", "test2", "test3"});
//        staticLabelsFormatter.setVerticalLabels(new String[] {"w", "t", "f"});
//        graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
            graphView.getViewport().setXAxisBoundsManual(true);
            graphView.getViewport().setYAxisBoundsManual(false);
            graphView.getViewport().setMinX(0);
            graphView.getViewport().setMaxX(10);
            graphView.getLegendRenderer().setVisible(true);
            graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            graphView.getGridLabelRenderer().setLabelVerticalWidth(40);
//        graphView.getGridLabelRenderer().setHorizontalAxisTitle(context.getString(R.string.time));
            graphView.setTitle(context.getString(SensorInformation.fromSensorType(sensorListener.sensorType).getDataType().getNameStringId()));

            Log.i("LittleLaboratory", "Load view item: " + position + ", " + sensorListener.hashCode() + ", " + sensorListener.sensorType + "");

            return convertView;
        }

        @Override
        public MeasurementActivity.SensorListener getItem(int position) {
            return handlers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    }

    public static class SensorListener implements SensorEventListener {

        public int sensorType;
        public float[] values;
        public LineGraphSeries<DataPoint>[] series;
        public List<Double>[] datas;

        public SensorListener(int sensorType) {
            this.sensorType = sensorType;
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            values = sensorEvent.values;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

    }

    private SensorManager sensorManager;
    private List<SensorListener> listeners;

    private LittleLaboratoryDbHelper littleLaboratoryDbHelper;

    private Runnable runnable;
    private Handler handler;
    private boolean measure = false;
    private int time;
    private int delay = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);

        int[] sensorTypes = getIntent().getIntArrayExtra("sensorTypes");

        Log.i("LittleLaboratory", "Start to load sensors");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        listeners = new ArrayList<SensorListener>();
        for (int sensorType : sensorTypes) {
            Sensor sensor = sensorManager.getDefaultSensor(sensorType);
            SensorListener sensorListener = new SensorListener(sensorType);

            int values = SensorInformation.fromSensorType(sensorType).getValues();
            int[] colors = SensorInformation.fromSensorType(sensorType).getColors();
            String[] valueNames = SensorInformation.fromSensorType(sensorType).getValueNames();

            sensorListener.series = new LineGraphSeries[values];
            sensorListener.datas = new ArrayList[values];
            for (int i = 0; i < values; i++) {
                LineGraphSeries<DataPoint> newSeries = new LineGraphSeries<DataPoint>();
                sensorListener.series[i] = newSeries;
                sensorListener.datas[i] = new ArrayList<>();

                newSeries.setColor(colors[i]);
                newSeries.setTitle(valueNames[i]);
            }

            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_GAME);

            Log.i("LittleLaboratory", "Load sensor: " + sensorType + ", " + sensor.getType() + ", " + sensor.getName());

            listeners.add(sensorListener);
        }

        littleLaboratoryDbHelper = new LittleLaboratoryDbHelper(getApplicationContext());

        Button complete = (Button) findViewById(R.id.done);
        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                measure = false;
                long[] measurementIds = new long[listeners.size()];

                for (int i = 0; i < listeners.size(); i++) {
                    SensorListener listener = listeners.get(i);

                    DataType dataType = SensorInformation.fromSensorType(listener.sensorType).getDataType();
                    String sensorName = getString(SensorInformation.fromSensorType(listener.sensorType).getDataType().getNameStringId());
                    int values = SensorInformation.fromSensorType(listener.sensorType).getValues();
                    String[] valueNames = SensorInformation.fromSensorType(listener.sensorType).getValueNames();
                    int[] colors = SensorInformation.fromSensorType(listener.sensorType).getColors();

                    long[] seriesIds = new long[values];

                    for (int j = 0; j < values; j++) {
                        seriesIds[j] = littleLaboratoryDbHelper.insertSeries(valueNames[j], colors[j], listener.datas[j]);
                    }

                    long measurementId = littleLaboratoryDbHelper.insertMeasurement(sensorName, dataType, seriesIds);
                    measurementIds[i] = measurementId;
                }

                Intent result = new Intent();
                result.putExtra("measurementIds", measurementIds);
                setResult(RESULT_OK, result);
                finish();
            }
        });

        measure = true;
        runnable = new Runnable() {

            @Override
            public void run() {
                if (measure) {
                    for (SensorListener sensorListener : listeners) {
                        int values = SensorInformation.fromSensorType(sensorListener.sensorType).getValues();
//                        String[] valueNames = DataTypeHolder.getDataType(sensorListener.sensorType).getValueNames();
//                        int[] colors = DataTypeHolder.getDataType(sensorListener.sensorType).getColors();

                        for (int i = 0; i < values; i++) {
                            double second = time * delay / 1000.0D;
                            double value = sensorListener.values != null ? sensorListener.values[i] : 0.0D;

                            sensorListener.series[i].appendData(new DataPoint(second, value), true, 100);
                            sensorListener.datas[i].add(second);
                            sensorListener.datas[i].add(value);
                        }
                    }

                    time++;
                    handler.postDelayed(runnable, delay);
                }
            }

        };
        handler = new Handler();
        handler.post(runnable);

        MeasurementGraphListViewAdapter adapter = new MeasurementGraphListViewAdapter(this, listeners);

        ListView graphView = (ListView) findViewById(R.id.graphList);
        graphView.setAdapter(adapter);
    }

}
