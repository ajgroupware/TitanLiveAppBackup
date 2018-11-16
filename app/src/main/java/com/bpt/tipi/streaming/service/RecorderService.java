package com.bpt.tipi.streaming.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.bpt.tipi.streaming.BitmapUtils;
import com.bpt.tipi.streaming.ConfigHelper;
import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.StateMachineHandler;
import com.bpt.tipi.streaming.UnCaughtException;

import com.bpt.tipi.streaming.Utils;
import com.bpt.tipi.streaming.helper.CameraHelper;
import com.bpt.tipi.streaming.helper.CameraRecorderHelper;
import com.bpt.tipi.streaming.helper.IrHelper;
import com.bpt.tipi.streaming.helper.VideoNameHelper;
import com.bpt.tipi.streaming.model.MessageEvent;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;

import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;
import org.bytedeco.javacv.FrameRecorder;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_NV21;

public class RecorderService extends Service implements Camera.PreviewCallback {

    //TAG para Log
    private static final String TAG = "RecorderService";

    //Nombre para el hilo del servicio
    public static final String THREAD_NAME = "RecorderService";

    public static final int AUDIO_BITRATE = 128000;
    public static final int AUDIO_RATE_IN_HZ = 44100;

    private final IBinder mBinder = new RecorderBinder();
    private Handler mHandler;

    private EventBus bus = EventBus.getDefault();
    public Context context;

    private String deviceId;

    //Camara a utilizar.
    private Camera camera = null;

    //Camara primaria.
    private Camera primaryCamera = null;

    private byte buffer[];
    private byte primaryBuffer[];

    int sequence = 1;

    /* Variables de streaming */
    private FFmpegFrameRecorder streamingRecorder;
    private Frame streamingYuvImage = null;
    private FFmpegFrameFilter streamingFilter;

    private AudioRecord streamingAudioRecord;
    private StreamingAudioRecordRunnable streamingAudioRecordRunnable;
    private Thread streamingAudioThread;
    /**
     *el streamingRunAudioThread antes iniciaba en true pero al tomar foto se quedaba el streaming corriendo forever
     */
    volatile boolean streamingRunAudioThread = false;
    long streamingStartTime = 0;
    public boolean isStreamingRecording = false;

    /* Variables de local */
    private FFmpegFrameRecorder localRecorder;
    private Frame localYuvImage = null;

    private AudioRecord localAudioRecord;
    private RecorderService.LocalAudioRecordRunnable localAudioRecordRunnable;
    private Thread localAudioThread;
    volatile boolean localRunAudioThread = true;
    long localStartTime = 0;
    public boolean isLocalRecording = false;

    public boolean isSos = false;
    int videoDuration = 0;

    boolean flashOn = false;
    boolean proccesingStreming = false;

    CounterLocalVideo counterLocalVideo;

    CounterPostVideo counterPostVideo;

    CounterFlash counterFlash;

    long mStartTX;
    Date streamingStarted;

    boolean takePhoto = false;

    StateMachineHandler machineHandler;

    MediaRecorder mediaRecorder;
    CamcorderProfile profile;
    boolean sosPressed = false;

