package redlaboratory.littlelaboratory;

import android.content.Context;
import android.hardware.Sensor;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

public class MeasurementGraphListViewAdapter extends BaseAdapter {

    private Context context;
    private List<MeasurementActivity.SensorHandler> handlers;

    private LayoutInflater inflater;

    public MeasurementGraphListViewAdapter(Context context, List<MeasurementActivity.SensorHandler> handlers) {
        this.context = context;
        this.handlers = handlers;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Log.i("MeasurementActivity", "Adapter load: " + handlers.size());
        for (MeasurementActivity.SensorHandler handler : handlers) Log.i("MeasurementActivity", "" + handler.getSensor().getType());
    }

    @Override
    public int getCount() {
        return handlers.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_graph, null);
        }

        MeasurementActivity.SensorHandler sensorHandler = handlers.get(position);

        GraphView graphView = (GraphView) convertView.findViewById(R.id.graph);
        graphView.removeAllSeries();
        for (LineGraphSeries series : sensorHandler.getSerieses()) graphView.addSeries(series);

//            graphView.setBackgroundColor(0x00000000);
//        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
//        staticLabelsFormatter.setHorizontalLabels(new String[] {"test", "test2", "test3"});
//        staticLabelsFormatter.setVerticalLabels(new String[] {"w", "t", "f"});
//        graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setYAxisBoundsManual(false);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(40);
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphView.getGridLabelRenderer().setLabelVerticalWidth(40);
//            graphView.getGridLabelRenderer().setHorizontalAxisTitle(context.getString(R.string.time));
        graphView.setTitle(context.getString(SensorInformation.fromSensorType(sensorHandler.getSensor().getType()).getTitleStringId()) + ", " + sensorHandler.getSensorType());

        Log.i("MeasurementActivity", "Load view item: " + position + ", " + sensorHandler.hashCode() + ", " + sensorHandler.getSensor().getType() + "");

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
