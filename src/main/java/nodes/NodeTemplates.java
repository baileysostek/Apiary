package nodes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import graphics.ShaderManager;
import simulation.SimulationManager;

import java.util.LinkedHashSet;
import java.util.Stack;

public enum NodeTemplates {

    // Math opperations
    ABS("@abs",
        new String[]{
            "A"
        },
        (stack, params) -> String.format("abs(%s)", params[0])
    ),

    ADD("@add",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("(%s + %s)", params[0], params[1])
    ),

    AGENT_WRITE("@agent_write",
        new String[]{
            "Type",
            "Index",
            "Property",
            "Value",
        },
        (stack, params) -> {
            // Since this is the all command, we are going to compute the number of agents of this type which have been allocated for the simulation.
            if(SimulationManager.getInstance().hasActiveSimulation()) {
                if(SimulationManager.getInstance().hasAgent(params[0])) {
                    NodeManager.getInstance().requireAgent(params[0]);
                    return String.format("%s%s.agent[%s].%s = %s;\n", params[0], ShaderManager.getInstance().getSSBOWriteIdentifier(),params[1],params[2],params[3]);
                }else{
                    //TODO throw compilation error.
                    System.err.println("Error cannot parse properties of this agent");
                }
            }
            return "";
        }
    ),

    AGENT_READ("@agent_read",
        new String[]{
            "Type",
            "Index",
            "Property"
        },
        (stack, params) -> {
            if(SimulationManager.getInstance().hasActiveSimulation()) {
                if(SimulationManager.getInstance().hasAgent(params[0])) {
                    NodeManager.getInstance().requireAgent(params[0]);
                    return String.format("%s%s.agent[%s].%s", params[0], ShaderManager.getInstance().getSSBOReadIdentifier(),params[1],params[2]);
                }else{
                    //TODO throw compilation error.
                    System.err.println("Error cannot parse properties of this agent");
                }
            }
            return "";
        }
    ),

    AND("@and",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("(%s && %s)", params[0], params[1])
    ),

    CAST("@cast",
        new String[]{
            "Value",
            "Type"
        },
        (stack, params) -> String.format("%s(%s)", params[1], params[0])
    ),

    CLAMP("@clamp",
        new String[]{
            "value",
            "min",
            "max"
        },
        (stack, params) -> String.format("clamp(%s, %s, %s)", params[0], params[1], params[2])
    ),

    CONDITIONAL("@conditional",
        new String[]{
            "Predicate",
        },
        new String[]{
            "Consequent",
            "Alternate"
        },
        (stack, params) -> {
            String conditional = params[0];
            String consequent = params[1];
            String alternate = params[2];

            // If we do not have a consequent but do have an alternate...
            if(consequent.isEmpty() && !alternate.isEmpty()){
                // We are going to invert the if statement to avoid the if/else case
                String out = "";
                out += (String.format("if (!(%s)) {\n", conditional)); // Inverted
                out += (String.format("\t%s", alternate.endsWith("\n") ? alternate : alternate + "\n"));
                out += ("}\n");
                return out;
            }else{
                // Default if else case
                String out = "";
                out += (String.format("if (%s) {\n", conditional));
                out += (String.format("\t%s", consequent.endsWith("\n") ? consequent : consequent + "\n"));
                if(alternate.isEmpty()) {
                    // End the conditional.
                    out += ("}\n");
                } else {
                    // Add the alternate
                    out += ("} else {\n");
                    out += (String.format("\t%s", alternate.endsWith("\n") ? alternate : alternate + "\n"));
                    out += ("}\n");
                }
                return out;
            }
        },
        new String[]{}, // Required Uniforms
        new String[]{}  // Required Libraries
    ),

    COS("@cos",
        new String[]{
            "A"
        },
        (stack, params) -> String.format("cos(%s)", params[0])
    ),

    DEFINE("@define",
        new String[]{
            "Type",
            "Name",
            "Value"
        },
        (stack, params) -> String.format("%s %s = %s ;\n", params[0], params[1], params[2])
    ),

    DIV("@div",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("(%s / %s)", params[0], params[1])
    ),

