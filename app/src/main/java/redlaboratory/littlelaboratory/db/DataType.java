package redlaboratory.littlelaboratory.db;

import java.util.HashMap;
import java.util.Map;

import redlaboratory.littlelaboratory.R;

public enum DataType {

    DATA_NONE(0, R.string.type_none),

    DATA_DISPLACEMENT(1, R.string.type_displacement),
    DATA_VELOCITY(2, R.string.type_velocity),
    DATA_ACCELERATION(3, R.string.type_accelerometer),
    DATA_LINEAR_ACCELERATION(4, R.string.type_linear_acceleration),
    DATA_JERK(5, R.string.type_jerk),

    DATA_MAGNETIC_FIELD(6, R.string.type_magnetic_field),
    DATA_GRAVITY(7, R.string.type_gravity),
    DATA_LIGHT(8, R.string.type_light),
    DATA_PRESSURE(9, R.string.type_pressure),
    DATA_PROXIMITY(10, R.string.type_proximity),
    DATA_GYROSCOPE(11, R.string.type_gyroscope),
    DATA_ORIENTATION(12, R.string.type_orientation),
    ;

    private static Map<Integer, DataType> dataTypesById;

    static {
        dataTypesById = new HashMap<>();

        for (DataType dataType : values()) {
            dataTypesById.put(dataType.getId(), dataType);
        }
    }

    private final int id;
    private final int nameStringId;

    private DataType(int id, int nameStringId) {
        this.id = id;
        this.nameStringId = nameStringId;
    }

    public int getId() {
        return id;
    }

    public int getNameStringId() {
        return nameStringId;
    }

    public static DataType fromId(int id) {
        return dataTypesById.get(id);
    }

}
