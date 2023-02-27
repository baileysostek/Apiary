package graphics.texture;

import org.lwjgl.opengl.GL43;

public enum FilterOption {
    LINEAR(GL43.GL_LINEAR),
    NEAREST(GL43.GL_NEAREST),
    ;

    private int filter_option;

    FilterOption(int filter_option){
        this.filter_option = filter_option;
    }

    public int getFilterOption() {
        return filter_option;
    }
}
