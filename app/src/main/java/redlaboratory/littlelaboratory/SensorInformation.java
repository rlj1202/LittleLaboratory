package redlaboratory.littlelaboratory;

import android.hardware.Sensor;

import java.util.HashMap;

import redlaboratory.littlelaboratory.db.DataType;

public enum SensorInformation {
    TYPE_NONE(-1, 0, new String[] {}, new int[] {}, DataType.DATA_NONE),
    TYPE_ACCELEROMETER(Sensor.TYPE_ACCELEROMETER, 3, new String[] {"x", "y", "z"}, new int[] {0xffff0000, 0xff00ff00, 0xff0000ff}, DataType.DATA_ACCELERATION),
    TYPE_PROXIMITY(Sensor.TYPE_PROXIMITY, 1, new String[] {"proximity"}, new int[] {0xffffffff}, DataType.DATA_PROXIMITY),
    TYPE_MAGNETIC_FIELD(Sensor.TYPE_MAGNETIC_FIELD, 1, new String[] {"magneticField"}, new int[] {0xffffffff}, DataType.DATA_MAGNETIC_FIELD),
    TYPE_GRAVITY(Sensor.TYPE_GRAVITY, 3, new String[] {"x", "y", "z"}, new int[] {0xffff0000, 0xff00ff00, 0xff0000ff}, DataType.DATA_GRAVITY),
    TYPE_GYROSCOPE(Sensor.TYPE_GYROSCOPE, 3, new String[] {"x", "y", "z"}, new int[] {0xffff0000, 0xff00ff00, 0xff0000ff}, DataType.DATA_GYROSCOPE),
    TYPE_LIGHT(Sensor.TYPE_LIGHT, 1, new String[] {"light"}, new int[] {0xffffffff}, DataType.DATA_LIGHT)
    ;

    private static HashMap<Integer, SensorInformation> sensorInformations;

    static {
        sensorInformations = new HashMap<Integer, SensorInformation>();

        for (SensorInformation sensorInformation : values()) {
            sensorInformations.put(sensorInformation.getSensorType(), sensorInformation);
        }
    }

    private int sensorType;
    private int values;
    private String[] valueNames;
    private int[] colors;
    private DataType dataType;

    private SensorInformation(int sensorType, int values, String[] valueNames, int[] colors, DataType dataType) {
        this.sensorType = sensorType;
        this.values = values;
        this.valueNames = valueNames;
        this.colors = colors;
        this.dataType = dataType;
    }

    public int getSensorType() {
        return sensorType;
    }

    public int getValues() {
        return values;
    }

    public String[] getValueNames() {
        return valueNames;
    }

    public int[] getColors() {
        return colors;
    }

    public DataType getDataType() {
        return dataType;
    }

    public static SensorInformation fromSensorType(int sensorType) {
        SensorInformation sensorInformation = sensorInformations.get(sensorType);
        return sensorInformation != null ? sensorInformation : TYPE_NONE;
    }

}
