package simulation.world;

import graphics.GLDataType;
import graphics.GLStruct;

import java.util.Arrays;
import java.util.LinkedHashMap;

public abstract class World extends GLStruct {

    private LinkedHashMap<String, float[]> attribute_values = new LinkedHashMap<>();

    public World(String name) {
        super(name);
    }

    @Override
    public void onAddAttribute(String attribute_name, GLDataType attribute_type) {
        float[] value = new float[attribute_type.getSizeInFloats()];
        Arrays.fill(value, 0);
        attribute_values.put(attribute_name, value);
    }

    public void put(){

    }
}
