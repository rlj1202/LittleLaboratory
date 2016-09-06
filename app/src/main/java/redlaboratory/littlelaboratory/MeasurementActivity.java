package redlaboratory.littlelaboratory;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.widget.ListView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

public class MeasurementActivity extends Activity {

    public static class SensorHandler implements SensorEventListener {

        private static int milliSecond = 100;

        private SensorManager sensorManager;
        private Sensor sensor;
        private float[] values;

        private String[] valueNames;
        private int[] colors;

        private LineGraphSeries<DataPoint>[] serieses;

        private int time = 0;

        public SensorHandler(SensorManager sensorManager, Sensor sensor, String[] valueNames, int[] colors) {
            this.sensorManager = sensorManager;
            this.sensor = sensor;
            this.valueNames = valueNames;
            this.colors = colors;

            startHandling();
        }

        public LineGraphSeries<DataPoint>[] getSerieses() {
            return serieses;
        }

        public void startHandling() {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

            serieses = new LineGraphSeries[valueNames.length];
            for (int i = 0; i < valueNames.length; i++) {
                LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
                serieses[i] = series;

                series.setColor(colors[i]);
            }

            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        if (values != null) for (int i = 0; i < values.length; i++) {
                            float value = values[i];

                            if (i < valueNames.length) serieses[i].appendData(new DataPoint(time++, value), true, 100);
                        }

                        try {
                            Thread.sleep(milliSecond);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
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
    private LayoutInflater inflater;
    private List<SensorHandler> handlers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);

        inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        int[] sensorTypes = getIntent().getIntArrayExtra("sensorTypes");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        handlers = new ArrayList<SensorHandler>();
        for (int sensorType : sensorTypes) {
            String[] valueNames = null;
            int[] colors = null;

            if (sensorType == Sensor.TYPE_PROXIMITY) {
                valueNames = new String[] {"proximity"}; colors = new int[] {0xffffffff};
            }
            if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                valueNames = new String[] {"x", "y", "z"}; colors = new int[] {0xffff0000, 0xff00ff00, 0xff0000ff};
            }

            Sensor sensor = sensorManager.getDefaultSensor(sensorType);
            SensorHandler sensorHandler = new SensorHandler(sensorManager, sensor, valueNames, colors);

            handlers.add(sensorHandler);
        }

//        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
//        staticLabelsFormatter.setHorizontalLabels(new String[] {"test", "test2", "test3"});
//        staticLabelsFormatter.setVerticalLabels(new String[] {"w", "t", "f"});
//        graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        MeasurementGraphListViewAdapter adapter = new MeasurementGraphListViewAdapter(getApplicationContext(), handlers);

        ListView graphView = (ListView) findViewById(R.id.graphList);
        graphView.setAdapter(adapter);
    }

}
