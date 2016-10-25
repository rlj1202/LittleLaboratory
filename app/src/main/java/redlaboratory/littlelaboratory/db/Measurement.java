package redlaboratory.littlelaboratory.db;

import java.util.ArrayList;

public class Measurement {

    private final long id;
    private final String title;
    private final ArrayList<Long> seriesIds;

    public Measurement(long id, String title, ArrayList<Long> seriesIds) {
        this.id = id;
        this.title = title;
        this.seriesIds = seriesIds;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Long> getSeriesIds() {
        return seriesIds;
    }

    @Override
    public String toString() {
        return "{id: " + id + ", title: " + title + ", seriesIds: " + seriesIds.toString() + "}";
    }

}
