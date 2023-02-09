int x_pos = int(gl_GlobalInvocationID.x);
int y_pos = int(gl_GlobalInvocationID.y);
int window_width_pixels = int(u_window_size.x);
int window_height_pixels = int(u_window_size.y);
int fragment_index = int(gl_LocalInvocationIndex);
int fragment_index_old = x_pos + (y_pos * window_width_pixels);