package com.example.pontus.projektdatorgrafik;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.opengl.GLES20.*;

/**
 * Created by Pontus on 2016-10-24.
 */
public class DEM2 {

    private final int program;

    private final static float LLX_CORNER = 0.0f;
    private final static float LLY_CORNER = 0.0f;
    private final static float CELL_SIZE = 50.0f;
    private final static int NCOLS = 4;
    private final static int NROWS = 6;

    private final static float MAX_X = LLX_CORNER + NCOLS * CELL_SIZE, MIN_X = LLX_CORNER * CELL_SIZE;
    private final static float MAX_Y = LLY_CORNER + NROWS * CELL_SIZE, MIN_Y = LLY_CORNER * CELL_SIZE;
    private final List<Square> SQUARE_LIST = new ArrayList<>();

    public DEM2() {
        int vertexShader = Shaders.loadShader(GL_VERTEX_SHADER, Shaders.VERTEX_SHADER_CODE);
        int fragmentShader = Shaders.loadShader(GL_FRAGMENT_SHADER, Shaders.FRAGMENT_SHADER_CODE);
        program = loadProgram(vertexShader, fragmentShader);

        /*SQUARE_LIST.add(new Square(new Vertex(25, -125, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(25, 25, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(75, 75, 88), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(125, 125, 32), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(175, 175, 3), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(215, 425, 3), CELL_SIZE, program));*/

        SQUARE_LIST.add(new Square(new Vertex(25, 25, 13), CELL_SIZE - 5, program));
        SQUARE_LIST.add(new Square(new Vertex(25, 75, 13), CELL_SIZE -5, program));
        SQUARE_LIST.add(new Square(new Vertex(25, 125, 13), CELL_SIZE-5, program));
        SQUARE_LIST.add(new Square(new Vertex(25, 175, 13), CELL_SIZE-5, program));
        SQUARE_LIST.add(new Square(new Vertex(25, 225, 13), CELL_SIZE-5, program));
        SQUARE_LIST.add(new Square(new Vertex(25, 275, 13), CELL_SIZE-5, program));
        SQUARE_LIST.add(new Square(new Vertex(75, 25, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(75, 75, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(75, 125, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(75, 175, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(75, 225, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(75, 275, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(125, 25, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(125, 75, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(125, 125, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(125, 175, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(125, 225, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(125, 275, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(175, 25, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(175, 75, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(175, 125, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(175, 175, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(175, 225, 13), CELL_SIZE, program));
        SQUARE_LIST.add(new Square(new Vertex(175, 275, 13), CELL_SIZE, program));

    }

    public void draw(float[] projectionMatrix, float[] viewMatrix) {
        float[] mvpMatrix = new float[16];

        final float xSize = (MAX_X - MIN_X);
        final float ySize = (MAX_Y - MIN_Y);

        final float X_TRANSLATION_DISTANCE = (float) (xSize /2);
        final float Y_TRANSLATION_DISTANCE = (float) (ySize /2);

        final float X_SCALE_FACTOR = (float) (1.0 / X_TRANSLATION_DISTANCE);
        final float Y_SCALE_FACTOR = (float) (1.0 / Y_TRANSLATION_DISTANCE);

        // View * Scale
        Matrix.scaleM(mvpMatrix, 0, viewMatrix, 0, X_SCALE_FACTOR, Y_SCALE_FACTOR, 1);
        // View * Scale * Translation
        Matrix.translateM(mvpMatrix, 0, mvpMatrix, 0, -X_TRANSLATION_DISTANCE, -Y_TRANSLATION_DISTANCE, 0);
        // Projection * View * Scale * Translation
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
        // MVP = Projection * View * Scale * Translation

        for(Square sq : SQUARE_LIST)
            sq.draw(mvpMatrix);
    }

    private static int loadProgram(int vertexShader, int fragmentShader) {
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        return program;
    }

    private static FloatBuffer loadFloatBuffer(float[] data, int sizeOfByte) {
        ByteBuffer bf = ByteBuffer.allocateDirect(data.length * sizeOfByte);
        bf.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = bf.asFloatBuffer();
        floatBuffer.put(data);
        floatBuffer.position(0);
        return floatBuffer;
    }
}

