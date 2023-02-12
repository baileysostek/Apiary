package nodes;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import editor.Editor;
import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesPinShape;

import java.util.LinkedHashMap;

public class NodeInstance implements NodeGraphElement{

    private final Nodes node_type;
    private final LinkedHashMap<String, JsonPrimitive> parameter_values = new LinkedHashMap<>();

    public NodeInstance(Nodes node_type){
        this.node_type = node_type;
        // Populate our parameter map based off our our template.
        for(String param_name : this.node_type.getParameterNames()){
            this.parameter_values.put(param_name, new JsonPrimitive(Math.random()));
        }
    }

    public JsonElement getParameter(String parameter_name) {
        return this.parameter_values.get(parameter_name);
    }

    @Override
    public final void renderNode(){
        // set the titlebar color of an individual node
        // TODO get from lookup
        ImNodes.pushColorStyle(ImNodesColorStyle.TitleBar, ImColor.rgba(11, 109, 191, 255));
        ImNodes.pushColorStyle(ImNodesColorStyle.TitleBar, ImColor.rgba(81, 148, 204, 255));

        ImNodes.beginNode(Editor.getInstance().nextID());
        // Title Bar
        ImNodes.beginNodeTitleBar();
        ImGui.text(this.node_type.getNodeID());
        ImNodes.endNodeTitleBar();

        // Input
        int index = 0;
        for(String param_name : this.node_type.getParameterNames()){
            ImNodes.beginInputAttribute(Editor.getInstance().nextID(), ImNodesPinShape.Triangle);
            ImGui.text(param_name+ " " + this.getParameter(param_name));
            ImNodes.endInputAttribute();
            if(index < this.node_type.getOutputNames().length){
                ImGui.sameLine();
                ImNodes.beginOutputAttribute(Editor.getInstance().nextID(), ImNodesPinShape.Triangle);
                ImGui.text(this.node_type.getOutputNames()[index]);
                ImNodes.endOutputAttribute();
            }
            index++;
        }
        for(int i = index; i < this.node_type.getOutputNames().length; i++){
            ImNodes.beginOutputAttribute(Editor.getInstance().nextID(), ImNodesPinShape.Triangle);
            ImGui.text(this.node_type.getOutputNames()[i]);
            ImNodes.endOutputAttribute();
        }

        ImNodes.endNode();

        ImNodes.popColorStyle();
        ImNodes.popColorStyle();
    }

    public void toIR() {
        JsonElement out = this.node_type.toIR(this);
        String out_glsl = NodeManager.getInstance().transpile(out);
        System.out.println(out_glsl);
    }
}
