package nodes.variables;

import com.google.gson.JsonElement;
import graphics.ShaderManager;
import nodes.Node;
import nodes.NodeManager;

import java.util.Stack;

public class NodeGet extends Node {
    public NodeGet() {
        super("@get", 1);
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        // Input Variables
        String variable_name  = NodeManager.getInstance().transpile(params[0]);

        // When we reference some variables
        if(ShaderManager.getInstance().hasUniform(variable_name)){
            this.requiresUniform(variable_name);
        }else{
            if(variable_name.contains(".")){
                String base_variable = variable_name.substring(0, variable_name.indexOf("."));
                if(ShaderManager.getInstance().hasUniform(base_variable)){
                    this.requiresUniform(base_variable);
                }
            }
        }

        // Must be a locally scoped variabel we dont know about... maybe we throw an exception here in the future if we also capture variable generation.

        return String.format("%s", variable_name);
    }
}
