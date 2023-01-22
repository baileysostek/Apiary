package graphics;

public enum GLDataType{

    //Different data types that we can use in our shader.
    INT(Integer.BYTES, 1, "int"),

    FLOAT(Float.BYTES, 1, "float"),
    VEC2 (Float.BYTES, 2, "vec2"),
    VEC3 (Float.BYTES, 3, "vec3"),
    VEC4 (Float.BYTES, 4, "vec4"),

    SAMPLER_CUBE(Integer.BYTES, 1, "samplerCube"),
    SAMPLER_2D(Integer.BYTES, 1, "sampler2D"),
    SAMPLER_3D(Integer.BYTES, 1, "sampler3D"),

    //Matrix
    MAT2(Float.BYTES, 4, "mat2"),
    MAT3(Float.BYTES, 9, "mat3"),
    MAT4(Float.BYTES, 16, "mat4"),

    //Bool
    BOOL(1, 1, "bool"),
    ;

    //Size in bytes of one pice of data
    private int instance_size; //Size of a single piece of data in bytes. IE a float
    private int size_per_vertex;
    private String glsl_representation;

    GLDataType(int instanceSize, int sizePerVertex, String glsl_representiation){
        this.instance_size = instanceSize;
        this.size_per_vertex = sizePerVertex;
        this.glsl_representation = glsl_representiation;
    }

    public int getSize(){
        return this.instance_size;
    }

    public int getSize_per_vertex() {
        return this.size_per_vertex;
    }

    public int getSizeInBytes(){
        return this.instance_size * this.size_per_vertex;
    }

    public int getSizeInFloats(){
        return (int) Math.ceil(this.getSizeInBytes() / Float.BYTES);
    }

    public String getGLSL() {
        return glsl_representation;
    }
}
