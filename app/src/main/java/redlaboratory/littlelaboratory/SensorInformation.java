package redlaboratory.littlelaboratory;

import android.hardware.Sensor;

import java.util.HashMap;

public enum SensorInformation {
    TYPE_NONE(-1, R.string.type_none, new String[] {}, new int[] {}),
    TYPE_ACCELEROMETER(Sensor.TYPE_ACCELEROMETER, R.string.type_accelerometer, new String[] {"x", "y", "z"}, new int[] {0xffff0000, 0xff00ff00, 0xff0000ff}),
    TYPE_PROXIMITY(Sensor.TYPE_PROXIMITY, R.string.type_proximity, new String[] {"proximity"}, new int[] {0xffffffff}),
    TYPE_MAGNETIC_FIELD(Sensor.TYPE_MAGNETIC_FIELD, R.string.type_magnetic_field, new String[] {"magneticField"}, new int[] {0xffffffff}),
    TYPE_GRAVITY(Sensor.TYPE_GRAVITY, R.string.type_gravity, new String[] {"x", "y", "z"}, new int[] {0xffff0000, 0xff00ff00, 0xff0000ff}),
    TYPE_GYROSCOPE(Sensor.TYPE_GYROSCOPE, R.string.type_gyroscope, new String[] {"x", "y", "z"}, new int[] {0xffff0000, 0xff00ff00, 0xff0000ff}),
    TYPE_LIGHT(Sensor.TYPE_LIGHT, R.string.type_light, new String[] {"light"}, new int[] {0xffffffff})
    ;

    private static HashMap<Integer, SensorInformation> sensorInformations;

    static {
        sensorInformations = new HashMap<Integer, SensorInformation>();

        for (SensorInformation sensorInformation : values()) {
            sensorInformations.put(sensorInformation.getSensorType(), sensorInformation);
        }
    }

    private int sensorType;
    private int titleStringId;
    private String[] valueNames;
    private int[] colors;

    private SensorInformation(int sensorType, int titleStringId, String[] valueNames, int[] colors) {
        this.sensorType = sensorType;
        this.titleStringId = titleStringId;
        this.valueNames = valueNames;
        this.colors = colors;
    }

    public int getSensorType() {
        return sensorType;
    }

    public int getTitleStringId() {
        return titleStringId;
    }

    public String[] getValueNames() {
        return valueNames;
    }

    public int[] getColors() {
        return colors;
    }

    public static SensorInformation fromSensorType(int sensorType) {
        SensorInformation sensorInformation = sensorInformations.get(sensorType);
        return sensorInformation != null ? sensorInformation : TYPE_NONE;
    }

}
