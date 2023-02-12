package nodes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import core.Apiary;
import graphics.ShaderManager;
import simulation.SimulationManager;

import java.util.Stack;

public enum Nodes {

    // Math opperations
    ABS("@abs",
        new String[]{
            "A"
        },
        (stack, params) -> String.format("abs(%s)", params[0]),
        (out, params) -> {
            out.set(0, params[0]);
        }
    ),

    ADD("@add",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("(%s + %s)", params[0], params[1]),
        (out, params) -> {
            out.set(0, params[0]);
            out.set(1, params[1]);
        }
    ),

    COS("@cos",
        new String[]{
            "A"
        },
        (stack, params) -> String.format("cos(%s)", params[0]),
        (out, params) -> {
            out.set(0, params[0]);
        }
    ),

    DIV("@div",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("(%s / %s)", params[0], params[1]),
        (out, params) -> {
            out.set(0, params[0]);
            out.set(1, params[1]);
        }
    ),

    GREATER("@greater",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("%s > %s", params[0], params[1]),
        (out, params) -> {
            out.set(0, params[0]);
            out.set(1, params[1]);
        }
    ),

    INCREMENT("@increment",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("%s += %s;\n", params[0], params[1]),
        (out, params) -> {
            out.set(0, params[0]);
            out.set(1, params[1]);
        }
    ),

    LESS("@less",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("%s < %s", params[0], params[1]),
        (out, params) -> {
            out.set(0, params[0]);
            out.set(1, params[1]);
        }
    ),

    MAX("@max",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("max(%s,%s)", params[0], params[1]),
        (out, params) -> {
            out.set(0, params[0]);
            out.set(1, params[1]);
        }
    ),

    MIN("@min",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("min(%s,%s)", params[0], params[1]),
        (out, params) -> {
            out.set(0, params[0]);
            out.set(1, params[1]);
        }
    ),

    MOD("@mod",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("mod(%s,%s)", params[0], params[1]),
        (out, params) -> {
            out.set(0, params[0]);
            out.set(1, params[1]);
        }
    ),

    MUL("@mul",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("mul(%s,%s)", params[0], params[1]),
        (out, params) -> {
            out.set(0, params[0]);
            out.set(1, params[1]);
        }
    ),

    ON_SCREEN("@on_screen",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("!(%s < -1.0 || %s > 1.0 || %s < -1.0 || %s > 1.0)", params[0], params[0], params[1], params[1]),
        (out, params) -> {
            out.set(0, params[0]);
            out.set(1, params[1]);
        }
    ),

    PI("@pi",
        new String[]{},
        (stack, params) -> "3.1415926",
        (out, params) -> {}
    ),

    PI_2("@pi_2",
        new String[]{},
        (stack, params) -> "1.5707963",
        (out, params) -> {}
    ),

    PI_3("@pi_3",
        new String[]{},
        (stack, params) -> "1.0471975",
        (out, params) -> {}
    ),

    PI_4("@pi_4",
        new String[]{},
        (stack, params) -> "0.7853981",
        (out, params) -> {}
    ),

    ROTATE_2D("@rotate_2d",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("rotate(%s,%s)", params[0], params[1]),
        (out, params) -> {
            out.set(0, params[0]);
            out.set(1, params[1]);
        },
        new String[]{}, // Uniforms
        new String[]{"rotation_2d"} // Libraries
    ),

    SIN("@sin",
        new String[]{
            "A"
        },
        (stack, params) -> String.format("sin(%s)", params[0]),
        (out, params) -> {
            out.set(0, params[0]);
        }
    ),

    SUB("@sub",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("(%s - %s)", params[0], params[1]),
        (out, params) -> {
            out.set(0, params[0]);
            out.set(1, params[1]);
        }
    ),

    TAN("@tan",
        new String[]{
            "A"
        },
        (stack, params) -> String.format("tan(%s)", params[0]),
        (out, params) -> {
            out.set(0, params[0]);
        }
    ),

    XY_TO_SCREEN_INDEX("@rotate_2d",
        new String[]{
            "A",
            "B"
        },
        (stack, params) -> String.format("int(mod(%s, window_width_pixels)) + (int(mod(%s, window_height_pixels)) * window_width_pixels)", params[0], params[1]),
        (out, params) -> {
            out.set(0, params[0]);
            out.set(1, params[1]);
        },
        new String[]{ShaderManager.getInstance().getWindowSize().getName()}, // Uniforms
        new String[]{} // Libraries
    ),

    ;

    // Define all of our functional interfaces. These are used as abstract implementations of specific pieces of functionality.
    @FunctionalInterface
    private interface ToIntermediateRepresentation<Output extends JsonArray, Parameters extends JsonElement>{
        void apply(Output out, Parameters[] params);
    }
    @FunctionalInterface
    private interface ToGLSLCallback<EvaluationStack extends Stack<JsonElement>, Parameters extends String>{
        String apply(EvaluationStack stack, Parameters[] params);
    }

    // Here is everything within an interface.
    private final String node_id;
    private final String[] parameter_names;
    private final String[] output_names = new String[]{"out"};
    private final String[] required_uniforms;
    private final String[] required_libraries;

    // These are our Abstract Methods
    private ToIntermediateRepresentation toIntermediateRepresentation;
    private ToGLSLCallback toGLSLCallback;

    Nodes(String node_id, String[] params, ToGLSLCallback toGLSLCallback, ToIntermediateRepresentation toIntermediateRepresentation){
        this(node_id, params, toGLSLCallback, toIntermediateRepresentation, new String[]{}, new String[]{});
    }

    Nodes(String node_id, String[] params, ToGLSLCallback toGLSLCallback, ToIntermediateRepresentation toIntermediateRepresentation, String[] required_uniforms, String[] required_libraries){
        this.node_id = (node_id.startsWith("@") ? node_id : "@" + node_id).toLowerCase();
        this.parameter_names = params;
        this.required_uniforms = required_uniforms;
        this.required_libraries = required_libraries;
        this.toIntermediateRepresentation = toIntermediateRepresentation;
        this.toGLSLCallback = toGLSLCallback;
    }

    public final String transpile(Stack<JsonElement> stack){
        String[] params = new String[this.parameter_names.length];
        for(int i = 0; i < this.parameter_names.length; i++){
            params[this.parameter_names.length - i - 1] = NodeManager.getInstance().transpile(stack.pop());
        }
        String out = toGLSL(stack, params);
        stack.push(new JsonPrimitive(out));
        return out;
    }

    public String toGLSL(Stack<JsonElement> stack, String[] params){
        return this.toGLSLCallback.apply(stack, params);
    }

    public JsonElement toIR(NodeInstance instance){
        JsonElement[] params = new JsonElement[this.parameter_names.length];
        for(int i = 0; i < params.length; i++){
            params[i] = instance.getParameter(this.parameter_names[i]);
        }
        // Create our out array
        int capacity = this.parameter_names.length + 1; // The +1 is for the name of this node it ALWAYS comes last
        JsonArray output = new JsonArray(capacity);
        for(int i = 0; i < capacity; i++){output.add(new JsonNull());}
        output.set(capacity - 1, new JsonPrimitive(this.node_id));
        // pass reference to output, it is mutated by the toIntermediateRepresentation function.
        this.toIntermediateRepresentation.apply(output, params);
        return output;
    }

    public String getNodeID() {
        return node_id;
    }

    public String[] getParameterNames() {
        return parameter_names;
    }

    public String[] getOutputNames() {
        return output_names;
    }

    public String[] getRequiredUniforms() {
        return required_uniforms;
    }

    public String[] getRequiredLibraries() {
        return required_libraries;
    }
}
