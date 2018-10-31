package com.bpt.tipi.streaming;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bpt.tipi.streaming.model.GeneralParameter;
import com.bpt.tipi.streaming.model.RemoteConfig;

/**
 * Created by jpujolji on 8/03/18.
 */

public class ConfigHelper {

    public static void saveConfig(Context context, RemoteConfig remoteConfig) {
        saveLocalVideoSize(context, remoteConfig.localVideoSize);
        saveLocalFramerate(context, remoteConfig.localFrameRate);
        saveLocalVideoDuration(context, remoteConfig.localVideoDurationMin);
        saveLocalVibrateAndSound(context, remoteConfig.vibrateAndSoundOnRecord);
        saveLocalPostRecorder(context, remoteConfig.postRecordingEnabled);
        saveLocalPostVideoDuration(context, remoteConfig.postRecordingTimeMin);
        saveStreamingVideoSize(context, remoteConfig.streamingVideoSize);
        saveStreamingFramerate(context, remoteConfig.streamingFrameRate);
        saveStreamingVibrateAndSound(context, remoteConfig.vibrateAndSoundOnStreaming);
        saveGeneralPassword(context, remoteConfig.masterPwd);
        saveIntervalLocation(context, remoteConfig.locationTimeInterval);
        saveIntervalLocationInSos(context, remoteConfig.sosTimeInterval);
        saveStreamHostAddress(context, remoteConfig.generalParameter.wowzaServerUrl);
        saveStreamPortNumber(context, remoteConfig.generalParameter.wowzaPort);
        saveStreamAppName(context, remoteConfig.generalParameter.wowzaAppName);
        saveStreamUsername(context, remoteConfig.generalParameter.wowzaUser);
        saveStreamPassword(context, remoteConfig.generalParameter.wowzaPwd);
    }

    public static RemoteConfig getConfig(Context context) {
        GeneralParameter generalParameter = new GeneralParameter(getStreamHostAddress(context),
                getStreamPortNumber(context), getStreamAppName(context), getStreamUsername(context),
                getStreamPassword(context));

        RemoteConfig remoteConfig = new RemoteConfig(getDeviceName(context), getGeneralPassword(context),
                getIntervalLocation(context), getIntervalLocationInSos(context),
                getLocalFramerate(context), getLocalVideoDuration(context),
                getLocalPostVideoDuration(context), getStreamingFramerate(context),
                getStreamingVideoSize(context), getLocalVideoSize(context),
                getLocalVibrateAndSound(context), getLocalPostRecorder(context),
                getStreamingVibrateAndSound(context), generalParameter);
        return remoteConfig;
    }

