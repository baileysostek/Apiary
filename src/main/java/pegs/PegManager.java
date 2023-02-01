package pegs;

import com.google.gson.*;
import pegs.code.PegCode;
import pegs.controlflow.PegConditional;
import pegs.controlflow.PegForEach;
import pegs.controlflow.PegOutColor;
import pegs.data.PegVec4;
import pegs.logic.PegAnd;
import pegs.logic.PegNot;
import pegs.logic.PegOr;
import pegs.logic.PegXor;
import pegs.math.PegGreater;
import pegs.math.PegLess;
import pegs.random.PegRandomFloat;
import pegs.simulation.PegAgentCount;
import pegs.simulation.PegAgentRead;
import pegs.simulation.PegAgentWrite;
import pegs.simulation.PegGetAgentAtIndex;
import pegs.variables.PegDefine;
import pegs.variables.PegSet;
import util.JsonUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

public class PegManager {
    private static PegManager instance;
    private PegManager(){}
    static{
        instance = new PegManager();

        // Reigster all of our pegs
        instance.registerPeg(new PegRandomFloat());

        // Code
        instance.registerPeg(new PegCode());

        // Controlflow
        instance.registerPeg(new PegConditional());
        instance.registerPeg(new PegForEach());
        instance.registerPeg(new PegOutColor());

        // Data
        instance.registerPeg(new PegVec4());

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

    // When parsing we need to know our scope depth so that we can name variables correctly.
    private static int scope_depth = 0;

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

    public JsonElement parse(String data){
        // Turn our input string into a json array and enqueue them on the stack
        JsonElement element = JsonUtils.getInstance().getParser().parse(data);
        return parse(element);
    }

    public JsonElement parse(JsonElement element){
        // Clear the stack.
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
                                Peg action = pegs.get(instruction);
                                action.performAction(stack);
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

        JsonElement top = stack.size() > 0 ? stack.peek() : new JsonNull();

        return top;
    }

    public String transpile(String data){
        // Turn our input string into a json array and enqueue them on the stack
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
                                Peg action = pegs.get(instruction);
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
