attribute vec3 vPosition;
uniform mat4 uMVPMatrix;
uniform float uMaxHeight;

varying float z;
varying float zMax;

void main() {
    vec4 position = vec4(vPosition.xyz, 1.0);
    z = vPosition.z;
    zMax = uMaxHeight;
    gl_Position = uMVPMatrix * position;
}
