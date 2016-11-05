package com.example.pontus.projektdatorgrafik;

import android.opengl.GLES20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glShaderSource;

class Shaders {

    private final String vertexShaderCode;
    private final String fragmentShaderCode;

    private Shaders(String vertexShaderCode, String fragmentShaderCode) {
        this.vertexShaderCode = vertexShaderCode;
        this.fragmentShaderCode = fragmentShaderCode;
    }

    public static Shaders from(InputStream vertexShaderId, InputStream fragmentShaderId) {
        return new Shaders(readShaderFile(vertexShaderId), readShaderFile(fragmentShaderId));
    }

    public int loadVertexShader() {
        return loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
    }

    public int loadFragmentShader() {
        return loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
    }

    private static String readShaderFile(InputStream shaderFileStream) {
        StringBuilder fileContent = new StringBuilder();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(shaderFileStream))) {
            String line;
            while((line = reader.readLine()) != null)
                fileContent.append(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileContent.toString();
    }

    private int loadShader(int type, String shaderCode){
        int shader = glCreateShader(type);
        glShaderSource(shader, shaderCode);
        glCompileShader(shader);
        return shader;
    }
}
