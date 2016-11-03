package com.example.pontus.projektdatorgrafik;

import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.*;

public class DEM {

    private static final int VERTEX_POS_SIZE = 3;
    private final int program;

    private final float[][] heightValues;
    private final int nCols;
    private final int nRows;
    private final float cellSize;

    private final float xMax;
    private final float yMax;

    private final float maxHeight;

    private final ShortBuffer indexBuffer;

    public DEM() {
        nCols = 4;
        nRows = 6;
        cellSize = 50;
        xMax = nCols * cellSize;
        yMax = nRows * cellSize;
        maxHeight = 50.0f;

        heightValues = new float[][]{
                {-9999, -9999, 5, 2},
                {-9999, 20, 100, 36},
                {3, 8, 35, 10},
                {32, 42, 50, 6},
                {88, 75, 27, 9},
                {13, 5, 1, -9999}
        };

        final short[] indices = drawingOrderIndices(nCols);

        indexBuffer = ByteBuffer.allocateDirect(VERTEX_POS_SIZE * nCols * 2) // Two rows, each element 4 bytes
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(indices);
        indexBuffer.position(0);

        int vertexShader = Shaders.loadShader(GL_VERTEX_SHADER, Shaders.VERTEX_SHADER_CODE);
        int fragmentShader = Shaders.loadShader(GL_FRAGMENT_SHADER, Shaders.FRAGMENT_SHADER_CODE);
        program = initProgram(vertexShader, fragmentShader);
    }

    public DEM(ArcGridTextFileParser parser) {
        nCols = parser.getNCols();
        nRows = parser.getNRows();
        cellSize = parser.getCellSize();
        xMax = nCols * cellSize;
        yMax = nRows * cellSize;
        maxHeight = parser.getMaxHeight();

        heightValues = parser.getData();

        final short[] indices = drawingOrderIndices(nCols);

        indexBuffer = ByteBuffer.allocateDirect(VERTEX_POS_SIZE * nCols * 2) // Two rows, each element 4 bytes
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(indices);
        indexBuffer.position(0);

        int vertexShader = Shaders.loadShader(GL_VERTEX_SHADER, Shaders.VERTEX_SHADER_CODE);
        int fragmentShader = Shaders.loadShader(GL_FRAGMENT_SHADER, Shaders.FRAGMENT_SHADER_CODE);
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
        float[] mvpMatrix = calcMVPMatrix(viewMatrix, projectionMatrix);

        for (int i = 0; i < nRows - 1; i++)
            drawTriangleStripRow(mvpMatrix, i);
    }

    public void drawTriangleStripRow(float[] mvpMatrix, int row) {
        glUseProgram(program);

        FloatBuffer vertexBuffer = createFloatBuffer(rowsToVertices(heightValues, row, 2));
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

    private float[] rowsToVertices(float[][] heightMap, int rowIndexStart, int nbrOfRows) {
        float[] vertices = new float[nbrOfRows * nCols * VERTEX_POS_SIZE];

        int verticesCounter = 0;
        for (int row = rowIndexStart; row < rowIndexStart + nbrOfRows; row++)
            for (int col = 0; col < nCols; col++) {
                float[] vertex = transformToVertex(heightMap[row][col], col + 1, row + 1);
                vertices[verticesCounter] = vertex[0];
                vertices[verticesCounter + 1] = vertex[1];
                vertices[verticesCounter + 2] = vertex[2];
                verticesCounter += VERTEX_POS_SIZE;
            }
        return vertices;
    }

    private float[] transformToVertex(float z, int colIndex, int rowIndex) {
        return new float[]{
                (cellSize / 2) + (colIndex - 1) * cellSize,
                (yMax - (cellSize / 2)) - (rowIndex - 1) * cellSize,
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

    private float[] calcMVPMatrix(float[] viewMatrix, float[] projectionMatrix) {
        float[] mvpMatrix = new float[16];

        final float xTranslationDistance = xMax / 2;
        final float yTranslationDistance = yMax / 2;

        final float xScaleFactor = 1 / xTranslationDistance;
        final float yScaleFactor = 1 / yTranslationDistance;

        // View * Scale
        Matrix.scaleM(mvpMatrix, 0, viewMatrix, 0, xScaleFactor, yScaleFactor, 1);
        // View * Scale * Translation
        Matrix.translateM(mvpMatrix, 0, mvpMatrix, 0, -xTranslationDistance, -yTranslationDistance, 0);
        // Projection * View * Scale * Translation
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
        // MVP = Projection * View * Model*/

        return mvpMatrix;
    }
}
