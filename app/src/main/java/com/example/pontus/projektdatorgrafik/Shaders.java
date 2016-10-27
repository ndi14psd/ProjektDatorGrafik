package com.example.pontus.projektdatorgrafik;

import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glShaderSource;

/**
 * Created by Pontus on 2016-10-24.
 */
class Shaders {
    public final static String VERTEX_SHADER_CODE =
                    "attribute vec3 vPosition; \n" +
                    "uniform mat4 uMVPMatrix;\n" +

                    "varying vec4 c; \n" +
                    "attribute vec4 vColor; \n" +
                    "void main() { \n" +
                    "  c = vColor; \n" +
                    "  gl_PointSize = 50; \n" +
                    "  vec4 point = vec4(vPosition, 1.0); \n" +
                    "  //point.xy -= 25; \n" +
                    "  gl_Position = uMVPMatrix * point;\n" +
                    "  gl_Position.z = 0.0;\n" +
                    "  gl_Position.w = 1.0;\n" +
                    "}";

    public final static String FRAGMENT_SHADER_CODE =
                    "precision mediump float; \n" +
                    "uniform vec4 vColor; \n" +
                    "void main() {\n" +
                    "  gl_FragColor = vColor;\n" +
                    "}";

    public static int loadShader(int type, String shaderCode){
        int shader = glCreateShader(type);
        glShaderSource(shader, shaderCode);
        glCompileShader(shader);
        return shader;
    }
}
