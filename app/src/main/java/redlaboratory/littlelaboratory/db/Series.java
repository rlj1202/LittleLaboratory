package redlaboratory.littlelaboratory.db;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

public class Series {

    private final long id;
    private final String title;
    private final int color;
    private final List<Double> data;
    private final LineGraphSeries<DataPoint> lineGraphSeries;

    public Series(long id, String title, int color, List<Double> data, LineGraphSeries<DataPoint> lineGraphSeries) {
        this.id = id;
        this.title = title;
        this.color = color;
        this.data = data;
        this.lineGraphSeries = lineGraphSeries;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getColor() {
        return color;
    }

    public List<Double> getData() {
        return data;
    }

    public LineGraphSeries<DataPoint> getLineGraphSeries() {
        return lineGraphSeries;
    }

    @Override
    public String toString() {
        return "{id: " + id + ", title: " + title + ", color: " + color + ", data: " + data + ", series: " + lineGraphSeries.toString() + "}";
    }

}
