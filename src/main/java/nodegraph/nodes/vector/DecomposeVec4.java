package nodegraph.nodes.vector;

import com.google.gson.JsonObject;
import graphics.GLDataType;

public class DecomposeVec4 extends DecomposeStructureNode{
    public DecomposeVec4(JsonObject initialization_data) {
        super(GLDataType.VEC4, new String[]{"x", "y", "z", "w"}, new GLDataType[]{GLDataType.FLOAT, GLDataType.FLOAT, GLDataType.FLOAT, GLDataType.FLOAT}, initialization_data);
    }
}