    EQUALS("@equals",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("(%s == %s)", params[0], params[1])
    ),

    GET("@get",
        new String[]{
            "Variable"
        },
        (stack, params) -> {
            if(ShaderManager.getInstance().hasUniform(params[0])){
                NodeManager.getInstance().requireUniform(params[0]);
            }else{
                if(params[0].contains(".")){
                    String base_variable = params[0].substring(0, params[0].indexOf("."));
                    if(ShaderManager.getInstance().hasUniform(base_variable)){
                        NodeManager.getInstance().requireUniform(base_variable);
                    }
                }
            }
            return params[0];
        }
    ),

    GREATER("@greater",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("%s > %s", params[0], params[1])
    ),

    INCREMENT("@increment",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("%s += %s;\n", params[0], params[1])
    ),

    LESS("@less",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("%s < %s", params[0], params[1])
    ),

    MAX("@max",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("max(%s,%s)", params[0], params[1])
    ),

    MIN("@min",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("min(%s,%s)", params[0], params[1])
    ),

    MIX("@mix",
        new String[]{
            "A",
            "B",
            "Delta"
        },
        (stack, params) -> String.format("mix(%s, %s, %s)", params[0], params[1], params[2])
    ),

    MOD("@mod",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("mod(%s,%s)", params[0], params[1])
    ),

    MUL("@mul",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("(%s * %s)", params[0], params[1])
    ),

    NORMALIZE("@normalize",
        new String[]{
            "A",
        },
        (stack, params) -> String.format("normalize(%s)", params[0])
    ),

    NOT("@not",
        new String[]{
            "A",
        },
        (stack, params) -> String.format("(!%s)", params[0])
    ),

    ON_SCREEN("@on_screen",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("!(%s < -1.0 || %s > 1.0 || %s < -1.0 || %s > 1.0)", params[0], params[0], params[1], params[1])
    ),

    OR("@or",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("(%s || %s)", params[0], params[1])
    ),

    PI("@pi",
        new String[]{},
        (stack, params) -> "3.1415926"
    ),

    PI_2("@pi_2",
        new String[]{},
        (stack, params) -> "1.5707963"
    ),

    PI_3("@pi_3",
        new String[]{},
        (stack, params) -> "1.0471975"
    ),

    PI_4("@pi_4",
        new String[]{},
        (stack, params) -> "0.7853981"
    ),

    RANDOM_BOOL("@random_bool",
        new String[]{},
        new String[]{},
        (stack, params) -> String.format("((random(vec3(gl_GlobalInvocationID.x, gl_GlobalInvocationID.y, %s%sf)) > 0.5) ? true : false)", SimulationManager.getInstance().getTimeUniformName(), ((float)Math.random() > 0.5 ? " - " : " + ") + ((float)Math.random())+""),
        new String[]{"u_time_seconds"}, // Uniforms
        new String[]{"noise"} // Libraries
    ),

    RANDOM_FLOAT("@random_float",
        new String[]{},
        new String[]{},
        (stack, params) ->   String.format("random(vec3(gl_GlobalInvocationID.x, gl_GlobalInvocationID.y, %s%sf))", SimulationManager.getInstance().getTimeUniformName(), ((float)Math.random() > 0.5 ? " - " : " + ") + ((float)Math.random())+""),
        new String[]{"u_time_seconds"}, // Uniforms
        new String[]{"noise"} // Libraries
    ),

    ROTATE_2D("@rotate_2d",
        new String[]{
            "A",
            "B"
        },
        new String[]{},
        (stack, params) -> String.format("rotate(%s,%s)", params[0], params[1]),
        new String[]{}, // Uniforms
        new String[]{"rotation_2d"} // Libraries
    ),

    SET("@set",
        new String[]{
            "Variable",
            "Value"
        },
        (stack, params) -> String.format("%s = %s ;\n", params[0], params[1])
    ),

    SIN("@sin",
        new String[]{
            "A"
        },
        (stack, params) -> String.format("sin(%s)", params[0])
    ),

    SUB("@sub",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("(%s - %s)", params[0], params[1])
    ),

