package com.example.pontus.projektdatorgrafik;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;
import static android.opengl.GLES20.glUseProgram;

/**
 * Created by Pontus on 2016-10-24.
 */
public class Square {

    private final FloatBuffer vertexDataBuffer;
    private final FloatBuffer colorDataBuffer;
    private final float[] triangleStrip;

    private final int program;
    private static final int VERTEX_POS_SIZE = 3;
    private static final int VERTEX_ATTRIB_SIZE = VERTEX_POS_SIZE;
    private static final int VERTEX_COUNT = 4;
    private static final int COLOR_SIZE = 4;

    private static final int COLOR_ATTRIB_SIZE = COLOR_SIZE;
    private final static float[] COLOR_DATA = new float[] {
            0.0f, 0.0f, 1.0f, 1.0f, // Red
            0.0f, 1.0f, 0.0f, 1.0f, // Green
            0.0f, 0.0f, 1.0f, 1.0f // Blue
    };

    public Square(Vertex center, float size, int program) {
        this.program =  program;
        this.triangleStrip = generateTriangleStrip(center, size);
        this.vertexDataBuffer = loadFloatBuffer(triangleStrip);
        this.colorDataBuffer = loadFloatBuffer(COLOR_DATA);
    }

    public Square(Vertex center, float size) {
        this(center, size, loadProgram(Shaders.loadShader(GL_VERTEX_SHADER, Shaders.VERTEX_SHADER_CODE), Shaders.loadShader(GL_FRAGMENT_SHADER, Shaders.FRAGMENT_SHADER_CODE)));
    }

    private static float[] generateTriangleStrip(final Vertex center, float size) {
        return new float[] {
                center.x - (size / 2), center.y - (size / 2), center.z,
                center.x - (size / 2), center.y + (size / 2), center.z,
                center.x + (size / 2), center.y - (size / 2), center.z,
                center.x + (size / 2), center.y + (size / 2), center.z,
        };
    }

    public float[] triangleStrip() {
        return triangleStrip;
    }

    public void draw(float[] mvpMatrix) {
        glUseProgram(program);

        int mMVPMatrixHandle = glGetUniformLocation(program, "uMVPMatrix");

        int positionHandle = glGetAttribLocation(program, "vPosition");
        glEnableVertexAttribArray(positionHandle);
        glVertexAttribPointer(positionHandle, VERTEX_POS_SIZE,
                GL_FLOAT, false,
                VERTEX_ATTRIB_SIZE * 4, vertexDataBuffer);

        int colorHandle = glGetAttribLocation(program, "vColor");
        glEnableVertexAttribArray(colorHandle);
        glVertexAttribPointer(colorHandle, COLOR_SIZE,
                GL_FLOAT, false,
                COLOR_ATTRIB_SIZE * 4, colorDataBuffer);

        glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);

        glDisableVertexAttribArray(positionHandle);
        glDisableVertexAttribArray(colorHandle);
        glUseProgram(0);
    }

    private static FloatBuffer loadFloatBuffer(float[] data) {
        ByteBuffer bf = ByteBuffer.allocateDirect(data.length * 4);
        bf.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = bf.asFloatBuffer();
        floatBuffer.put(data);
        floatBuffer.position(0);
        return floatBuffer;
    }

    private static int loadProgram(int vertexShader, int fragmentShader) {
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        return program;
    }
}