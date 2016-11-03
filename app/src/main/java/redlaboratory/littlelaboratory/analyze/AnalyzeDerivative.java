package redlaboratory.littlelaboratory.analyze;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import redlaboratory.littlelaboratory.R;
import redlaboratory.littlelaboratory.db.DataType;

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
    public DataType getAnalyzedDataType(DataType dataType) {
        switch (dataType) {
            case DATA_DISPLACEMENT: return DataType.DATA_ACCELERATION;
            case DATA_ACCELERATION: return DataType.DATA_JERK;
            default: return DataType.DATA_NONE;
        }
    }

    @Override
    public int getName() {
        return R.string.derivative;
    }

}
