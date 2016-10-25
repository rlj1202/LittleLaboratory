package redlaboratory.littlelaboratory.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Experiment implements Serializable {

    private long id;
    private String title;
    private String description;
    private Calendar addedDate;
    private ArrayList<Long> measurements;

    public Experiment(long id, String title, String description, Calendar addedDate, ArrayList<Long> measurements) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.addedDate = addedDate;
        this.measurements = measurements;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Calendar getAddedDate() {
        return addedDate;
    }

    public ArrayList<Long> getMeasurements() {
        return measurements;
    }

    @Override
    public String toString() {
        return "{id: " + id + ", title: " + title + ", description: " + description + ", addedDate: " + addedDate.toString() + ", measurements: " + measurements.toString() + "}";
    }

}
