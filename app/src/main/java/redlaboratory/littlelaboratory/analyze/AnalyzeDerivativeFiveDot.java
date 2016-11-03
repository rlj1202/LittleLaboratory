package redlaboratory.littlelaboratory.analyze;

import java.util.ArrayList;
import java.util.List;

import redlaboratory.littlelaboratory.R;
import redlaboratory.littlelaboratory.db.DataType;
import redlaboratory.littlelaboratory.util.ArrayUtil;

public class AnalyzeDerivativeFiveDot implements Analyze {

    @Override
    public List<Double> analyze(List<Double> data) {
        List<Double> newData = new ArrayList<>();

        double[] rawData = ArrayUtil.doubleListToDoubleArray(data);

        for (int x = 0; x < rawData.length / 2; x++) {
            double dot1 = rawData[Math.max(x - 2, 0)];
            double dot2 = rawData[Math.max(x + 2, 0)];
            double dot3 = rawData[Math.max(x - 1, 0)];
            double dot4 = rawData[Math.max(x + 1, 0)];
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
