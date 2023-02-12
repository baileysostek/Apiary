package nodes;

import com.google.gson.*;
import graphics.SSBO;
import graphics.ShaderManager;
import graphics.Uniform;
import nodes.code.NodeCode;
import nodes.controlflow.NodeConditional;
import nodes.controlflow.NodeForEach;
import nodes.controlflow.NodeOutColor;
import nodes.controlflow.NodeTernary;
import nodes.data.NodeVec2;
import nodes.data.NodeVec3;
import nodes.data.NodeVec4;
import nodes.geometry.NodeEmitPoint;
import nodes.logic.*;
import nodes.math.*;
import nodes.random.NodeRandomBool;
import nodes.random.NodeRandomFloat;
import nodes.simulation.NodeAgentCount;
import nodes.simulation.NodeAgentRead;
import nodes.simulation.NodeAgentWrite;
import nodes.simulation.NodeGetAgentAtIndex;
import nodes.types.NodeCast;
import nodes.variables.NodeDefine;
import nodes.variables.NodeGet;
import nodes.variables.NodeSet;
import nodes.vector.NodeMix;
import nodes.vector.NodeNormalize;
import simulation.SimulationManager;
import simulation.world.World;
import util.JsonUtils;
import util.StringUtils;

import java.util.*;

public class NodeManager {
    private static NodeManager instance;
    private NodeManager(){}
    static{
        instance = new NodeManager();

        // Random
        instance.registerNode(new NodeRandomFloat());
        instance.registerNode(new NodeRandomBool());

        // Code
        instance.registerNode(new NodeCode());

        // Controlflow
        instance.registerNode(new NodeConditional());
        instance.registerNode(new NodeForEach());
        instance.registerNode(new NodeOutColor());
        instance.registerNode(new NodeTernary());

        // Data
        instance.registerNode(new NodeVec4());
        instance.registerNode(new NodeVec3());
        instance.registerNode(new NodeVec2());

        // Geometry
        instance.registerNode(new NodeEmitPoint());

        // Boolean Logic
        instance.registerNode(new NodeAnd());
        instance.registerNode(new NodeEquals());
        instance.registerNode(new NodeNot());
        instance.registerNode(new NodeOr());
        instance.registerNode(new NodeXor());

        // Math operations
        instance.registerNode(new NodeAbs());
        instance.registerNode(new NodeAdd());
        instance.registerNode(new NodeCos());
        instance.registerNode(new NodeDiv());
        instance.registerNode(new NodeGreater());
        instance.registerNode(new NodeIncrementBy());
        instance.registerNode(new NodeLess());
        instance.registerNode(new NodeMax());
        instance.registerNode(new NodeMin());
        instance.registerNode(new NodeMod());
        instance.registerNode(new NodeMul());
        instance.registerNode(new NodePI());
        instance.registerNode(new NodeReadSensor());
        instance.registerNode(new NodeRotate2D());
        instance.registerNode(new NodeSin());
        instance.registerNode(new NodeSub());
        instance.registerNode(new NodeTan());
        instance.registerNode(new NodeXYToScreenIndex());
        instance.registerNode(new NodeOnScreen());

        // Simulation
        instance.registerNode(new NodeAgentCount());
        instance.registerNode(new NodeGetAgentAtIndex());
        instance.registerNode(new NodeAgentRead());
        instance.registerNode(new NodeAgentWrite());

        // Types
        instance.registerNode(new NodeCast());

        // Variables
        instance.registerNode(new NodeDefine());
        instance.registerNode(new NodeGet());
        instance.registerNode(new NodeSet());

        // Vector math
        instance.registerNode(new NodeMix());
        instance.registerNode(new NodeNormalize());
    }
    // Holds all nodes that our system knows about.
    private HashMap<String, nodes.Node> nodes = new HashMap<>();

    // Required uniforms
    private HashSet<String> required_uniforms = new HashSet<>();
    private HashSet<String> required_imports = new HashSet<>();
    private HashSet<String> required_imports_in_main = new HashSet<>();
    private HashSet<String> requried_agents = new HashSet<>();

    public static NodeManager getInstance(){
        return instance;
    }

    // TODO: Maybe make an API that can be called into here.
    private void registerNode(Node node){
        String key = node.getKey();
        if(!this.nodes.containsKey(key)){
            this.nodes.put(key, node);
        }else{
            // TODO throw exception
        }
    }

