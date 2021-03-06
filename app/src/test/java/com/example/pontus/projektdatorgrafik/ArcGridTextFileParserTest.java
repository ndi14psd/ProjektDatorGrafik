package com.example.pontus.projektdatorgrafik;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

public class ArcGridTextFileParserTest {

    private final static File PATH = new File("src\\main\\assets");

    private File getTestFile() {
        return new File(PATH, "ArcGridTestFile.txt");
    }

    private ArcGridTextFile createInstance(File file) {
        return new ArcGridTextFile(file);
    }

    @Test
    public void getMetaDataFromArcGridTextFile() {
        ArcGridTextFile arcGrid = createInstance(getTestFile());
        assertEquals(4, arcGrid.getNCols());
        assertEquals(6, arcGrid.getNRows());
        assertEquals(0.0f, arcGrid.getXLLCorner(), 0.05f);
        assertEquals(0.0f, arcGrid.getYLLCorner(), 0.05f);
        assertEquals(50.0f, arcGrid.getCellSize(), 0.05f);
        assertEquals(-9999, arcGrid.getNoDataValue(), 0.05f);
    }

    @Test
    public void findMax() {
        assertEquals(100, createInstance(getTestFile()).getMaxHeight(), 0.005f);
    }

    @Test
    public void getDataFromArcGridTextFile() {
        ArcGridTextFile arcGrid = createInstance(getTestFile());
        float[][] expected =  {
                {-9999, -9999, 5, 2},
                {-9999, 20, 100, 36},
                {3, 8, 35, 10},
                {32, 42, 50, 6},
                {88, 75, 27, 9},
                {13, 5, 1, -9999}
        };
        assertTrue("Read height data", Arrays.deepEquals(arcGrid.getHeightData(), expected));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowWhenInvalidFile() {
        createInstance(new File("files\\Invalid.txt"));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenFileIsNull() {
        File file = null;
        new ArcGridTextFile(file);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenFileInputStreamIsNull() {
        InputStream stream = null;
        new ArcGridTextFile(stream);
    }


}
