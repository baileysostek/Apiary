package editor;

import graphics.Uniform;
import imgui.ImGui;

import java.util.Map;

public enum UniformRenderer {
    COLOR_PICKER((uniform , constraints) -> {
        switch (uniform.getType()) {
            case VEC3: {
                ImGui.colorPicker3(uniform.getName(), uniform.get());
                break;
            }
            case VEC4: {
                break;
            }
        }
    }),
    SLIDER((uniform , constraints) -> {
        switch (uniform.getType()) {
            case FLOAT:{
                // check if we have a constraint
                float min = (float)constraints.getOrDefault(UniformConstraint.MIN , 0f);
                float max = (float)constraints.getOrDefault(UniformConstraint.MAX , 1f);
                ImGui.sliderFloat(uniform.getName(), uniform.get(), min, max);
            }
        }
    }),
    CHECKBOX((uniform , constraints) -> {

    }),
    DRAG((uniform , constraints) -> {
        switch (uniform.getType()) {
            case FLOAT:{
                ImGui.dragFloat(uniform.getName(), uniform.get());
            }
        }
    }),
    ;

    @FunctionalInterface
    private interface UniformRender<uniform extends Uniform>{
        void render(Uniform uniform, Map<UniformConstraint, Float> constraints);
    }
    private UniformRender renderFunction;

    UniformRenderer(UniformRender render_function) {
        this.renderFunction = render_function;
    }

    public void render(Uniform uniform, Map<UniformConstraint, Float> constraints){
        this.renderFunction.render(uniform,constraints);
    }
}
