package util;

import java.util.Collection;
import java.util.LinkedHashMap;

public class ObservableLinkedHashMap<Key , Value> {

    @FunctionalInterface
    public interface AddCallback{
        void onAdd(Object key);
    }

    @FunctionalInterface
    public interface RemoveCallback{
        void onRemove(Object value);
    }

    private LinkedHashMap<Key, Value> values = new LinkedHashMap<>();

    private AddCallback add_callback;
    private RemoveCallback remove_callback;

    public void onAdd(AddCallback add_callback){
        this.add_callback = add_callback;
    }

    public void onRemove(RemoveCallback remove_callback){
        this.remove_callback = remove_callback;
    }

    public void put(Key key, Value value){
        if(!this.values.containsKey(key)){
            if(this.add_callback != null) {
                this.add_callback.onAdd(key);
            }
        }

        this.values.put(key, value);
    }

    public void remove(Key key){
        if(this.values.containsKey(key)){
            if(this.remove_callback != null){
                this.remove_callback.onRemove(key);
            }
        }
        values.remove(key);
    }

    public boolean containsKey(Key key){
        return this.values.containsKey(key);
    }

    public boolean containsValue(Value value){
        return this.values.containsValue(value);
    }

    public Value get(Key key){
        return this.values.get(key);
    }

    public Value getOrDefault(Key key, Value default_value){
        return this.values.getOrDefault(key, default_value);
    }

    public Collection<Value> getValues(){
        return this.values.values();
    }
}
