package nodegraph;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;

public class ImNodeAddons {
    private static ImNodeAddons instance;

    private ImNodeAddons() {

    }

    public static void initialize() {
        if (instance == null) {
            instance = new ImNodeAddons();
        }
    }

    public static ImNodeAddons getInstance() {
        return instance;
    }

    // Fancy methods
    // https://stackoverflow.com/questions/64653747/how-to-center-align-text-horizontally
    void textCentered(float containerWidth, String text) {
        float text_width = ImGui.calcTextSize(text).x;

        // calculate the indentation that centers the text on one line, relative
        // to window left, regardless of the `ImGuiStyleVar_WindowPadding` value
        float text_indentation = (containerWidth - text_width) * 0.5f;

        // if text is too long to be drawn on one line, `text_indentation` can
        // become too small or even negative, so we check a minimum indentation
        float min_indentation = 20.0f;
        if (text_indentation <= min_indentation) {
            text_indentation = min_indentation;
        }

        ImGui.sameLine(text_indentation);
        ImGui.pushTextWrapPos(containerWidth - text_indentation);
        ImGui.textWrapped(text);
        ImGui.popTextWrapPos();
    }
}
