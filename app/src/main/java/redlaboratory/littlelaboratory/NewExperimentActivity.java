package redlaboratory.littlelaboratory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import redlaboratory.littlelaboratory.db.Experiment;

public class NewExperimentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newexperiment);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle(R.string.new_experiment);

        Button newExperiment = (Button) findViewById(R.id.done);

        newExperiment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = ((EditText) findViewById(R.id.title)).getText().toString();
                String description = ((EditText) findViewById(R.id.description)).getText().toString();
                Calendar addedDate = Calendar.getInstance(Locale.KOREA);

                if (!title.isEmpty()) {
                    Intent intent = new Intent();
                    intent.putExtra("experiment", new Experiment(-1, title, description, addedDate, new ArrayList<Long>()));
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Snackbar.make(view, "제목을 반드시 입력해야 합니다", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

}
