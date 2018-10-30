package com.bpt.tipi.streaming.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
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

import com.bpt.tipi.streaming.Utils;
import com.bpt.tipi.streaming.camera_state_machine.StateMachineHandler;
import com.bpt.tipi.streaming.helper.CameraHelper;
import com.bpt.tipi.streaming.helper.IrHelper;
import com.bpt.tipi.streaming.helper.PreferencesHelper;
import com.bpt.tipi.streaming.helper.VideoNameHelper;
import com.bpt.tipi.streaming.model.MessageEvent;
import com.bpt.tipi.streaming.network.HttpClient;
import com.bpt.tipi.streaming.network.HttpHelper;
import com.bpt.tipi.streaming.network.HttpInterface;

import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;
import org.bytedeco.javacv.FrameRecorder;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class CameraService extends Service implements Camera.PreviewCallback {

    public static final int AUDIO_BITRATE = 128000;
    public static final int AUDIO_RATE_IN_HZ = 44100;

    private Context context;
    private Camera camera = null;
    private Camera cameraFront = null;


    /* Variables de local */
    FFmpegFrameFilter streamingFilter;
    private FFmpegFrameRecorder streamingRecorder;
    private Frame streamingYuvImage = null;

    long streamingStartTime = 0;

    boolean initializingStreaming = false;
    boolean inStreaming = false;


    /* Variables de local */
    FFmpegFrameFilter localFilter;
    private FFmpegFrameRecorder localRecorder;
    private Frame localYuvImage = null;

    long localStartTime = 0;

    public boolean recording = false;

    private AudioRecord audioRecord;
    private AudioRecordRunnable audioRecordRunnable;
    private Thread audioThread;
    volatile boolean runAudioThread = true;

    private static final String TAG = "CameraService";
    StateMachineHandler machineHandler;
    private EventBus bus = EventBus.getDefault();

    private int sequence = 1;

    boolean takePhoto = false;

    private Handler mHandler;

    private boolean inSos = false;

    CounterLocalVideo counterLocalVideo;
    private int videoDuration = 0;

    long mStartTX;
    Date streamingStarted;

    boolean flashOn = false;
    CounterFlash counterFlash;

    private final IBinder mBinder = new CameraBinder();

    public class CameraBinder extends Binder {
        public CameraService getService() {
            return CameraService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate()");
        super.onCreate();
        context = CameraService.this;
        HandlerThread thread = new HandlerThread("CameraService");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bus.register(this);
        machineHandler = new StateMachineHandler(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        bus.unregister(this);
        finishCamera();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(MessageEvent event) {
        Log.e(TAG, "onMessageEvent " + event.key);
        switch (event.key) {
            case MessageEvent.SOS_PRESSED:
                if (!inSos) {
                    inSos = true;
                    if (!recording) {
                        setLocalRecorderStateMachine();
                    }
                    //sendSos();
                } else {
                    inSos = false;
                    setLocalRecorderStateMachine();
                }
                break;
            case MessageEvent.LOCAL_RECORD:
                if (inSos) {
                    inSos = false;
                }
                setLocalRecorderStateMachine();
                break;
            case MessageEvent.START_STREAMING:
                if (!initializingStreaming) {
                    initializingStreaming = true;
                    if (!inStreaming) {
                        machineHandler.sendEmptyMessage(StateMachineHandler.STREAMING);
                        mStartTX = TrafficStats.getTotalTxBytes();
                        Calendar cal = Calendar.getInstance();
                        streamingStarted = cal.getTime();
                    } else {
                        initializingStreaming = false;
                    }
                }
                break;
            case MessageEvent.STOP_STREAMING:
                if (!initializingStreaming) {
                    initializingStreaming = true;
                    if (inStreaming) {
                        machineHandler.sendEmptyMessage(StateMachineHandler.STREAMING);
                        //sendLogStreaming();
                    } else {
                        initializingStreaming = false;
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
                    stopFlashCounter();
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

    public synchronized void initCamera() {
        if (!CameraHelper.isCameraExist(context)) {
            throw new IllegalStateException("There is no device, not possible to start recording");
        }

        if (camera == null) {
            camera = CameraHelper.getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
        if (camera != null) {
            try {
                camera.setPreviewTexture(new SurfaceTexture(10));
            } catch (Exception e) {
                e.printStackTrace();
            }
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(1920, 1080);
            parameters.setPreviewFormat(ImageFormat.NV21);
            camera.setParameters(parameters);

            camera.setPreviewCallback(this);
            try {
                camera.startPreview();
                IrHelper.setIrState(IrHelper.STATE_ON);
            } catch (Exception e) {
                Log.d(TAG, "Error " + e);
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Get camera from service failed");
        }
    }

    public synchronized void finishCamera() {
        IrHelper.setIrState(IrHelper.STATE_OFF);
        try {
            if (camera != null) {
                camera.stopPreview();
                camera.setPreviewCallback(null);
                camera.release();
                camera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void configLocalRecorder(boolean playSound) {
        if (!playSound) {
            sequence = sequence + 1;
        }
        localYuvImage = new Frame(1920, 1080, Frame.DEPTH_UBYTE, 2);
        localRecorder = CameraHelper.initLocalRecorder(context, VideoNameHelper.getOutputFile(context, sequence).getAbsolutePath());

        String filterString = "drawtext=fontsize=60:fontfile=/system/fonts/DroidSans.ttf:fontcolor=white@0.8:text='TITAN-" + PreferencesHelper.getDeviceId(context) + " %{localtime\\:%T %d/%m/%Y}':x=20:y=20,scale=w=" + CameraHelper.getLocalImageWidth(context) + ":h=" + +CameraHelper.getLocalImageHeight(context);
        localFilter = new FFmpegFrameFilter(filterString, CameraHelper.getLocalImageWidth(context), CameraHelper.getLocalImageHeight(context));

        localFilter.setPixelFormat(avutil.AV_PIX_FMT_NV21);
    }

    public synchronized void startLocalRecorder(boolean playSound) {
        if (recording) {
            return;
        }
        try {
            localRecorder.start();
            localFilter.start();
            localStartTime = System.currentTimeMillis();
            recording = true;
            startLocalCounter();
        } catch (FrameRecorder.Exception | FrameFilter.Exception e) {
            e.printStackTrace();
            if (!inStreaming) {
                finishCamera();
            }
        }
        if (playSound) {
            if (PreferencesHelper.getLocalVibrateAndSound(context)) {
                CameraHelper.soundStart(context);
            }
            bus.post(new MessageEvent(MessageEvent.START_LOCAL_RECORDING));
        }
    }

    public void stopLocalRecorder(boolean playSound) {
        if (localRecorder != null && recording) {
            recording = false;
            try {
                localRecorder.stop();
                localRecorder.release();
                localFilter.stop();
                localFilter.release();
                stopLocalCounter();
            } catch (FrameRecorder.Exception | FrameFilter.Exception e) {
                e.printStackTrace();
            }
            localRecorder = null;
            localFilter = null;
            localYuvImage = null;
            if (playSound) {
                videoDuration = 0;
                sequence = 1;
                if (PreferencesHelper.getLocalVibrateAndSound(context)) {
                    CameraHelper.soundStop(context);
                }
                bus.post(new MessageEvent(MessageEvent.STOP_LOCAL_RECORDING));
            }
        }
    }

    public synchronized void configStreamingRecorder() {
        streamingYuvImage = new Frame(1920, 1080, Frame.DEPTH_UBYTE, 2);
        streamingRecorder = CameraHelper.initStreamingRecorder(context);

        String filterString = "drawtext=fontsize=60:fontfile=/system/fonts/DroidSans.ttf:fontcolor=white@0.8:text='TITAN-" + PreferencesHelper.getDeviceId(context) + " %{localtime\\:%T %d/%m/%Y}':x=20:y=20,scale=w=" + CameraHelper.getStreamingImageWidth(context) + ":h=" + +CameraHelper.getStreamingImageHeight(context);
        streamingFilter = new FFmpegFrameFilter(filterString, CameraHelper.getStreamingImageWidth(context), CameraHelper.getStreamingImageHeight(context));

        streamingFilter.setPixelFormat(avutil.AV_PIX_FMT_NV21);
    }

    public synchronized void startStreamingRecorder() {
        if (inStreaming) {
            return;
        }
        try {
            streamingRecorder.start();
            streamingFilter.start();
            streamingStartTime = System.currentTimeMillis();
            inStreaming = true;
            initializingStreaming = false;
        } catch (FrameRecorder.Exception | FrameFilter.Exception e) {
            e.printStackTrace();
            if (!recording) {
                initializingStreaming = false;
                machineHandler.sendEmptyMessage(StateMachineHandler.STREAMING);
            }
        }
        if (PreferencesHelper.getStreamingVibrateAndSound(context)) {
            CameraHelper.soundStart(context);
        }
    }

    public void stopStreamingRecorder() {
        if (streamingRecorder != null && inStreaming) {
            inStreaming = false;
            initializingStreaming = false;
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
            if (PreferencesHelper.getStreamingVibrateAndSound(context)) {
                CameraHelper.soundStop(context);
            }
        }
    }

    public void initAudioRecord() {
        audioRecordRunnable = new AudioRecordRunnable();
        audioThread = new Thread(audioRecordRunnable);
        runAudioThread = true;
        audioThread.start();
    }

    public void finishAudioRecord() {
        runAudioThread = false;
        try {
            if (audioThread != null) {
                audioThread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        audioRecordRunnable = null;
        audioThread = null;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (recording && localRecorder != null) {
            localRecord(data);
        }

        if (inStreaming && streamingRecorder != null) {
            streamingRecord(data);
        }
        if (takePhoto) {
            takePhoto = false;
            savePhoto(data);
            machineHandler.sendEmptyMessage(StateMachineHandler.TAKE_PHOTO);
        }
    }

    public void takePhoto() {
        takePhoto = true;
    }

    public void savePhoto(byte[] data) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            YuvImage image = new YuvImage(data, parameters.getPreviewFormat(),
                    size.width, size.height, null);
            FileOutputStream file = new FileOutputStream(VideoNameHelper.getNamePhoto(context));
            image.compressToJpeg(
                    new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
                    file);
            CameraHelper.soundTakePhoto(context);
        } catch (FileNotFoundException e) {

        }
    }

    public void localRecord(byte[] data) {
        if (localYuvImage != null && recording) {
            ((ByteBuffer) localYuvImage.image[0].position(0)).put(data);
            try {
                long t = 1000 * (System.currentTimeMillis() - localStartTime);
                if (t > localRecorder.getTimestamp()) {
                    localRecorder.setTimestamp(t);
                }
                localFilter.push(localYuvImage);
                Frame frame2;
                while ((frame2 = localFilter.pull()) != null) {
                    localRecorder.record(frame2, localFilter.getPixelFormat());
                }
            } catch (FFmpegFrameRecorder.Exception | FrameFilter.Exception e) {
                Log.v(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void streamingRecord(byte[] data) {
        if (streamingYuvImage != null && inStreaming) {
            ((ByteBuffer) streamingYuvImage.image[0].position(0)).put(data);
            try {
                long t = 1000 * (System.currentTimeMillis() - streamingStartTime);
                if (t > streamingRecorder.getTimestamp()) {
                    streamingRecorder.setTimestamp(t);
                }
                streamingFilter.push(streamingYuvImage);
                Frame frame2;
                while ((frame2 = streamingFilter.pull()) != null) {
                    streamingRecorder.record(frame2, streamingFilter.getPixelFormat());
                }
            } catch (FFmpegFrameRecorder.Exception | FrameFilter.Exception e) {
                Log.v(TAG, e.getMessage());
                e.printStackTrace();
                machineHandler.sendEmptyMessage(StateMachineHandler.STREAMING);
            }
        }
    }

    public void startLocalCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int duration = PreferencesHelper.getLocalVideoDuration(context) * 60 * 1000;
                counterLocalVideo = new CounterLocalVideo(duration, 1000);
                counterLocalVideo.start();
            }
        });
    }

    public void stopLocalCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                counterLocalVideo.cancel();
                counterLocalVideo = null;
            }
        });
    }

    public void flashLightOn() {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                if (cameraFront == null) {
                    final int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    cameraFront = CameraHelper.getCameraInstance(cameraId);
                }
                Camera.Parameters params = cameraFront.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cameraFront.setParameters(params);
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
                Camera.Parameters params = cameraFront.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                cameraFront.setParameters(params);
                cameraFront.stopPreview();
                cameraFront.release();
                cameraFront = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class AudioRecordRunnable implements Runnable {

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

            int bufferSize = AudioRecord.getMinBufferSize(AUDIO_RATE_IN_HZ,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_RATE_IN_HZ,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            ShortBuffer shortBuffer = ShortBuffer.allocate(bufferSize);
            audioRecord.startRecording();
            while (runAudioThread) {
                int bufferResult = audioRecord.read(shortBuffer.array(), 0, shortBuffer.capacity());
                shortBuffer.limit(bufferResult);
                if (bufferResult > 0) {
                    if (inStreaming) {
                        try {
                            streamingRecorder.recordSamples(shortBuffer);
                        } catch (FFmpegFrameRecorder.Exception e) {
                            Log.v(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    if (recording) {
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
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        }
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
        }

        @Override
        public void onTick(long millisUntilFinished) {
            videoDuration += 1;
            int seconds = (int) millisUntilFinished / 1000;
            if (seconds % 10 == 0) {
                Utils.saveVideoLocation(context, VideoNameHelper.getCurrentNameFile(context));
            }
            if (videoDuration % 120 == 0) {
                CameraHelper.soundStart(context);
            }
            @SuppressLint("DefaultLocale")
            String value = String.format("%02d:%02d", videoDuration / 60, videoDuration % 60);
            MessageEvent event = new MessageEvent(MessageEvent.TIME_ELAPSED, value);
            bus.post(event);
        }
    }

    public void startFlashCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                counterFlash = new CounterFlash(15000, 1000);
                counterFlash.start();
            }
        });
    }

    public void stopFlashCounter() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                counterFlash.cancel();
                counterFlash = null;
            }
        });
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

    /*
    public void sendSos() {
        HttpClient httpClient = new HttpClient(context, new HttpInterface() {
            @Override
            public void onSuccess(String method, JSONObject response) {

            }

            @Override
            public void onFailed(String method, JSONObject errorResponse) {

            }
        });
        String method = HttpHelper.Method.SOS + "/" + PreferencesHelper.getDeviceId(context);
        httpClient.httpRequest("", method, HttpHelper.TypeRequest.TYPE_PUT, false);
    }

    public void sendLogStreaming() {
        HttpClient httpClient = new HttpClient(context, new HttpInterface() {
            @Override
            public void onSuccess(String method, JSONObject response) {

            }

            @Override
            public void onFailed(String method, JSONObject errorResponse) {

            }
        });

        long bytesTransmited = TrafficStats.getTotalTxBytes() - mStartTX;
        JSONObject json = new JSONObject();
        try {
            DateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            json.put("deviceName", PreferencesHelper.getDeviceId(context));
            json.put("startStr", dt.format(streamingStarted));
            json.put("bytesTransmited", bytesTransmited);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpClient.httpRequest(json.toString(), HttpHelper.Method.LOG_STREAMING, HttpHelper.TypeRequest.TYPE_POST, false);
    }
    */
}
