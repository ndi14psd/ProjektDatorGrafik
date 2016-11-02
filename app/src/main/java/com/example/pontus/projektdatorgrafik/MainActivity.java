package com.example.pontus.projektdatorgrafik;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class MainActivity extends AppCompatActivity {

    private int ySize;
    private int xSize;

    private GLSurfaceView glView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        ySize = point.y;
        xSize = point.x;

        glView = new MyGLSurfaceView(this);
        setContentView(glView);
    }

    public int getYSize() {
        return ySize;
    }

    public int getXSize() {
        return xSize;
    }
}

class MyGLSurfaceView extends GLSurfaceView {

    private final GLRenderer renderer;

    public MyGLSurfaceView(Context context) {
        super(context);
        InputStream inputStream;

        //String fileName = "ArcGridTestFile.txt";
        String fileName = "DEM.txt";
        AssetManager manager = context.getAssets();

        try {
            inputStream = manager.open(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        renderer = new GLRenderer(inputStream);

        setEGLContextClientVersion(2);

        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }
}

class GLRenderer implements GLSurfaceView.Renderer {

    private DEM dem;
    private final ArcGridTextFileParser parser;

    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];

    public GLRenderer(InputStream arcGridFile) {
        super();
        parser = new ArcGridTextFileParser(arcGridFile);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glClearColor(1, 1, 1, 1);
        dem = new DEM(parser);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 0.5f, 10);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.translateM(viewMatrix, 0, 0, 0, -1.0f);

        dem.draw(viewMatrix, projectionMatrix);
    }
}