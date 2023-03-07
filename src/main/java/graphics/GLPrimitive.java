package graphics;

public class GLPrimitive extends GLStruct{

    private GLDataType type;

    public GLPrimitive(String name, GLDataType type) {
        super(name);
        this.type = type;

        // Add this as a type.
        super.addAttribute(name, type);
    }

    public GLDataType getPrimitiveType(){
        return this.type;
    }
}
