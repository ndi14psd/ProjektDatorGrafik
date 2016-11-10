package com.example.pontus.projektdatorgrafik;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ArcGridTextFile {

    enum MetaDataType {
        NCOLS, NROWS, XLLCENTER, YLLCENTER, CELLSIZE, NODATA_VALUE
    }

    private final Map<MetaDataType, Float> metaData;
    private final float[][] data;

    private final static int HEIGHT_DATA_START_INDEX = MetaDataType.values().length;
    private final float maxHeight;

    private ArcGridTextFile(List<String> lines) {
        metaData = readMetaData(lines);
        data = readValues(lines, getNCols(), getNRows());
        maxHeight = findMax(data, getNCols(), getNRows());
    }

    public ArcGridTextFile(File file) {
        this(getLines(file));
    }

    public ArcGridTextFile(InputStream stream) {
        this(getLines(stream));
    }

    private static List<String> getLines(File file) {
        try {
            return getLines(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> getLines(InputStream stream) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null)
                if(!line.isEmpty())
                    lines.add(line.toUpperCase());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    private Map<MetaDataType, Float> readMetaData(List<String> lines) {
        Map<MetaDataType, Float> metaData = new EnumMap<>(MetaDataType.class);

        for (int i = 0; i < MetaDataType.values().length; i++) {
            String[] line = lines.get(i).trim().split("\\s+");
            if (line.length != 2) {
                throw new RuntimeException("Meta data error. \nLine " + (i+1) + ", 2 elements expected, actual: " + line.length);
            }
            metaData.put(MetaDataType.valueOf(line[0]), Float.parseFloat(line[1]));
        }
        return metaData;
    }

    private static float[][] readValues(List<String> lines, int nCols, int nRows) {
        List<String> heightLines = lines.subList(HEIGHT_DATA_START_INDEX, lines.size());
        if(heightLines.size() != nRows) {
            throw new RuntimeException("Number height values don't match with nRows value! \nnRows: " + nRows + ", actual: " + heightLines.size());
        }
        float[][] values = new float[nRows][nCols];

        for (int row = 0; row < nRows; row++) {
            String[] line = heightLines.get(row).trim().split("\\s+");
            if(line.length != nCols) {
                throw new RuntimeException("Height value rows are not all formatted properly! \nrow number: " + (row + 1) + " nCols: " + nCols + ", actual: " + line.length);
            }
            for (int col = 0; col < nCols; col++)
                values[row][col] = Float.parseFloat(line[col]);
        }
        return values;
    }

    private float findMax(float[][] data, int nCols, int nRows) {
        float max = 0;
        for (int row = 0; row < nRows; row++)
            for (int col = 0; col < nCols; col++)
                max = Math.max(max, data[row][col]);
        return max;
    }

    public int getNCols() {
        return metaData.get(MetaDataType.NCOLS).intValue();
    }

    public int getNRows() {
        return metaData.get(MetaDataType.NROWS).intValue();
    }

    public float getXLLCorner() {
        return metaData.get(MetaDataType.XLLCENTER);
    }

    public float getYLLCorner() {
        return metaData.get(MetaDataType.YLLCENTER);
    }

    public float getCellSize() {
        return metaData.get(MetaDataType.CELLSIZE);
    }

    public float getNoDataValue() {
        return metaData.get(MetaDataType.NODATA_VALUE);
    }

    public float[][] getHeightData() {
        return data;
    }

    public float getMaxHeight() {
        return maxHeight;
    }
}
