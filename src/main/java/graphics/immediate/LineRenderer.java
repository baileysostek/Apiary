package graphics.immediate;

import graphics.ShaderManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;

public class LineRenderer {

    private int MAX_LINES = 1024;
    private float[] lines;
    private float[] colors;

    private int lines_this_frame = 0;

    private static LineRenderer instance;

    private int vao_id;
    private int vbo_id_positions;
    private int vbo_id_colors;

    private int shader_program_id;

    private LineRenderer(){
        vao_id = GL43.glGenVertexArrays();
        vbo_id_positions = GL43.glGenBuffers();
        vbo_id_colors = GL43.glGenBuffers();

        this.reallocate(MAX_LINES);

        shader_program_id = ShaderManager.getInstance().linkShader(
            ShaderManager.getInstance().compileShader(GL43.GL_VERTEX_SHADER,
    "#version 430\n" +
                "layout (location = 0) in vec3 pos;\n" +
                "layout (location = 1) in vec3 col;\n" +
                "uniform mat4 model;\n" +
                "uniform mat4 view;\n" +
                "uniform mat4 projection;\n" +
                "out vec3 pass_color;\n" +
                "void main(){\n" +
                "pass_color = col;\n" +
//                "gl_Position = projection * view * vec4(pos, 1.0);\n" +
                "gl_Position = vec4(pos, 1.0);\n" +
                "}\n"
            ),
            ShaderManager.getInstance().compileShader(GL43.GL_FRAGMENT_SHADER,
    "#version 430\n" +
                "in vec3 pass_color;\n" +
                "out vec4 fragment_color;\n" +
                "void main(){\n" +
                "fragment_color = vec4(pass_color.rgb, 1.0);\n" +
                "}\n"
            )
        );

        System.out.println(shader_program_id);
    }

    private void reallocate(int capacity){
        lines  = new float[capacity * (2 * 3)];
        colors = new float[capacity * (2 * 3)];

        GL43.glBindVertexArray(vao_id);
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo_id_positions);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, lines, GL43.GL_STATIC_DRAW);
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo_id_colors);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, colors, GL43.GL_STATIC_DRAW);
    }

    // Methods for drawing lines
    public void drawLine(float start_x, float start_y, float start_z, float end_x, float end_y, float end_z, float start_color_r, float start_color_g, float start_color_b, float end_color_r, float end_color_g, float end_color_b){
        if(lines_this_frame >= instance.MAX_LINES){
            // NOP
        } else {
            lines[lines_this_frame * 6 + 0] = start_x;
            lines[lines_this_frame * 6 + 1] = start_y;
            lines[lines_this_frame * 6 + 2] = start_z;
            lines[lines_this_frame * 6 + 3] = end_x;
            lines[lines_this_frame * 6 + 4] = end_y;
            lines[lines_this_frame * 6 + 5] = end_z;

            colors[lines_this_frame * 6 + 0] = start_color_r;
            colors[lines_this_frame * 6 + 1] = start_color_g;
            colors[lines_this_frame * 6 + 2] = start_color_b;
            colors[lines_this_frame * 6 + 3] = end_color_r;
            colors[lines_this_frame * 6 + 4] = end_color_g;
            colors[lines_this_frame * 6 + 5] = end_color_b;
        }

        lines_this_frame++;
    }


    public void render(){

        GL43.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        GL43.glUseProgram(this.shader_program_id);
        // TODO load camera junk in

        GL43.glBindVertexArray(this.vao_id);
        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo_id_positions);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, lines, GL43.GL_STATIC_DRAW);
        GL43.glVertexAttribPointer(0, 3, GL43.GL_FLOAT, false, 0, 0);

        GL43.glBindBuffer(GL43.GL_ARRAY_BUFFER, vbo_id_colors);
        GL43.glBufferData(GL43.GL_ARRAY_BUFFER, colors, GL43.GL_STATIC_DRAW);
        GL43.glVertexAttribPointer(1, 3, GL43.GL_FLOAT, false, 0, 0);

        GL43.glEnableVertexAttribArray(0);
        GL43.glEnableVertexAttribArray(1);

        GL43.glDrawArrays(GL43.GL_LINES, 0, lines_this_frame * 2);

        GL43.glDisableVertexAttribArray(0);
        GL43.glDisableVertexAttribArray(1);

        GL43.glBindVertexArray(0);

        lines_this_frame = 0;
    }


    // Singleton Methods
    public static void initialize(){
        if(instance == null){
            instance = new LineRenderer();
        }
    }

    public static LineRenderer getInstance(){
        return instance;
    }

}