    public String generateGLSL(JsonElement element){
        // First thing we want to do is clear the set of requirements that was generated the last time this method was called.
        clearPersistentData();

        HashMap<String, Object> substitutions = new HashMap<>();
        substitutions.put("shader_version", ShaderManager.getInstance().generateVersionString());
        substitutions.put("main_method", transpile(element));

        // Determine the uniforms that we need to include
        String uniforms = "";
        for(String uniform_name : required_uniforms){
            if(ShaderManager.getInstance().hasUniform(uniform_name)) {
                Uniform uniform = ShaderManager.getInstance().getUniform(uniform_name);
                uniforms += String.format("uniform %s %s;\n", uniform.getTypeName(), uniform.getName());
            }
        }
        substitutions.put("required_uniforms", uniforms);

        return StringUtils.format("" +
            "{{shader_version}}" +
            "{{required_uniforms}}" +
            "{{required_agents}}" +
            "{{required_imports}}" +
            "{{main_method}}" +
            "", substitutions);
    }

    public String transpile(String data){
        return transpile(JsonUtils.getInstance().getParser().parse(data));
    }

    public String transpile(JsonElement element){

        LinkedList<LinkedList<String>> glsl = new LinkedList<>();

        Stack<JsonElement> stack = new Stack<>();

        if(element.isJsonArray()){
            JsonArray array = element.getAsJsonArray();
            for(int i = 0; i < array.size(); i++){
                JsonElement top = array.get(i);
                if(!top.isJsonObject() && !top.isJsonNull() && !top.isJsonArray()) {
                    try {
                        String instruction = top.getAsString();
                        if (instruction.startsWith("@")) {
                            if (nodes.containsKey(instruction)) {
                                // Determine what node we are trying to reference.
                                nodes.Node action = nodes.get(instruction);

                                // Now that we have our node lets figure out the requirements.
                                for(String uniform_name : action.getRequiredUniforms()){
                                    this.required_uniforms.add(uniform_name);
                                }

                                // Now that we have our node lets figure out the requirements.
                                for(String import_name : action.getRequiredImports()){
                                    this.required_imports.add(import_name);
                                }

                                // Now that we have our node lets figure out the requirements.
                                for(String import_name : action.getRequiredInMain()){
                                    this.required_imports_in_main.add(import_name);
                                }

                                action.transpile(stack);
                                continue;
                            }else{
                                System.err.println("Unknown instruction:"+instruction);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // We have interpreted a literal and we just want to push it onto the stack.
                pushElementOntoStack(top, stack);
            }
        }else{
            pushElementOntoStack(element, stack);
        }

        while(stack.size() > 0) {
            JsonElement top = stack.size() > 0 ? stack.pop() : new JsonNull();

            if (!(top instanceof JsonNull)) {
                LinkedList<String> test = new LinkedList<>();
                loop:
                {
                    if (top instanceof JsonArray) {
                        JsonArray array = top.getAsJsonArray();
                        if (array.size() == 1) {
                            test.push(array.get(0).getAsString());
                            break loop;
                        }else{
                            test.push(transpile(array));
                            break loop;
                        }
                    }
                    test.push(top.getAsString());
                }
                glsl.addFirst(test);
            }
        }

        // Flat Map our strings into a single list of string.
        String out = "";
        for(LinkedList<String> lines : glsl){
            for(String line : lines){
                out += line;
            }
        }
        return out;
    }

    private void pushElementOntoStack(JsonElement element, Stack<JsonElement> stack){
        if (element instanceof JsonArray) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() == 1) {
                stack.push(array.get(0));
                return;
            }
        }
        stack.push(element);
    }

    public void requireAgent(String agent_type) {
        this.requried_agents.add(agent_type);
    }

    public HashMap<String, SSBO> getRequiredAgents() {
        HashMap<String, SSBO> required_agents = new HashMap<>();
        for(String agent_name : this.requried_agents){
            if(SimulationManager.getInstance().hasAgent(agent_name)){
                required_agents.put(agent_name, SimulationManager.getInstance().getAgent(agent_name));
            }
        }
        return required_agents;
    }

    public void clearPersistentData() {
        this.requried_agents.clear();
        this.required_uniforms.clear();
        this.required_imports_in_main.clear();
        this.required_imports.clear();
    }

    public HashSet<String> getRequiredUniforms() {
        return this.required_uniforms;
    }

    public HashSet<String> getRequiredIncludesInMain() {
        return this.required_imports_in_main;
    }

    public HashSet<String> getRequiredIncludes() {
        return this.required_imports;
    }

    public Node getNode(String node_identifier) {
        return this.nodes.getOrDefault(node_identifier, null);
    }

    public Collection<Node> getAllNodes() {
        return this.nodes.values();
    }
}
