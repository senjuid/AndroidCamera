package com.senjuid.camera;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public abstract class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {

    private RunTimePermission runTimePermission;
    private File folder = null;

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (myOrientationEventListener != null)
                myOrientationEventListener.enable();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getSupportActionBar().hide();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        runTimePermission = new RunTimePermission(this);
        runTimePermission.requestPermission(new String[]{Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, new RunTimePermission.RunTimePermissionListener() {

            @Override
            public void permissionGranted() {
                // First we need to check availability of play services
                initControls();
                identifyOrientationEvents();

                // Create a folder to get image and file .nomedia
                folder = new File(Environment.getExternalStorageDirectory() + "/GreatDayHR");
                if (!folder.exists()) {
                    folder.mkdirs();
                    File file = new File(folder + ".nomedia");
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Capture image on callback
                captureImageCallback();

                if (camera != null) {
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        imgFlashOnOff.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void permissionDenied() {
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (runTimePermission != null) {
            runTimePermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Handler customHandler = new Handler();
    int flag = 1;
    private File tempFile = null;
    private Camera.PictureCallback jpegCallback;
    int MAX_VIDEO_SIZE_UPLOAD = 25; //MB

    private SurfaceView imgSurface;
    private ImageButton imgCapture;
    private ImageView imgFlashOnOff;
    private TextView hintTextView;
    private ImageButton button_save_picture;
    private ImageView backButton;
    private ImageView imgShow;
    private FrameLayout imgShowJepret;

    private void initControls() {
        imgSurface = (SurfaceView) findViewById(R.id.imgSurface);
        imgCapture = (ImageButton) findViewById(R.id.imgCapture);
        imgFlashOnOff = (ImageView) findViewById(R.id.imgFlashOnOff);
        hintTextView = (TextView) findViewById(R.id.hintTextView);
        button_save_picture = findViewById(R.id.button_save_picture);
        backButton = findViewById(R.id.backButton);
        imgShow = findViewById(R.id.imgShow);
        imgShowJepret = findViewById(R.id.imgShowJepret);
        activeCameraCapture();
        startComponent();
    }

    private void startComponent() {
        imgSurface.setVisibility(View.VISIBLE);
        imgCapture.setVisibility(View.VISIBLE);
        hintTextView.setVisibility(View.VISIBLE);
        button_save_picture.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
        imgShow.setVisibility(View.GONE);
    }

    private void showImageComponent() {
        imgSurface.setVisibility(View.GONE);
        imgCapture.setVisibility(View.GONE);
        hintTextView.setVisibility(View.GONE);
        button_save_picture.setVisibility(View.VISIBLE);
        button_save_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempFile != null) {
                    onYesButtonPressed(tempFile.toString());
                }
                finish();
            }
        });
        imgShow.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempFile != null) {
                    tempFile.delete();
                }
                activeCameraCapture();
                startComponent();
            }
        });
    }

    int flashType = 1;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (flag == 0) {
                camera = Camera.open(0);
            } else {
                camera = Camera.open(1);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }

        try {
            Camera.Parameters param;
            param = camera.getParameters();
            List<Camera.Size> sizes = param.getSupportedPreviewSizes();
            //get diff to get perfact preview sizes
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int height = displaymetrics.heightPixels;
            int width = displaymetrics.widthPixels;
            long diff = (height * 1000 / width);
            long cdistance = Integer.MAX_VALUE;
            int idx = 0;
            for (int i = 0; i < sizes.size(); i++) {
                long value = (long) (sizes.get(i).width * 1000) / sizes.get(i).height;
                if (value > diff && value < cdistance) {
                    idx = i;
                    cdistance = value;
                }
            }
            Camera.Size cs = sizes.get(idx);
            param.setPreviewSize(cs.width, cs.height);
            param.setPictureSize(cs.width, cs.height);
            camera.setParameters(param);
            setCameraDisplayOrientation(0);

            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

            if (flashType == 1) {
                param.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                imgFlashOnOff.setImageResource(R.drawable.ic_flash_auto);

            } else if (flashType == 2) {
                param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                Camera.Parameters params = null;
                if (camera != null) {
                    params = camera.getParameters();

                    if (params != null) {
                        List<String> supportedFlashModes = params.getSupportedFlashModes();

                        if (supportedFlashModes != null) {
                            if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                                param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            } else if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                                param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                            }
                        }
                    }
                }
                imgFlashOnOff.setImageResource(R.drawable.ic_flash_on);

            } else if (flashType == 3) {
                param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                imgFlashOnOff.setImageResource(R.drawable.ic_flash_off);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            camera.stopPreview();
            camera.release();
            camera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

    }

    private void activeCameraCapture() {
        if (imgCapture != null) {
            imgCapture.setAlpha(1.0f);
            imgCapture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imgShowJepret.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    imgShowJepret.setVisibility(View.GONE);
                                    if (isSpaceAvailable()) {
                                        captureImage();
                                    } else {
                                        Toast.makeText(CameraActivity.this, "Memory is not available", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, 100);
                        }
                    }, 150);
                }
            });
        }
    }

    private void captureImage() {
        camera.takePicture(null, null, jpegCallback);
        inActiveCameraCapture();
    }

    private void captureImageCallback() {
        surfaceHolder = imgSurface.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                refreshCamera();
                cancelSavePicTaskIfNeed();
                savePicTask = new SavePicTask(data, getPhotoRotation());
                savePicTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
        };
    }

    private void inActiveCameraCapture() {
        if (imgCapture != null) {
            imgCapture.setAlpha(0.5f);
            imgCapture.setOnClickListener(null);
        }
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        try {
            camera.stopPreview();
            Camera.Parameters param = camera.getParameters();

            if (flag == 0) {
                if (flashType == 1) {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    imgFlashOnOff.setImageResource(R.drawable.ic_flash_auto);
                } else if (flashType == 2) {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    Camera.Parameters params = null;
                    if (camera != null) {
                        params = camera.getParameters();

                        if (params != null) {
                            List<String> supportedFlashModes = params.getSupportedFlashModes();

                            if (supportedFlashModes != null) {
                                if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                                    param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                } else if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                                    param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                                }
                            }
                        }
                    }
                    imgFlashOnOff.setImageResource(R.drawable.ic_flash_on);
                } else if (flashType == 3) {
                    param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    imgFlashOnOff.setImageResource(R.drawable.ic_flash_off);
                }
            }

            refreshCameraPreview(param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshCameraPreview(Camera.Parameters param) {
        try {
            camera.setParameters(param);
            setCameraDisplayOrientation(0);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelSavePicTaskIfNeed() {
        if (savePicTask != null && savePicTask.getStatus() == AsyncTask.Status.RUNNING) {
            savePicTask.cancel(true);
        }
    }

    private SavePicTask savePicTask;

    private class SavePicTask extends AsyncTask<Void, Void, String> {
        private byte[] data;
        private int rotation = 0;

        public SavePicTask(byte[] data, int rotation) {
            this.data = data;
            this.rotation = rotation;
        }

        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return saveToSDCard(data, rotation);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            activeCameraCapture();
            tempFile = new File(result);
            showImageComponent();

            Glide.with(CameraActivity.this)
                    .load(tempFile.toString())
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(imgShow);

        }
    }

    public String saveToSDCard(byte[] data, int rotation) throws IOException {
        String imagePath = "";
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int reqHeight = metrics.heightPixels;
            int reqWidth = metrics.widthPixels;

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            if (rotation != 0) {
                Matrix mat = new Matrix();
                mat.postRotate(rotation);
                Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
                if (bitmap != bitmap1) {
                    bitmap.recycle();
                }
                imagePath = getSavePhotoLocal(bitmap1);
                if (bitmap1 != null) {
                    bitmap1.recycle();
                }
            } else {
                imagePath = getSavePhotoLocal(bitmap);
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagePath;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    private String getSavePhotoLocal(Bitmap bitmap) {
        String path = "";
        try {
            OutputStream output;
            File file = new File(folder.getAbsolutePath(), "greatday" + System.currentTimeMillis() + ".jpg");
            try {
                output = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                output.flush();
                output.close();
                path = file.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    // CHECK FOR MEMORY
    public int getFreeSpacePercantage() {
        int percantage = (int) (freeMemory() * 100 / totalMemory());
        int modValue = percantage % 5;
        return percantage - modValue;
    }

    public double totalMemory() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getBlockCount() * (double) stat.getBlockSize();
        return sdAvailSize / 1073741824;
    }

    public double freeMemory() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks() * (double) stat.getBlockSize();
        return sdAvailSize / 1073741824;
    }

    public boolean isSpaceAvailable() {
        if (getFreeSpacePercantage() >= 1) {
            return true;
        } else {
            return false;
        }
    }
    // END METHODS OF CHECK MEMORY

    OrientationEventListener myOrientationEventListener;
    int iOrientation = 0;
    int mOrientation = 90;
    private int mPhotoAngle = 90;

    private void identifyOrientationEvents() {
        myOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int iAngle) {

                final int iLookup[] = {0, 0, 0, 90, 90, 90, 90, 90, 90, 180, 180, 180, 180, 180, 180, 270, 270, 270, 270, 270, 270, 0, 0, 0}; // 15-degree increments
                if (iAngle != ORIENTATION_UNKNOWN) {

                    int iNewOrientation = iLookup[iAngle / 15];
                    if (iOrientation != iNewOrientation) {
                        iOrientation = iNewOrientation;
                        if (iOrientation == 0) {
                            mOrientation = 90;
                        } else if (iOrientation == 270) {
                            mOrientation = 0;
                        } else if (iOrientation == 90) {
                            mOrientation = 180;
                        }

                    }
                    mPhotoAngle = normalize(iAngle);
                }
            }
        };

        if (myOrientationEventListener.canDetectOrientation()) {
            myOrientationEventListener.enable();
        }
    }

    private int normalize(int degrees) {
        if (degrees > 315 || degrees <= 45) {
            return 0;
        }
        if (degrees > 45 && degrees <= 135) {
            return 90;
        }
        if (degrees > 135 && degrees <= 225) {
            return 180;
        }
        if (degrees > 225 && degrees <= 315) {
            return 270;
        }
        throw new RuntimeException("Error....");
    }

    private int getPhotoRotation() {
        int rotation;
        int orientation = mPhotoAngle;

        Camera.CameraInfo info = new Camera.CameraInfo();
        if (flag == 0) {
            Camera.getCameraInfo(0, info);
        } else {
            Camera.getCameraInfo(1, info);
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {
            rotation = (info.orientation + orientation) % 360;
        }
        return rotation;
    }

    public void setCameraDisplayOrientation(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        if (Build.MODEL.equalsIgnoreCase("Nexus 6") && flag == 1) {
            rotation = Surface.ROTATION_180;
        }
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:

                degrees = 0;
                break;
            case Surface.ROTATION_90:

                degrees = 90;
                break;
            case Surface.ROTATION_180:

                degrees = 180;
                break;
            case Surface.ROTATION_270:

                degrees = 270;
                break;
        }

        int result;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {

            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror

        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public abstract void onYesButtonPressed(String photo);

}
