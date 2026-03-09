vec2 screenIndexToXY(int index) {
    return vec2(mod(index,window_width_pixels), floor(index / window_width_pixels));
}