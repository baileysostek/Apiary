package util;

import java.util.Collection;
import java.util.LinkedHashSet;

public class ObservableLinkedHashSet<Key> {

    @FunctionalInterface
    public interface AddCallback{
        void onAdd(Object key);
    }

    @FunctionalInterface
    public interface RemoveCallback{
        void onRemove(Object value);
    }

    private LinkedHashSet<Key> values = new LinkedHashSet<>();

    private AddCallback add_callback;
    private RemoveCallback remove_callback;

    public void onAdd(AddCallback add_callback){
        this.add_callback = add_callback;
    }

    public void onRemove(RemoveCallback remove_callback){
        this.remove_callback = remove_callback;
    }

    public void add(Key key){
        if(!this.values.contains(key)){
            if(this.add_callback != null) {
                this.add_callback.onAdd(key);
            }
        }

        this.values.add(key);
    }

    public void remove(Key key){
        if(this.values.contains(key)){
            if(this.remove_callback != null){
                this.remove_callback.onRemove(key);
            }
        }
        values.remove(key);
    }

    public boolean contains(Key key){
        return this.values.contains(key);
    }

    public Collection<Key> getValues(){
        return values;
    }
}
