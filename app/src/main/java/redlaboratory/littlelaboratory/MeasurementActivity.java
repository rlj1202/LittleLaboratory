package redlaboratory.littlelaboratory;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MeasurementActivity extends Activity {

    public static class SensorEventHandler implements SensorEventListener {

        private LineGraphSeries<DataPoint>[] serieses;
        private GraphView graphView;
        private int time = 0;

        public SensorEventHandler(GraphView graphView, int valueSize) {
            this.graphView = graphView;
            serieses = new LineGraphSeries[valueSize];
            for (int i = 0; i < serieses.length; i++) {
                serieses[i] = new LineGraphSeries<DataPoint>();
                graphView.addSeries(serieses[i]);
            }
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            for (int i = 0; i < sensorEvent.values.length; i++) {
                float value = sensorEvent.values[i];

                serieses[i].appendData(new DataPoint(time++, value), true, 100);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    private SensorManager sensorManager;
    private List<Sensor> sensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);

        int[] sensorTypes = getIntent().getIntArrayExtra("sensorTypes");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensors = new ArrayList<Sensor>();
        for (int sensorType : sensorTypes) {
            sensors.add(sensorManager.getDefaultSensor(sensorType));
        }

        GraphView graphView = (GraphView) findViewById(R.id.graph);
//        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
//        staticLabelsFormatter.setHorizontalLabels(new String[] {"test", "test2", "test3"});
//        staticLabelsFormatter.setVerticalLabels(new String[] {"w", "t", "f"});
//        graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        for (Sensor sensor : sensors) {
            int valueSize = 3;

            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) valueSize = 3;
            sensorManager.registerListener(new SensorEventHandler(graphView, valueSize), sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
}