    public static void saveLocalVideoSize(Context context, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.local_key_video_size), "" + value);
        editor.apply();
    }

    public static void saveLocalFramerate(Context context, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.local_key_framerate), "" + value);
        editor.apply();
    }

    public static void saveLocalVideoDuration(Context context, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.local_key_video_duration), "" + value);
        editor.apply();
    }

    public static void saveLocalVibrateAndSound(Context context, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(R.string.local_key_vibrate_and_sound), value);
        editor.apply();
    }

    public static void saveLocalPostRecorder(Context context, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(R.string.local_key_post_recorder), value);
        editor.apply();
    }

    public static void saveLocalPostVideoDuration(Context context, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.local_key_post_video_duration), "" + value);
        editor.apply();
    }

    public static void saveStreamingVideoSize(Context context, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.streaming_key_video_size), "" + value);
        editor.apply();
    }

    public static void saveStreamingFramerate(Context context, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.streaming_key_framerate), "" + value);
        editor.apply();
    }

    public static void saveStreamingVibrateAndSound(Context context, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(R.string.streaming_key_vibrate_and_sound), value);
        editor.apply();
    }

    public static void saveGeneralPassword(Context context, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.general_key_settings_password), value);
        editor.apply();
    }

    public static void saveIntervalLocation(Context context, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.general_key_interval_location), "" + value);
        editor.apply();
    }

    public static void saveIntervalLocationInSos(Context context, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.general_key_interval_location_sos), "" + value);
        editor.apply();
    }

    public static void saveStreamHostAddress(Context context, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_host_address), value);
        editor.apply();
    }

    public static void saveStreamPortNumber(Context context, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_port_number), "" + value);
        editor.apply();
    }

    public static void saveStreamAppName(Context context, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_stream_app_name), value);
        editor.apply();
    }

    public static void saveStreamUsername(Context context, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_username), value);
        editor.apply();
    }

    public static void saveStreamPassword(Context context, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_password), value);
        editor.apply();
    }

    public static String getDeviceName(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.general_key_id_device), "");
    }

    public static int getLocalVideoSize(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String videoSize = preferences.getString(context.getString(R.string.local_key_video_size), context.getString(R.string.pref_default_local_video_size));
        return Integer.parseInt(videoSize);
    }

    public static int getLocalFramerate(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String framerate = preferences.getString(context.getString(R.string.local_key_framerate), context.getString(R.string.pref_default_local_framerate));
        return Integer.parseInt(framerate);
    }

    public static int getLocalVideoDuration(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String videoDuration = preferences.getString(context.getString(R.string.local_key_video_duration), context.getString(R.string.pref_default_local_video_duration));
        return Integer.parseInt(videoDuration);
    }

    public static int getLocalVideoDurationInMill(Context context) {
        int duration = getLocalVideoDuration(context);
        return duration * 60 * 1000;
    }

    public static boolean getLocalVibrateAndSound(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(context.getString(R.string.local_key_vibrate_and_sound), true);
    }

    public static boolean getLocalPostRecorder(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(context.getString(R.string.local_key_post_recorder), false);
    }

    public static int getLocalPostVideoDuration(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String videoDuration = preferences.getString(context.getString(R.string.local_key_post_video_duration), context.getString(R.string.pref_default_post_video_duration));
        return Integer.parseInt(videoDuration);
    }

    public static int getLocalPostVideoDurationInMill(Context context) {
        int duration = getLocalPostVideoDuration(context);
        return duration * 60 * 1000;
    }

    public static int getStreamingVideoSize(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String videoSize = preferences.getString(context.getString(R.string.streaming_key_video_size), context.getString(R.string.pref_default_streaming_video_size));
        return Integer.parseInt(videoSize);
    }

    public static int getStreamingFramerate(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String framerate = preferences.getString(context.getString(R.string.streaming_key_framerate), context.getString(R.string.pref_default_streaming_framerate));
        return Integer.parseInt(framerate);
    }

    public static boolean getStreamingVibrateAndSound(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(context.getString(R.string.streaming_key_vibrate_and_sound), false);
    }

    public static String getGeneralPassword(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.general_key_settings_password), context.getString(R.string.pref_default_settings_password));
    }

    public static int getIntervalLocation(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String interval = preferences.getString(context.getString(R.string.general_key_interval_location), context.getString(R.string.pref_default_interval_location));
        return Integer.parseInt(interval);
    }

    public static int getIntervalLocationInSos(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String interval = preferences.getString(context.getString(R.string.general_key_interval_location_sos), context.getString(R.string.pref_default_interval_location_sos));
        return Integer.parseInt(interval);
    }

    public static String getStreamHostAddress(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_host_address), context.getString(R.string.pref_default_host_address));
    }

    public static int getStreamPortNumber(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String portNumber = preferences.getString(context.getString(R.string.key_port_number), context.getString(R.string.pref_default_port_number));
        return Integer.parseInt(portNumber);
    }

    public static String getStreamAppName(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_stream_app_name), context.getString(R.string.pref_default_app_name));
    }

    public static String getStreamUsername(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_username), context.getString(R.string.pref_default_username));
    }

    public static String getStreamPassword(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_password), context.getString(R.string.pref_default_password));
    }
}