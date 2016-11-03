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

public class ArcGridTextFileParser {

    enum MetaDataType {
        NCOLS, NROWS, XLLCENTER, YLLCENTER, CELLSIZE, NODATA_VALUE;
    }

    private final Map<MetaDataType, Float> metaData;
    private final float[][] data;

    private final static int HEIGHT_DATA_START_INDEX = MetaDataType.values().length;
    private final float maxHeight;

    private ArcGridTextFileParser(List<String> lines) {
        metaData = readMetaData(lines);
        data = readValues(lines);
        maxHeight = findMax(data);
    }

    public ArcGridTextFileParser(File file) {
        this(getLines(file));
    }

    public ArcGridTextFileParser(InputStream stream) {
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
                lines.add(line.toUpperCase());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    private Map<MetaDataType, Float> readMetaData(List<String> lines) {
        Map<MetaDataType, Float> metaData = new EnumMap<>(MetaDataType.class);

        for (int i = 0; i < MetaDataType.values().length; i++) {
            String[] words = lines.get(i).trim().split("\\s+");
            if (words.length != 2) {
                throw new RuntimeException("File is not formatted correctly.");
            }
            metaData.put(MetaDataType.valueOf(words[0]), Float.parseFloat(words[1]));
        }
        return metaData;
    }

    private static float[][] readValues(final List<String> lines) {
        List<String> heightLines = lines.subList(HEIGHT_DATA_START_INDEX, lines.size());
        float[][] values = new float[heightLines.size()][];

        for (int row = 0; row < heightLines.size(); row++) {
            String[] words = heightLines.get(row).trim().split("\\s+");
            values[row] = new float[words.length];
            for (int col = 0; col < words.length; col++)
                values[row][col] = Float.parseFloat(words[col]);
        }
        return values;
    }

    private float findMax(float[][] data) {
        float max = 0;
        for (int row = 0; row < data.length; row++)
            for (int col = 0; col < data[0].length; col++)
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

    public float[][] getData() {
        return data;
    }

    public float getMaxHeight() {
        return maxHeight;
    }
}
