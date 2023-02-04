package simulation.world;

import com.google.gson.JsonObject;
import graphics.ShaderManager;

public class DefaultWorld2D extends World{
    public DefaultWorld2D(String name) {
        super(name, new JsonObject());
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
        return ShaderManager.getInstance().getDefaultFragmentShader();
    }
}
