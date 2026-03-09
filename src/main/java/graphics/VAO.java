package graphics;

import org.lwjgl.opengl.GL43;

import java.util.LinkedHashMap;

public class VAO {
    private final int id;
    private final LinkedHashMap<String, VBO> vbos = new LinkedHashMap<>();

    public VAO() {
        this.id = GL43.glGenVertexArrays();
    }

    public void addVBO(){
        int binding_location = vbos.size();
    }

    public void cleanup(){
        GL43.glDeleteVertexArrays(this.id);
    }

    public int getID() {
        return this.id;
    }

    public void bind() {
        GL43.glBindVertexArray(this.id);
    }

    public void unbind(){
        GL43.glBindVertexArray(0);
    }
}
