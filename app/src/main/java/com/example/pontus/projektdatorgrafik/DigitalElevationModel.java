package com.example.pontus.projektdatorgrafik;

import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.*;

public class DigitalElevationModel {

    private static final int VERTEX_POS_SIZE = 3;
    private final int program;

    private final float[][] heightData;
    private final int nCols;
    private final int nRows;
    private final float cellSize;

    private final float xMax;
    private final float yMax;

    private final float maxHeight;

    private final ShortBuffer indexBuffer;


    public DigitalElevationModel(ArcGridTextFile arcGridTextFile, Shaders shaders) {
        nCols = arcGridTextFile.getNCols();
        nRows = arcGridTextFile.getNRows();
        cellSize = arcGridTextFile.getCellSize();

        xMax = nCols * cellSize;
        yMax = nRows * cellSize;

        maxHeight = arcGridTextFile.getMaxHeight();
        heightData = arcGridTextFile.getHeightData();

        final short[] indices = getDrawingOrderIndices(nCols);

        indexBuffer = ByteBuffer.allocateDirect(VERTEX_POS_SIZE * nCols * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(indices);
        indexBuffer.position(0);

        int vertexShader = shaders.loadVertexShader();
        int fragmentShader = shaders.loadFragmentShader();
        program = initProgram(vertexShader, fragmentShader);
    }


    private int initProgram(int vertexShader, int fragmentShader) {
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        return program;
    }

    public void draw(float[] viewMatrix, float[] projectionMatrix) {
        float[] mvpMatrix = calculateMVPMatrix(viewMatrix, projectionMatrix);

        for (int i = 0; i < nRows - 1; i++)
            drawTriangleStripRow(mvpMatrix, i);
    }

    private void drawTriangleStripRow(float[] mvpMatrix, int row) {
        glUseProgram(program);

        float[] verticesInRows = rowsToVertices(heightData, row, 2);
        FloatBuffer vertexBuffer = createFloatBuffer(verticesInRows);

        int positionHandle = glGetAttribLocation(program, "vPosition");
        glEnableVertexAttribArray(positionHandle);
        glVertexAttribPointer(positionHandle, VERTEX_POS_SIZE,
                GL_FLOAT, false,
                VERTEX_POS_SIZE * 4, vertexBuffer);

        int mvpHandle = glGetUniformLocation(program, "uMVPMatrix");
        glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0);

        int maxHeightHandle = glGetUniformLocation(program, "uMaxHeight");
        glUniform1f(maxHeightHandle, maxHeight);

        glDrawElements(GL_TRIANGLE_STRIP, nCols * 2, GL_UNSIGNED_SHORT, indexBuffer);

        glDisableVertexAttribArray(positionHandle);
        glUseProgram(0);
    }


    private float[] rowsToVertices(float[][] heightValues, int rowIndexStart, int nbrOfRows) {
        float[] vertices = new float[nbrOfRows * nCols * VERTEX_POS_SIZE];

        int verticesCounter = 0;
        for (int row = rowIndexStart; row < rowIndexStart + nbrOfRows; row++)
            for (int col = 0; col < nCols; col++) {
                float[] vertex = toVertex(heightValues[row][col], col, row);
                vertices[verticesCounter] = vertex[0];
                vertices[verticesCounter + 1] = vertex[1];
                vertices[verticesCounter + 2] = vertex[2];
                verticesCounter += VERTEX_POS_SIZE;
            }
        return vertices;
    }

    private float[] toVertex(float z, int colIndex, int rowIndex) {
        return new float[]{
                (cellSize / 2) + colIndex * cellSize,
                (yMax - (cellSize / 2)) - rowIndex * cellSize,
                z
        };
    }

    private short[] getDrawingOrderIndices(int rowSize) {
        short[] indices = new short[rowSize * 2];
        for (short i = 0; i < rowSize; i++) {
            indices[i * 2] = i;
            indices[i * 2 + 1] = (short) (i + rowSize);
        }
        return indices;
    }

    private static FloatBuffer createFloatBuffer(float[] data) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(data.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(data);
        buffer.position(0);
        return buffer;
    }

    private float[] calculateMVPMatrix(float[] viewMatrix, float[] projectionMatrix) {
        float[] mvpMatrix = new float[16];

        final float xScaleFactor = 2 / xMax;
        final float yScaleFactor = 2 / yMax;
        final float zScaleFactor = 0.5f / maxHeight;

        // View * Translation
        Matrix.translateM(mvpMatrix, 0, viewMatrix, 0, -1, -1, -1);
        // View * Translation * Scale
        Matrix.scaleM(mvpMatrix, 0, mvpMatrix, 0, xScaleFactor, yScaleFactor, zScaleFactor);
        // Projection * View * Translation * Scale
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);

        // MVP = Projection * View * Model
        return mvpMatrix;
    }
}
