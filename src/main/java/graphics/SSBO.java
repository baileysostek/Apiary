package graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;
import util.StringUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SSBO extends GLStruct{

    private static int next_available_location = 0;

    private final int id;  // GL provided instance ID used to reference where in memory this is stored.
    private final int location; // Sequential integer representing the location where this SSBO is bound.
    private int capacity = 1;

    private ByteBuffer buffer;

    public SSBO (String name, LinkedHashMap<String, GLDataType> attributes){
        super(name);

        // Load in our attributes
        for(String attribute_name : attributes.keySet()){
            this.addAttribute(attribute_name, attributes.get(attribute_name));
        }

        // Locations of SSBOs are determined when SSBOs are generated.
        // These are globally unique and constant.
        this.location = next_available_location;
        next_available_location++;

        // Now we ask GL for an ID to use for the SSBO
        this.id = GL43.glGenBuffers();
        this.bind();
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, location, id);
        // Load data based on our initial capacity
        this.unbind();
    }

    /**
     Thread Safe
     */
    public void flush(){
        bind();
        GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, this.buffer, GL43.GL_DYNAMIC_DRAW);
        //TODO: move this to a ByteBuffer upload instead of float array. That will better support datatypes GPU side.
        unbind();
    }

    public void allocate(int number_of_agents){
        this.capacity = number_of_agents; //Temp
        this.buffer = MemoryUtil.memAlloc(this.capacity);
        for(int i = 0; i < this.capacity / 4; i++){
            this.buffer.putFloat((float) Math.random());
            if(i % 100000 == 0) {
                System.out.println(i);
            }
        }
        this.buffer.flip();
//
//        for(int i = 0; i < this.data.length; i++){
//            this.data[i] = (float) Math.random();
//            if(i % 1000 == 0) {
//                System.out.println(i);
//            }
//        }
    }

    private void bind(){
        if(id >= 0) {
            GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, id);
        }else{
            System.out.println("Error tried to bind SSBO before id was assigned.");
            System.exit(1);
        }
    }

    private void unbind(){
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
    }

    /**\
     * Genreates the correct GLSL accessor for this SSBO. This function is called when a simulation is loaded per shader step that needs to access the agent this SSBO represents.
     * @return
     */
    public String generateGLSL(){

        HashMap<String, Object> substitutions = new HashMap<>();
        substitutions.put("version", ShaderManager.getInstance().getGLTargetVersion());
        substitutions.put("binding_location", this.location);
        substitutions.put("struct_name", this.getName());

        // Compute how to represent our attributes
        String attribute_definitions = "";
        for(String attribute_name : this.getAttributes().keySet()){
            GLDataType type = this.getAttributes().get(attribute_name);
            attribute_definitions += String.format("\t%s %s;\n", type.getGLSL(), attribute_name);
        }
        substitutions.put("attributes", attribute_definitions);

        return StringUtils.format(
    "struct {{struct_name}}\n" +
            "{\n" +
            "{{attributes}}"+
            "};\n" +
            "layout(std{{version}}, binding = {{binding_location}}) buffer ssbo_{{struct_name}}{\n" +
            "{{struct_name}} agent[];\n" +
            "} all_{{struct_name}};\n",
            substitutions
        );
    }

    /**
     * On Shutdown we will execute all cleanup functions to correctly deallocate buffers and such.
     */
    public void cleanup(){
        MemoryUtil.memFree(this.buffer);
        GL43.glDeleteBuffers(id);
    }

    public int getCapacity() {
        return this.capacity;
    }
}
