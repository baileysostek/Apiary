package simulation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import core.Apiary;
import graphics.GLDataType;
import graphics.SSBO;
import graphics.ShaderManager;
import graphics.VAO;
import input.Mouse;
import org.lwjgl.opengl.GL43;
import pegs.PegManager;
import simulation.world.World;
import simulation.world.World2D;
import util.JsonUtils;

import java.nio.FloatBuffer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class Simulation {

    private final int program_id;
    private final int initialize_id;
    private final int compute_id;
    private final int copy_id; // Double buffer copy.
    private final VAO vao;

    private float simulation_time = 0;
    private float simulation_updates_per_second = 8000.0f;
    private float simulation_target_time = ( 1.0f / simulation_updates_per_second);

    float vertices[] = {
        -1.0f, -1.0f, 0.0f,
         1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
         1.0f, -1.0f, 0.0f,
        -1.0f,  1.0f, 0.0f,
         1.0f,  1.0f, 0.0f
    };

//    float vertices[] = {
//        -0.5f, -0.5f, 0.0f,
//        0.5f, -0.5f, 0.0f,
//        0.0f,  0.5f, 0.0f
//    };




    private String name;
    private World simulation_world; // The simulation world is used as the tempalte for the worldstate during simulation.
    // We have 2D and 3D worlds ready for simulations.
    private LinkedHashMap<String, SSBO> agents = new LinkedHashMap<>();

    private LinkedHashSet<Step> pipeline = new LinkedHashSet<Step>();

    private float delta_over_time = 0;
    private boolean initialized = false;

    /**
     * We use constructor overloading
     * @param path
     */
    protected Simulation(String path) {
        this(JsonUtils.loadJson(path));
    }

    protected Simulation(JsonObject object){
        // First thing we do is set the active simulation to this one
        SimulationManager.getInstance().setActiveSimulation(this);

        // Determine what world template we are using
        JsonObject simulation_world_template = object.getAsJsonObject("world");
        resolve_world_template:{
            // Check for a name
            String simulation_world_name = "Default Simulation Name";
            if(simulation_world_template.has("name")){
                simulation_world_name = simulation_world_template.get("name").getAsString();
            }
            // Resolve the type
            if (simulation_world_template.has("type")) {
                String template_identifier = simulation_world_template.get("type").getAsString();
                //TODO: Better resolution of what template the user wants that just an IF chain here
                if(template_identifier.toLowerCase().equals("2d")){
                    this.simulation_world = new World2D(simulation_world_name);
                    break resolve_world_template;
                }

                // We didnt recognise the world type
                System.err.println(String.format("Error: Simulation:\"%s\" uses a world template that we did not recognise. Unrecognised template:\"%s\"", simulation_world_name, template_identifier));
            } else {
                System.err.println(String.format("Error: Simulation:\"%s\" does not specify a world template. Using default of World2D."));
            }

            // At this point we have not resolved the world template so use the default world template of World2D
            this.simulation_world = new World2D(simulation_world_name);
        }
        // Load the agents
        JsonObject simulation_agents = object.getAsJsonObject("agents");

        for(String agent_name : simulation_agents.keySet()){
            JsonObject agent = simulation_agents.get(agent_name).getAsJsonObject();

            // Here we define what attributes are present on this agent.
            LinkedHashMap<String, GLDataType> agent_types = new LinkedHashMap<>();

            // This is where we will hold attributes for this agent.
            SSBO agent_ssbo = new SSBO(agent_name, agent_types);

            JsonObject agent_attributes = agent.get("attributes").getAsJsonObject();
            for (String attribute_name : agent_attributes.keySet()) {
                JsonObject attribute_data = agent_attributes.get(attribute_name).getAsJsonObject();
                String attribute_type_name = attribute_data.get("type").getAsString();
                GLDataType attribute_type = ShaderManager.getInstance().getGLDataTypeFromString(attribute_type_name);
                if (attribute_type != null) {
                    agent_ssbo.addAttribute(attribute_name, attribute_type);
                } else {
                    System.err.println(String.format("Unrecognised GL uniform type[%s] for agent[%s] at attribute[%s].", attribute_type_name, agent_name, attribute_name));
                }
            }

//            agent_ssbo.allocate(Integer.MAX_VALUE / 32);
            agent_ssbo.allocate(Apiary.getWindowWidth() * Apiary.getWindowHeight());
            agent_ssbo.flush();

            // Add agent to sim.
            agents.put(agent_name, agent_ssbo);
        }
        // Now we are going to determine the pipeline steps used
        JsonArray simulation_steps = object.getAsJsonArray("steps");
        for(int i = 0; i < simulation_steps.size(); i++){
            JsonObject step = simulation_steps.get(i).getAsJsonObject();
            // Process data about each step.
//            if(JsonUtils.validate(step, JsonUtils.getInstance().STEP_SCHEMA)){
                JsonArray pegs_input = step.getAsJsonArray("logic");
                String glsl = PegManager.getInstance().transpile(pegs_input);
                System.out.println(glsl);
//            }
            // Schema validate the step
        }

        // Shader

        String vertex_source =
                String.format("#version %s\n", ShaderManager.getInstance().getGLTargetVersion()) +
                "layout (location = 0) in vec3 position;\n" +
                "out vec3 pass_position;\n" +
                "void main(void){\n" +
                "pass_position = position;\n" +
                "gl_Position = vec4(position, 1.0);\n" +
                "}\n";

        String fragment_source =
                String.format("#version %s\n", ShaderManager.getInstance().getGLTargetVersion()) +
                        agents.get("cell").generateGLSL() +
                        "in vec3 pass_position;\n" +
                        "uniform vec2 u_window_size;\n" +
                        "uniform vec2 u_mouse_pos_pixels;\n" +
                        "uniform float u_time_seconds;\n" +
                        "out vec4 out_color; \n" +
                        "void main(void){\n" +
                        "vec2 screen_pos = (pass_position.xy + vec2(1.0)) / vec2(2.0);\n" +
                        "int x_pos = int(floor(screen_pos.x * u_window_size.x));\n" +
                        "int y_pos = int(floor(screen_pos.y * u_window_size.y));\n" +
                        "int fragment_index = x_pos + (y_pos * int(u_window_size.x));\n" +
                        "if (all_cell.agent[fragment_index].alive) {\n" +
                        "    out_color = vec4(all_cell.agent[fragment_index].color , 1.0);\n" +
//                        "    out_color = vec4(1.0);\n" +
                        "} else {\n" +
                        "    out_color = vec4(0.0 , 0.0 , 0.0 , 1.0);\n" +
                        "}\n" +
                        "}\n";

//        String fragment_source = String.format("#version %s\n", ShaderManager.getInstance().getGLTargetVersion()) +
//            "in vec3 pass_position;\n" +
//            "uniform vec2 u_window_size;\n" +
//            "uniform vec2 u_mouse_pos_pixels;\n" +
//            "uniform float u_time_seconds;\n" +
//            "out vec4 out_color; \n" +
//            "void main(void){\n" +
//            "    out_color = vec4(0.0 , 0.0 , 0.0 , 1.0);\n" +
//            "}\n";

        int vertex   = ShaderManager.getInstance().compileShader(GL43.GL_VERTEX_SHADER, vertex_source);
        int fragment = ShaderManager.getInstance().compileShader(GL43.GL_FRAGMENT_SHADER, fragment_source);
        this.program_id = ShaderManager.getInstance().linkShader(vertex, fragment);

        String compute_source =
            "#version 430 core\n" +
            "\n" +
            "uniform vec2 u_window_size;\n" +
            "layout (local_size_x = 1, local_size_y = 1) in;\n" +
            agents.get("cell").generateGLSL() +
            "void main() {\n" +
            "int x_pos = int(gl_GlobalInvocationID.x);\n" +
            "int y_pos = int(gl_GlobalInvocationID.y);\n" +
            "int window_width_pixels = int(u_window_size.x);\n" +
            "int window_height_pixels = int(u_window_size.y);\n" +
            "int fragment_index = x_pos + (y_pos * window_width_pixels);\n" +
            "int neighbors = 0;\n" +
            "neighbors += all_cell.agent[int(mod(x_pos - 1, window_width_pixels)) + (int(mod(y_pos - 1, window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //top left\n" +
            "neighbors += all_cell.agent[int(mod(x_pos    , window_width_pixels)) + (int(mod(y_pos - 1, window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //top middle\n" +
            "neighbors += all_cell.agent[int(mod(x_pos + 1, window_width_pixels)) + (int(mod(y_pos - 1, window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //top right\n" +
            "neighbors += all_cell.agent[int(mod(x_pos - 1, window_width_pixels)) + (int(mod(y_pos    , window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //mid left\n" +
            "neighbors += all_cell.agent[int(mod(x_pos + 1, window_width_pixels)) + (int(mod(y_pos    , window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //mid right\n" +
            "neighbors += all_cell.agent[int(mod(x_pos - 1, window_width_pixels)) + (int(mod(y_pos + 1, window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //bottom left\n" +
            "neighbors += all_cell.agent[int(mod(x_pos    , window_width_pixels)) + (int(mod(y_pos + 1, window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //bottom mid\n" +
            "neighbors += all_cell.agent[int(mod(x_pos + 1, window_width_pixels)) + (int(mod(y_pos + 1, window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //bottom right\n" +

            "if(all_cell.agent[fragment_index].alive){\n" +
            "all_cell_2.agent[fragment_index].alive = (neighbors == 2 || neighbors == 3);\n" +
//                    "all_cell_2.agent[int(mod(x_pos - 1, window_width_pixels)) + (int(mod(y_pos - 1, window_height_pixels)) * window_width_pixels)].alive = true; //top left\n" +
//                    "all_cell_2.agent[int(mod(x_pos    , window_width_pixels)) + (int(mod(y_pos - 1, window_height_pixels)) * window_width_pixels)].alive = true; //top middle\n" +
//                    "all_cell_2.agent[int(mod(x_pos + 1, window_width_pixels)) + (int(mod(y_pos - 1, window_height_pixels)) * window_width_pixels)].alive = true; //top right\n" +
//                    "all_cell_2.agent[int(mod(x_pos - 1, window_width_pixels)) + (int(mod(y_pos    , window_height_pixels)) * window_width_pixels)].alive = true; //mid left\n" +
//                    "all_cell_2.agent[int(mod(x_pos + 1, window_width_pixels)) + (int(mod(y_pos    , window_height_pixels)) * window_width_pixels)].alive = true; //mid right\n" +
//                    "all_cell_2.agent[int(mod(x_pos - 1, window_width_pixels)) + (int(mod(y_pos + 1, window_height_pixels)) * window_width_pixels)].alive = true; //bottom left\n" +
//                    "all_cell_2.agent[int(mod(x_pos    , window_width_pixels)) + (int(mod(y_pos + 1, window_height_pixels)) * window_width_pixels)].alive = true; //bottom mid\n" +
//                    "all_cell_2.agent[int(mod(x_pos + 1, window_width_pixels)) + (int(mod(y_pos + 1, window_height_pixels)) * window_width_pixels)].alive = true; //bottom right\n" +
            "}else{\n" +
            "all_cell_2.agent[fragment_index].alive = (neighbors == 3);\n" +
            "}\n" +
            "}\n";

        int compute   = ShaderManager.getInstance().compileShader(GL43.GL_COMPUTE_SHADER, compute_source);
        this.compute_id = ShaderManager.getInstance().linkShader(compute);

        String initialize_source =
                "#version 430 core\n" +
                        "\n" +
                        "uniform float u_time_seconds;\n" +
                        "uniform vec2 u_window_size;\n" +
                        "layout (local_size_x = 1, local_size_y = 1) in;\n" +
                        agents.get("cell").generateGLSL() +
                        "// A single iteration of Bob Jenkins' One-At-A-Time hashing algorithm.\n" +
                        "uint hash( uint x ) {\n" +
                        "    x += ( x << 10u );\n" +
                        "    x ^= ( x >>  6u );\n" +
                        "    x += ( x <<  3u );\n" +
                        "    x ^= ( x >> 11u );\n" +
                        "    x += ( x << 15u );\n" +
                        "    return x;\n" +
                        "}\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "// Compound versions of the hashing algorithm I whipped together.\n" +
                        "uint hash( uvec2 v ) { return hash( v.x ^ hash(v.y)                         ); }\n" +
                        "uint hash( uvec3 v ) { return hash( v.x ^ hash(v.y) ^ hash(v.z)             ); }\n" +
                        "uint hash( uvec4 v ) { return hash( v.x ^ hash(v.y) ^ hash(v.z) ^ hash(v.w) ); }\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "// Construct a float with half-open range [0:1] using low 23 bits.\n" +
                        "// All zeroes yields 0.0, all ones yields the next smallest representable value below 1.0.\n" +
                        "float floatConstruct( uint m ) {\n" +
                        "    const uint ieeeMantissa = 0x007FFFFFu; // binary32 mantissa bitmask\n" +
                        "    const uint ieeeOne      = 0x3F800000u; // 1.0 in IEEE binary32\n" +
                        "\n" +
                        "    m &= ieeeMantissa;                     // Keep only mantissa bits (fractional part)\n" +
                        "    m |= ieeeOne;                          // Add fractional part to 1.0\n" +
                        "\n" +
                        "    float  f = uintBitsToFloat( m );       // Range [1:2]\n" +
                        "    return f - 1.0;                        // Range [0:1]\n" +
                        "}\n" +
                        "// Pseudo-random value in half-open range [0:1].\n" +
                        "float random( float x ) { return floatConstruct(hash(floatBitsToUint(x))); }\n" +
                        "float random( vec2  v ) { return floatConstruct(hash(floatBitsToUint(v))); }\n" +
                        "float random( vec3  v ) { return floatConstruct(hash(floatBitsToUint(v))); }\n" +
                        "float random( vec4  v ) { return floatConstruct(hash(floatBitsToUint(v))); }\n" +
                        "void main() {\n" +
                        "int x_pos = int(gl_GlobalInvocationID.x);\n" +
                        "int y_pos = int(gl_GlobalInvocationID.y);\n" +
                        "vec3  inputs = vec3( x_pos, y_pos, u_time_seconds ); // Spatial and temporal inputs\n" +
                        "float rand   = random( inputs );              // Random per-pixel value\n" +
                        "int fragment_index = x_pos + (y_pos * int(u_window_size.x));\n" +
                        "all_cell.agent[fragment_index].alive = (rand <= 0.02);\n" +
                        "all_cell.agent[fragment_index].color = vec3(1.0);\n" +
                        "all_cell_2.agent[fragment_index].alive = all_cell.agent[fragment_index].alive;\n" +
                        "all_cell_2.agent[fragment_index].color = all_cell.agent[fragment_index].color;\n" +
                        "}";

        int initialize   = ShaderManager.getInstance().compileShader(GL43.GL_COMPUTE_SHADER, initialize_source);
        this.initialize_id = ShaderManager.getInstance().linkShader(initialize);

        // Copy data
        String copy_source =
            "#version 430 core\n" +
            "uniform vec2 u_window_size;\n" +
            "layout (local_size_x = 1, local_size_y = 1) in;\n" +
            agents.get("cell").generateGLSL() +
            "void main() {\n" +
            "int x_pos = int(gl_GlobalInvocationID.x);\n" +
            "int y_pos = int(gl_GlobalInvocationID.y);\n" +
            "int fragment_index = x_pos + (y_pos * int(u_window_size.x));\n" +
            "all_cell.agent[fragment_index].alive = all_cell_2.agent[fragment_index].alive;\n" +
            "all_cell.agent[fragment_index].color = all_cell_2.agent[fragment_index].color;\n" +
            "}";

        int copy   = ShaderManager.getInstance().compileShader(GL43.GL_COMPUTE_SHADER, copy_source);
        this.copy_id = ShaderManager.getInstance().linkShader(copy);

        this.vao = new VAO();
        this.vao.bind();

        int vbo_vertex_id = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo_vertex_id);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, vertices, GL43.GL_STATIC_DRAW);
        GL43.glVertexAttribPointer(0, 3, GL43.GL_FLOAT, false, 0, 0);
        GL43.glEnableVertexAttribArray(0);
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, 0);
        this.vao.unbind();
    }

    public void update(double delta){

        // Initialize Data
        if(!initialized) {
            ShaderManager.getInstance().bind(initialize_id);
            Mouse.getInstance().bindUniforms();
            ShaderManager.getInstance().bindUniforms();
            GL43.glDispatchCompute(Apiary.getWindowWidth(), Apiary.getWindowHeight(), 1);
            GL43.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);
            initialized = true;
        }

        // Execute compute
        if(simulation_time >= simulation_target_time) {
            simulation_time -= simulation_target_time;
            ShaderManager.getInstance().bind(compute_id);
            Mouse.getInstance().bindUniforms();
            ShaderManager.getInstance().bindUniforms();
            GL43.glDispatchCompute(Apiary.getWindowWidth(), Apiary.getWindowHeight(), 1);
            GL43.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);

            // Copy buffers over
            ShaderManager.getInstance().bind(copy_id);
            Mouse.getInstance().bindUniforms();
            ShaderManager.getInstance().bindUniforms();
            GL43.glDispatchCompute(Apiary.getWindowWidth(), Apiary.getWindowHeight(), 1);
            GL43.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);
        }

        if(program_id > -1){
            // Then our shader
            ShaderManager.getInstance().bind(program_id);
        }
        // Bind all of our uniform variable
        Mouse.getInstance().bindUniforms();

        simulation_time += delta;
    }

    public void render(){
        // Now we try to step the simulation
//        for(Step step : pipeline){
//            for(String agent_name : step.getAgents()){
//
//            }
//        }

        GL43.glClearColor(1f, 1f, 1f, 1.0f);
        GL43.glClear(GL43.GL_COLOR_BUFFER_BIT);

//        this.agents.get("cell").flush();

        // This stage renders an input image to the screen.
        this.vao.bind();
        GL43.glDrawArrays(GL43.GL_TRIANGLES, 0, vertices.length / 3);
        this.vao.unbind();
        GL43.glUseProgram(0);

    }

    public void cleanup(){
        // Free all allocated buffers and stuff
    }

    public boolean hasAgent(String agent_type) {
        return this.agents.containsKey(agent_type);
    }

    //TODO: FIX
    public int getAllocatedCapacity(String agent_type){
        if(hasAgent(agent_type)){
            return this.agents.get(agent_type).getCapacity();
        }
        // We dont have an agent of this type. So our allocated capacity is 0
        return 0;
    }
}
