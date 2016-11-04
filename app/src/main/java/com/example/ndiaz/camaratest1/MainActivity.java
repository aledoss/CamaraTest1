package com.example.ndiaz.camaratest1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        View.OnClickListener {
    @BindView(R.id.btnSacarFoto)
    ImageView btnSacarFoto;
    @BindView(R.id.btnConfirmar)
    Button btnConfirmar;
    @BindView(R.id.btnCancelar)
    Button btnCancelar;
    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;

    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Camera.ShutterCallback shutterCallback;
    private byte[] mImagen;
    private static final int FOCUS_AREA_SIZE = 300;
    BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).build();
    final CameraSource cameraSource = new CameraSource.Builder(this, barcodeDetector)
            .build();
    //no me sale el barcodedetector.setprocesor
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupSurfaceHolder();
        setupUI();
        setupBarCode();
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    focusOnTouch(motionEvent);
                return true;
            }
        });
    }

    private void setupBarCode() {

    }


    private void setupUI() {
        btnConfirmar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        btnSacarFoto.setOnClickListener(this);
    }

    private void setupSurfaceHolder() {
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            camera = Camera.open();
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraSource.start(surfaceView.getHolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //camera.stopPreview();
        try{
            cameraSource.stop();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSacarFoto:
                takePicture();
                btnSacarFoto.setEnabled(false);
                btnConfirmar.setEnabled(true);
                btnCancelar.setEnabled(true);
                break;
            case R.id.btnConfirmar:
                btnSacarFoto.setEnabled(true);
                btnConfirmar.setEnabled(false);
                btnCancelar.setEnabled(false);
                Toast.makeText(this, "Imagen Guardada", Toast.LENGTH_SHORT).show();
                camera.startPreview();
                break;
            case R.id.btnCancelar:
                btnSacarFoto.setEnabled(true);
                btnConfirmar.setEnabled(false);
                btnCancelar.setEnabled(false);
                camera.startPreview();
                break;
        }
    }

    private void focusOnTouch(MotionEvent event) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0) {
                Log.i("NICOTEST", "fancy !");
                Rect rect = calculateFocusArea(event.getX(), event.getY());

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);

                camera.setParameters(parameters);
                camera.autoFocus(mAutoFocusTakePictureCallback);
            } else {
                camera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / surfaceView.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / surfaceView.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }

    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                Log.i("NICOTEST", "success!");
            } else {
                Log.i("NICOTEST", "fail!");
            }
        }
    };

    private void takePicture() {
        camera.takePicture(shutterCallback, null, pictureCallback);
    }

    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            setmImagen(bytes);
            Toast.makeText(MainActivity.this, "Foto tomada", Toast.LENGTH_SHORT).show();
        }
    };

    public byte[] getmImagen() {
        return mImagen;
    }

    public void setmImagen(byte[] mImagen) {
        this.mImagen = mImagen;
    }

    @Override
    protected void onPause() {
        releaseCamera();
        super.onPause();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }
}
