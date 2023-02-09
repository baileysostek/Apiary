package graphics;

import com.google.gson.JsonElement;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL43;
import pegs.PegManager;
import util.MathUtil;
import util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;

public class ComputeShader {

    private int primary_buffer = -1;
    private int secondary_buffer = -1;

    private int instance_width  = 0;
    private int instance_height = 0;
    private int workgroup_width  = 1;
    private int workgroup_height = 1;

    private final JsonElement compute_source_nodes;

    private final String[] required_uniforms = new String[]{};

    // These are all of the computed includes and agents and buffers this simulation is referenceing.

    public ComputeShader(JsonElement compute_source_nodes, int instance_width, int instance_height){
        this.compute_source_nodes = compute_source_nodes;
        this.instance_width = instance_width;
        this.instance_height = instance_height;

        Vector2i optimal = MathUtil.find_optimal_tiling(instance_width, instance_height, ShaderManager.getInstance().getMaxWorkgroupInvocations());
        workgroup_width  = optimal.x;
        workgroup_height = optimal.y;

        regenerateShaders();
    }

    public void regenerateShaders(){
        if(primary_buffer >= 0) {
            ShaderManager.getInstance().deleteProgram(primary_buffer);
        }
        if(secondary_buffer >= 0) {
            ShaderManager.getInstance().deleteProgram(secondary_buffer);
        }

        int primary_shader_id = ShaderManager.getInstance().compileShader(GL43.GL_COMPUTE_SHADER, generateShaderSource(true));
        primary_buffer = ShaderManager.getInstance().linkShader(primary_shader_id);
        ShaderManager.getInstance().deleteShader(primary_shader_id);

        int secondary_shader_id = ShaderManager.getInstance().compileShader(GL43.GL_COMPUTE_SHADER, generateShaderSource(false));
        secondary_buffer = ShaderManager.getInstance().linkShader(secondary_shader_id);
        ShaderManager.getInstance().deleteShader(secondary_shader_id);
    }

    private String generateShaderSource(boolean isRead){
        // Clear the state of the required imports in the PegManager. This way we only import what is needed.
        PegManager.getInstance().clearPersistentData();

        // Define our substitutions to make to GLSL file
        HashMap<String, Object> substitutions = new HashMap<>();
        substitutions.put("shader_version", ShaderManager.getInstance().generateVersionString());
        substitutions.put("workgroup_width", this.workgroup_width);
        substitutions.put("workgroup_height", this.workgroup_height);

        // First thing we need to do besides mapping the current state of shader variables, is to compute our source code.
        substitutions.put("compute_source", PegManager.getInstance().transpile(compute_source_nodes));
        // Note this changes the state of PegManager.

        // Get the Agents accessor functions.
        String agent_ssbos = "";
        HashMap<String, SSBO> required_agents = PegManager.getInstance().getRequiredAgents();
        for(String agent_name : required_agents.keySet()){
            agent_ssbos += required_agents.get(agent_name).generateGLSL(isRead);
        }
        substitutions.put("agents_ssbos", agent_ssbos);

        // Uniforms
        String uniforms = "";
        HashSet<String> uniform_names = PegManager.getInstance().getRequiredUniforms();
        uniform_names.add("u_window_size");
        for(String uniform_name : uniform_names){
            if(ShaderManager.getInstance().hasUniform(uniform_name)) {
                uniforms += ShaderManager.getInstance().getUniform(uniform_name).toGLSL();
            }
        }
        substitutions.put("uniforms", uniforms);

        // Includes
        String includes = "";
        HashSet<String> required_includes = PegManager.getInstance().getRequiredIncludes();
        for(String requirement_name : required_includes){
            includes += String.format("#include %s\n", requirement_name);
        }
        substitutions.put("includes", includes);

        // Includes In Main
        String include_in_main = "";
        HashSet<String> required_to_include_in_main = PegManager.getInstance().getRequiredIncludesInMain();
        required_to_include_in_main.add("fragment_index");
        for(String requirement_name : required_to_include_in_main){
            include_in_main += String.format("#include %s\n", requirement_name);
        }
        substitutions.put("include_in_main", include_in_main);

//        substitutions.put("initializer_glsl", initializer_glsl);

        String source =
            "{{shader_version}}" +
            "{{uniforms}}" +
            "layout (local_size_x = {{workgroup_width}}, local_size_y = {{workgroup_height}}) in;\n" +
            "{{includes}}" +
            "{{agents_ssbos}}" +
            "void main() {\n" +
            "{{include_in_main}}" +
            "{{compute_source}}"+
            "}\n";

        return StringUtils.format(source, substitutions);
    }

    // Return a buffer depending on the current frame.
    // This gives us double buffering without a memcopy
    public int getShaderID(int current_frame){
        return (current_frame % 2 == 0) ? primary_buffer : secondary_buffer;
    }

    public void computeAndWait() {
        GL43.glDispatchCompute(this.instance_width / workgroup_width, this.instance_height/ workgroup_height, 1);
        GL43.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);
    }
}
