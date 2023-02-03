package graphics;

import org.lwjgl.opengl.GL43;

import java.util.Arrays;

public class Uniform {
    private final String name;
    private final GLDataType type;
    private final float[] value;

    protected Uniform(String name, GLDataType type) {
        this.name = name;
        this.type = type;

        this.value = new float[type.getSizeInFloats()];
        Arrays.fill(value, 0);
    }

    public float[] get(){
        return value;
    }

    public void set(float ... data){
        int index = 0;
        for(float value : data){
            setAtIndex(index, value);
            index++;
        }
    }

    public void setAtIndex(int index, float value){
        if(index >= 0 && index < this.value.length){
            this.value[index] = value;
        }else{
            System.out.println(String.format("warning : Tried to access index[%s] of values array however values is size[%s]", index+"", this.value.length+""));
        }
    }

    public void bind(){
        int bound_shader = ShaderManager.getInstance().getBoundShader();
        if(bound_shader >= 0) {
            int uniform_location = GL43.glGetUniformLocation(bound_shader, name);
            if(uniform_location >= 0){
                switch (type){
                    case INT:
                        GL43.glUniform1i(uniform_location, Math.round(value[0]));
                        break;
                    case FLOAT:
                        GL43.glUniform1f(uniform_location, value[0]);
                        break;
                    case VEC2:
                        GL43.glUniform2f(uniform_location, value[0], value[1]);
                        break;
                    case VEC3:
                        GL43.glUniform3f(uniform_location, value[0], value[1], value[2]);
                        break;
                    case VEC4:
                        GL43.glUniform4f(uniform_location, value[0], value[1], value[2], value[3]);
                        break;
                    case SAMPLER_CUBE:
                        break;
                    case SAMPLER_2D:
                        break;
                    case SAMPLER_3D:
                        break;
                    case MAT2:
                        break;
                    case MAT3:
                        break;
                    case MAT4:
                        break;
                    case BOOL:
                        break;
                }
            }
        }else{
            System.err.println(String.format("error : Trying to get the uniform location of uniform %s, however there is no currently bound shader program.", name));
        }
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return this.type.getGLSL();
    }
}
