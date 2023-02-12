package nodes.random;

import com.google.gson.JsonElement;
import nodes.Node;
import simulation.SimulationManager;

import java.util.Stack;

public class NodeRandomFloat extends Node {

    public NodeRandomFloat(){
        super("@random_float", 0);

        this.requiresUniform(SimulationManager.getInstance().getTimeUniformName());
        this.requiresInclude("noise");
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // We need to ensure that every time this method is called we get a unique random output
        // Therefore we will generate a random offset seed client side.
        return String.format("random(vec3(gl_GlobalInvocationID.x, gl_GlobalInvocationID.y, %s%sf))", SimulationManager.getInstance().getTimeUniformName(), ((float)Math.random() > 0.5 ? " - " : " + ") + ((float)Math.random())+"");
    }

}
