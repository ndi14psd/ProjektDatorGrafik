package com.example.pontus.projektdatorgrafik;

import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glShaderSource;

class Shaders {
    public final static String VERTEX_SHADER_CODE =
            "attribute vec3 vPosition; \n" +
                    "uniform mat4 uMVPMatrix;\n" +
                    "uniform float uMaxHeight; \n" +
                    "varying vec4 c; \n" +
                    "void main() { \n" +
                    "  vec4 position = vec4(vPosition.xy, 0.0, 1.0); \n" +
                    "  gl_Position = uMVPMatrix * position;\n" +
                    "  float z = max(vPosition.z, 0.0) / uMaxHeight; \n" +
                    "  c = vec4(z, z, z, 1.0); \n" +
                    "}";

    public final static String FRAGMENT_SHADER_CODE =
            "precision highp float;\n" +
                    "varying vec4 c;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = c;\n" +
                    "}";

    public static int loadShader(int type, String shaderCode){
        int shader = glCreateShader(type);
        glShaderSource(shader, shaderCode);
        glCompileShader(shader);
        return shader;
    }
}
