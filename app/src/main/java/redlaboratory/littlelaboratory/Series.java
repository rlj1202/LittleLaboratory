package redlaboratory.littlelaboratory;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class Series {

    private final long id;
    private final LineGraphSeries<DataPoint> lineGraphSeries;

    public Series(long id, LineGraphSeries<DataPoint> lineGraphSeries) {
        this.id = id;
        this.lineGraphSeries = lineGraphSeries;
    }

    public long getId() {
        return id;
    }

    public LineGraphSeries<DataPoint> getLineGraphSeries() {
        return lineGraphSeries;
    }

    @Override
    public String toString() {
        return "{id: " + id + ", " + lineGraphSeries.toString() + "}";
    }

}
