package com.example.myapplication;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.ImageView;
import android.view.WindowManager;
import android.widget.Toast;
import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
import static com.example.myapplication.Utils.SYSTEM_TYPE_IMAGE;
import static com.example.myapplication.Utils.SYSTEM_TYPE_VIDEO;
import static com.example.myapplication.Utils.getOutputMediaFile;

public class CustomCameraActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private File image_file;
    private File video_file;
    private static final int PICK_VIDEO = 2;
    public Uri mSelectedImage;
    private Uri mSelectedVideo;

    private int CAMERA_TYPE = CAMERA_FACING_BACK;

    private boolean isRecording = false;

    private int rotationDegree = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_custom_camera);


        mCamera = getCamera(CAMERA_FACING_BACK);
        mSurfaceView = findViewById(R.id.img);
        //todo 给SurfaceHolder添加Callback
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    mCamera.setPreviewDisplay(surfaceHolder);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        });

        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            //todo 拍一张照片
            mCamera.takePicture(null, null, mPicture);
        });


        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            //todo 拍一张照片
            mCamera.takePicture(null, null, mPicture);
        });

        findViewById(R.id.button10).setOnClickListener(v -> {
            if (isRecording) {
            } else {
                //todo 录制
                isRecording = true;
                Log.e("XJP", "onCreate: STEP1" );
                prepareVideoRecorder();
                mMediaRecorder.setMaxDuration(10000);
                Log.e("XJP", "onCreate: STEP2" );
                startPreview(mSurfaceView.getHolder());
                try {
                    mMediaRecorder.prepare();
                    mMediaRecorder.start();
                } catch (Exception e) {
                    Log.e("XJP", "onCreate: STEP3" );
                    e.printStackTrace();
                    releaseMediaRecorder();
                }
            }
        });

        findViewById(R.id.btn_record).setOnClickListener(v -> {
            //todo 录制，第一次点击是start，第二次点击是stop
            if (isRecording) {
                //todo 停止录制
                isRecording = false;
                releaseMediaRecorder();
                mCamera.lock();
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(video_file)));
                chooseVideo();

            } else {
                //todo 录制
                isRecording = true;
                Log.e("XJP", "onCreate: STEP1" );
                prepareVideoRecorder();
                Log.e("XJP", "onCreate: STEP2" );
                startPreview(mSurfaceView.getHolder());
                try {
                    mMediaRecorder.prepare();
                    mMediaRecorder.start();
                } catch (Exception e) {
                    Log.e("XJP", "onCreate: STEP3" );
                    e.printStackTrace();
                    releaseMediaRecorder();
                }
            }
        });

        findViewById(R.id.btn_facing).setOnClickListener(v -> {
            //todo 切换前后摄像头
            if (CAMERA_TYPE == CAMERA_FACING_BACK) {
                CAMERA_TYPE = CAMERA_FACING_FRONT;
                mCamera = getCamera(CAMERA_FACING_FRONT);
            } else {
                CAMERA_TYPE = CAMERA_FACING_BACK;
                mCamera = getCamera(CAMERA_FACING_BACK);
            }
            try {
                mCamera.setPreviewDisplay(surfaceHolder);

            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
        });

        findViewById(R.id.btn_zoom).setOnClickListener(v -> {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.isZoomSupported()) {
                int zoom = parameters.getZoom();
                if ((zoom = (zoom + 1) * 2) >= parameters.getMaxZoom()) {
                    zoom = 0;
                }
                parameters.setZoom(zoom);
                mCamera.setParameters(parameters);
            } else {
                Toast.makeText(CustomCameraActivity.this,
                        "Zoom is not supported in your device", Toast.LENGTH_LONG);
            }
        });

    }

    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);

        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等
        rotationDegree = getCameraDisplayOrientation(position);
        cam.setDisplayOrientation(rotationDegree);
        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        //todo 释放camera资源
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    Camera.Size size;

    private void startPreview(SurfaceHolder holder) {
        //todo 开始预览
        mMediaRecorder.setPreviewDisplay(holder.getSurface());
        mMediaRecorder.setOrientationHint(rotationDegree);
    }


    private boolean prepareVideoRecorder() {
        //todo 准备MediaRecorder
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        video_file = getOutputMediaFile(SYSTEM_TYPE_VIDEO);
        mMediaRecorder.setOutputFile(video_file.toString());
        return true;
    }


    private void releaseMediaRecorder() {
        //todo 释放MediaRecorder
        Log.e("XJP", "onCreate: STEP4" );
        mMediaRecorder.stop();
        Log.e("XJP", "onCreate: STEP5" );
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }


    private Camera.PictureCallback mPicture = (data, camera) -> {
        image_file = getOutputMediaFile(SYSTEM_TYPE_IMAGE);
        if (image_file == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(image_file);
            Log.e("XJP", ": IMAGE_URL:" + image_file.getAbsolutePath());
            Log.e("XJP", ": IMAGE_URL:" + Environment.getExternalStorageDirectory()
                    + File.separator + Environment.DIRECTORY_DCIM
                    + File.separator + "Camera" + File.separator);
            fos.write(data);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(image_file)));
            fos.close();
        } catch (IOException e) {
            Log.d("mPicture", "Error accessing file: " + e.getMessage());
        }

        mCamera.startPreview();
    };


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void chooseVideo() {
        // TODO-C2 (5) Start Activity to select a video
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"),
                PICK_VIDEO);
        //Log.e(TAG, "chooseVideo");

    }

    public static void loadCover(ImageView imageView, String url, Context context) {

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(context)
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .frame(1000000)
                                .centerCrop()
                                .error(R.mipmap.ic_launcher)//可以忽略
                                .placeholder(R.mipmap.ic_launcher)//可以忽略
                )
                .load(url)
                .into(imageView);
    }


}
