package redlaboratory.littlelaboratory.analyze;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import redlaboratory.littlelaboratory.R;

public class AnalyzeDerivative implements Analyze {

    @Override
    public List<Double> analyze(List<Double> data) {
        List<Double> newData = new ArrayList<>();

        Iterator<Double> it = data.iterator();

        double prevX = it.next();
        double prevY = it.next();

        for (; it.hasNext(); ) {
            double x = it.next();
            double y = it.next();

            double delta = (y - prevY) / (x - prevX);

            newData.add(prevX);
            newData.add(delta);

            prevX = x;
            prevY = y;
        }

        return newData;
    }

    @Override
    public int getName() {
        return R.string.derivative;
    }

}
