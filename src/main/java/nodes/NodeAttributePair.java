package nodes;

public class NodeAttributePair {
    public final Node node;
    public final String attribute;
    public final int node_id;
    public final int pin_id;

    public NodeAttributePair(Node node, String attribute, int node_id, int pin_id) {
        this.node = node;
        this.attribute = attribute;
        this.node_id = node_id;
        this.pin_id = pin_id;
    }
}
