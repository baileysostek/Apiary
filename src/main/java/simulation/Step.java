package simulation;


import java.util.Collection;
import java.util.LinkedHashSet;

public class Step {
// Involved Agents
    private LinkedHashSet<String> agents = new LinkedHashSet<>();

    public Collection<String> getAgents() {
        return agents;
    }

}
