package com.example.pontus.projektdatorgrafik;

import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;

/**
 * Created by Pontus on 2016-10-19.
 */
public class DEM {

    static final int VERTEX_POS_SIZE = 3;
    static final int COLOR_SIZE = 4;

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;

    private final int program;

    static final float[] POSITION_DATA = new float[] {
            0, 0, 0,
            25, 25, 13,
            25, 75, 88,
            25, 125, 32,
            25, 175, 3, 1,
            25, 225, -9999,
            25, 275, -9999,
            75, 25, 5,
            75, 75, 75,
            75, 125, 42,
            75, 175, 8,
            75, 225, 20,
            75, 275, -9999,
            125, 25, 1,
            125, 75, 27,
            125, 125, 50,
            125, 175, 35,
            125, 225, 100,
            125, 275, 5,
            175, 25, -9999,
            175, 75, 9,
            175, 125, 6,
            175, 175, 10,
            175, 225, 36,
            175, 275, 2,

    };

    private final static float LLX_CORNER = 0.0f;
    private final static float LLY_CORNER = 0.0f;
    private final static int NCOLS = 4;
    private final static int NROWS = 6;
    private final static float CELL_SIZE = 50;

    private final static float MAX_X = LLX_CORNER + NCOLS * CELL_SIZE, MIN_X = LLX_CORNER * CELL_SIZE;
    private final static float MAX_Y = LLY_CORNER + NROWS * CELL_SIZE, MIN_Y = LLY_CORNER * CELL_SIZE;

    static final float COLOR_DATA[] = {   // in counterclockwise order:
            1.0f, 0.0f, 0.0f, 1.0f, // Red
            0.0f, 1.0f, 0.0f, 1.0f, // Green
            0.0f, 0.0f, 1.0f, 1.0f// Blue
    };

    public DEM() {

        vertexBuffer = ByteBuffer.allocateDirect(POSITION_DATA.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(POSITION_DATA);

        vertexBuffer.position(0);

        colorBuffer = ByteBuffer.allocateDirect(COLOR_DATA.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(COLOR_DATA);

        colorBuffer.position(0);

        int vertexShader = Shaders.loadShader(GL_VERTEX_SHADER, Shaders.VERTEX_SHADER_CODE);
        int fragmentShader = Shaders.loadShader(GL_FRAGMENT_SHADER, Shaders.FRAGMENT_SHADER_CODE);

        program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
    }

    public void draw(float[] projectionMatrix, float[] viewMatrix) {
        int vertexCount = POSITION_DATA.length / VERTEX_POS_SIZE;
        final float X_AXIS_SIZE = MAX_X - MIN_X;
        final float Y_AXIS_SIZE = MAX_Y - MIN_Y;

        glUseProgram(program);

        float[] mvpMatrix = new float[16];

        final float X_TRANSLATION_DISTANCE = (float) (X_AXIS_SIZE / 2.0);
        final float Y_TRANSLATION_DISTANCE = (float) (Y_AXIS_SIZE / 2.0);

        final float X_SCALE_FACTOR = (float) (1.0 / X_AXIS_SIZE);
        final float Y_SCALE_FACTOR = (float) (1.0 / Y_AXIS_SIZE);


        // View * Translate
        Matrix.translateM(mvpMatrix, 0, viewMatrix, 0, -1, -1, 0);

        // View * Translate * Scale
        Matrix.scaleM(mvpMatrix, 0, mvpMatrix, 0, X_SCALE_FACTOR, Y_SCALE_FACTOR, 1);
        // View * Scale * Translation
       // Matrix.translateM(mvpMatrix, 0, mvpMatrix, 0, -X_TRANSLATION_DISTANCE, -Y_TRANSLATION_DISTANCE, 0);
        // Projection * View * Scale * Translation
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
        // MVP = Projection * View * Model*/

        int positionHandle = glGetAttribLocation(program, "vPosition");
        glEnableVertexAttribArray(positionHandle);

        /*int thicknessHandle = glGetUniformLocation(program, "uThickness");
        glUniform1f(thicknessHandle, CELL_SIZE);*/

        glVertexAttribPointer(positionHandle, VERTEX_POS_SIZE,
                GL_FLOAT, false,
                VERTEX_POS_SIZE * 4, vertexBuffer);

        int colorHandle = glGetAttribLocation(program, "vColor");

        glVertexAttribPointer(colorHandle, COLOR_SIZE,
                GL_FLOAT, false,
                COLOR_SIZE * 4, colorBuffer);

        int mvpHandle = glGetUniformLocation(program, "uMVPMatrix");
        glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0);

        glDrawArrays(GL_POINTS, 0, vertexCount);

        glDisableVertexAttribArray(positionHandle);
        glDisableVertexAttribArray(colorHandle);
    }
}

