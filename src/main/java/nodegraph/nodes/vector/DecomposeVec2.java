package nodegraph.nodes.vector;

import com.google.gson.JsonObject;
import graphics.GLDataType;

public class DecomposeVec2 extends DecomposeStructureNode{
    public DecomposeVec2(JsonObject initialization_data) {
        super(GLDataType.VEC2, new String[]{"x", "y"}, new GLDataType[]{GLDataType.FLOAT, GLDataType.FLOAT}, initialization_data);
    }
}