    public class RecorderBinder extends Binder {
        public RecorderService getService() {
            return RecorderService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(this));
        context = RecorderService.this;
        HandlerThread thread = new HandlerThread(THREAD_NAME);
        thread.start();
        mHandler = new Handler(thread.getLooper());
        Log.i(TAG, "RecorderService onCreate()");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bus.register(this);
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, loaderCallback);
        } else {
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        machineHandler = new StateMachineHandler(RecorderService.this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        bus.unregister(this);
        Log.i(TAG, "RecorderService onDestroy()");
        finishCamera();
        finishPrimaryCamera();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(MessageEvent event) {
        Log.e("Depuracion", "onMessageEvent " + event.key);
        switch (event.key) {
            case MessageEvent.SOS_PRESSED:
                if (!isSos) {
                    if (!isLocalRecording) {
                        setLocalRecorderStateMachine();
                    }
                    sosPressed = true;
                    isSos = true;
                    sendSos();
                } else {
                    isSos = false;
                    setLocalRecorderStateMachine();
                }
                break;
            case MessageEvent.LOCAL_RECORD:
                if (isSos) {
                    isSos = false;
                }
                setLocalRecorderStateMachine();
                break;
            case MessageEvent.START_STREAMING:
                if (!proccesingStreming) {
                    proccesingStreming = true;
                    if (!isStreamingRecording) {
                        machineHandler.sendEmptyMessage(StateMachineHandler.STREAMING);
                        mStartTX = TrafficStats.getTotalTxBytes();
                        Calendar cal = Calendar.getInstance();
                        streamingStarted = cal.getTime();
                    } else {
                        proccesingStreming = false;
                    }
                }
                break;
            case MessageEvent.STOP_STREAMING:
                if (!proccesingStreming) {
                    proccesingStreming = true;
                    if (isStreamingRecording) {
                        machineHandler.sendEmptyMessage(StateMachineHandler.STREAMING);
                        sendLogStreaming();
                    } else {
                        proccesingStreming = false;
                    }
                }
                break;
            case MessageEvent.TAKE_PHOTO:
                machineHandler.sendEmptyMessage(StateMachineHandler.TAKE_PHOTO);
                break;
            case MessageEvent.STATE_FLASH:
                if (!flashOn) {
                    flashLightOn();
                } else {
                    pauseFlashCounter();
                    flashLightOff();
                }
                flashOn = !flashOn;
                break;
        }
    }

    public void setLocalRecorderStateMachine() {
        Message message = new Message();
        message.what = StateMachineHandler.LOCAL_RECORDER_PRESSED;
        message.arg1 = StateMachineHandler.PLAY_SOUND;
        machineHandler.handleMessage(message);
    }

    /**
     * Se debe abrir siempre primero la cámara secundaria 0
     */
    public void prepareStreamingCamera() {
        if (camera == null) {
            //camera = Utils.getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);

            try {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);

                Camera.Parameters parameters = camera.getParameters();

                parameters.setPreviewSize(CameraRecorderHelper.getStreamingImageWidth(context), CameraRecorderHelper.getStreamingImageHeight(context));
                parameters.setPreviewFrameRate(ConfigHelper.getStreamingFramerate(context));

                parameters.setPreviewFormat(ImageFormat.NV21);
                camera.setParameters(parameters);

                int height = parameters.getPreviewSize().height;
                switch (height) {
                    case 480:
                        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                        break;
                    case 720:
                        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
                        break;
                    case 1080:
                        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
                        break;
                    default:
                        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                        break;
                }
                profile.videoFrameRate = parameters.getPreviewFrameRate();
                profile.videoFrameWidth = parameters.getPreviewSize().width;
                profile.videoFrameHeight = parameters.getPreviewSize().height;

                int size = CameraRecorderHelper.getStreamingImageWidth(context) * CameraRecorderHelper.getStreamingImageHeight(context);

                size = size * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
                buffer = new byte[size];
                camera.addCallbackBuffer(buffer);

                try {
                    camera.setPreviewTexture(new SurfaceTexture(10));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                    //finishCamera();
                }
                IrHelper.setIrState(IrHelper.STATE_ON);



            } catch (Exception e) {
                Log.d("TAG", "Open camera failed: " + e);
            }
        }
    }

    public void initCamera() {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
        //finishCamera();
        if (!Utils.isCameraExist(context)) {
            throw new IllegalStateException("There is no device, not possible to start recording");
        }
        deviceId = ConfigHelper.getDeviceName(context);
        if (camera == null) {
            //camera = Utils.getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);

            try {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            } catch (Exception e) {
                Log.d("TAG", "Open camera failed: " + e);
            }
        }
        if (camera != null) {
            //camera.setDisplayOrientation(270);
            Camera.Parameters parameters = camera.getParameters();

            parameters.setPreviewSize(CameraRecorderHelper.getStreamingImageWidth(context), CameraRecorderHelper.getStreamingImageHeight(context));
            parameters.setPreviewFrameRate(ConfigHelper.getStreamingFramerate(context));
            //parameters.setRotation(270);
            parameters.setPreviewFormat(ImageFormat.NV21);
            camera.setParameters(parameters);
            //camera.setDisplayOrientation(270);

            int height = parameters.getPreviewSize().height;
            switch (height) {
                case 480:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                    break;
                case 720:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
                    break;
                case 1080:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
                    break;
                default:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                    break;
            }
            profile.videoFrameRate = parameters.getPreviewFrameRate();
            profile.videoFrameWidth = parameters.getPreviewSize().width;
            profile.videoFrameHeight = parameters.getPreviewSize().height;

            int size = CameraRecorderHelper.getStreamingImageWidth(context) * CameraRecorderHelper.getStreamingImageHeight(context);

            size = size * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
            buffer = new byte[size];
            camera.addCallbackBuffer(buffer);
            camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera mCamera) {
                    Camera.Parameters parameters = camera.getParameters();

                    Mat mat = new Mat(parameters.getPreviewSize().height, parameters.getPreviewSize().width, CvType.CV_8UC2);
                    mat.put(0, 0, bytes);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentDate = sdf.format(new Date());
                    CameraRecorderHelper.putWaterMark(mat, currentDate, "TITAN-" + deviceId);



                    int bufferSize = (int) (mat.total() * mat.elemSize());
                    byte[] b = new byte[bufferSize];

                    mat.get(0, 0, b);

                    if (isStreamingRecording) {
                    streamingRecord(b);
                }

                    if (camera != null) {
                        camera.addCallbackBuffer(buffer);
                    }
                }
            });

            try {
                camera.setPreviewTexture(new SurfaceTexture(10));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
                //finishCamera();
            }
            //IrHelper.setIrState(IrHelper.STATE_ON);
        } else {
            Log.d(TAG, "Get camera from service failed");
        }
