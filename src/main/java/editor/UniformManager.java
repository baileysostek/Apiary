package editor;

import graphics.GLDataType;
import graphics.ShaderManager;
import graphics.Uniform;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class UniformManager {
    private static UniformManager instance;

    private LinkedList<ConstraintedUniform> uniforms = new LinkedList<>();

    private UniformManager() {
        uniforms.add(new ConstraintedUniform(ShaderManager.getInstance().createUniform("bg_color"  , GLDataType.VEC3), UniformRenderer.COLOR_PICKER));
        uniforms.add(new ConstraintedUniform(ShaderManager.getInstance().createUniform("fg_color"  , GLDataType.VEC3), UniformRenderer.COLOR_PICKER));
        uniforms.add(new ConstraintedUniform(ShaderManager.getInstance().createUniform("sensor", GLDataType.FLOAT), UniformRenderer.DRAG));
        HashMap<UniformConstraint, Float> turn_constraints = new HashMap<>();
        turn_constraints.put(UniformConstraint.MIN, 0f);
        turn_constraints.put(UniformConstraint.MAX, 3.14159f * 2f);
        uniforms.add(new ConstraintedUniform(ShaderManager.getInstance().createUniform("turn"  , GLDataType.FLOAT), UniformRenderer.SLIDER, turn_constraints));
        HashMap<UniformConstraint, Float> angle_constraints = new HashMap<>();
        angle_constraints.put(UniformConstraint.MIN, 0f);
        angle_constraints.put(UniformConstraint.MAX, 3.14159f);
        uniforms.add(new ConstraintedUniform(ShaderManager.getInstance().createUniform("angle" , GLDataType.FLOAT), UniformRenderer.SLIDER, angle_constraints));
        HashMap<UniformConstraint, Float> evaporation_constraints = new HashMap<>();
        evaporation_constraints.put(UniformConstraint.MIN, 0f);
        evaporation_constraints.put(UniformConstraint.MAX, 1.05f);
        evaporation_constraints.put(UniformConstraint.DEFAULT, 1.05f);
        uniforms.add(new ConstraintedUniform(ShaderManager.getInstance().createUniform("evaporation_constant" , GLDataType.FLOAT), UniformRenderer.SLIDER, evaporation_constraints));
        uniforms.add(new ConstraintedUniform(ShaderManager.getInstance().createUniform("speed" , GLDataType.FLOAT), UniformRenderer.DRAG));
    }

    public static void initialize() {
        if (instance == null) {
            instance = new UniformManager();
        }
    }

    public static UniformManager getInstance() {
        return instance;
    }

    public void render(Collection<Uniform> to_render){
        for(ConstraintedUniform uniform : uniforms){
            uniform.render();
        }
    }

    // Internal class to hold a uniform and constraints
    private class ConstraintedUniform {
        Uniform uniform;
        UniformRenderer renderer;
        HashMap<UniformConstraint, Float> constraints;

        public ConstraintedUniform(Uniform uniform, UniformRenderer renderer) {
            this(uniform, renderer, new HashMap<>());
        }

        public ConstraintedUniform(Uniform uniform, UniformRenderer renderer, HashMap<UniformConstraint, Float> constraints) {
            this.uniform = uniform;
            this.renderer = renderer;
            this.constraints = constraints;
        }

        public void setDefaultValue(float[] value){
            this.uniform.set(value);
        }

        public void render(){
            renderer.render(uniform, constraints);
        }
    }

}
