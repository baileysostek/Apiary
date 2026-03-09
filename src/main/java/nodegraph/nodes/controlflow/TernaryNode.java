package nodegraph.nodes.controlflow;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import editor.Editor;
import graphics.GLDataType;
import imgui.ImGui;
import imgui.flag.ImGuiComboFlags;
import nodegraph.Node;
import nodegraph.nodes.TemplateNode;
import nodegraph.nodes.agent.AgentNode;
import nodegraph.pin.InflowPin;
import nodegraph.pin.OutflowPin;

public class TernaryNode extends TemplateNode {

    private GLDataType return_type = GLDataType.INT;
    private OutflowPin outflow_data;

    private InflowPin consequent;
    private InflowPin alternate;

    public TernaryNode(JsonObject initialization_data) {
        super("Ternary", FunctionDirective.TERNARY, GLDataType.INT, initialization_data);

        outflow_data = (OutflowPin) super.getPinFromName("Ternary");

        super.addInputPin("condition", GLDataType.BOOL);

        consequent = super.addInputPin("consequent", GLDataType.FLOAT);
        alternate = super.addInputPin("alternate", GLDataType.FLOAT);
    }

    @Override
    public void onLoad(JsonObject initialization_data) {
        if(initialization_data.has("type")){
            this.setType(GLDataType.valueOf(initialization_data.get("type").getAsString()));
        }
    }

    @Override
    public JsonObject serializeNode() {
        JsonObject out = new JsonObject();
        out.addProperty("type", this.return_type.name());
        return out;
    }

    @Override
    public void render() {
        super.render();
        // Render some fields
        ImGui.pushItemWidth(96);
        if (ImGui.beginCombo("##"+super.getID()+"_type", return_type.getGLSL(), ImGuiComboFlags.None)){
            for (GLDataType type : GLDataType.values()) {
                boolean is_selected = return_type.equals(type);
                if (ImGui.selectable(type.getGLSL(), is_selected)){
                    setType(type);
                }
                if (is_selected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }
        ImGui.popItemWidth();
    }

    private void setType(GLDataType type){
        return_type = type;
        consequent.setType(type);
        alternate.setType(type);
        outflow_data.setType(type);
    }
}
