package pegs;

import com.google.gson.*;
import pegs.logic.PegAnd;
import pegs.logic.PegNot;
import pegs.logic.PegOr;
import pegs.logic.PegXor;
import pegs.math.PegGreater;
import pegs.math.PegLess;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

public class PegManager {
    private static PegManager instance;
    private static JsonParser parser = new JsonParser();
    private PegManager(){}
    static{
        instance = new PegManager();

        instance.registerPeg(new PegRandomFloat());
        instance.registerPeg(new PegConditional());

        // Boolean Logic
        instance.registerPeg(new PegAnd());
        instance.registerPeg(new PegNot());
        instance.registerPeg(new PegOr());
        instance.registerPeg(new PegXor());


        // Math
        instance.registerPeg(new PegGreater());
        instance.registerPeg(new PegLess());
    }
    private HashMap<String, Peg> pegs = new HashMap<>();

    public static PegManager getInstance(){
        return instance;
    }

    public void registerPeg(Peg peg){
        String key = peg.getKey();
        if(!this.pegs.containsKey(key)){
            this.pegs.put(key, peg);
        }else{
            // TODO throw exception
        }
    }

    public JsonElement parse(String data){
        // Turn our input string into a json array and enqueue them on the stack
        JsonElement element = parser.parse(data);
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
                stack.push(top);
            }
        }else{
            stack.push(element);
        }

        JsonElement top = stack.size() > 0 ? stack.peek() : new JsonNull();

        return top;
    }

    public LinkedList<String> transpile(String data){
        // Turn our input string into a json array and enqueue them on the stack
        return transpile(parser.parse(data));
    }
    public LinkedList<String> transpile(JsonElement element){

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
                stack.push(top);
            }
        }else{
            stack.push(element);
        }

        JsonElement top = stack.size() > 0 ? stack.peek() : new JsonNull();

        if(!(top instanceof JsonNull)) {
            LinkedList<String> test = new LinkedList<>();
            test.push(top.getAsString());
            glsl.push(test);
        }

        // Flat Map our strings into a single list of string.
        LinkedList<String> out = new LinkedList<>();
        for(LinkedList<String> lines : glsl){
            for(String line : lines){
                out.push(line);
            }
        }
        return out;
    }
}
