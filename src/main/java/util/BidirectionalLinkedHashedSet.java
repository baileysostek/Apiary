package util;

import java.util.LinkedHashMap;

public class BidirectionalLinkedHashedSet<Key , Value> {

    @FunctionalInterface
    public interface AddCallback{
        void onAdd(Object key);
    }

    @FunctionalInterface
    public interface RemoveCallback{
        void onRemove(Object value);
    }

    private LinkedHashMap<Key, Value> key_value = new LinkedHashMap<>();
    private LinkedHashMap<Value, Key> value_key = new LinkedHashMap<>();

    private AddCallback add_callback;
    private RemoveCallback remove_callback;

    public void onAdd(AddCallback add_callback){
        this.add_callback = add_callback;
    }

    public void onRemove(RemoveCallback remove_callback){
        this.remove_callback = remove_callback;
    }

    public void add(Key key, Value value){
        Value last_value = value;

        if(!this.key_value.containsKey(key)){
            if(this.add_callback != null) {
                this.add_callback.onAdd(key);
            }
        }else{
            last_value = this.key_value.get(key);
        }

        this.key_value.put(key, value);
        this.value_key.put(last_value, key);
    }

    public void remove(Key key){
        Value value = key_value.get(key);

        if(this.key_value.containsKey(key)){
            if(this.remove_callback != null){
                this.remove_callback.onRemove(key);
            }
        }

        key_value.remove(key);
        value_key.remove(value);
    }

    public boolean containsKey(Key key){
        return this.key_value.containsKey(key);
    }

    public boolean containsValue(Value value){
        return this.value_key.containsKey(value);
    }

    public Key getKey(Value value){
        return this.value_key.get(value);
    }

    public Key getKeyOrDefault(Value value, Key default_value){
        return this.value_key.getOrDefault(value, default_value);
    }

    public Value get(Key key){
        return this.key_value.get(key);
    }

    public Value getValueOrDefault(Key key, Value default_value){
        return this.key_value.getOrDefault(key, default_value);
    }
}
