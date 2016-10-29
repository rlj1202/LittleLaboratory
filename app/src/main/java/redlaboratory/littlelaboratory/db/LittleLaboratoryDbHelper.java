package redlaboratory.littlelaboratory.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class LittleLaboratoryDbHelper extends SQLiteOpenHelper {

    public static abstract class ExperimentEntry implements BaseColumns {
        public static final String TABLE_NAME = "experiments";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "title";// String
        public static final String COLUMN_NAME_DESCRIPTION = "description";// String
        public static final String COLUMN_NAME_DATE = "date";// long
        public static final String COLUMN_NAME_MEASUREMENTS = "measurementIds";
    }

    public static abstract class MeasurementEntry implements BaseColumns {
        public static final String TABLE_NAME = "measurements";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "title";// String
        public static final String COLUMN_NAME_SERIES_IDS = "series_ids";// long array
    }

    public static abstract class SeriesEntry implements BaseColumns {
        public static final String TABLE_NAME = "series";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "title";// String
        public static final String COLUMN_NAME_COLOR = "color";// int
        public static final String COLUMN_NAME_DATA = "data";// blob
    }

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "LittleLaboratory.db";

    private static final String NULL_TYPE = " NULL";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String BLOB_TYPE = " BLOB";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_EXPERIMENTS =
            "CREATE TABLE " + ExperimentEntry.TABLE_NAME + " (" +
            ExperimentEntry.COLUMN_NAME_ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
            ExperimentEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
            ExperimentEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
            ExperimentEntry.COLUMN_NAME_DATE + INTEGER_TYPE + COMMA_SEP +
            ExperimentEntry.COLUMN_NAME_MEASUREMENTS + BLOB_TYPE +
            " )";
    private static final String SQL_CREATE_MEASUREMENTS =
            "CREATE TABLE " + MeasurementEntry.TABLE_NAME + " (" +
            MeasurementEntry.COLUMN_NAME_ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
            MeasurementEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
            MeasurementEntry.COLUMN_NAME_SERIES_IDS + BLOB_TYPE +
            " )";
    private static final String SQL_CREATE_SERIES =
            "CREATE TABLE " + SeriesEntry.TABLE_NAME + " (" +
            SeriesEntry.COLUMN_NAME_ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
            SeriesEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
            SeriesEntry.COLUMN_NAME_COLOR + INTEGER_TYPE + COMMA_SEP +
            SeriesEntry.COLUMN_NAME_DATA + BLOB_TYPE +
            " )";
    private static final String SQL_DELETE_EXPERIMENTS =
            "DROP TABLE IF EXISTS " + ExperimentEntry.TABLE_NAME;
    private static final String SQL_DELETE_MEASUREMENTS =
            "DROP TABLE IF EXISTS " + MeasurementEntry.TABLE_NAME;
    private static final String SQL_DELETE_SERIES =
            "DROP TABLE IF EXISTS " + SeriesEntry.TABLE_NAME;

    public LittleLaboratoryDbHelper(Context context) {
        super(context, Environment.getExternalStorageDirectory().getPath().concat("/") + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_EXPERIMENTS);
        db.execSQL(SQL_CREATE_MEASUREMENTS);
        db.execSQL(SQL_CREATE_SERIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_EXPERIMENTS);
        db.execSQL(SQL_DELETE_MEASUREMENTS);
        db.execSQL(SQL_DELETE_SERIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long insertExperiment(String title, String description, Calendar date, ArrayList<Long> measurementIds) {
        SQLiteDatabase db = getWritableDatabase();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
         try { for (long l : measurementIds) dos.writeLong(l); } catch (IOException e) { e.printStackTrace(); }

        ContentValues values = new ContentValues();
        values.put(ExperimentEntry.COLUMN_NAME_TITLE, title);
        values.put(ExperimentEntry.COLUMN_NAME_DESCRIPTION, description);
        values.put(ExperimentEntry.COLUMN_NAME_DATE, date.getTimeInMillis());
        values.put(ExperimentEntry.COLUMN_NAME_MEASUREMENTS, baos.toByteArray());

        long newRowId = db.insert(ExperimentEntry.TABLE_NAME, null, values);

        return newRowId;
    }

    public long insertMeasurement(String title, long[] seriesIds) {
        SQLiteDatabase db = getWritableDatabase();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try { for (long seriesId : seriesIds) dos.writeLong(seriesId); } catch (IOException e) {}

        ContentValues values = new ContentValues();
        values.put(MeasurementEntry.COLUMN_NAME_TITLE, title);
        values.put(MeasurementEntry.COLUMN_NAME_SERIES_IDS, baos.toByteArray());

        long newRowId = db.insert(MeasurementEntry.TABLE_NAME, null, values);

        Log.i("LittleLaboratory", "insertMeasurement: " + newRowId);

        return newRowId;
    }

    public long insertSeries(String title, int color, List<Double> data) {
        SQLiteDatabase db = getWritableDatabase();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try { for (double d : data) dos.writeDouble(d); } catch (IOException e) {}

        ContentValues values = new ContentValues();
        values.put(SeriesEntry.COLUMN_NAME_TITLE, title);
        values.put(SeriesEntry.COLUMN_NAME_COLOR, color);
        values.put(SeriesEntry.COLUMN_NAME_DATA, baos.toByteArray());

        long newRowId = db.insert(SeriesEntry.TABLE_NAME, null, values);

        Log.i("LittleLaboratory", "insertSeries: " + newRowId);

        return newRowId;
    }

    public void updateExperiment(Experiment experiment) {
        SQLiteDatabase db = getWritableDatabase();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try { for (long l : experiment.getMeasurements()) dos.writeLong(l); } catch (IOException e) { e.printStackTrace(); }

        ContentValues values = new ContentValues();
        values.put(ExperimentEntry.COLUMN_NAME_ID, experiment.getId());
        values.put(ExperimentEntry.COLUMN_NAME_TITLE, experiment.getTitle());
        values.put(ExperimentEntry.COLUMN_NAME_DESCRIPTION, experiment.getDescription());
        values.put(ExperimentEntry.COLUMN_NAME_DATE, experiment.getAddedDate().getTimeInMillis());
        values.put(ExperimentEntry.COLUMN_NAME_MEASUREMENTS, baos.toByteArray());

        String selection = ExperimentEntry.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(experiment.getId()) };

        db.update(ExperimentEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    public void deleteExperiment(long id) {
        Experiment experiment = selectExperiment(id);

        for (long measurementId : experiment.getMeasurements()) deleteMeasurement(measurementId);

        Log.i("LittleLaboratory", "Delete experiment: " + id + ", " + experiment.toString());

        SQLiteDatabase db = getWritableDatabase();

        String selection = ExperimentEntry.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(id) };
        db.delete(ExperimentEntry.TABLE_NAME, selection, selectionArgs);
    }

    public void deleteMeasurementInExperiment(long experimentId, long measurementId) {
        Experiment experiment = selectExperiment(experimentId);
        experiment.getMeasurements().remove(measurementId);
        updateExperiment(experiment);

        deleteMeasurement(measurementId);
    }

    public void deleteMeasurement(long id) {
        Measurement measurement = selectMeasurement(id);
        if (measurement == null) return;

        for (long seriesId : measurement.getSeriesIds()) deleteSeries(seriesId);

        Log.i("LittleLaboratory", "Delete measurement: " + id + ", " + measurement.toString());

        SQLiteDatabase db = getWritableDatabase();

        String selection = MeasurementEntry.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(id) };
        db.delete(MeasurementEntry.TABLE_NAME, selection, selectionArgs);
    }

    public void deleteSeries(long id) {
        Log.i("LittleLaboratory", "Delete series: " + id);

        SQLiteDatabase db = getWritableDatabase();

        String selection = SeriesEntry.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(id) };
        db.delete(SeriesEntry.TABLE_NAME, selection, selectionArgs);
    }

    public ArrayList<Experiment> selectExperiments() {
        ArrayList<Experiment> experiments = new ArrayList<Experiment>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query(ExperimentEntry.TABLE_NAME, null, null, null, null, null, null);

        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndex(ExperimentEntry.COLUMN_NAME_ID));
            String title = c.getString(c.getColumnIndex(ExperimentEntry.COLUMN_NAME_TITLE));
            String description = c.getString(c.getColumnIndex(ExperimentEntry.COLUMN_NAME_DESCRIPTION));
            long date = c.getLong(c.getColumnIndex(ExperimentEntry.COLUMN_NAME_DATE));
            byte[] rawMeasurements = c.getBlob(c.getColumnIndex(ExperimentEntry.COLUMN_NAME_MEASUREMENTS));

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(rawMeasurements));
            ArrayList<Long> measurements = new ArrayList<Long>();
            try  {
                while (dis.available() > 0) {
                    measurements.add(dis.readLong());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance(Locale.KOREA);
            calendar.setTimeInMillis(date);

            Experiment experiment = new Experiment(id, title, description, calendar, measurements);
            experiments.add(experiment);
        }

        return experiments;
    }

    public Experiment selectExperiment(long id) {
        SQLiteDatabase db = getReadableDatabase();

//        String[] projection = {
//                ExperimentEntry.COLUMN_NAME_ID,
//                ExperimentEntry.COLUMN_NAME_TITLE,
//                ExperimentEntry.COLUMN_NAME_DESCRIPTION,
//                ExperimentEntry.COLUMN_NAME_DATE,
//                ExperimentEntry.COLUMN_NAME_MEASUREMENTS
//        };

        String selection = ExperimentEntry.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(id) };

        Cursor c = db.query(
                ExperimentEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (c.getCount() == 0) return null;

        c.moveToNext();

        String title = c.getString(c.getColumnIndex(ExperimentEntry.COLUMN_NAME_TITLE));
        String description = c.getString(c.getColumnIndex(ExperimentEntry.COLUMN_NAME_DESCRIPTION));
        long date = c.getLong(c.getColumnIndex(ExperimentEntry.COLUMN_NAME_DATE));
        byte[] rawMeasurements = c.getBlob(c.getColumnIndex(ExperimentEntry.COLUMN_NAME_MEASUREMENTS));

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(rawMeasurements));
        ArrayList<Long> measurements = new ArrayList<Long>();
        try  {
            while (dis.available() > 0) {
                measurements.add(dis.readLong());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.setTimeInMillis(date);

        Experiment experiment = new Experiment(id, title, description, calendar, measurements);

        return experiment;
    }

    public ArrayList<Measurement> selectMeasurements() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query(MeasurementEntry.TABLE_NAME, null, null, null, null, null, null);
        ArrayList<Measurement> result = new ArrayList<Measurement>();

        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndex(MeasurementEntry.COLUMN_NAME_ID));
            String title = c.getString(c.getColumnIndex(MeasurementEntry.COLUMN_NAME_TITLE));
            byte[] rawSeriesIds = c.getBlob(c.getColumnIndex(MeasurementEntry.COLUMN_NAME_SERIES_IDS));

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(rawSeriesIds));
            ArrayList<Long> seriesIds = new ArrayList<>();
            try {
                while (dis.available() > 0) {
                    seriesIds.add(dis.readLong());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Measurement measurement = new Measurement(id, title, seriesIds);
            result.add(measurement);
        }

        return result;
    }

    public Measurement selectMeasurement(long id) {
        Log.i("LittleLaboratory", "selectMeasurement: " + id);

        SQLiteDatabase db = getReadableDatabase();

        String selection  = MeasurementEntry.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(id) };

        Cursor c = db.query(MeasurementEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        if (c.getCount() == 0) return null;

        c.moveToNext();

        String title = c.getString(c.getColumnIndex(MeasurementEntry.COLUMN_NAME_TITLE));
        byte[] rawSeriesIds = c.getBlob(c.getColumnIndex(MeasurementEntry.COLUMN_NAME_SERIES_IDS));

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(rawSeriesIds));
        ArrayList<Long> seriesIds = new ArrayList<>();
        try {
            while (dis.available() > 0) {
                seriesIds.add(dis.readLong());
            }
        } catch (IOException e) {}

        return new Measurement(id, title, seriesIds);
    }

    public ArrayList<Series> selectSeries() {
        ArrayList<Series> result = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query(SeriesEntry.TABLE_NAME, null, null, null, null, null, null);

        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndex(SeriesEntry.COLUMN_NAME_ID));
            String title = c.getString(c.getColumnIndex(SeriesEntry.COLUMN_NAME_TITLE));
            int color = c.getInt(c.getColumnIndex(SeriesEntry.COLUMN_NAME_COLOR));
            byte[] rawData = c.getBlob(c.getColumnIndex(SeriesEntry.COLUMN_NAME_DATA));

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(rawData));
            ArrayList<Double> data = new ArrayList<>();
            try {
                while (dis.available() > 0) {
                    data.add(dis.readDouble());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
            for (Iterator<Double> it = data.iterator(); it.hasNext();) {
                series.appendData(new DataPoint(it.next(), it.next()), true, data.size() / 2);
            }

            result.add(new Series(id, title, color, data, series));
        }

        return result;
    }

    public Series selectSeries(long id) {
        SQLiteDatabase db = getReadableDatabase();

        String selection = SeriesEntry.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(id) };

        Cursor c = db.query(SeriesEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        if (c.getCount() == 0) return null;

        c.moveToNext();

        String title = c.getString(c.getColumnIndex(SeriesEntry.COLUMN_NAME_TITLE));
        int color = c.getInt(c.getColumnIndex(SeriesEntry.COLUMN_NAME_COLOR));
        byte[] rawData = c.getBlob(c.getColumnIndex(SeriesEntry.COLUMN_NAME_DATA));

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(rawData));
        ArrayList<Double> data = new ArrayList<>();
        try {
            while (dis.available() > 0) {
                data.add(dis.readDouble());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        for (Iterator<Double> it = data.iterator(); it.hasNext();) {
            series.appendData(new DataPoint(it.next(), it.next()), true, data.size() / 2);
        }
        series.setTitle(title);
        series.setColor(color);

        return new Series(id, title, color, data, series);
    }

}
