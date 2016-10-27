package com.example.pontus.projektdatorgrafik;

import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import static android.opengl.GLES20.*;

/**
 * Created by perjee on 10/6/16.
 */

public class Triangle {

    static final int VERTEX_POS_SIZE = 4;
    static final int COLOR_SIZE = 4;

    static final int VERTEX_POS_INDEX = 0;
    static final int COLOR_INDEX = 1;

    static final int VERTEX_POS_OFFSET = 0;
    static final int COLOR_OFFSET = 0;

    static final int COLOR_ATTRIB_SIZE = COLOR_SIZE;
    static final int VERTEX_ATTRIB_SIZE = VERTEX_POS_SIZE;

    private final int VERTEX_COUNT = triangleData.length / VERTEX_ATTRIB_SIZE;

    private FloatBuffer vertexDataBuffer;
    private FloatBuffer colorDataBuffer;

    static float triangleData[] = {   // in counterclockwise order:
            //0.0f,  0.622008459f, 0.0f, 1.0f, // top
            //-0.5f, -0.311004243f, 0.0f, 1.0f, // bottom left
            //0.6f, -0.311004243f, 0.0f, 1.0f, // bottom right

            50, 100, 0, 1,
            16f, 10, 0, 1,
            90f, 0, 0, 1
    };

    private final static float XLL_CORNER = 0.0f;
    private final static float YLL_CORNER = 0.0f;
    private final static float CELL_SIZE = 1.0f;
    private final static int MIN_X = 0, MAX_X = 90;
    private final static int MIN_Y = 0, MAX_Y = 100;

    static float colorData[] = {   // in counterclockwise order:
            1.0f, 0.0f, 0.0f, 1.0f, // Red
            0.0f, 1.0f, 0.0f, 1.0f, // Green
            0.0f, 0.0f, 1.0f, 1.0f// Blue
    };

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    private final int mProgram;

    /*
    MVP = Projection * View * Translate from origo * Scale * Translate to origo * Translate by -half
     */

    private final String vertexShaderCode =
            "attribute vec4 vPosition; \n" +
            "attribute vec4 vColor; \n" +
            "uniform mat4 uMVPMatrix;\n" +

            "varying vec4 c; \n" +
            "void main() { \n" +
            "  c = vColor; \n" +
            "  gl_Position = uMVPMatrix * vPosition;\n" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;\n" +
                    "varying vec4 c;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = c;\n" +
                    "}";

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

    private int positionHandle;
    private int colorHandle;

    public Triangle() {

        ByteBuffer bbv = ByteBuffer.allocateDirect(
                triangleData.length * 4);
        bbv.order(ByteOrder.nativeOrder());

        vertexDataBuffer = bbv.asFloatBuffer();
        vertexDataBuffer.put(triangleData);
        vertexDataBuffer.position(0);

        ByteBuffer bbc = ByteBuffer.allocateDirect(
                colorData.length * 4);
        bbc.order(ByteOrder.nativeOrder());

        colorDataBuffer = bbc.asFloatBuffer();
        colorDataBuffer.put(colorData);
        colorDataBuffer.position(0);

        int vertexShader = Shaders.loadShader(GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = Shaders.loadShader(GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = glCreateProgram();
        glAttachShader(mProgram, vertexShader);
        glAttachShader(mProgram, fragmentShader);
        glLinkProgram(mProgram);
    }

    public void draw(float[] projMatrix, float[] viewMatrix) {
        glUseProgram(mProgram);

        float[] mvpMatrix = new float[16];


        final float X_TRANSLATION_DISTANCE = (float) ((MAX_X - MIN_X) / 2.0);
        final float Y_TRANSLATION_DISTANCE = (float) ((MAX_Y - MIN_Y) / 2.0);

        final float X_SCALE_FACTOR = (float) (1.0 / X_TRANSLATION_DISTANCE);
        final float Y_SCALE_FACTOR = (float) (1.0 / Y_TRANSLATION_DISTANCE);

        // View * Scale
        Matrix.scaleM(mvpMatrix, 0, viewMatrix, 0, X_SCALE_FACTOR, Y_SCALE_FACTOR, 1);
        // View * Scale * Translation
        Matrix.translateM(mvpMatrix, 0, mvpMatrix, 0, -X_TRANSLATION_DISTANCE, -Y_TRANSLATION_DISTANCE, 0);
        // Projection * View * Scale * Translation
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0);
        // MVP = Projection * View * Scale * Translation

        positionHandle = glGetAttribLocation(mProgram, "vPosition");
        glEnableVertexAttribArray(positionHandle);

        glVertexAttribPointer(positionHandle, VERTEX_POS_SIZE,
                GL_FLOAT, false,
                VERTEX_ATTRIB_SIZE * 4, vertexDataBuffer);

        colorHandle = glGetAttribLocation(mProgram, "vColor");
        glEnableVertexAttribArray(colorHandle);
        glVertexAttribPointer(colorHandle, COLOR_SIZE,
                GL_FLOAT, false,
                COLOR_ATTRIB_SIZE * 4, colorDataBuffer);

        int mvpMatrixHandle = glGetUniformLocation(mProgram, "uMVPMatrix");
        glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the triangle
        glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT);

        // Disable vertex array
        glDisableVertexAttribArray(positionHandle);
        glDisableVertexAttribArray(colorHandle);

    }
}