//            }
//        });
    }

    public void initPrimaryCamera() {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
        //finishCamera();
        if (!Utils.isCameraExist(context)) {
            throw new IllegalStateException("There is no device, not possible to start recording");
        }
        deviceId = ConfigHelper.getDeviceName(context);
        if (primaryCamera == null) {
            //primaryCamera = Utils.getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);

            try {
                primaryCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } catch (Exception e) {
                Log.d("TAG", "Open camera failed: " + e);
            }
        }
        if (primaryCamera != null) {
            Camera.Parameters parameters = primaryCamera.getParameters();

            parameters.setPreviewSize(CameraRecorderHelper.getLocalImageWidth(context), CameraRecorderHelper.getLocalImageHeight(context));
            parameters.setPreviewFrameRate(ConfigHelper.getLocalFramerate(context));

            parameters.setPreviewFormat(ImageFormat.NV21);
            primaryCamera.setParameters(parameters);

            int height = parameters.getPreviewSize().height;
            switch (height) {
                case 480:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                    break;
                case 720:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
                    break;
                case 1080:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
                    break;
                default:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                    break;
            }
            profile.videoFrameRate = parameters.getPreviewFrameRate();
            profile.videoFrameWidth = parameters.getPreviewSize().width;
            profile.videoFrameHeight = parameters.getPreviewSize().height;

            int size = CameraRecorderHelper.getLocalImageWidth(context) * CameraRecorderHelper.getLocalImageHeight(context);

            size = size * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
            primaryBuffer = new byte[size];
            primaryCamera.addCallbackBuffer(primaryBuffer);
            primaryCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera mCamera) {
                    Camera.Parameters parameters = primaryCamera.getParameters();

                    Mat mat = new Mat(parameters.getPreviewSize().height, parameters.getPreviewSize().width, CvType.CV_8UC2);
                    mat.put(0, 0, bytes);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentDate = sdf.format(new Date());
                    CameraRecorderHelper.putWaterMark(mat, currentDate, "TITAN-" + deviceId);

                    int bufferSize = (int) (mat.total() * mat.elemSize());
                    byte[] b = new byte[bufferSize];

                    mat.get(0, 0, b);

                    if (isLocalRecording && localRecorder != null) {
                        localRecord(b);
                    }

                    if (primaryCamera != null) {
                        primaryCamera.addCallbackBuffer(primaryBuffer);
                    }

                    if (takePhoto) {
                        takePhoto = false;
                        savePhoto(b);
                        machineHandler.sendEmptyMessage(StateMachineHandler.TAKE_PHOTO);
                    }
                }
            });
            try {
                primaryCamera.setPreviewTexture(new SurfaceTexture(10));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                primaryCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
                //finishCamera();
            }
            IrHelper.setIrState(IrHelper.STATE_ON);
        } else {
            Log.d(TAG, "Get camera from service failed");
        }
