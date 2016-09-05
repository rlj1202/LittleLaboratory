package redlaboratory.littlelaboratory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ExperimentActivity extends Activity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment);

        Experiment experiment = (Experiment) getIntent().getSerializableExtra("experiment");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(experiment.getTitle());

        TextView description = (TextView) findViewById(R.id.description);
        description.setText(experiment.getDescription());

        FloatingActionButton newMeasurementFab = (FloatingActionButton) findViewById(R.id.newmeasurementfab);
        newMeasurementFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExperimentActivity.this, NewMeasurementActivity.class);
                startActivity(intent);
            }
        });
//        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager((this)));
//        recyclerView.setAdapter(new MyAdapter(new String[] {"test", "test2"}));
    }

}
