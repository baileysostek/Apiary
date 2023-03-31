package nodegraph.nodes.vector;

import com.google.gson.JsonObject;
import graphics.GLDataType;

public class DecomposeVec3 extends DecomposeStructureNode{
    public DecomposeVec3(JsonObject initialization_data) {
        super(GLDataType.VEC3, new String[]{"x", "y", "z"}, new GLDataType[]{GLDataType.FLOAT, GLDataType.FLOAT, GLDataType.FLOAT}, initialization_data);
    }
}
