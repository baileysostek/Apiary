package pegs.simulation;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import graphics.ShaderManager;
import pegs.Peg;
import pegs.PegManager;
import simulation.Simulation;
import simulation.SimulationManager;
import util.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

public class PegAgentRead extends Peg {

    public PegAgentRead() {
        super("@agent_read", 3);
    }

    @Override
    protected void action(Stack<JsonElement> stack, JsonElement[] params) {
        //TODO
    }

    @Override
    protected String toGLSL(Stack<JsonElement> stack, JsonElement[] params) {
        HashMap<String, Object> substitutions = new HashMap<>();
        substitutions.put("agent_type", PegManager.getInstance().transpile(params[0]));
        substitutions.put("agent_index", PegManager.getInstance().transpile(params[1]));
        substitutions.put("agent_property", PegManager.getInstance().transpile(params[2]));
        substitutions.put("read_identifier", ShaderManager.getInstance().getSSBOReadIdentifier());

        // Since this is the all command, we are going to compute the number of agents of this type which have been allocated for the simulation.
        if(SimulationManager.getInstance().hasActiveSimulation()) {
            Simulation simulation = SimulationManager.getInstance().getActiveSimulation();
            if(simulation.hasAgent((String) substitutions.get("agent_type"))) {
                return StringUtils.format("{{agent_type}}{{read_identifier}}.agent[{{agent_index}}].{{agent_property}}", substitutions);
            }else{
                //TODO throw compilation error.
                System.err.println("Error cannot parse properties of this agent");
            }
        }

        // No agents found.
        return "0";
    }
}
