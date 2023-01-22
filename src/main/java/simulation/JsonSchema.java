package simulation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import exceptions.JsonMissingKeyException;

import java.util.Collection;
import java.util.HashMap;

public class JsonSchema {
    private String name;
    private HashMap<String, String> fields = new HashMap<>();

    public JsonSchema(JsonObject schema) {
        try {
            if (schema != null) {
                // Parse the name of this schema
                if (!schema.has("name")) {
                    throw new JsonMissingKeyException("name");
                }
                this.name = schema.get("name").getAsString();

                // Parse the fields that we expect this schema to have.
                if (!schema.has("fields")) {
                    throw new JsonMissingKeyException("fields");
                }
                JsonObject schema_fields = ((JsonObject) schema.get("fields"));
                for (String key : schema_fields.keySet()) {
                    JsonElement field = schema_fields.get(key);
                    if(!field.getAsJsonObject().has("type")){
                        throw new JsonMissingKeyException(key+".type");
                    }else {
                        fields.put(key, field.getAsJsonObject().get("type").getAsString());
                    }
                }
            }
        }catch (JsonMissingKeyException e){
            e.printStackTrace();
        }
    }

    public Collection<String> getKeys(){
        return fields.keySet();
    }

    public String getTypeOf(String key){
        if(fields.containsKey(key)){
            return fields.get(key);
        }
        return "";
    }
}
