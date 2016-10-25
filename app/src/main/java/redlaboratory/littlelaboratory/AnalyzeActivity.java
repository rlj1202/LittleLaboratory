package redlaboratory.littlelaboratory;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import redlaboratory.littlelaboratory.db.LittleLaboratoryDbHelper;
import redlaboratory.littlelaboratory.db.Measurement;

public class AnalyzeActivity extends AppCompatActivity {

    private LittleLaboratoryDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);

        dbHelper = new LittleLaboratoryDbHelper(this);

        long measurementId = getIntent().getLongExtra("measurementId", -1);

        Measurement measurement = dbHelper.selectMeasurement(measurementId);


    }
}
