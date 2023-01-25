package graphics;

import org.lwjgl.opengl.GL46;

import java.util.LinkedHashMap;

public class VAO {
    private final int id;
    private final LinkedHashMap<String, VBO> vbos = new LinkedHashMap<>();

    public VAO() {
        this.id = GL46.glCreateVertexArrays();
    }

    public void addVBO(){
        int binding_location = vbos.size();
    }

    public void cleanup(){
        GL46.glDeleteVertexArrays(this.id);
    }

    public int getID() {
        return this.id;
    }

    public void bind() {
        GL46.glBindVertexArray(this.id);
    }

    public void unbind(){
        GL46.glBindVertexArray(0);
    }
}