//            }
//        });
    }

    public void initPhotoCamera(){
        //inicia la camara principal para tomar la foto
        if (!isStreamingRecording) {
            prepareStreamingCamera();//Abrir cámara 0 antes de la 1
        }
        initPrimaryCamera();


    }

    public void initCamera_(final boolean localConfig) {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
        //finishCamera();
        if (!Utils.isCameraExist(context)) {
            throw new IllegalStateException("There is no device, not possible to start recording");
        }
        deviceId = ConfigHelper.getDeviceName(context);
        if (camera == null) {
            //camera = Utils.getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
            camera = Utils.getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (localConfig) {
                parameters.setPreviewSize(CameraRecorderHelper.getLocalImageWidth(context), CameraRecorderHelper.getLocalImageHeight(context));
                parameters.setPreviewFrameRate(ConfigHelper.getLocalFramerate(context));
            } else {
                parameters.setPreviewSize(CameraRecorderHelper.getStreamingImageWidth(context), CameraRecorderHelper.getStreamingImageHeight(context));
                parameters.setPreviewFrameRate(ConfigHelper.getStreamingFramerate(context));
            }
            parameters.setPreviewFormat(ImageFormat.NV21);
            camera.setParameters(parameters);

            int height = parameters.getPreviewSize().height;
            switch (height) {
                case 480:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                    break;
                case 720:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
                    break;
                case 1080:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
                    break;
                default:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                    break;
            }
            profile.videoFrameRate = parameters.getPreviewFrameRate();
            profile.videoFrameWidth = parameters.getPreviewSize().width;
            profile.videoFrameHeight = parameters.getPreviewSize().height;

            int size;
            if (localConfig) {
                size = CameraRecorderHelper.getLocalImageWidth(context) * CameraRecorderHelper.getLocalImageHeight(context);
            } else {
                size = CameraRecorderHelper.getStreamingImageWidth(context) * CameraRecorderHelper.getStreamingImageHeight(context);
            }
            size = size * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
            buffer = new byte[size];
            camera.addCallbackBuffer(buffer);
            camera.setPreviewCallbackWithBuffer(RecorderService.this);
            try {
                camera.setPreviewTexture(new SurfaceTexture(10));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
                //finishCamera();
            }
            IrHelper.setIrState(IrHelper.STATE_ON);
        } else {
            Log.d(TAG, "Get camera from service failed");
        }
//            }
//        });
    }

    public void finishCamera() {
        if (!isStreamingRecording && !streamingRunAudioThread && !isSos) {
            try {
                if (camera != null) {
                    camera.stopPreview();
                    camera.setPreviewCallback(null);
                    camera.lock();
                    camera.release();
                    camera = null;
                }
                //IrHelper.setIrState(IrHelper.STATE_OFF);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void finishPrimaryCamera() {
        try {
            if (primaryCamera != null) {
                primaryCamera.stopPreview();
                primaryCamera.setPreviewCallback(null);
                primaryCamera.lock();
                primaryCamera.release();
                primaryCamera = null;
            }
            IrHelper.setIrState(IrHelper.STATE_OFF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void configLocalRecorder() {
        if (isStreamingRecording) {
            localYuvImage = new Frame(CameraRecorderHelper.getStreamingImageWidth(context),
                    CameraRecorderHelper.getStreamingImageHeight(context), Frame.DEPTH_UBYTE, 2);
            localRecorder = CameraRecorderHelper.initRecorder(context, CameraRecorderHelper.RECORDER_TYPE_LOCAL, VideoNameHelper.getOutputFile(context, sequence).getAbsolutePath(), CameraRecorderHelper.FORMAT_MP4);
        } else {
            localYuvImage = new Frame(CameraRecorderHelper.getLocalImageWidth(context),
                    CameraRecorderHelper.getLocalImageHeight(context), Frame.DEPTH_UBYTE, 2);
            localRecorder = CameraRecorderHelper.initRecorder(context, CameraRecorderHelper.RECORDER_TYPE_LOCAL, VideoNameHelper.getOutputFile(context, sequence).getAbsolutePath(), CameraRecorderHelper.FORMAT_MP4);
        }

        localAudioRecordRunnable = new LocalAudioRecordRunnable();
        localAudioThread = new Thread(localAudioRecordRunnable);
        localRunAudioThread = true;
    }

    public void configLocalMediaRecorder() {
        primaryCamera.unlock();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(primaryCamera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(profile);
        mediaRecorder.setOutputFile(VideoNameHelper.getOutputFile(context, sequence).getAbsolutePath());
    }

    public void configStreamingRecorder() {
        streamingYuvImage = new Frame(CameraRecorderHelper.getStreamingImageWidth(context),
                CameraRecorderHelper.getStreamingImageHeight(context), Frame.DEPTH_UBYTE, 2);
        streamingRecorder = CameraRecorderHelper.initRecorder(context, CameraRecorderHelper.RECORDER_TYPE_STREAMING,
                CameraRecorderHelper.buildStreamEndpoint(context), CameraRecorderHelper.FORMAT_FLV);
        //String filterString = "drawtext=fontsize=60:fontfile=/system/fonts/DroidSans.ttf:fontcolor=white@0.8:text='TITAN-" + PreferencesHelper.getDeviceId(context) + " %{localtime\\:%T %d/%m/%Y}':x=20:y=20,scale=w=" + CameraHelper.getStreamingImageWidth(context) + ":h=" + +CameraHelper.getStreamingImageHeight(context);
        String filterString = "transpose=dir=1:passthrough=portrait";
        streamingFilter = new FFmpegFrameFilter(filterString, CameraHelper.getStreamingImageWidth(context), CameraHelper.getStreamingImageHeight(context));
        streamingFilter.setPixelFormat(avutil.AV_PIX_FMT_NV21);
        streamingAudioRecordRunnable = new StreamingAudioRecordRunnable();
        streamingAudioThread = new Thread(streamingAudioRecordRunnable);
        streamingRunAudioThread = true;
    }

    public void startLocalRecorder(boolean playSound) {
        startLocalMediaRecorder(playSound);
    }

    public void startLocalRecorder_(boolean playSound) {
        if (isStreamingRecording || sosPressed) {
            startLocalFrameRecorder_(playSound);
            sosPressed = false;
        } else {
            startLocalMediaRecorder_(playSound);
        }
    }

    public void stopLocalRecorder(boolean playSound) {
        if (mediaRecorder != null) {
            stopLocalMediaRecorder(playSound);
        }
    }

    public void stopLocalRecorder_(boolean playSound) {
        if (mediaRecorder != null) {
            stopLocalMediaRecorder(playSound);
        }
        if (localRecorder != null) {
            stopLocalFrameRecorder(playSound);
        }
    }


    private void startLocalFrameRecorder_(boolean playSound) {
        if (isLocalRecording) {
            return;
        }
        if (!playSound) {
            sequence = sequence + 1;
        }
        if (!isStreamingRecording) {
            initCamera_(true);
        }
        configLocalRecorder();
        try {
            localRecorder.start();
            localStartTime = System.currentTimeMillis();
            isLocalRecording = true;
            localAudioThread.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
        if (playSound) {
            if (ConfigHelper.getLocalVibrateAndSound(context)) {
                CameraRecorderHelper.soundStart(context);
            }
            bus.post(new MessageEvent(MessageEvent.START_LOCAL_RECORDING));
        }
    }

    private void startLocalMediaRecorder(boolean playSound) {
        if (isLocalRecording) {
            return;
        }
        if (!playSound) {
            sequence = sequence + 1;
        }
        if (!isStreamingRecording) {
            prepareStreamingCamera();//Abrir cámara 0 antes de la 1
        }
        initPrimaryCamera();
        Utils.updateWaterMark(context);
        configLocalMediaRecorder();
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
                    //Log.d("--onInfo", "Info " + i);
                }
            });
            isLocalRecording = true;
        } catch (IOException | IllegalStateException e) {
            //Log.e("Depuracion", "Error " + e.getMessage());
        }
        if (playSound) {
            if (ConfigHelper.getLocalVibrateAndSound(context)) {
                CameraRecorderHelper.soundStart(context);
            }
            bus.post(new MessageEvent(MessageEvent.START_LOCAL_RECORDING));
        }
    }

    private void startLocalMediaRecorder_(boolean playSound) {
        if (isLocalRecording) {
            return;
        }
        if (!playSound) {
            sequence = sequence + 1;
        }
        if (!isStreamingRecording) {
            initCamera_(true);
        }
        Utils.updateWaterMark(context);
        configLocalMediaRecorder();
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isLocalRecording = true;
        } catch (IOException | IllegalStateException e) {
            //Log.e("Depuracion", "Error " + e.getMessage());
        }
        if (playSound) {
            if (ConfigHelper.getLocalVibrateAndSound(context)) {
                CameraRecorderHelper.soundStart(context);
            }
            bus.post(new MessageEvent(MessageEvent.START_LOCAL_RECORDING));
        }
    }

    private void stopLocalFrameRecorder(boolean playSound) {
        if (!isStreamingRecording) {
            finishCamera();
        }
        localRunAudioThread = false;
        try {
            if (localAudioThread != null) {
                localAudioThread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        localAudioRecordRunnable = null;
        localAudioThread = null;
        if (localRecorder != null && isLocalRecording) {
            isLocalRecording = false;
            try {
                localRecorder.stop();
                localRecorder.release();
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
            localRecorder = null;
            localYuvImage = null;
        }
        if (playSound) {
            sendBusStopRecorder();
        }
    }

    private void stopLocalMediaRecorder(boolean playSound) {
        if (mediaRecorder != null && isLocalRecording) {
            isLocalRecording = false;
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            } catch (RuntimeException e) {
                File file = new File(VideoNameHelper.getOutputFile(context, sequence).getAbsolutePath());
                if (file.exists()) {
                    file.delete();
                }
                mediaRecorder.reset();
            } finally {
                mediaRecorder = null;
                if (!isStreamingRecording) {
                    finishCamera();
                }

                finishPrimaryCamera();

            }
        }
        if (playSound) {
            sendBusStopRecorder();
        }
    }

    private void stopLocalMediaRecorder_(boolean playSound) {
        if (mediaRecorder != null && isLocalRecording) {
            isLocalRecording = false;
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            } catch (RuntimeException e) {
                File file = new File(VideoNameHelper.getOutputFile(context, sequence).getAbsolutePath());
                if (file.exists()) {
                    file.delete();
                }
                mediaRecorder.reset();
            } finally {
                mediaRecorder = null;
                if (!isStreamingRecording) {
                    finishPrimaryCamera();
                }
            }
        }
        if (playSound) {
            sendBusStopRecorder();
        }
    }

    public void sendBusStopRecorder() {
        videoDuration = 0;
        sequence = 1;
        if (ConfigHelper.getLocalVibrateAndSound(context)) {
            CameraRecorderHelper.soundStop(context);
        }
        bus.post(new MessageEvent(MessageEvent.STOP_LOCAL_RECORDING));
    }

    public void startStreamingRecorder() {
        if (isStreamingRecording) {
            return;
        }
        initCamera();
        configStreamingRecorder();
        try {
            streamingRecorder.start();
            streamingFilter.start();
            streamingStartTime = System.currentTimeMillis();
            isStreamingRecording = true;
            proccesingStreming = false;
            streamingAudioThread.start();
        } catch (FrameRecorder.Exception | FrameFilter.Exception e) {
            e.printStackTrace();
            MessageEvent event = new MessageEvent(MessageEvent.STOP_STREAMING);
            bus.post(event);
        }
        if (ConfigHelper.getStreamingVibrateAndSound(context)) {
            CameraRecorderHelper.soundStart(context);
        }
    }

    public void startStreamingRecorder_() {
        if (isStreamingRecording) {
            return;
        }
        initCamera_(false);
        configStreamingRecorder();
        try {
            streamingRecorder.start();
            streamingStartTime = System.currentTimeMillis();
            isStreamingRecording = true;
            proccesingStreming = false;
            streamingAudioThread.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
            MessageEvent event = new MessageEvent(MessageEvent.STOP_STREAMING);
            bus.post(event);
        }
        if (ConfigHelper.getStreamingVibrateAndSound(context)) {
            CameraRecorderHelper.soundStart(context);
        }
    }

    public void stopStreamingRecorder() {
        streamingRunAudioThread = false;
        if (!isLocalRecording) { // Si aún hay grabación local no cerrar la camara 0
            finishCamera();
        }
        try {
            if (streamingAudioThread != null) {
                streamingAudioThread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        streamingAudioRecordRunnable = null;
        streamingAudioThread = null;
        if (streamingRecorder != null && isStreamingRecording) {
            isStreamingRecording = false;
            proccesingStreming = false;
            try {
                streamingRecorder.stop();
                streamingRecorder.release();
                streamingFilter.stop();
                streamingFilter.release();
            } catch (FrameRecorder.Exception | FrameFilter.Exception e) {
                e.printStackTrace();
            }
            streamingRecorder = null;
            streamingFilter = null;
            streamingYuvImage = null;
        }
        if (ConfigHelper.getStreamingVibrateAndSound(context)) {
            CameraRecorderHelper.soundStop(context);
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera mCamera) {
        Camera.Parameters parameters = camera.getParameters();

        Mat mat = new Mat(parameters.getPreviewSize().height, parameters.getPreviewSize().width, CvType.CV_8UC2);
        mat.put(0, 0, bytes);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = sdf.format(new Date());
        CameraRecorderHelper.putWaterMark(mat, currentDate, "TITAN-" + deviceId);

        int bufferSize = (int) (mat.total() * mat.elemSize());
        byte[] b = new byte[bufferSize];

        mat.get(0, 0, b);

        if (isStreamingRecording) {
            streamingRecord(b);
        }

        if (camera != null) {
            camera.addCallbackBuffer(buffer);
        }
    }

    public void takePhoto() {
        takePhoto = true;
    }

    public void takePhotoDirect() {
        try{
            primaryCamera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {

                }
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    Log.i(TAG, "onPictureTaken - raw");
                    //savePhoto(bytes);
                }
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    Log.i(TAG, "onPictureTaken - jpeg");
                    /*Bitmap bmp = BitmapFactory.decodeByteArray(bytes , 0, bytes.length);
                    Mat orig = new Mat(bmp.getHeight(),bmp.getWidth(),CvType.CV_8UC2);
                    Bitmap myBitmap32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
                    org.opencv.android.Utils.bitmapToMat(myBitmap32, orig);
                    //Mat mImage = new Mat();
                    //Imgproc.cvtColor(orig,mImage,Imgproc.COLOR_RGB2YUV);
                    //Imgproc.cvtColor(orig, mImage, Imgproc.COLOR_BGR2RGB,1);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentDate = sdf.format(new Date());
                    CameraRecorderHelper.putWaterMark(orig, currentDate, "TITAN-" + deviceId);

                    int bufferSize = (int) (orig.step1(0)*orig.rows());
                    byte[] b = new byte[bufferSize];
                    orig.get(0,0,b);

                    savePhoto(b);*/
                    Bitmap bmp = BitmapUtils.convertCompressedByteArrayToBitmap(bytes);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentDate = sdf.format(new Date());

                    Bitmap bmpWm = applyWaterMarkEffect(bmp, currentDate,"TITAN-" + deviceId);
                    byte[] data = BitmapUtils.convertBitmapToByteArray(bmpWm);
                    savePhotoDirect(data);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public void savePhoto(byte[] data) {
        FileOutputStream file = null;
        try {
            Camera.Parameters parameters = primaryCamera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            YuvImage image = new YuvImage(data, parameters.getPreviewFormat(),
                    size.width, size.height, null);
            file = new FileOutputStream(VideoNameHelper.getNamePhoto(context));
            image.compressToJpeg(
                    new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
                    file);
            CameraRecorderHelper.soundTakePhoto(context);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void savePhotoDirect(byte[] data) {
        FileOutputStream file = null;
        try {
            file = new FileOutputStream(VideoNameHelper.getNamePhoto(context));
            file.write(data);
            CameraRecorderHelper.soundTakePhoto(context);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*FileOutputStream file = null;
        try {
            file = new FileOutputStream(VideoNameHelper.getNamePhoto(context));
            file.write(data);
            file.close();
            CameraRecorderHelper.soundTakePhoto(context);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    public static byte[] bitmapToByteArray(Bitmap bm) {
        // Create the buffer with the correct size
        int iBytes = bm.getWidth() * bm.getHeight()*4;
        ByteBuffer buffer = ByteBuffer.allocate(iBytes);

        // Log.e("DBG", buffer.remaining()+""); -- Returns a correct number based on dimensions
        // Copy to buffer and then into byte array
        bm.copyPixelsToBuffer(buffer);
        // Log.e("DBG", buffer.remaining()+""); -- Returns 0
        return buffer.array();
    }

    public Bitmap applyWaterMarkEffect(Bitmap src, String fecha , String watermark) {
        int w = src.getWidth();
        int h = src.getHeight();

        Bitmap.Config conf = src.getConfig();
        Bitmap result = Bitmap.createBitmap(w, h, conf);

        Canvas canvas = new Canvas(result);

        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.white));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawBitmap(src, 0, 0, paint);
        paint.setTextSize(20);
        canvas.drawText(fecha, 20, 50, paint);
        canvas.drawText(watermark, 550, 800, paint);

        return result;
    }

    public void savePhoto_(byte[] data) {
        FileOutputStream file = null;
        try {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            YuvImage image = new YuvImage(data, parameters.getPreviewFormat(),
                    size.width, size.height, null);
            file = new FileOutputStream(VideoNameHelper.getNamePhoto(context));
            image.compressToJpeg(
                    new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
                    file);
            CameraRecorderHelper.soundTakePhoto(context);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    @Override
    public void onPreviewFrame(byte[] bytes, Camera mCamera) {
        Camera.Parameters parameters = camera.getParameters();

        Mat mat = new Mat(parameters.getPreviewSize().height, parameters.getPreviewSize().width, CvType.CV_8UC2);
        mat.put(0, 0, bytes);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = sdf.format(new Date());
        CameraRecorderHelper.putWaterMark(mat, currentDate, "TITAN-" + deviceId);

        int bufferSize = (int) (mat.total() * mat.elemSize());
        byte[] b = new byte[bufferSize];

        mat.get(0, 0, b);

        if (isStreamingRecording) {
            streamingRecord(b);
        }

        if (isLocalRecording && localRecorder != null) {
            localRecord(b);
        }

        if (camera != null) {
            camera.addCallbackBuffer(buffer);
        }
    }
    */

    public void localRecord(byte[] data) {
        if (localAudioRecord == null || localAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            localStartTime = System.currentTimeMillis();
            return;
        }

        if (localYuvImage != null && isLocalRecording) {
            ((ByteBuffer) localYuvImage.image[0].position(0)).put(data);
            try {
                long t = 1000 * (System.currentTimeMillis() - localStartTime);
                if (t > localRecorder.getTimestamp()) {
                    localRecorder.setTimestamp(t);
                }
                localRecorder.record(localYuvImage);
            } catch (FFmpegFrameRecorder.Exception e) {
                Log.v(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void streamingRecord(byte[] data) {
        if (streamingAudioRecord == null || streamingAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            streamingStartTime = System.currentTimeMillis();
            return;
        }


        if (streamingRecorder != null && streamingYuvImage != null && isStreamingRecording) {
            ((ByteBuffer) streamingYuvImage.image[0].position(0)).put(data);
            try {
                long t = 1000 * (System.currentTimeMillis() - streamingStartTime);
                if (t > streamingRecorder.getTimestamp()) {
                    streamingRecorder.setTimestamp(t);
                }
                synchronized (this) {
                    //streamingRecorder.record(streamingYuvImage);
                    streamingFilter.push(streamingYuvImage);
                    Frame frame2;
                    while ((frame2 = streamingFilter.pull()) != null) {
                        streamingRecorder.record(frame2, streamingFilter.getPixelFormat());
                    }
                }
            } catch (FFmpegFrameRecorder.Exception | FrameFilter.Exception e) {
                Log.v(TAG, e.getMessage());
                e.printStackTrace();
                MessageEvent event = new MessageEvent(MessageEvent.STOP_STREAMING);
                bus.post(event);
            } /*catch (FrameFilter.Exception e) {
                e.printStackTrace();
            }*/
        }
    }

    public void initLocalVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                counterLocalVideo = new CounterLocalVideo(ConfigHelper.getLocalVideoDurationInMill(context), 1000);
            }
        });
    }

    public void sendSos() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CameraRecorderHelper.sendSignalSOS(context);
            }
        });
    }

    public void pauseLocalVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (counterLocalVideo != null) {
                    counterLocalVideo.cancel();
                }
            }
        });
    }

    public void startLocalVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (counterLocalVideo != null) {
                    counterLocalVideo.start();
                }
            }
        });
    }

    public void initPostVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                counterPostVideo = new CounterPostVideo(ConfigHelper.getLocalPostVideoDurationInMill(context), 1000);
            }
        });
    }

    public void pausePostVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (counterPostVideo != null) {
                    counterPostVideo.cancel();
                }
            }
        });
    }

    public void startPostVideoCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (counterPostVideo != null) {
                    counterPostVideo.start();
                }
            }
        });
    }

    public void initFlashCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                counterFlash = new CounterFlash(15000, 1000);
            }
        });
    }

    public void pauseFlashCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (counterFlash != null) {
                    counterFlash.cancel();
                }
            }
        });
    }

    public void startFlashCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (counterFlash != null) {
                    counterFlash.start();
                }
            }
        });
    }

    public void sendLogStreaming() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CameraRecorderHelper.sendLogStreaming(context, TrafficStats.getTotalTxBytes() - mStartTX, streamingStarted);
            }
        });
    }

    private class CounterLocalVideo extends CountDownTimer {
        CounterLocalVideo(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            Message message = new Message();
            message.what = StateMachineHandler.LOCAL_RECORDER_PRESSED;
            message.arg1 = StateMachineHandler.DO_NOT_PLAY_SOUND;
            machineHandler.handleMessage(message);
            machineHandler.handleMessage(message);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            videoDuration += 1;
            int seconds = (int) millisUntilFinished / 1000;
            if (seconds % 10 == 0) {
                Utils.saveVideoLocation(context, VideoNameHelper.getCurrentNameFile(context));
            }
            showVideoDuration();
        }
    }

    private class CounterPostVideo extends CountDownTimer {
        CounterPostVideo(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            Message message = new Message();
            message.what = StateMachineHandler.POST_RECORDING;
            machineHandler.handleMessage(message);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }
    }

    private class CounterFlash extends CountDownTimer {
        CounterFlash(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            flashLightOff();
            flashOn = !flashOn;
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }
    }

    public void showVideoDuration() {
        if (videoDuration % 120 == 0) {
            CameraRecorderHelper.soundStart(context);
        }
        @SuppressLint("DefaultLocale")
        String value = String.format("%02d:%02d", videoDuration / 60, videoDuration % 60);
        MessageEvent event = new MessageEvent(MessageEvent.TIME_ELAPSED, value);
        bus.post(event);
    }

    public void flashLightOn() {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                if (camera == null) {
                    final int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    camera = Utils.getCameraInstance(cameraId);
                }
                Camera.Parameters params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                initFlashCounter();
                startFlashCounter();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void flashLightOff() {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                Camera.Parameters params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);
                if (!isLocalRecording && !isStreamingRecording) {
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private class LocalAudioRecordRunnable implements Runnable {

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

            int bufferSize = AudioRecord.getMinBufferSize(AUDIO_RATE_IN_HZ,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            localAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_RATE_IN_HZ,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            ShortBuffer shortBuffer = ShortBuffer.allocate(bufferSize);
            localAudioRecord.startRecording();
            while (localRunAudioThread) {
                int bufferResult = localAudioRecord.read(shortBuffer.array(), 0, shortBuffer.capacity());
                shortBuffer.limit(bufferResult);
                if (bufferResult > 0) {
                    if (isLocalRecording) {
                        try {
                            localRecorder.recordSamples(shortBuffer);
                        } catch (FFmpegFrameRecorder.Exception e) {
                            Log.v(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

            }
            Log.v(TAG, "AudioThread Finished, release audioRecord");
            if (localAudioRecord != null) {
                localAudioRecord.stop();
                localAudioRecord.release();
                localAudioRecord = null;
            }
        }
    }

    private class StreamingAudioRecordRunnable implements Runnable {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

            int bufferSize = AudioRecord.getMinBufferSize(AUDIO_RATE_IN_HZ,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            streamingAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_RATE_IN_HZ,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            ShortBuffer shortBuffer = ShortBuffer.allocate(bufferSize);
            streamingAudioRecord.startRecording();
            while (streamingRunAudioThread) {
                int bufferResult = streamingAudioRecord.read(shortBuffer.array(), 0, shortBuffer.capacity());
                shortBuffer.limit(bufferResult);
                if (bufferResult > 0) {
                    if (isStreamingRecording) {
                        try {
                            streamingRecorder.recordSamples(shortBuffer);
                        } catch (FFmpegFrameRecorder.Exception e) {
                            Log.v(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

            }
            Log.v(TAG, "AudioThread Finished, release audioRecord");
            if (streamingAudioRecord != null) {
                streamingAudioRecord.stop();
                streamingAudioRecord.release();
                streamingAudioRecord = null;
            }
        }
    }
}
