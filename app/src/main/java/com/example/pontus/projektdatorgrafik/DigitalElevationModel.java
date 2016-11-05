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

        final short[] indices = drawingOrderIndices(nCols);

        indexBuffer = ByteBuffer.allocateDirect(VERTEX_POS_SIZE * nCols * 2) // Two rows, each element 4 bytes
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

    public void draw(float[] viewMatrix, float[] projectionMatrix, float[] rotationMatrix) {
        float[] mvpMatrix = calcMVPMatrix(viewMatrix, projectionMatrix, rotationMatrix);

        for (int i = 0; i < nRows - 1; i++)
            drawTriangleStripRow(mvpMatrix, i);
    }

    public void drawTriangleStripRow(float[] mvpMatrix, int row) {
        glUseProgram(program);

        FloatBuffer vertexBuffer = createFloatBuffer(rowsToVertices(heightData, row, 2));
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
                float[] vertex = transformToVertex(heightValues[row][col], col, row);
                vertices[verticesCounter] = vertex[0];
                vertices[verticesCounter + 1] = vertex[1];
                vertices[verticesCounter + 2] = vertex[2];
                verticesCounter += VERTEX_POS_SIZE;
            }
        return vertices;
    }

    private float[] transformToVertex(float z, int colIndex, int rowIndex) {
        return new float[]{
                (cellSize / 2) + colIndex * cellSize,
                (yMax - (cellSize / 2)) - rowIndex * cellSize,
                z
        };
    }

    private short[] drawingOrderIndices(int rowSize) {
        short[] indices = new short[rowSize * 2];
        for (short i = 0; i < rowSize; i++) {
            indices[i * 2] = i;
            indices[i * 2 + 1] = (short) (i + rowSize);
        }
        return indices;
    }

    private FloatBuffer createFloatBuffer(float[] data) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(data.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(data);
        buffer.position(0);
        return buffer;
    }

    private float[] calcMVPMatrix(float[] viewMatrix, float[] projectionMatrix, float[] additionalTransformationsMatrix) { // AdditionalTransformations include rotation, scaling etc
        float[] mvpMatrix = new float[16];

        final float xScaleFactor = 2 / xMax;
        final float yScaleFactor = 2 / yMax;
        final float zScaleFactor = 1 / maxHeight;

        float[] startingRotationMatrix = new float[16];
        Matrix.setRotateM(startingRotationMatrix, 0, -70, 1, 0, 0);
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, startingRotationMatrix, 0);

        // View * AdditionalTransformation
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, additionalTransformationsMatrix, 0);
        // View * AdditionalTransformation * Translation
        Matrix.translateM(mvpMatrix, 0, mvpMatrix, 0, -1, -1, -1);
        // View * AdditionalTransformation * Translation * Scale
        Matrix.scaleM(mvpMatrix, 0, mvpMatrix, 0, xScaleFactor, yScaleFactor, zScaleFactor);
        // Projection * View * AdditionalTransformation * Translation * Scale
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
        // MVP = Projection * View * Model


        return mvpMatrix;
    }
}
