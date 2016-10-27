package com.example.pontus.projektdatorgrafik;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Pontus on 2016-10-18.
 */
public class ArcGridTextFileParserTest {

    private final static String PATH = new File("files").getAbsolutePath();

    private File getTestFile() {
        File file = new File(PATH + "\\ArcGridTestFile.txt");
        return file;
    }

    private ArcGridTextFileParser createInstance(File file) {
        ArcGridTextFileParser parser = new ArcGridTextFileParser(file);
        return parser;
    }

    @Test
    public void getMetaDataFromArcGridTextFile() {
        ArcGridTextFileParser arcGrid = createInstance(getTestFile());
        assertEquals(4, arcGrid.getNCols());
        assertEquals(6, arcGrid.getNRows());
        assertEquals(0.0f, arcGrid.getXLLCorner(), 0.05f);
        assertEquals(0.0f, arcGrid.getYLLCorner(), 0.05f);
        assertEquals(50.0f, arcGrid.getCellSize(), 0.05f);
        assertEquals(-9999, arcGrid.getNoDataValue(), 0.05f);
    }

    @Test
    public void getDataFromArcGridTextFile() {
        ArcGridTextFileParser arcGrid = createInstance(getTestFile());
        float[][] expected = new float[][] {
                {-9999, -9999, 5, 2},
                {-9999, 20, 100, 36},
                {3, 8, 35, 10},
                {32, 42, 50, 6},
                {88, 75, 27, 9},
                {13, 5, 1, -9999}
        };
        assertTrue("Reads data correctly", Arrays.deepEquals(arcGrid.getData(), expected));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowWhenInvalidFile() {
        createInstance(new File(PATH + "\\Invalid.txt"));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowWhenNull() {
        createInstance(null);
    }
}
