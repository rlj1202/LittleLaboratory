package redlaboratory.littlelaboratory;

import android.content.Context;
import android.hardware.Sensor;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

public class MeasurementGraphListViewAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<MeasurementActivity.SensorHandler> handlers;

    public MeasurementGraphListViewAdapter(Context context, List<MeasurementActivity.SensorHandler> handlers) {
        this.handlers = handlers;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return handlers.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_graph, null);

            MeasurementActivity.SensorHandler sensorHandler = handlers.get(position);

            GraphView graphView = (GraphView) convertView.findViewById(R.id.graph);
            for (LineGraphSeries series : sensorHandler.getSerieses()) graphView.addSeries(series);
        }

        return convertView;
    }

    @Override
    public MeasurementActivity.SensorHandler getItem(int position) {
        return handlers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
