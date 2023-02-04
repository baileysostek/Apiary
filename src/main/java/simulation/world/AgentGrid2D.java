package simulation.world;

import com.google.gson.JsonElement;
import com.sun.prism.ps.Shader;
import graphics.SSBO;
import graphics.ShaderManager;
import org.lwjgl.opengl.GL43;
import pegs.PegManager;
import util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;

public class AgentGrid2D extends World{

    public AgentGrid2D(String name, JsonElement arguments) {
        super(name, arguments);
    }

    @Override
    public int generateVertex() {
        return ShaderManager.getInstance().getDefaultVertexShader();
    }

    @Override
    public int generateGeometryShader() {
        return ShaderManager.getInstance().getDefaultGeometryShader();
    }

    @Override
    public int generateFragmentShader() {

        if(!this.arguments.has("fragment_logic")){
            System.err.println("Error: no member \"fragment_logic\" exists in the arguments of this simulation's definition file.");
            return ShaderManager.getInstance().getDefaultFragmentShader();
        }
        return ShaderManager.getInstance().generateFragmentShaderFromPegs(this.arguments.get("fragment_logic"));
    }
}
