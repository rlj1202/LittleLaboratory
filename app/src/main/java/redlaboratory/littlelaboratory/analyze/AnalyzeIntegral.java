package redlaboratory.littlelaboratory.analyze;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import redlaboratory.littlelaboratory.R;
import redlaboratory.littlelaboratory.db.DataType;

public class AnalyzeIntegral implements Analyze {

    @Override
    public List<Double> analyze(List<Double> data) {
        List<Double> newData = new ArrayList<>();

        Iterator<Double> it = data.iterator();

        double prevX = it.next();
        double prevY = it.next();

        double total = 0;

        for (; it.hasNext(); ) {
            double x = it.next();
            double y = it.next();

            double xDelta = Math.abs(x - prevX);
            double surface = (y + prevY) * xDelta / 2;

            total += surface;

            newData.add(prevX);
            newData.add(total);

            prevX = x;
            prevY = y;
        }

        return newData;
    }

    @Override
    public DataType getAnalyzedDataType(DataType dataType) {
        switch (dataType) {
        case DATA_VELOCITY: return DataType.DATA_DISPLACEMENT;
        case DATA_ACCELERATION: return DataType.DATA_VELOCITY;
        case DATA_JERK: return DataType.DATA_ACCELERATION;
        default: return DataType.DATA_NONE;
        }
    }

    @Override
    public int getName() {
        return R.string.integral;
    }

}
