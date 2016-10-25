package redlaboratory.littlelaboratory.util;

import java.util.List;

public class ArrayUtil {

    public static double[] doubleListToDoubleArray(List<Double> list) {
        double[] array = new double[list.size()];

        for (int i = 0; i < list.size(); i++) array[i] = list.get(i);

        return array;
    }

}
