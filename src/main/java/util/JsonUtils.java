package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import exceptions.JsonMissingKeyException;
import simulation.JsonSchema;

import java.io.StringReader;

public class JsonUtils {

    // Singleton Instance
    private static JsonUtils singleton;
    // Singleton variables

    private static JsonParser parser;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonSchema SCHEMA_SCHEMA;
    public JsonSchema SIMULATION_SCHEMA;
    public JsonSchema STEP_SCHEMA;

    private JsonUtils() {
        parser = new JsonParser();

        SIMULATION_SCHEMA = new JsonSchema(loadJson("schema/simulation.json"));
        STEP_SCHEMA = new JsonSchema(loadJson("schema/step.json"));
    }

    public static JsonObject parseJson(String json_data){
        JsonReader reader = new JsonReader(new StringReader(json_data));
        reader.setLenient(true);
        return parser.parse(reader).getAsJsonObject();
    }

    // Singleton initializer and getter
    public static void initialize() {
        if (singleton == null) {
            singleton = new JsonUtils();
        }
    }

    public static JsonUtils getInstance() {
        return singleton;
    }

    public JsonParser getParser(){
        return parser;
    }

    // TODO remove static instance.
    public static JsonObject loadJson(String file_name){
        String data = StringUtils.loadNoCache(file_name);
        if(data != null) {
            return parser.parse(data).getAsJsonObject();
        }else{
            return null;
        }
    }

    public static JsonObject loadJsonWithValidate(String file_name, JsonSchema schema){
        JsonObject object = loadJson(file_name);
        if(validate(object, schema)){
            return object;
        }
        return null;
    }
    public static boolean validate(JsonObject object, JsonSchema schema){
        if(object == null){
            return false;
        }

        for(String key : schema.getKeys()){
            try {
                if (!object.has(key)) {
                    throw new JsonMissingKeyException(key);
                }else{
                    // Check for a type mismatch
                    if(!schema.getTypeOf(key).equals(object.get(key).getClass().getSimpleName())){
                        System.out.println("Type Mismatch.");
                    }
                }
            }catch(JsonMissingKeyException exception){
                exception.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public static String prettyPrint(JsonObject source){
        return gson.toJson(source);
    }

    public static String prettyPrint(String source){
        return gson.toJson(source);
    }

}
