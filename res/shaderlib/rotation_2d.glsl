// this function accepts a direction (header) for a
// agent and a rotation in radians, returning the
// new, rotated direction
vec2 rotate(vec2 dir, float angle) {
    float  s = sin( angle );
    float  c = cos( angle );
    mat2   m = mat2( c, -s, s, c );
    return m * dir;
}

vec2 readSensor( vec2 pos, vec2 dir, float angle, float distance ) {
    vec2 newangle  = rotate( dir, angle  );
    vec2 offset = newangle * distance;
    return pos + offset;
}