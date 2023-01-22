package simulation;


import graphics.ComputeShader;

import java.util.Collection;
import java.util.LinkedHashSet;

public class Step {
// Involved Agents
    private LinkedHashSet<String> agents = new LinkedHashSet<>();
    private ComputeShader compute_shader;

    public Collection<String> getAgents() {
        return agents;
    }
//    inputs
//    uniform adjustments?
//    outputs
}
