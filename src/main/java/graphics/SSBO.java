package graphics;

import org.lwjgl.opengl.GL43;
import util.MathUtil;
import util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * SSBOs are shared memory buffers that are able to be accessed from the GPU and CPU.
 * This SSBO class makes several assumptions as to how a user wants to interface with GPU memory.
 * This class sets up two SSBOs with very similar names.
 * One is named {{agent_name}}_read and the other is {{agent_name}}_write
 * This sets up two buffers in GPU memory. Since we are using Compute shaders to access the contents of this SSBO,
 * there is no guarantee as to when specific areas of this SSBO are accessed. This can create race cibTo fix this we disallow the program to
 * continue until all Compute shaders have finished.
 */
public class SSBO extends GLStruct{
    private final int id_read;  // GL provided instance ID used to reference where in memory this is stored.
    private final int id_write; // GL provided instance ID used to reference where in memory this is stored.

    // Binding locations for the read and write portions of this SSBO
    private final int location_read;  // Sequential integer representing the location where this SSBO is bound.
    private final int location_write; // Sequential integer representing the location where this SSBO is bound.

    // The starting capacity.
    private int capacity = 1;

    // This is a byte buffer representing the content stored in our SSBO.
//    private ByteBuffer buffer;
    private int buffer_size;

    public SSBO (String name, LinkedHashMap<String, GLDataType> attributes){
        super(name);

        // Load in our attributes
        for(String attribute_name : attributes.keySet()){
            this.addAttribute(attribute_name, attributes.get(attribute_name));
        }

        // Locations of SSBOs are determined when SSBOs are generated.
        // These are globally unique and constant.
        this.location_read  = ShaderManager.getInstance().getNextAvailableSSBOLocation();
        this.location_write = ShaderManager.getInstance().getNextAvailableSSBOLocation();

        // Now we ask GL for an ID to use for the SSBO
        this.id_read = GL43.glGenBuffers();
        this.bind();
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, location_read, id_read);
        // Load data based on our initial capacity
        this.unbind();

        this.id_write = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, id_write);
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, location_write, id_write);
        // Load data based on our initial capacity
        this.unbind();
    }

    /**
     Thread Safe
     */
    public void flush(){
        bind(); // Bind Read
        GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, buffer_size, GL43.GL_DYNAMIC_DRAW);
        unbind();
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, id_write); // Bind write
        GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, buffer_size, GL43.GL_DYNAMIC_DRAW);
        unbind();
    }

    public void allocate(int number_of_agents){

//        // I am not sire if this memory alignement is needed at all.
        this.capacity = number_of_agents; //Temp
        int alignment_4 = 4 - (this.computeSizeInBytes() % 4);
        int alignment_16 = 16 - (this.computeSizeInBytes() % 16);
//        buffer_size = ((long) (this.computeSizeInBytes()) * capacity); //TODO maybe remove?
        buffer_size = ((int) (this.computeSizeInBytes() + (this.computeSizeInBytes() < 4 ? alignment_4 : (this.computeSizeInBytes() == 4) ? 0 :alignment_16)) * capacity);

//        this.capacity = number_of_agents;
//        int bytes = this.computeSizeInBytes(); // Check if this is a multiple of 4
//        int offset = bytes <= 4 ? MathUtil.computeAlignment(bytes, 4) : MathUtil.computeAlignment(bytes, 16);
//        buffer_size = ( (bytes + offset) * capacity);
//        System.out.println(String.format("Buffer Size:%s Larger than int:%s", buffer_size, buffer_size > Integer.MAX_VALUE));
    }

    private void bind(){
        if(id_read >= 0) {
            GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, id_read);
        }else{
            System.out.println("Error tried to bind SSBO before id was assigned.");
            System.exit(1);
        }
    }

    private void unbind(){
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
    }

    public String generateGLSL(){
        return generateGLSL(true);
    }

    /**\
     * Genreates the correct GLSL accessor for this SSBO. This function is called when a simulation is loaded per shader step that needs to access the agent this SSBO represents.
     * @return
     */
    public String generateGLSL(boolean isRead){

        HashMap<String, Object> substitutions = new HashMap<>();
        substitutions.put("version", ShaderManager.getInstance().getGLTargetVersion());
        substitutions.put("binding_location_read", (isRead ? this.location_read : this.location_write));
        substitutions.put("binding_location_write", (isRead ? this.location_write : this.location_read));
        substitutions.put("struct_name", this.getName());
        substitutions.put("read_name", ShaderManager.getInstance().getSSBOReadIdentifier());
        substitutions.put("write_name", ShaderManager.getInstance().getSSBOWriteIdentifier());

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
            "layout(std{{version}}, binding = {{binding_location_read}}) buffer ssbo_{{struct_name}}{{read_name}}{\n" +
            "{{struct_name}} agent[];\n" +
            "}{{struct_name}}{{read_name}};\n"+
            "layout(std{{version}}, binding = {{binding_location_write}}) buffer ssbo_{{struct_name}}{{write_name}}{\n" +
            "{{struct_name}} agent[];\n" +
            "}{{struct_name}}{{write_name}};\n",
            substitutions
        );
    }

    // Convenient strings used to help generate references.
    public String generateReadString(String index){
        return String.format("");
    }

    /**
     * On Shutdown we will execute all cleanup functions to correctly deallocate buffers and such.
     */
    public void cleanup(){
        GL43.glDeleteBuffers(id_read);
        GL43.glDeleteBuffers(id_write);
        ShaderManager.getInstance().freeSSBOLocation(location_read);
        ShaderManager.getInstance().freeSSBOLocation(location_write);
    }

    public int getCapacity() {
        return this.capacity;
    }

    public long getBufferSizeInBytes() {
        return buffer_size * 2; // We need to x2 here because we are double buffering.
    }
}
