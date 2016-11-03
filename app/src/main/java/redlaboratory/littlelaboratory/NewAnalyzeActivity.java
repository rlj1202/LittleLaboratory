package redlaboratory.littlelaboratory;

import android.app.Activity;
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
import java.util.List;

import redlaboratory.littlelaboratory.analyze.Analyze;
import redlaboratory.littlelaboratory.db.DataType;
import redlaboratory.littlelaboratory.db.Experiment;
import redlaboratory.littlelaboratory.db.LittleLaboratoryDbHelper;
import redlaboratory.littlelaboratory.db.Measurement;
import redlaboratory.littlelaboratory.db.Series;

public class NewAnalyzeActivity extends AppCompatActivity {

    public static class AnalyzeMethodListAdapter extends BaseAdapter {

        private Activity activity;
        private LayoutInflater inflater;

        private LittleLaboratoryDbHelper dbHelper;

        private Experiment experiment;
        private Measurement measurement;

        public AnalyzeMethodListAdapter(Activity activity, LittleLaboratoryDbHelper dbHelper, Experiment experiment, Measurement measurement) {
            this.activity = activity;
            this.experiment = experiment;
            this.measurement = measurement;
            this.dbHelper = dbHelper;

            inflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_new_analyze, null);
            }

            final Analyze analyze = Analyze.values[position];

            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(analyze.getName());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long[] newSeriesIds = new long[measurement.getSeriesIds().size()];
                    for (int i = 0; i < newSeriesIds.length; i++) {
                        long seriesId = measurement.getSeriesIds().get(i);
                        Series series = dbHelper.selectSeries(seriesId);

                        List<Double> analyzed = analyze.analyze(series.getData());

                        long newSeriesId = dbHelper.insertSeries(series.getTitle(), series.getColor(), analyzed);
                        newSeriesIds[i] = newSeriesId;
                    }
                    DataType analyzedDataType = analyze.getAnalyzedDataType(measurement.getDataType());
                    experiment.getMeasurements().add(dbHelper.insertMeasurement(activity.getString(analyzedDataType.getNameStringId()), analyzedDataType, newSeriesIds));
                    dbHelper.updateExperiment(experiment);

                    activity.finish();
                }
            });

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
    private Measurement measurement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_analyze);

        dbHelper = new LittleLaboratoryDbHelper(this);

        long experimentId = getIntent().getLongExtra("experimentId", -1);
        long measurementId = getIntent().getLongExtra("measurementId", -1);

        experiment = dbHelper.selectExperiment(experimentId);
        measurement = dbHelper.selectMeasurement(measurementId);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new AnalyzeMethodListAdapter(this, dbHelper, experiment, measurement));

        Button done = (Button) findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
