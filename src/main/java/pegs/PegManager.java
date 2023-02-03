package pegs;

import com.google.gson.*;
import graphics.ShaderManager;
import graphics.Uniform;
import pegs.code.PegCode;
import pegs.controlflow.PegConditional;
import pegs.controlflow.PegForEach;
import pegs.controlflow.PegOutColor;
import pegs.data.PegVec2;
import pegs.data.PegVec3;
import pegs.data.PegVec4;
import pegs.logic.PegAnd;
import pegs.logic.PegNot;
import pegs.logic.PegOr;
import pegs.logic.PegXor;
import pegs.math.PegGreater;
import pegs.math.PegLess;
import pegs.random.PegRandomBool;
import pegs.random.PegRandomFloat;
import pegs.simulation.PegAgentCount;
import pegs.simulation.PegAgentRead;
import pegs.simulation.PegAgentWrite;
import pegs.simulation.PegGetAgentAtIndex;
import pegs.variables.PegDefine;
import pegs.variables.PegSet;
import util.JsonUtils;
import util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

public class PegManager {
    private static PegManager instance;
    private PegManager(){}
    static{
        instance = new PegManager();

        // Random
        instance.registerPeg(new PegRandomFloat());
        instance.registerPeg(new PegRandomBool());

        // Code
        instance.registerPeg(new PegCode());

        // Controlflow
        instance.registerPeg(new PegConditional());
        instance.registerPeg(new PegForEach());
        instance.registerPeg(new PegOutColor());

        // Data
        instance.registerPeg(new PegVec4());
        instance.registerPeg(new PegVec3());
        instance.registerPeg(new PegVec2());


        // Boolean Logic
        instance.registerPeg(new PegAnd());
        instance.registerPeg(new PegNot());
        instance.registerPeg(new PegOr());
        instance.registerPeg(new PegXor());

        // Math
        instance.registerPeg(new PegGreater());
        instance.registerPeg(new PegLess());

        // Simulation
        instance.registerPeg(new PegAgentCount());
        instance.registerPeg(new PegGetAgentAtIndex());
        instance.registerPeg(new PegAgentRead());
        instance.registerPeg(new PegAgentWrite());

        // Variables
        instance.registerPeg(new PegDefine());
        instance.registerPeg(new PegSet());
    }
    // Holds all pegs that our system knows about.
    private HashMap<String, Peg> pegs = new HashMap<>();

    // Required uniforms
    private HashSet<String> required_uniforms = new HashSet<>();
    private HashSet<String> required_imports = new HashSet<>();

    public static PegManager getInstance(){
        return instance;
    }

    // TODO: Maybe make an API that can be called into here.
    private void registerPeg(Peg peg){
        String key = peg.getKey();
        if(!this.pegs.containsKey(key)){
            this.pegs.put(key, peg);
        }else{
            // TODO throw exception
        }
    }

    public String generateGLSL(JsonElement element){
        // First thing we want to do is clear the set of requirements that was generated the last time this method was called.
        required_uniforms.clear();
        required_imports.clear();

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
                                Peg action = pegs.get(instruction);

                                // Now that we have our peg lets figure out the requirements.
                                for(String uniform_name : action.getRequiredUniforms()){
                                    this.required_uniforms.add(uniform_name);
                                }

                                // Now that we have our peg lets figure out the requirements.
                                for(String import_name : action.getRequiredImports()){
                                    this.required_imports.add(import_name);
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
}
