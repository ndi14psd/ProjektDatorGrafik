package com.example.pontus.projektdatorgrafik;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Pontus on 2016-10-18.
 */

public class ArcGridTextFileParser {

    enum MetaDataType {
        NCOLS, NROWS, XLLCORNER, YLLCORNER, CELLSIZE, NODATA_VALUE;
    }

    private final Map<MetaDataType, Float> metaData;
    private final float[][] data;

    private final static int HEIGHT_DATA_START_INDEX = MetaDataType.values().length;

    public ArcGridTextFileParser(File file) {
        List<String> lines = getLines(file);

        metaData = readMetaData(lines);
        data = readValues(lines);
    }

    private List<String> getLines(File file) {
        List<String> lines = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while((line = reader.readLine()) != null)
                lines.add(line.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    private Map<MetaDataType, Float> readMetaData(List<String> lines) {
        Map<MetaDataType, Float> metaData = new EnumMap<>(MetaDataType.class);

        for (int i = 0; i < MetaDataType.values().length; i++) {
            String[] words = lines.get(i).split("\\s+");
            if(words.length != 2)
                throw new RuntimeException("File is not formatted correctly.");
            metaData.put(MetaDataType.valueOf(words[0]), Float.parseFloat(words[1]));
        }
        return metaData;
    }

    private float[][] readValues(final List<String> lines) {
        List<String> heightLines = lines.subList(HEIGHT_DATA_START_INDEX, lines.size());
        float[][] values = new float[heightLines.size()][];

        for (int row = 0; row < heightLines.size(); row++) {
            String[] line = heightLines.get(row).split("\\s+");
            values[row] = new float[line.length];
            for (int col = 0; col < line.length; col++)
                values[row][col] = Float.parseFloat(line[col]);
        }
        return values;
    }

    public int getNCols() {
        return metaData.get(MetaDataType.NCOLS).intValue();
    }

    public int getNRows() {
        return metaData.get(MetaDataType.NROWS).intValue();
    }

    public float getXLLCorner() {
        return metaData.get(MetaDataType.XLLCORNER);
    }

    public float getYLLCorner() {
        return metaData.get(MetaDataType.YLLCORNER);
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
}
