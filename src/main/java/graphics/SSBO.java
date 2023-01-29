package graphics;

import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;
import util.StringUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class SSBO extends GLStruct{

    private static int next_available_location = 0;

    private final int id;  // GL provided instance ID used to reference where in memory this is stored.
    private final int id_copy;
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
        next_available_location+=2; // Double Buffer

        // Now we ask GL for an ID to use for the SSBO
        this.id = GL43.glGenBuffers();
        this.bind();
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, location, id);
        // Load data based on our initial capacity
        this.unbind();

        this.id_copy = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, id_copy);
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, location + 1, id_copy);
        // Load data based on our initial capacity
        this.unbind();
    }

    /**
     Thread Safe
     */
    public void flush(){
        bind();
        GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, this.buffer, GL43.GL_DYNAMIC_DRAW);
        unbind();
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, id_copy);
        GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, this.buffer, GL43.GL_DYNAMIC_DRAW);
        // Load data based on our initial capacity
        this.unbind();
    }

    public void allocate(int number_of_agents){
        this.capacity = number_of_agents; //Temp
        int buffer_capacity = this.computeSizeInBytes() * capacity;
        this.buffer = MemoryUtil.memAlloc(buffer_capacity);
        for(int i = 0; i < buffer_capacity; i++){
//            this.buffer.put((byte)(255*Math.random()));
            this.buffer.put((byte)127);
        }
        this.buffer.flip();
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
        substitutions.put("binding_location_second_buffer", this.location + 1);
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
            "} all_{{struct_name}};\n"+
            "layout(std{{version}}, binding = {{binding_location_second_buffer}}) buffer ssbo_{{struct_name}}_2{\n" +
            "{{struct_name}} agent[];\n" +
            "} all_{{struct_name}}_2;\n",
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
