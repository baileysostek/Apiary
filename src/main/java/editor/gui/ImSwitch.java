package editor.gui;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;

/**
 * Self-contained animated toggle switch widget (MUI-style).
 * The caller owns an instance and calls {@link #render()} each frame.
 *
 * <pre>{@code
 * // Field in a node or widget:
 * private final SwitchState mySwitch = new SwitchState(false);
 *
 * // Each frame:
 * if (mySwitch.render()) {
 *     System.out.println("Toggled to: " + mySwitch.isOn());
 * }
 * }</pre>
 */
public class ImSwitch {
    private static final float TRACK_W    = 40f;
    private static final float TRACK_H    = 22f;
    private static final float THUMB_R    = 9f;
    private static final float PADDING    = 2f;
    private static final float ANIM_SPEED = 8f;   // full sweep in ~125 ms

    private static final int COLOR_TRACK_OFF = ImColor.rgb("#555558");
    private static final int COLOR_TRACK_ON  = ImColor.rgb("#1978D2");
    private static final int COLOR_THUMB     = ImColor.rgb("#FFFFFF");
    private static final int COLOR_HOVER_RING = ImColor.rgba("#FFFFFF28");  // white, ~16% alpha

    private boolean on;
    private float animationDelta;

    public ImSwitch() {
        this(false);
    }

    public ImSwitch(boolean initialValue) {
        this.on = initialValue;
        this.animationDelta = initialValue ? 1f : 0f;
    }

    public boolean render() {
        ImVec2 p = ImGui.getCursorScreenPos();

        // Reserve layout space and detect clicks.
        ImGui.pushID(System.identityHashCode(this));
        boolean clicked = ImGui.invisibleButton("##sw", TRACK_W, TRACK_H);
        ImGui.popID();
        if (clicked) on = !on;

        // Advance animation toward the target value.
        float dt = ImGui.getIO().getDeltaTime();
        animationDelta = on ? Math.min(animationDelta + dt * ANIM_SPEED, 1f)
               : Math.max(animationDelta - dt * ANIM_SPEED, 0f);

        // ── draw track ────────────────────────────────────────────────────────
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(
            p.x, p.y, p.x + TRACK_W, p.y + TRACK_H,
            lerpColor(COLOR_TRACK_OFF, COLOR_TRACK_ON, animationDelta),
            TRACK_H / 2f
        );

        // ── draw thumb ────────────────────────────────────────────────────────
        float travelLeft  = p.x + THUMB_R + PADDING;
        float travelRight = p.x + TRACK_W - THUMB_R - PADDING;
        float thumbCx     = travelLeft + animationDelta * (travelRight - travelLeft);
        float thumbCy     = p.y + TRACK_H / 2f;

        if (ImGui.isItemHovered()) {
            dl.addCircleFilled(thumbCx, thumbCy, THUMB_R + 3f, COLOR_HOVER_RING);
        }
        dl.addCircleFilled(thumbCx, thumbCy, THUMB_R, COLOR_THUMB);

        return clicked;
    }


    public boolean isOn() {
        return this.on;
    }
    public void set(boolean on) {
        this.on = on;
    }
    public void toggle() {
        this.on = !this.on;
    }

    private static int lerpColor(int a, int b, float t) {
        int ablue  =  a        & 0xFF,  bblue  =  b        & 0xFF;
        int agreen = (a >>  8) & 0xFF,  bgreen = (b >>  8) & 0xFF;
        int ared   = (a >> 16) & 0xFF,  bred   = (b >> 16) & 0xFF;
        int aalpha = (a >> 24) & 0xFF,  balpha = (b >> 24) & 0xFF;
        return ((int)(aalpha + (balpha - aalpha) * t) << 24)
             | ((int)(ared   + (bred   - ared  ) * t) << 16)
             | ((int)(agreen + (bgreen - agreen) * t) <<  8)
             |  (int)(ablue  + (bblue  - ablue ) * t);
    }
}
