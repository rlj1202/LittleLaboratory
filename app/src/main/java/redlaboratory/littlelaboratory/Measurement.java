package redlaboratory.littlelaboratory;

import java.util.ArrayList;

public class Measurement {

    private final long id;
    private final int sensorType;
    private final ArrayList<Long> seriesIds;

    public Measurement(long id, int sensorType, ArrayList<Long> seriesIds) {
        this.id = id;
        this.sensorType = sensorType;
        this.seriesIds = seriesIds;
    }

    public long getId() {
        return id;
    }

    public int getSensorType() {
        return sensorType;
    }

    public ArrayList<Long> getSeriesIds() {
        return seriesIds;
    }

    @Override
    public String toString() {
        return "{id: " + id + ", sensorType: " + sensorType + ", seriesIds: " + seriesIds.toString() + "}";
    }

}
