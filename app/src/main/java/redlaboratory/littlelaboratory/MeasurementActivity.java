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
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import redlaboratory.littlelaboratory.db.LittleLaboratoryDbHelper;

public class MeasurementActivity extends Activity {

    public static class SensorListener implements SensorEventListener {

        public int sensorType;
        public float[] values;
        public LineGraphSeries<DataPoint>[] series;
        public ByteArrayOutputStream[] datas;

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
            sensorListener.datas = new ByteArrayOutputStream[values];
            for (int i = 0; i < values; i++) {
                LineGraphSeries<DataPoint> newSeries = new LineGraphSeries<DataPoint>();
                sensorListener.series[i] = newSeries;
                sensorListener.datas[i] = new ByteArrayOutputStream();

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

                    String sensorName = getString(SensorInformation.fromSensorType(listener.sensorType).getTitleStringId());
                    int values = SensorInformation.fromSensorType(listener.sensorType).getValues();
                    String[] valueNames = SensorInformation.fromSensorType(listener.sensorType).getValueNames();
                    int[] colors = SensorInformation.fromSensorType(listener.sensorType).getColors();

                    long[] seriesIds = new long[values];

                    for (int j = 0; j < values; j++) {
                        seriesIds[j] = littleLaboratoryDbHelper.insertSeries(valueNames[j], colors[j], listener.datas[j].toByteArray());
                    }

                    long measurementId = littleLaboratoryDbHelper.insertMeasurement(sensorName, seriesIds);
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
                            try {
                                DataOutputStream dataOutputStream = new DataOutputStream(sensorListener.datas[i]);
                                dataOutputStream.writeDouble(second);
                                dataOutputStream.writeDouble(value);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    time++;
                    handler.postDelayed(runnable, delay);
                }
            }

        };
        handler = new Handler();
        handler.post(runnable);

        MeasurementGraphListViewAdapter adapter = new MeasurementGraphListViewAdapter(getApplicationContext(), listeners);

        ListView graphView = (ListView) findViewById(R.id.graphList);
        graphView.setAdapter(adapter);
    }

}
