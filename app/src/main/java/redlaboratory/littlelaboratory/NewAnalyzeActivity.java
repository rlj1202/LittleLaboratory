package redlaboratory.littlelaboratory;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import redlaboratory.littlelaboratory.analyze.Analyze;
import redlaboratory.littlelaboratory.db.Experiment;
import redlaboratory.littlelaboratory.db.LittleLaboratoryDbHelper;
import redlaboratory.littlelaboratory.db.Measurement;

public class NewAnalyzeActivity extends AppCompatActivity {

    public static class AnalyzeMethodListAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;

        public AnalyzeMethodListAdapter(Context context) {
            this.context = context;

            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.support_simple_spinner_dropdown_item, null);
            }

            Analyze analyze = Analyze.values[position];

            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(analyze.getName());

            return convertView;
        }

        @Override
        public int getCount() {
            return Analyze.values.length;
        }

        @Override
        public Object getItem(int position) {
            return Analyze.values[position];
        }

    }

    private LittleLaboratoryDbHelper dbHelper;
    private Experiment experiment;
    private ArrayList<Measurement> measurements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_analyze);

        dbHelper = new LittleLaboratoryDbHelper(this);

        long experimentId = getIntent().getLongExtra("experimentId", -1);
        long measurementId = getIntent().getLongExtra("measurementId", -1);
//        Long[] measurementIds = (Long[]) getIntent().getSerializableExtra("measurementIds");

        experiment = dbHelper.selectExperiment(experimentId);
        measurements = new ArrayList<Measurement>();

//        for (Long id : measurementIds) {
//            measurements.add(dbHelper.selectMeasurement(id));
//        }

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new AnalyzeMethodListAdapter(this));

        Button done = (Button) findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
