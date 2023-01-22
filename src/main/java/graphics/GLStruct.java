package graphics;

import java.util.HashMap;
import java.util.LinkedHashMap;

public abstract class GLStruct {
    private LinkedHashMap<String, GLDataType> attributes = new LinkedHashMap<>();
    private LinkedHashMap<String, Integer> location_cache = new LinkedHashMap<>();

    private final String name;

    public GLStruct(String name) {
        this.name = name;
    }

    //TODO: make this more efficient maybe?
    /**
     * So, at a minimum we need to use 4 floats to represent something in memory, this is a little bit wasteful in that we will have holes in our SSBO data. But its okay for now.
     * @return Returns the size in floats that can be used to SAFELY represent this data
     */
    public final int computeSafeSizeInFloats(){
        int total = 0;
        for(GLDataType type : attributes.values()){
            total += Math.ceil(type.getSizeInFloats() / 4.0f) * 4;
        }
        return total;
    }

    public final int computeSizeInFloats(){
        int total = 0;
        for(GLDataType type : attributes.values()){
            total += type.getSizeInFloats();
        }
        return total;
    }

    public final void addAttribute(String attribute_name, GLDataType type){
        // Compute position of this new attribute
        if(attributes.containsKey(attribute_name)){
            // Error we cant have two attributes with the same name.
            System.err.println("Cant create a struct with two attributes with the same name.");
            System.exit(1);
        }else{
            // Insert our attribute and compute the offset.
            attributes.put(attribute_name, type);
            calculateOffsetTo(attribute_name);
            this.onAddAttribute(attribute_name, type);
        }
    }

    public void onAddAttribute(String attribute_name, GLDataType attribute_type){
        return;
    }

    public void removeAttribute(String attribute_name){
        if(attributes.containsKey(attribute_name)){
            System.err.println("removing attributes from GL Structs is not supported yet");
            System.exit(1);
        }
    }

    public int getOffsetTo(String attribute_name){
        if(location_cache.containsKey(attribute_name)){
            return location_cache.get(attribute_name);
        }
        return -1;
    }

    private void calculateOffsetTo(String attribute_name){
        // Calculate offset to this object
        int total = 0;
        // Very important that this is a linked hash map because we want to guarantee that the order of traversal through the data structure is the same as the addition order.
        for(String stored_attribute_name : attributes.keySet()){
            // If we find the attribute we are looking for, return the accumulated offset to this location.
            if(stored_attribute_name.equals(attribute_name)){
                location_cache.put(attribute_name, total);
                return;
            }else{
                // We haven't found the name we are looking for yet so increment total by the size of this GL type.
                total += Math.ceil(attributes.get(stored_attribute_name).getSizeInFloats() / ShaderManager.getInstance().ALIGNMENT) * ShaderManager.getInstance().ALIGNMENT;
            }
        }
    }

    public String getName() {
        return name;
    }

    /**
     * NOTE this returns a MUTABLE reference to the attributes representing this Struct, don't add or remove from the returned hashmap.
     * @return
     */
    protected HashMap<String, GLDataType> getAttributes() {
        return attributes;
    }
}
