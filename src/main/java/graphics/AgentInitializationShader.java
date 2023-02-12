package graphics;

import com.google.gson.JsonObject;
import input.Mouse;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL43;
import nodes.NodeManager;
import simulation.SimulationManager;
import util.MathUtil;
import util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class AgentInitializationShader {

    private int program_id;

    // This represents how much work we need to do with this shader.
    private int instance_width;
    private int instance_height;
    private int workgroup_width;
    private int workgroup_height;

    // These are some precomputed values which determine how many shader invocations we will be doing
    int invocations_width;
    int invocations_height;

    public AgentInitializationShader(String agent_name, JsonObject agent_attributes) {
        // Get the SSBO
        SSBO agent_ssbo = SimulationManager.getInstance().getAgent(agent_name);

        int allocated = agent_ssbo.getCapacity();

        int[] factors = MathUtil.factor(allocated);

        instance_width  = factors[(factors.length / 2) - 1];
        instance_height = factors[factors.length / 2];

        Vector2i optimal = MathUtil.find_optimal_tiling(instance_width, instance_height, ShaderManager.getInstance().getMaxWorkgroupInvocations());
        workgroup_width  = optimal.x;
        workgroup_height = optimal.y;

        this.invocations_width = this.instance_width / workgroup_width;
        this.invocations_height = this.instance_height/ workgroup_height;

        // Buffer for holding strings while we are generating the agent.
        String attribute_initializer_setup_data = "";
        LinkedList<String> attributes_initialization_glsl = new LinkedList<>();
        String attributes_copy_glsl = "";

        for (String attribute_name : agent_attributes.keySet()) {
            JsonObject attribute_data = agent_attributes.get(attribute_name).getAsJsonObject();
            // In an attribute definition we also define the way that the data is initialized. We need to generate GLSL from these instructions and put that glsl in a compute shader.
            if(attribute_data.has("default_value")) {
                // TODO: the transpialtion step on the line below can produce multiple lines, rather than a string substitution inject the strings onto the end of the default_value array.
                // This will have the effect of just adding on the setter in the correct place and let any additional logic happen as well.
                String[] default_value = NodeManager.getInstance().transpile(attribute_data.get("default_value").getAsJsonArray()).split("\n");
                for(int i = 0; i < default_value.length - 1; i++){
                    attribute_initializer_setup_data += default_value[i];
                }
                String attribute_initializer_glsl = String.format("[\"%s\", \"%s\", \"%s\", \"%s\", \"@agent_write\"]", agent_name, "fragment_index", attribute_name, default_value[default_value.length - 1]);
                attributes_initialization_glsl.push(attribute_initializer_glsl);
                attributes_copy_glsl += String.format("%s_read.agent[fragment_index].%s = %s_write.agent[fragment_index].%s;\n", agent_name, attribute_name, agent_name, attribute_name);
            }
        }

        String agent_ssbo_glsl = "";
        String initializer_glsl = "";

        // Now that the agent is added, we can get the SSBO accessor code
        agent_ssbo_glsl += agent_ssbo.generateGLSL();

        initializer_glsl += attribute_initializer_setup_data;
        // Now we need to compute the initialization data used for this agent.
        // This code will be inserted into an initialization shader.
        for(String attribute_initializer_glsl : attributes_initialization_glsl){
            // Populate the write buffer
            initializer_glsl += String.format("%s\n", NodeManager.getInstance().transpile(attribute_initializer_glsl));
        }
        initializer_glsl += attributes_copy_glsl;

        // We are going to need to construct a compute shader to initialize this agent's data.
        HashMap<String, Object> substitutions = new HashMap<>();
        substitutions.put("shader_version", ShaderManager.getInstance().generateVersionString());
        substitutions.put("workgroup_width", workgroup_width);
        substitutions.put("workgroup_height", workgroup_height);
        substitutions.put("agent_ssbo_glsl", agent_ssbo_glsl);
        substitutions.put("initializer_glsl", initializer_glsl);

        // Uniforms
        String uniforms = "";
        HashSet<String> uniform_names = NodeManager.getInstance().getRequiredUniforms();
        uniform_names.add("u_window_size");
        for(String uniform_name : uniform_names){
            if(ShaderManager.getInstance().hasUniform(uniform_name)) {
                uniforms += ShaderManager.getInstance().getUniform(uniform_name).toGLSL();
            }
        }
        substitutions.put("uniforms", uniforms);

        // Includes
        String includes = "";
        HashSet<String> required_includes = NodeManager.getInstance().getRequiredIncludes();
        for(String requirement_name : required_includes){
            includes += String.format("#include %s\n", requirement_name);
        }
        substitutions.put("includes", includes);

        // Includes In Main
        String include_in_main = "";
        HashSet<String> required_to_include_in_main = NodeManager.getInstance().getRequiredIncludesInMain();
        required_to_include_in_main.add("fragment_index");
        for(String requirement_name : required_to_include_in_main){
            include_in_main += String.format("#include %s\n", requirement_name);
        }
        substitutions.put("include_in_main", include_in_main);

        String initialize_source = StringUtils.format(
    "{{shader_version}}" +
            "{{uniforms}}" +
            "layout (local_size_x = {{workgroup_width}}, local_size_y = {{workgroup_height}}) in;\n" +
            "{{includes}}" +
            "{{agent_ssbo_glsl}}" +
            "void main() {\n" +
            "{{include_in_main}}" +
            "{{compute_source}}"+
            "{{initializer_glsl}}" +
            "}\n", substitutions
        );

        int shader_id = ShaderManager.getInstance().compileShader(GL43.GL_COMPUTE_SHADER, initialize_source);
        this.program_id = ShaderManager.getInstance().linkShader(shader_id);
        ShaderManager.getInstance().deleteShader(shader_id);
    }

    public void initialize(){
        ShaderManager.getInstance().bind(program_id);
        Mouse.getInstance().bindUniforms();
        ShaderManager.getInstance().bindUniforms();
        GL43.glDispatchCompute(invocations_width, invocations_height, 1);
        GL43.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);
    }

    public void destroy() {
        ShaderManager.getInstance().deleteProgram(this.program_id);
    }
}
