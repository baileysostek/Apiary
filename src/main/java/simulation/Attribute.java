package simulation;

import graphics.GLDataType;

public class Attribute {

    private final String name;
    private final GLDataType type;

    public Attribute(String name, GLDataType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public GLDataType getType() {
        return type;
    }
}
