package com.example.pontus.projektdatorgrafik;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GLSurfaceView glView = new MyGLSurfaceView(this);
        setContentView(glView);
    }
}

class MyGLSurfaceView extends GLSurfaceView {

    private final GLRenderer renderer;

    private final static float TOUCH_SCALE_FACTOR = 0.13f;
    private float previousX;

    private final ScaleGestureDetector scaleGestureDetector;

    public MyGLSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        renderer = setUpRenderer(context);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private GLRenderer setUpRenderer(Context context) {
        try {
            InputStream arcGridFile = context.getAssets().open("DEM.txt");
            Resources resources = context.getResources();
            Shaders shaders = Shaders.from(resources.openRawResource(R.raw.vertex_shader), resources.openRawResource(R.raw.fragment_shader));

            return new GLRenderer(arcGridFile, shaders);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - previousX;

                renderer.xAngle += dx * TOUCH_SCALE_FACTOR;
                requestRender();
        }

        previousX = x;

        scaleGestureDetector.onTouchEvent(e);

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            renderer.multiplyScaleFactor(detector.getScaleFactor());
            invalidate();
            return true;
        }
    }
}

class GLRenderer implements GLSurfaceView.Renderer {

    private final Shaders shaders;
    private DigitalElevationModel dem;
    private final ArcGridTextFile arcGridTextFile;

    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];

    public volatile float xAngle;
    private float scaleFactor = 1.f;

    public GLRenderer(InputStream arcGridInputStream, Shaders shaders) {
        super();
        this.shaders = shaders;
        arcGridTextFile = new ArcGridTextFile(arcGridInputStream);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glClearColor(1, 1, 1, 1);
        dem = new DigitalElevationModel(arcGridTextFile, shaders);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 0.5f, 10);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);

        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.translateM(viewMatrix, 0, 0, 0.5f, -2.0f);

        float[] scalingRotationMatrix = new float[16];
        Matrix.setRotateM(scalingRotationMatrix, 0, xAngle, 0, 0, 1);
        Matrix.scaleM(scalingRotationMatrix, 0, scaleFactor, scaleFactor, scaleFactor);

        dem.draw(viewMatrix, projectionMatrix, scalingRotationMatrix);
    }

    public void multiplyScaleFactor(float factor) {
        scaleFactor *= factor;
    }

}