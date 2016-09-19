package redlaboratory.littlelaboratory;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

public class MeasurementGraphListViewAdapter extends BaseAdapter {

    private Context context;
    private List<MeasurementActivity.SensorListener> handlers;

    private LayoutInflater inflater;

    public MeasurementGraphListViewAdapter(Context context, List<MeasurementActivity.SensorListener> handlers) {
        this.context = context;
        this.handlers = handlers;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Log.i("LittleLaboratory", "Adapter load: " + handlers.size());
        for (MeasurementActivity.SensorListener handler : handlers) Log.i("LittleLaboratory", "" + handler.sensorType);
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

        MeasurementActivity.SensorListener sensorListener = handlers.get(position);

        GraphView graphView = (GraphView) convertView.findViewById(R.id.graph);
        graphView.removeAllSeries();
        for (LineGraphSeries series : sensorListener.series) graphView.addSeries(series);
//        graphView.setBackgroundColor(0x00000000);
//        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
//        staticLabelsFormatter.setHorizontalLabels(new String[] {"test", "test2", "test3"});
//        staticLabelsFormatter.setVerticalLabels(new String[] {"w", "t", "f"});
//        graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setYAxisBoundsManual(false);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(10);
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphView.getGridLabelRenderer().setLabelVerticalWidth(40);
//        graphView.getGridLabelRenderer().setHorizontalAxisTitle(context.getString(R.string.time));
        graphView.setTitle(context.getString(SensorInformation.fromSensorType(sensorListener.sensorType).getTitleStringId()) + ", " + sensorListener.sensorType);

        Log.i("LittleLaboratory", "Load view item: " + position + ", " + sensorListener.hashCode() + ", " + sensorListener.sensorType + "");

        return convertView;
    }

    @Override
    public MeasurementActivity.SensorListener getItem(int position) {
        return handlers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