    TAN("@tan",
        new String[]{
            "A"
        },
        (stack, params) -> String.format("tan(%s)", params[0])
    ),

    TERNARY("@?",
        new String[]{
            "Predicate",
            "Consequent",
            "Alternate"
        },
        (stack, params) -> String.format("(%s ? %s : %s)", params[0], params[1], params[2])
    ),

    VEC2("@vec2",
        new String[]{
            "X",
            "Y",
        },
        (stack, params) -> String.format("vec2(%s , %s)", params[0], params[1])
    ),

    VEC3("@vec3",
        new String[]{
            "X",
            "Y",
            "Z",
        },
        (stack, params) -> String.format("vec3(%s , %s , %s)", params[0], params[1], params[2])
    ),

    VEC4("@vec4",
        new String[]{
            "X",
            "Y",
            "Z",
            "W",
        },
        (stack, params) -> String.format("vec4(%s , %s , %s , %s)", params[0], params[1], params[2], params[3])
    ),

    XOR("@xor",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("(%s ^^ %s)", params[0], params[1])
    ),

    XY_TO_SCREEN_INDEX("@xy_to_screen_index",
        new String[]{
            "A",
            "B"
        },
        new String[]{},
        (stack, params) -> String.format("int(mod(%s, window_width_pixels)) + (int(mod(%s, window_height_pixels)) * window_width_pixels)", params[0], params[1]),
        new String[]{ShaderManager.getInstance().getWindowSize().getName()}, // Uniforms
        new String[]{} // Libraries
    ),
    ;

    @FunctionalInterface
    private interface ToGLSLCallback<EvaluationStack extends Stack<JsonElement>, Parameters extends String>{
        String apply(EvaluationStack stack, Parameters[] params);
    }

    // Here is everything within an interface.
    private final String node_id;

    // TODO make these immutable
    private final LinkedHashSet<String> input_names = new LinkedHashSet<>();
    private final LinkedHashSet<String> output_names       = new LinkedHashSet<>();
    private final LinkedHashSet<String> required_uniforms  = new LinkedHashSet<>();
    private final LinkedHashSet<String> required_libraries = new LinkedHashSet<>();

    // These are our Abstract Methods
    private ToGLSLCallback toGLSLCallback;

    NodeTemplates(String node_id, String[] params, ToGLSLCallback toGLSLCallback){
        this(node_id, params, new String[]{}, toGLSLCallback, new String[]{}, new String[]{});
    }

    NodeTemplates(String node_id, String[] params, String[] output, ToGLSLCallback toGLSLCallback, String[] required_uniforms, String[] required_libraries){
        this.node_id = (node_id.startsWith("@") ? node_id : "@" + node_id).toLowerCase();
        for(String param_name : params){
            this.input_names.add(param_name);
        }
        if(output.length > 0){
            for(String output_name : output){
                this.output_names.add(output_name);
            }
        }else{
            this.output_names.add("out");
        }

        for(String uniform_name : required_uniforms){
            this.required_uniforms.add(uniform_name);
        }
        for(String library_name : required_libraries){
            this.required_libraries.add(library_name);
        }

        this.toGLSLCallback = toGLSLCallback;
    }

    public final String transpile(Stack<JsonElement> stack){
        int num_inputs = this.input_names.size() + (this.output_names.size() == 1 ? 0 : this.output_names.size());
        String[] params = new String[num_inputs];
        for(int i = 0; i < num_inputs; i++){
            params[num_inputs - i - 1] = NodeManager.getInstance().transpile(stack.pop());
        }
        String out = toGLSL(stack, params);
        stack.push(new JsonPrimitive(out));
        return out;
    }

    public String toGLSL(Stack<JsonElement> stack, String[] params){
        return this.toGLSLCallback.apply(stack, params);
    }

    public String getNodeID() {
        return node_id;
    }

    public LinkedHashSet<String> getInputNames() {
        return input_names;
    }

    public LinkedHashSet<String> getOutputNames() {
        return output_names;
    }

    public LinkedHashSet<String> getRequiredUniforms() {
        return required_uniforms;
    }

    public LinkedHashSet<String> getRequiredLibraries() {
        return required_libraries;
    }
}
