package camera;

public enum ViewMode {

    VIEW_2D(0),
    VIEW_3D(1),
    ;

    protected int view_mode;

    ViewMode(int view_mode) {
        this.view_mode = view_mode;
    }

    public int getViewMode() {
        return view_mode;
    }
}
