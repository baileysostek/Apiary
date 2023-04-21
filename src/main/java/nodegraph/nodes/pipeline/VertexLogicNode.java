package nodegraph.nodes.pipeline;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import imgui.ImGui;
import nodegraph.Node;
import nodegraph.pin.OutflowPin;
import simulation.SimulationManager;

public class VertexLogicNode extends Node {

    public VertexLogicNode(JsonObject initialization_data) {
        super(initialization_data);

        super.setTitle("Vertex");

        super.addInputPin("position", GLDataType.VEC4);
    }

    @Override
    public JsonElement getValueOfPin(OutflowPin outflow) {
        return outflow.getValue();
    }

    @Override
    public void transpile(JsonArray evaluation_stack) {
        evaluation_stack.add("gl_Position");
        super.transpile(evaluation_stack);
        evaluation_stack.add(FunctionDirective.SET.getNodeID());
    }

    @Override
    public void render() {
        super.render();
    }
}
