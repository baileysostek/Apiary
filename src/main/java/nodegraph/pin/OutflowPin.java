package nodegraph.pin;

import com.google.gson.JsonElement;
import nodegraph.Node;

import java.util.Collection;
import java.util.LinkedHashSet;

public class OutflowPin extends Pin{

    private LinkedHashSet<InflowPin> links = new LinkedHashSet<>();

    public OutflowPin(Node parent, String attribute_name, PinType type) {
        super(parent, attribute_name, type, PinDirection.SOURCE);
    }

    @Override
    public void link(Pin other){
        // We can only allow a connection to an InflowPin.
        if(!(other instanceof InflowPin)){
            return;
        }

        if(canLink(other)){
            // Check if this is a flow
            if(this.getType().equals(PinType.FLOW)){
                if(isConnected()){
                    // We cant be connected to more than one thing as a flow
                    this.links.clear();
                }
            }

            // Add this new connection
            this.links.add((InflowPin) other);
        }
    }

    @Override
    public void disconnect(){
        for(Pin connection : new LinkedHashSet<>(this.links)){
            connection.disconnect();
        }
        this.links.clear();
    }

    public void disconnect(InflowPin pin){
        if(this.links.contains(pin)){
            this.links.remove(pin);
        }
    }

    public Collection<InflowPin> getConnections(){
        return this.links;
    }

    public InflowPin getConnection(){
        return this.links.stream().findFirst().get();
    }

    @Override
    public boolean isConnected() {
        return this.links.size() > 0;
    }

    @Override
    public boolean isConnectedTo(Pin other) {
        return links.contains(other);
    }

    public void renderLinks() {

    }

    public JsonElement getValue(){
        return this.getParent().getValueOfPin(this);
    }
}
