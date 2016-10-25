package redlaboratory.littlelaboratory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NewMeasurementActivity extends Activity {

    public static class SensorItem {

        private Sensor sensor;
        private boolean checked;

        public SensorItem(Sensor sensor, boolean checked) {
            this.sensor = sensor;
            this.checked = checked;
        }

        public Sensor getSensor() {
            return sensor;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

    }

    public static class SensorItemAdapter extends BaseAdapter {

        private static class CheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

            private SensorItem sensorItem;

            public CheckedChangeListener(SensorItem sensorItem) {
                this.sensorItem = sensorItem;
            }

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sensorItem.setChecked(b);
            }

        }

        private Context context;
        private LayoutInflater inflater;
        private List<SensorItem> sensorItems;

        public SensorItemAdapter(Context context, List<SensorItem> sensorItems) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.context = context;
            this.sensorItems = sensorItems;
        }

        @Override
        public int getCount() {
            return sensorItems.size();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = inflater.inflate(R.layout.item_sensor, null);
            }

            SensorItem sensorItem = sensorItems.get(i);

            TextView sensorName = (TextView) view.findViewById(R.id.sensorName);
            CheckBox sensorCheck = (CheckBox) view.findViewById(R.id.sensorCheck);

            sensorName.setText(getSensorTypeName(sensorItem.getSensor()) + ", id: " + sensorItem.getSensor().getType());
            sensorCheck.setChecked(sensorItem.isChecked());
            sensorCheck.setOnCheckedChangeListener(new CheckedChangeListener(sensorItem));

            return view;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public SensorItem getItem(int i) {
            return sensorItems.get(i);
        }

        private String getSensorTypeName(Sensor sensor) {
            int strResId = -1;
            int sensorType = sensor.getType();

            if (sensorType == Sensor.TYPE_ACCELEROMETER) strResId = R.string.type_accelerometer;
            else if (sensorType == Sensor.TYPE_GYROSCOPE) strResId =  R.string.type_gyroscope;
            else if (sensorType == Sensor.TYPE_GRAVITY) strResId = R.string.type_gravity;
            else if (sensorType == Sensor.TYPE_LIGHT) strResId = R.string.type_light;
            else if (sensorType == Sensor.TYPE_PROXIMITY) strResId = R.string.type_proximity;
            else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) strResId = R.string.type_magnetic_field;
            else if (sensorType == Sensor.TYPE_ORIENTATION) strResId = R.string.type_orientation;

            if (strResId != -1) return context.getString(strResId);
            else return sensor.getName();
        }

    }

    private SensorManager sensorManager;
    private List<SensorItem> sensorItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmeasurement);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorItems = new ArrayList<SensorItem>();
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            sensorItems.add(new SensorItem(sensor, false));
        }
        SensorItemAdapter sensorItemAdapter = new SensorItemAdapter(getApplicationContext(), sensorItems);

        ListView listView = (ListView) findViewById(R.id.sensorsListView);
        listView.setAdapter(sensorItemAdapter);

        Button done = (Button) findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<SensorItem> checkedItems = new ArrayList<SensorItem>();
                for (SensorItem item : sensorItems) {
                    if (item.isChecked()) checkedItems.add(item);
                }
                if (checkedItems.size() == 0) {
                    Snackbar.make(NewMeasurementActivity.this.findViewById(R.id.linearLayout), "하나 이상을 선택해야 합니다", Snackbar.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(NewMeasurementActivity.this, MeasurementActivity.class);
                    int[] sensorTypes = new int[checkedItems.size()];
                    for (int i = 0; i < checkedItems.size(); i++) sensorTypes[i] = checkedItems.get(i).getSensor().getType();
                    intent.putExtra("sensorTypes", sensorTypes);
                    intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

}
