package compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import graphics.SSBO;
import graphics.ShaderManager;
import graphics.Uniform;
import simulation.SimulationManager;
import util.JsonUtils;
import util.StringUtils;

import java.util.*;

public class GLSLCompiler {
    private static GLSLCompiler instance;
    private GLSLCompiler(){
        for(FunctionDirective nodes : FunctionDirective.values()){
            registerNode(nodes);
        }
    }
    // Holds all nodes that our system knows about.
    private HashMap<String, FunctionDirective> nodes = new HashMap<>();

    // Required uniforms
    private HashSet<String> required_uniforms = new HashSet<>();
    private HashSet<String> required_imports = new HashSet<>();
    private HashSet<String> required_imports_in_main = new HashSet<>();
    private HashSet<String> requried_agents = new HashSet<>();

    public static void initialize(){
        if(instance == null){
            instance = new GLSLCompiler();
        }
    }

    public static GLSLCompiler getInstance(){
        return instance;
    }

    // TODO: Maybe make an API that can be called into here.
    private void registerNode(FunctionDirective node){
        String key = node.getNodeID();
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
                                FunctionDirective action = nodes.get(instruction);

                                // Now that we have our node lets figure out the requirements.
                                for(String uniform_name : action.getRequiredUniforms()){
                                    this.required_uniforms.add(uniform_name);
                                }

                                // Now that we have our node lets figure out the requirements.
                                for(String import_name : action.getRequiredLibraries()){
                                    this.required_imports.add(import_name);
                                }

//                                // Now that we have our node lets figure out the requirements.
//                                for(String import_name : action.getRequiredInMain()){
//                                    this.required_imports_in_main.add(import_name);
//                                }

                                action.transpile(stack);
                                continue;
                            }else{
                                System.err.println("Unknown instruction:"+instruction);
                                System.exit(1);
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
                        // Here we need to handle how to process the next element
                        while(true){
                            if (array.size() == 1) {
                                // Single Element
                                if(array.get(0).isJsonArray()){
                                    array = array.get(0).getAsJsonArray();
                                }else {
                                    // Not Json Array either object or literal
                                    test.push(array.get(0).getAsString());
                                    break loop;
                                }
                            }else{
                                // Complex element
                                test.push(transpile(array));
                                break loop;
                            }
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

    public FunctionDirective getNode(String node_identifier) {
        return this.nodes.getOrDefault(node_identifier, null);
    }

    public Collection<FunctionDirective> getAllNodes() {
        return this.nodes.values();
    }

    public void requireUniform(String uniform_name) {
        this.required_uniforms.add(uniform_name);
    }
}
