package graphics;

import com.google.gson.JsonElement;
import pegs.PegManager;
import util.StringUtils;

import java.util.HashMap;

public class ComputeShader {

    private int primary_buffer;
    private int secondary_buffer;

    private final JsonElement compute_source_nodes;

    // These are all of the computed includes and agents and buffers this simulation is referenceing.

    public ComputeShader(JsonElement compute_source_nodes){
        this.compute_source_nodes = compute_source_nodes;
        PegManager.getInstance().transpile(compute_source_nodes);
    }

    private String generateShaderSource(){
        HashMap<String, Object> substitutions = new HashMap<>();
//
//        "uniform float u_time_seconds;\n" +
//        "uniform vec2 u_window_size;\n" +
//
//        "uniform vec2 u_mouse_pos_pixels;\n" +
//        "uniform vec4 u_mouse_pressed;\n" +


        String source =
            ShaderManager.getInstance().generateVersionString() +
            "{{uniforms}}" +
            String.format("layout (local_size_x = %s, local_size_y = %s) in;\n", ShaderManager.getInstance().getWorkGroupWidth(), ShaderManager.getInstance().getWorkGroupHeight()) +
            "{{includes}}" +
            "{{agents_ssbos}}" +
            "void main() {\n" +

            "int x_pos = int(gl_GlobalInvocationID.x);\n" +
            "int y_pos = int(gl_GlobalInvocationID.y);\n" +
            "int window_width_pixels = int(u_window_size.x);\n" +
            "int window_height_pixels = int(u_window_size.y);\n" +
            "int fragment_index = x_pos + (y_pos * window_width_pixels);\n" +

            "int neighbors = 0;\n" +
            "neighbors += cell_read.agent[int(mod(x_pos - 1, window_width_pixels)) + (int(mod(y_pos - 1, window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //top left\n" +
            "neighbors += cell_read.agent[int(mod(x_pos    , window_width_pixels)) + (int(mod(y_pos - 1, window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //top middle\n" +
            "neighbors += cell_read.agent[int(mod(x_pos + 1, window_width_pixels)) + (int(mod(y_pos - 1, window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //top right\n" +
            "neighbors += cell_read.agent[int(mod(x_pos - 1, window_width_pixels)) + (int(mod(y_pos    , window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //mid left\n" +
            "neighbors += cell_read.agent[int(mod(x_pos + 1, window_width_pixels)) + (int(mod(y_pos    , window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //mid right\n" +
            "neighbors += cell_read.agent[int(mod(x_pos - 1, window_width_pixels)) + (int(mod(y_pos + 1, window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //bottom left\n" +
            "neighbors += cell_read.agent[int(mod(x_pos    , window_width_pixels)) + (int(mod(y_pos + 1, window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //bottom mid\n" +
            "neighbors += cell_read.agent[int(mod(x_pos + 1, window_width_pixels)) + (int(mod(y_pos + 1, window_height_pixels)) * window_width_pixels)].alive ? 1 : 0; //bottom right\n" +
            "vec3  inputs = vec3( x_pos, y_pos, u_time_seconds ); // Spatial and temporal inputs\n" +
            "float rand   = random( inputs );              // Random per-pixel value\n" +
//                "vec3 color = rand > 0.5 ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);\n" +
            "if(cell_read.agent[fragment_index].alive){\n" +
            "cell_write.agent[fragment_index].alive = (neighbors == 2 || neighbors == 3);\n" +
            "}else{\n" +
            "cell_write.agent[fragment_index].alive = (neighbors == 3);\n" +
            "}\n" +

            "vec3 color = cell_read.agent[fragment_index].alive ? vec3(1.0, 1.0, 1.0) : cell_read.agent[fragment_index].color * 0.99;\n" +
            "cell_write.agent[fragment_index].color = color;\n" +
            "cell_write.agent[fragment_index].color = vec3(float(x_pos) / float(window_width_pixels), float(y_pos) / float(window_height_pixels), 0.0);\n" +
            "if(u_mouse_pressed.x > 0.0){\n"+
            "int mouse_index = int(clamp(u_mouse_pos_pixels.x, 0, u_window_size.x)) + (int(clamp(u_mouse_pos_pixels.y, 0, u_window_size.y)) * window_width_pixels);\n" +
            "cell_write.agent[mouse_index].alive = true;\n" +
            "}\n"+
            "}\n";

        return StringUtils.format(source, substitutions);
    }

    // Return a buffer depending on the current frame.
    // This gives us double buffering without a memcopy
    public int getShaderID(int current_frame){
        return (current_frame % 2 == 0) ? primary_buffer : secondary_buffer;
    }
}
