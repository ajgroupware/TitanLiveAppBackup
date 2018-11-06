package com.bpt.tipi.streaming.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bpt.tipi.streaming.ConfigHelper;
import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.network.HttpClient;
import com.bpt.tipi.streaming.network.HttpHelper;
import com.bpt.tipi.streaming.service.RecorderService;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_YUV420P;

/**
 * Created by jpujolji on 26/11/17.
 */

public class CameraRecorderHelper {

    public static final int RECORDER_TYPE_LOCAL = 0;
    public static final int RECORDER_TYPE_STREAMING = 1;

    public static final String FORMAT_MP4 = "mp4";
    public static final String FORMAT_FLV = "flv";

    private static final Scalar TEXT_COLOR = new Scalar(255, 255, 255);

    public static void putWaterMark(Mat mat, String text, String text2) {

        //Calculate size of new matrix
        double radians = Math.toRadians(270);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int newWidth = (int) (mat.width() * cos + mat.height() * sin);
        int newHeight = (int) (mat.width() * sin + mat.height() * cos);

        // rotating image
        Point center = new Point(newWidth / 2, newHeight / 2);


        //Creating the transformation matrix M
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, 270, 1);
        switch (mat.rows()) {
            case 240:
                Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 10), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.45, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 115), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.45, TEXT_COLOR);
                break;
            case 288:
                Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 10), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.45, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 140), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.45, TEXT_COLOR);
                break;
            case 320:
                Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 10), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.45, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 150), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.45, TEXT_COLOR);
                break;
            case 480:
                Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 20), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.7, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 230), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.7, TEXT_COLOR);
                break;
            case 600:
                Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 20), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 290), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, TEXT_COLOR);
                break;
            case 720:
                Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 20), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 340), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, TEXT_COLOR);
                break;
            case 1080:
                Imgproc.warpAffine(mat, mat,rotationMatrix, mat.size());
                Imgproc.putText(mat, text, new Point(2, 30), Core.FONT_HERSHEY_COMPLEX_SMALL, 1.5, TEXT_COLOR);
                Imgproc.putText(mat, text2, new Point(2, 500), Core.FONT_HERSHEY_COMPLEX_SMALL, 1.5, TEXT_COLOR);
                break;
        }
    }

    public static int getStreamingImageWidth(Context context) {
        int videoSize = ConfigHelper.getStreamingVideoSize(context);
        switch (videoSize) {
            case 1:
            case 2:
            case 3:
                return 320;
            case 4:
            case 5:
            case 6:
                return 352;
            case 7:
            case 8:
            case 9:
                return 480;
            case 10:
            case 11:
            case 12:
                return 640;
            case 13:
            case 14:
            case 15:
                return 800;
            case 16:
            case 17:
            case 18:
                return 1280;
            default:
                return 0;
        }
    }

    public static int getStreamingImageHeight(Context context) {
        int videoSize = ConfigHelper.getStreamingVideoSize(context);
        switch (videoSize) {
            case 1:
            case 2:
            case 3:
                return 240;
            case 4:
            case 5:
            case 6:
                return 288;
            case 7:
            case 8:
            case 9:
                return 320;
            case 10:
            case 11:
            case 12:
                return 480;
            case 13:
            case 14:
            case 15:
                return 600;
            case 16:
            case 17:
            case 18:
                return 720;
            default:
                return 0;
        }
    }

    public static int getLocalImageWidth(Context context) {
        int videoSize = ConfigHelper.getLocalVideoSize(context);
        switch (videoSize) {
            case 1:
            case 2:
            case 3:
                return 1920;
            case 4:
            case 5:
            case 6:
                return 1280;
            case 7:
            case 8:
            case 9:
                return 640;
            default:
                return 0;
        }
    }

    public static int getLocalImageHeight(Context context) {
        int videoSize = ConfigHelper.getLocalVideoSize(context);
        switch (videoSize) {
            case 1:
            case 2:
            case 3:
                return 1080;
            case 4:
            case 5:
            case 6:
                return 720;
            case 7:
            case 8:
            case 9:
                return 480;
            default:
                return 0;
        }
    }

    private static int getLocalVideoBitrate(Context context) {
        int videoSize = ConfigHelper.getLocalVideoSize(context);
        switch (videoSize) {
            case 1:
            case 2:
            case 3:
                return 5000;
            case 4:
            case 5:
            case 6:
                return 2500;
            case 7:
            case 8:
            case 9:
                return 1500;
            default:
                return 0;
        }
    }

    private static int getStreamingVideoBitrate(Context context) {
        int videoSize = ConfigHelper.getStreamingVideoSize(context);
        switch (videoSize) {
            case 1:
            case 4:
                return 140;
            case 2:
            case 5:
            case 7:
                return 280;
            case 3:
            case 6:
            case 8:
                return 560;
            case 9:
            case 10:
                return 700;
            case 11:
            case 13:
            case 16:
                return 1000;
            case 12:
            case 14:
                return 1500;
            case 15:
                return 2000;
            case 17:
                return 2500;
            case 18:
                return 3750;
            default:
                return 0;
        }
    }

    public static FFmpegFrameRecorder initRecorder(Context context, int recorderType, String filename, String format) {
        FFmpegFrameRecorder frameRecorder;

        frameRecorder = new FFmpegFrameRecorder(filename, getStreamingImageWidth(context), getStreamingImageHeight(context), 1);
        frameRecorder.setFrameRate(ConfigHelper.getStreamingFramerate(context));
        frameRecorder.setVideoBitrate(getStreamingVideoBitrate(context));

        frameRecorder.setFormat(format);
        frameRecorder.setInterleaved(true);
        frameRecorder.setVideoOption("preset", "ultrafast");
        frameRecorder.setVideoOption("tune", "zerolatency");
        frameRecorder.setVideoOption("fflags", "nobuffer");
        frameRecorder.setVideoOption("analyzeduration", "0");
        frameRecorder.setVideoOption("crf", "28");
        frameRecorder.setGopSize(30);
        frameRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        frameRecorder.setPixelFormat(AV_PIX_FMT_YUV420P);
        frameRecorder.setAudioBitrate(RecorderService.AUDIO_BITRATE);
        frameRecorder.setAudioOption("crf", "0");
        frameRecorder.setAudioQuality(0);
        frameRecorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        frameRecorder.setSampleRate(RecorderService.AUDIO_RATE_IN_HZ);

        return frameRecorder;
    }


    public static FFmpegFrameRecorder initRecorder_(Context context, int recorderType, String filename, String format) {
        FFmpegFrameRecorder frameRecorder;
        if (recorderType == RECORDER_TYPE_LOCAL) {
            frameRecorder = new FFmpegFrameRecorder(filename, getLocalImageWidth(context), getLocalImageHeight(context), 1);
            frameRecorder.setFrameRate(ConfigHelper.getLocalFramerate(context));
            frameRecorder.setVideoBitrate(getLocalVideoBitrate(context));
        } else {
            frameRecorder = new FFmpegFrameRecorder(filename, getStreamingImageWidth(context), getStreamingImageHeight(context), 1);
            frameRecorder.setFrameRate(ConfigHelper.getStreamingFramerate(context));
            frameRecorder.setVideoBitrate(getStreamingVideoBitrate(context));
        }
        frameRecorder.setFormat(format);
        frameRecorder.setInterleaved(true);
        frameRecorder.setVideoOption("preset", "ultrafast");
        frameRecorder.setVideoOption("tune", "zerolatency");
        frameRecorder.setVideoOption("fflags", "nobuffer");
        frameRecorder.setVideoOption("analyzeduration", "0");
        frameRecorder.setVideoOption("crf", "28");
        frameRecorder.setGopSize(30);
        frameRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        frameRecorder.setPixelFormat(AV_PIX_FMT_YUV420P);
        frameRecorder.setAudioBitrate(RecorderService.AUDIO_BITRATE);
        frameRecorder.setAudioOption("crf", "0");
        frameRecorder.setAudioQuality(0);
        frameRecorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        frameRecorder.setSampleRate(RecorderService.AUDIO_RATE_IN_HZ);

        return frameRecorder;
    }


    public static String buildStreamEndpoint(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String wowzaIp = ConfigHelper.getStreamHostAddress(context);
        String portNumber = "" + ConfigHelper.getStreamPortNumber(context);
        String appName = ConfigHelper.getStreamAppName(context);
        String streamName = preferences.getString(context.getString(R.string.id_device), "");
        String username = ConfigHelper.getStreamUsername(context);
        String password = ConfigHelper.getStreamPassword(context);

        StringBuilder builder = new StringBuilder();
        builder.append("rtmp://");
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            builder.append(username).append(":").append(password).append("@");
        }

        if (wowzaIp.isEmpty()) {
            Toast.makeText(context, "Dirección IP del servidor no válida", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(appName)) {
            Toast.makeText(context, "Nombre de la app no válido", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(streamName)) {
            Toast.makeText(context, "ID del dispositivo no válido", Toast.LENGTH_SHORT).show();
        } else {
            builder.append(wowzaIp).append(":").append(portNumber).append("/");
            builder.append(appName).append("/");
            builder.append(streamName);
            Log.i("Depuracion", "wowzaIp " + builder.toString());
            return builder.toString();
        }
        return "";
    }

    public static void sendSignalSOS(final Context context) {
        new CountDownTimer(1000, 1000) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                sendSos(context);
            }
        }.start();
    }

    private static void sendSos(Context context) {
        AsyncHttpClient client = new SyncHttpClient();
        client.setTimeout(20000);
        client.addHeader("Authorization", "Basic Y2FtZXJhOmNhbWVyNCoh");
        JSONObject json = new JSONObject();
        StringEntity entity = null;
        try {
            entity = new StringEntity(json.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String idDevice = preferences.getString(context.getString(R.string.id_device), "");

        String url = HttpClient.URL + HttpHelper.Method.SOS + "/" + idDevice;

        client.put(context, url, entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("Depuracion", "errorResponse " + errorResponse + " throwable " + throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("Depuracion", "response " + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("Depuracion", "Error " + statusCode + " responseString " + responseString);
            }
        });
    }

    public static void sendLogStreaming(Context context, long bytesTransmited, Date startDate) {
        AsyncHttpClient client = new SyncHttpClient();
        client.setTimeout(20000);
        client.addHeader("Authorization", "Basic Y2FtZXJhOmNhbWVyNCoh");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String idDevice = preferences.getString(context.getString(R.string.id_device), "");

        JSONObject json = new JSONObject();
        try {
            DateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            json.put("deviceName", idDevice);
            json.put("startStr", dt.format(startDate));
            json.put("bytesTransmited", bytesTransmited);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringEntity entity = null;
        try {
            entity = new StringEntity(json.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String url = HttpClient.URL + HttpHelper.Method.LOG_STREAMING;

        Log.i("Depuracion", "JSONObject " + json);

        client.post(context, url, entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("Depuracion", "errorResponse " + errorResponse + " throwable " + throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("Depuracion", "response " + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("Depuracion", "Error " + statusCode + " responseString " + throwable);
            }
        });
    }

    public static void soundStart(Context context) {
        MediaPlayer mPlayer = MediaPlayer.create(context, R.raw.audio_start);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
            }
        });
        mPlayer.start();
        ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(800);
    }

    public static void soundStop(Context context) {
        MediaPlayer mPlayer = MediaPlayer.create(context, R.raw.audio_stop);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
            }
        });
        mPlayer.start();
        ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(800);
    }

    public static void soundTakePhoto(Context context) {
        MediaPlayer mPlayer = MediaPlayer.create(context, R.raw.audio_take_photo);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
            }
        });
        mPlayer.start();
    }
}