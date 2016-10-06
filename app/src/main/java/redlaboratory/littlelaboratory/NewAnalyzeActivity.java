package redlaboratory.littlelaboratory;

import android.content.Context;
import android.database.DataSetObserver;
import android.icu.util.Measure;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewAnalyzeActivity extends AppCompatActivity {
//
//    private static class DefaultSpinnerAdapter implements SpinnerAdapter {
//
//        private Context context;
//        private List<Integer> texts;
//        private List<Long> ids;
//
//        private LayoutInflater inflater;
//
//        public DefaultSpinnerAdapter(Context context, List<Integer> texts, List<Long> ids) {
//            this.context = context;
//            this.texts = texts;
//            this.ids = ids;
//
//            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//
//            }
//
//            TextView textView = (TextView) convertView;
//            textView.setText(texts.get(position));
//
//            return convertView;
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return texts.get(position);
//        }
//
//        @Override
//        public int getCount() {
//            return texts.size();
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            return IGNORE_ITEM_VIEW_TYPE;
//        }
//
//        @Override
//        public int getViewTypeCount() {
//            return 1;
//        }
//
//        @Override
//        public View getDropDownView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = null;
//            }
//
//            return convertView;
//        }
//
//        @Override
//        public void unregisterDataSetObserver(DataSetObserver observer) {
//
//        }
//
//        @Override
//        public void registerDataSetObserver(DataSetObserver observer) {
//
//        }
//
//        @Override
//        public boolean isEmpty() {
//            return texts.size() != 0;
//        }
//
//        @Override
//        public boolean hasStableIds() {
//            return false;
//        }
//
//    }

    private static class ListItem {

        public ListItem() {

        }

    }

    private static class ListAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;

        public ListAdapter(Context context) {
            this.context = context;
            this.inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(0, null);
            }

            return convertView;
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
        Long[] measurementIds = (Long[]) getIntent().getSerializableExtra("measurementIds");

        experiment = dbHelper.selectExperiment(experimentId);
        measurements = new ArrayList<Measurement>();

        for (Long measurementId : measurementIds) {
            measurements.add(dbHelper.selectMeasurement(measurementId));
        }

        Spinner from = (Spinner) findViewById(R.id.from);
        Spinner to = (Spinner) findViewById(R.id.to);
        from.setAdapter(new ArrayAdapter<String>(this, R.layout.item_analyze, R.id.textView, new String[] {"test"}));
        to.setAdapter(new ArrayAdapter<String>(this, R.layout.item_analyze, R.id.textView, new String[] {"yap"}));

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(null);

        Button done = (Button) findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
