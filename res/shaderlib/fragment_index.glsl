int x_pos = int(gl_GlobalInvocationID.x);
int y_pos = int(gl_GlobalInvocationID.y);
int window_width_pixels = int(u_window_size.x);
int window_height_pixels = int(u_window_size.y);


uint work_area_width = gl_WorkGroupSize.x * gl_NumWorkGroups.x;
uint row_size = (gl_WorkGroupSize.x * gl_WorkGroupSize.y) * gl_NumWorkGroups.x;
uint local_x = gl_LocalInvocationID.x;
uint grid_offset_x = gl_WorkGroupSize.x * gl_WorkGroupID.x;
uint local_y = gl_LocalInvocationID.y * work_area_width;

uint grid_offset_y = row_size * gl_WorkGroupID.y;

uint fragment_index = (local_x + grid_offset_x) + (local_y + grid_offset_y);
uint instance = fragment_index;