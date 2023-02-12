package nodes;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import editor.Editor;
import graphics.Uniform;
import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesPinShape;

import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;

public abstract class Node {

    // This is the unique key used to create this uniform.
    private final String key;
    private final int num_params;
    private final int num_output = 1;

    // In order to use some pegs we need to import uniforms or specific modules.
    private final HashSet<String> required_uniforms = new HashSet<String>();
    private final HashSet<String> required_imports  = new HashSet<String>();
    private final HashSet<String> required_imports_in_main  = new HashSet<String>();

    protected Node(String key, int num_params) {
        // Ensure that the key starts with an @ symbol and is lower case
            this.key = (key.startsWith("@") ? key : "@" + key).toLowerCase();

        // This is the number of parameters that this peg expects to be on the stack to consume.
        this.num_params = num_params;
    }

    protected void requiresUniform(String uniform_name){
        this.required_uniforms.add(uniform_name);
    }

    protected void requiresInclude(String include_name){
        this.required_imports.add(include_name);
    }

    protected void requiresIncludeInMain(String include_name) {
        this.required_imports_in_main.add(include_name);
    }

    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params){
        return this.key;
    }

    protected final String transpile(Stack<JsonElement> stack){
        JsonElement[] params = new JsonElement[num_params];
        for(int i = 0; i < num_params; i++){
            params[num_params - i - 1] = stack.pop();
        }
        String out = toGLSL(stack, params);
        stack.push(new JsonPrimitive(out));
        return out;
    }

    public String getKey() {
        return this.key;
    }

    public Collection<String> getRequiredUniforms() {
        return this.required_uniforms;
    }

    public Collection<String> getRequiredImports() {
        return this.required_imports;
    }

    public Collection<String> getRequiredInMain() {
        return this.required_imports_in_main;
    }

    public final void renderNode(){
        // set the titlebar color of an individual node
        ImNodes.pushColorStyle(ImNodesColorStyle.TitleBar, ImColor.rgba(11, 109, 191, 255));
        ImNodes.pushColorStyle(ImNodesColorStyle.TitleBar, ImColor.rgba(81, 148, 204, 255));

        ImNodes.beginNode(Editor.getInstance().nextID());
        // Title Bar
        ImNodes.beginNodeTitleBar();
        ImGui.text(this.key);
        ImNodes.endNodeTitleBar();

        // Input
        ImNodes.beginInputAttribute(Editor.getInstance().nextID(), ImNodesPinShape.Quad);
        ImGui.text("Label?");
        ImNodes.endInputAttribute();
        ImGui.sameLine();
        // Output
        ImNodes.beginOutputAttribute(Editor.getInstance().nextID(), ImNodesPinShape.Quad);
        ImGui.text("Label?");
        ImNodes.endOutputAttribute();

        ImNodes.beginInputAttribute(Editor.getInstance().nextID(), ImNodesPinShape.Quad);
        ImGui.text("Label?");
        ImNodes.endInputAttribute();

        ImNodes.endNode();

        ImNodes.popColorStyle();
        ImNodes.popColorStyle();
    }
}
