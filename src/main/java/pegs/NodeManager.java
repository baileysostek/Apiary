package pegs;

import com.google.gson.*;
import graphics.SSBO;
import graphics.ShaderManager;
import graphics.Uniform;
import pegs.code.NodeCode;
import pegs.controlflow.NodeConditional;
import pegs.controlflow.NodeForEach;
import pegs.controlflow.NodeOutColor;
import pegs.controlflow.NodeTernary;
import pegs.data.NodeVec2;
import pegs.data.NodeVec3;
import pegs.data.NodeVec4;
import pegs.geometry.NodeEmitPoint;
import pegs.logic.*;
import pegs.math.*;
import pegs.random.NodeRandomBool;
import pegs.random.NodeRandomFloat;
import pegs.simulation.NodeAgentCount;
import pegs.simulation.NodeAgentRead;
import pegs.simulation.NodeAgentWrite;
import pegs.simulation.NodeGetAgentAtIndex;
import pegs.types.NodeCast;
import pegs.variables.NodeDefine;
import pegs.variables.NodeGet;
import pegs.variables.NodeSet;
import pegs.vector.Node;
import pegs.vector.NodeNormalize;
import simulation.SimulationManager;
import util.JsonUtils;
import util.StringUtils;

import java.util.*;

public class NodeManager {
    private static NodeManager instance;
    private NodeManager(){}
    static{
        instance = new NodeManager();

        // Random
        instance.registerPeg(new NodeRandomFloat());
        instance.registerPeg(new NodeRandomBool());

        // Code
        instance.registerPeg(new NodeCode());

        // Controlflow
        instance.registerPeg(new NodeConditional());
        instance.registerPeg(new NodeForEach());
        instance.registerPeg(new NodeOutColor());
        instance.registerPeg(new NodeTernary());

        // Data
        instance.registerPeg(new NodeVec4());
        instance.registerPeg(new NodeVec3());
        instance.registerPeg(new NodeVec2());

        // Geometry
        instance.registerPeg(new NodeEmitPoint());

        // Boolean Logic
        instance.registerPeg(new NodeAnd());
        instance.registerPeg(new NodeEquals());
        instance.registerPeg(new NodeNot());
        instance.registerPeg(new NodeOr());
        instance.registerPeg(new NodeXor());

        // Math operations
        instance.registerPeg(new NodeAbs());
        instance.registerPeg(new NodeAdd());
        instance.registerPeg(new NodeCos());
        instance.registerPeg(new NodeDiv());
        instance.registerPeg(new NodeGreater());
        instance.registerPeg(new NodeIncrementBy());
        instance.registerPeg(new NodeLess());
        instance.registerPeg(new NodeMax());
        instance.registerPeg(new NodeMin());
        instance.registerPeg(new NodeMod());
        instance.registerPeg(new NodeMul());
        instance.registerPeg(new NodePI());
        instance.registerPeg(new NodeReadSensor());
        instance.registerPeg(new NodeRotate2D());
        instance.registerPeg(new NodeSin());
        instance.registerPeg(new NodeSub());
        instance.registerPeg(new NodeTan());
        instance.registerPeg(new NodeXYToScreenIndex());
        instance.registerPeg(new NodeOnScreen());

        // Simulation
        instance.registerPeg(new NodeAgentCount());
        instance.registerPeg(new NodeGetAgentAtIndex());
        instance.registerPeg(new NodeAgentRead());
        instance.registerPeg(new NodeAgentWrite());

        // Types
        instance.registerPeg(new NodeCast());

        // Variables
        instance.registerPeg(new NodeDefine());
        instance.registerPeg(new NodeGet());
        instance.registerPeg(new NodeSet());

        // Vector math
        instance.registerPeg(new Node());
        instance.registerPeg(new NodeNormalize());
    }
    // Holds all pegs that our system knows about.
    private HashMap<String, pegs.Node> pegs = new HashMap<>();

    // Required uniforms
    private HashSet<String> required_uniforms = new HashSet<>();
    private HashSet<String> required_imports = new HashSet<>();
    private HashSet<String> required_imports_in_main = new HashSet<>();
    private HashSet<String> requried_agents = new HashSet<>();

    public static NodeManager getInstance(){
        return instance;
    }

    // TODO: Maybe make an API that can be called into here.
    private void registerPeg(pegs.Node peg){
        String key = peg.getKey();
        if(!this.pegs.containsKey(key)){
            this.pegs.put(key, peg);
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
                            if (pegs.containsKey(instruction)) {
                                // Determine what peg we are trying to reference.
                                pegs.Node action = pegs.get(instruction);

                                // Now that we have our peg lets figure out the requirements.
                                for(String uniform_name : action.getRequiredUniforms()){
                                    this.required_uniforms.add(uniform_name);
                                }

                                // Now that we have our peg lets figure out the requirements.
                                for(String import_name : action.getRequiredImports()){
                                    this.required_imports.add(import_name);
                                }

                                // Now that we have our peg lets figure out the requirements.
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
}
