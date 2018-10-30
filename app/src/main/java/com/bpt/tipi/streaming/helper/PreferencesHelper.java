package com.bpt.tipi.streaming.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.model.GeneralParameter;
import com.bpt.tipi.streaming.model.RemoteConfig;

public class PreferencesHelper {

    /**
     * Método para consultar el ID del dispositivo.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return ID del dispositivo.
     */
    public static String getDeviceId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_device_id), context.getString(R.string.default_device_id));
    }

    /**
     * Método para configurar el ID del dispositivo.
     *
     * @param context  Contexto para inicializar las preferencia.
     * @param idDevice ID del dispositivo.
     */
    public static void setDeviceId(Context context, String idDevice) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_device_id), idDevice);
        editor.apply();
    }

    /**
     * Método para consultar la contraseña de ingreso al módulo de configuración.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Contraseña de acceso.
     */
    public static String getPasswordForSettings(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_password_for_settings), context.getString(R.string.default_password_for_settings));
    }

    /**
     * Método para configurar contraseña de ingreso al módulo de configuración.
     *
     * @param context  Contexto para inicializar las preferencia.
     * @param password Contraseña de acceso.
     */
    public static void setPasswordForSettings(Context context, String password) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_password_for_settings), password);
        editor.apply();
    }

    /**
     * Método para consultar la URL del servidor MQTT.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return url del servidor MQTT.
     */
    public static String getURLMqtt(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_url_mqtt), context.getString(R.string.default_url_mqtt));
    }

    /**
     * Método para configurar la URL del servidor MQTT.
     *
     * @param context Contexto para inicializar las preferencia.
     * @param url     Url del servidor MQTT.
     */
    public static void setUrlMqtt(Context context, String url) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_url_mqtt), url);
        editor.apply();
    }

    /**
     * Método para consultar el puerto del servidor MQTT.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Puerto del servidor MQTT.
     */
    public static String getPortMqtt(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_port_mqtt), context.getString(R.string.default_port_mqtt));
    }

    /**
     * Método para configurar el puerto del servidor MQTT.
     *
     * @param context Contexto para inicializar las preferencia.
     * @param port    Puerto del servidor MQTT.
     */
    public static void setPortMqtt(Context context, String port) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_port_mqtt), port);
        editor.apply();
    }

    /**
     * Método para consultar el usuario del servidor MQTT.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Usuario del servidor MQTT.
     */
    public static String getUsernameMqtt(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_username_mqtt), context.getString(R.string.default_username_mqtt));
    }

    /**
     * Método para configurar el usuario del servidor MQTT.
     *
     * @param context  Contexto para inicializar las preferencia.
     * @param username Usuario del servidor MQTT.
     */
    public static void setUsernameMqtt(Context context, String username) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_username_mqtt), username);
        editor.apply();
    }

    /**
     * Método para consultar la contraseña del servidor MQTT.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Contraseña del servidor MQTT.
     */
    public static String getPasswordMqtt(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_password_mqtt), context.getString(R.string.default_password_mqtt));
    }

    /**
     * Método para configurar la contraseña del servidor MQTT.
     *
     * @param context  Contexto para inicializar las preferencia.
     * @param password Contraseña del servidor MQTT.
     */
    public static void setPasswordMqtt(Context context, String password) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_password_mqtt), password);
        editor.apply();
    }

    /**
     * Método para consultar la URL del servidor Titan.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Url del servidor Titan.
     */
    public static String getUrlTitan(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_url_titan), context.getString(R.string.default_url_titan));
    }

    /**
     * Método para configurar la URL del servidor Titan.
     *
     * @param context Contexto para inicializar las preferencia.
     * @param url     Url del servidor Titan.
     */
    public static void setUrlTitan(Context context, String url) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_url_titan), url);
        editor.apply();
    }

    /**
     * Método para consultar el tamaño del video de streaming.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return tamaño video de streaming.
     */
    public static int getStreamingVideoSize(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String videoSize = preferences.getString(context.getString(R.string.key_streaming_video_size), context.getString(R.string.default_streaming_video_size));
        return Integer.parseInt(videoSize);
    }

    /**
     * Método para configurar el tamaño del video de streaming.
     *
     * @param context Contexto para inicializar las preferencia.
     * @param size    Tamaño del video.
     */
    public static void setStreamingVideoSize(Context context, String size) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_streaming_video_size), size);
        editor.apply();
    }

    /**
     * Método para consultar el framerate del video de streaming.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Framerate del video de streaming.
     */
    public static int getStreamingFramerate(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String framerate = preferences.getString(context.getString(R.string.key_streaming_framerate), context.getString(R.string.default_streaming_framerate));
        return Integer.parseInt(framerate);
    }

    /**
     * Método para configurar el framerate del video de streaming.
     *
     * @param context   Contexto para inicializar las preferencia.
     * @param framerate Framerate del video de streaming.
     */
    public static void setStreamingFramerate(Context context, String framerate) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_streaming_framerate), framerate);
        editor.apply();
    }

    /**
     * Método para consultar si se emite un sonido y vibra al momento de realizar streaming.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Si se emite el sonido o no.
     */
    public static boolean getStreamingVibrateAndSound(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(context.getString(R.string.key_streaming_vibrate_and_sound), false);
    }

    /**
     * Método para configurar si se emite un sonido y vibra al momento de realizar streaming.
     *
     * @param context   Contexto para inicializar las preferencia.
     * @param emitSound Valor si se emite el sonido o no.
     */
    public static void setStreamingVibrateAndSound(Context context, boolean emitSound) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(R.string.key_streaming_vibrate_and_sound), emitSound);
        editor.apply();
    }

    /**
     * Método para consultar el tamaño del video local.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Tamaño del video.
     */
    public static int getLocalVideoSize(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String videoSize = preferences.getString(context.getString(R.string.key_local_video_size), context.getString(R.string.default_local_video_size));
        return Integer.parseInt(videoSize);
    }

    /**
     * Método para configurar el tamaño del video local.
     *
     * @param context Contexto para inicializar las preferencia.
     * @param size    Tamaño del video.
     */
    public static void setLocalVideoSize(Context context, String size) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_local_video_size), size);
        editor.apply();
    }

    /**
     * Método para consultar el framerate del video local.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Framerate del video local.
     */
    public static int getLocalFramerate(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String framerate = preferences.getString(context.getString(R.string.key_local_framerate), context.getString(R.string.default_local_framerate));
        return Integer.parseInt(framerate);
    }

    /**
     * Método para configurar el framerate del video local.
     *
     * @param context   Contexto para inicializar las preferencia.
     * @param framerate Framerate del video local.
     */
    public static void setLocalFramerate(Context context, String framerate) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_local_framerate), framerate);
        editor.apply();
    }

    /**
     * Método para consultar la duración del video local.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Duración del video local.
     */
    public static int getLocalVideoDuration(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String videoDuration = preferences.getString(context.getString(R.string.key_local_video_duration), context.getString(R.string.default_local_video_duration));
        return Integer.parseInt(videoDuration);
    }

    /**
     * Método para configurar la duración del video local.
     *
     * @param context  Contexto para inicializar las preferencia.
     * @param duration Duración del video local.
     */
    public static void setLocalVideoDuration(Context context, String duration) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_local_video_duration), duration);
        editor.apply();
    }

    /**
     * Método para consultar si se emite un sonido y vibra al momento de realizar grabación local.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Si se emite el sonido o no.
     */
    public static boolean getLocalVibrateAndSound(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(context.getString(R.string.key_local_vibrate_and_sound), true);
    }

    /**
     * Método para configurar si se emite un sonido y vibra al momento de realizar grabación local.
     *
     * @param context   Contexto para inicializar las preferencia.
     * @param emitSound Valor si se emite el sonido o no.
     */
    public static void setLocalVibrateAndSound(Context context, boolean emitSound) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(R.string.key_local_vibrate_and_sound), emitSound);
        editor.apply();
    }

    /**
     * Método para consultar si se realiza post-grabado.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Si esta habilitado el post-grabado o no.
     */
    public static boolean getLocalPostRecord(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(context.getString(R.string.key_post_record), false);
    }

    /**
     * Método para configurar si se realiza post-grabado.
     *
     * @param context    Contexto para inicializar las preferencia.
     * @param postRecord Valor si se realiza post-grabado.
     */
    public static void setLocalPostRecord(Context context, boolean postRecord) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(R.string.key_post_record), postRecord);
        editor.apply();
    }

    /**
     * Método para consultar la duración del video de post-grabado.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Duración del video local.
     */
    public static int getPostVideoDuration(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String videoDuration = preferences.getString(context.getString(R.string.key_post_video_duration), context.getString(R.string.default_post_video_duration));
        return Integer.parseInt(videoDuration);
    }

    /**
     * Método para configurar la duración del video de post-grabado.
     *
     * @param context  Contexto para inicializar las preferencia.
     * @param duration Duración del video de post-grabado.
     */
    public static void setPostVideoDuration(Context context, String duration) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_post_video_duration), duration);
        editor.apply();
    }

    /**
     * Método para consultar la URL del servidor de aplicaciones.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return URL del servidor.
     */
    public static String getUrlApi(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_url_api), context.getString(R.string.default_url_api));
    }

    /**
     * Método para configurar la URL del servidor de aplicaciones.
     *
     * @param context Contexto para inicializar las preferencia.
     * @param url     URL del servidor de aplicaciones.
     */
    public static void setUrlApi(Context context, String url) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_url_api), url);
        editor.apply();
    }

    /**
     * Método para consultar el intervalo de tiempo de envío de la ubicación.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Intervalo de tiempo.
     */
    public static int getIntervalLocation(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String interval = preferences.getString(context.getString(R.string.key_interval_location), context.getString(R.string.default_interval_location));
        return Integer.parseInt(interval);
    }

    /**
     * Método para configurar el intervalo de tiempo de envío de la ubicación.
     *
     * @param context  Contexto para inicializar las preferencia.
     * @param interval Intervalo de tiempo.
     */
    public static void setIntervalLocation(Context context, String interval) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_interval_location), interval);
        editor.apply();
    }

    /**
     * Método para consultar el intervalo de tiempo de envío de la ubicación en SOS.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return intervalo de tiempo.
     */
    public static int getIntervalLocationInSos(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String interval = preferences.getString(context.getString(R.string.key_interval_location_sos), context.getString(R.string.default_interval_location_sos));
        return Integer.parseInt(interval);
    }

    /**
     * Método para configurar el intervalo de tiempo de envío de la ubicación cuando está en SOS.
     *
     * @param context  Contexto para inicializar las preferencia.
     * @param interval Intervalo de tiempo.
     */
    public static void setIntervalLocationInSos(Context context, String interval) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_interval_location_sos), interval);
        editor.apply();
    }

    /**
     * Método para consultar la URL del servidor de streaming.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return URL del servidor.
     */
    public static String getUrlStreaming(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_url_streaming), context.getString(R.string.default_url_streaming));
    }

    public static void setUrlStreaming(Context context, String url) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_url_streaming), url);
        editor.apply();
    }

    /**
     * Método para consultar el puerto del servidor de streaming.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Puerto del servidor.
     */
    public static String getPortStreaming(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_port_streaming), context.getString(R.string.default_port_streaming));
    }

    public static void setPortStreaming(Context context, String port) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_port_streaming), port);
        editor.apply();
    }

    /**
     * Método para consultar el nombre de la aplicación del servidor de streaming.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Nombre de la aplicación.
     */
    public static String getAppNameStreaming(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_app_name_streaming), context.getString(R.string.default_app_name_streaming));
    }

    public static void setAppNameStreaming(Context context, String appName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_app_name_streaming), appName);
        editor.apply();
    }

    /**
     * Método para consultar el usuario para la autenticación al servidor de streaming.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Usuario.
     */
    public static String getUsernameStreaming(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_username_streaming), context.getString(R.string.default_username_streaming));
    }

    public static void setUsernameStreaming(Context context, String username) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_username_streaming), username);
        editor.apply();
    }

    /**
     * Método para consultar la contraseña para la autenticación al servidor de streaming.
     *
     * @param context Contexto para inicializar las preferencia.
     * @return Contraseña.
     */
    public static String getPasswordStreaming(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_password_streaming), context.getString(R.string.default_password_streaming));
    }

    public static void setPasswordStreaming(Context context, String password) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_password_streaming), password);
        editor.apply();
    }

    public static String getLoggedUser(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.key_logged_user), "");
    }

    public static void setLoggedUser(Context context, String user) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.key_logged_user), user);
        editor.apply();
    }

    public static void saveConfig(Context context, RemoteConfig remoteConfig) {
        ServiceHelper.stopAllServices(context);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(context.getString(R.string.key_device_id), remoteConfig.getDeviceName());
        editor.putString(context.getString(R.string.key_password_for_settings), remoteConfig.getMasterPwd());
        editor.putString(context.getString(R.string.key_interval_location), "" + remoteConfig.getLocationTimeInterval());
        editor.putString(context.getString(R.string.key_interval_location_sos), "" + remoteConfig.getSosTimeInterval());
        editor.putString(context.getString(R.string.key_local_video_size), "" + remoteConfig.getLocalVideoSize());
        editor.putString(context.getString(R.string.key_local_framerate), "" + remoteConfig.getLocalFrameRate());
        editor.putString(context.getString(R.string.key_local_video_duration), "" + remoteConfig.getLocalVideoDurationMin());
        editor.putBoolean(context.getString(R.string.key_local_vibrate_and_sound), remoteConfig.isVibrateAndSoundOnRecord());
        editor.putBoolean(context.getString(R.string.key_post_record), remoteConfig.isPostRecordingEnabled());
        editor.putString(context.getString(R.string.key_post_video_duration), "" + remoteConfig.getPostRecordingTimeMin());
        editor.putString(context.getString(R.string.key_streaming_video_size), "" + remoteConfig.getStreamingVideoSize());
        editor.putString(context.getString(R.string.key_streaming_framerate), "" + remoteConfig.getStreamingFrameRate());
        editor.putBoolean(context.getString(R.string.key_streaming_vibrate_and_sound), remoteConfig.isVibrateAndSoundOnStreaming());
        editor.putString(context.getString(R.string.key_url_streaming), remoteConfig.getGeneralParameter().getWowzaServerUrl());
        editor.putString(context.getString(R.string.key_port_streaming), "" + remoteConfig.getGeneralParameter().getWowzaPort());
        editor.putString(context.getString(R.string.key_app_name_streaming), remoteConfig.getGeneralParameter().getWowzaAppName());
        editor.putString(context.getString(R.string.key_username_streaming), remoteConfig.getGeneralParameter().getWowzaUser());
        editor.putString(context.getString(R.string.key_password_streaming), remoteConfig.getGeneralParameter().getWowzaPwd());
        editor.putString(context.getString(R.string.key_url_mqtt), remoteConfig.getMqttUrl());
        editor.putString(context.getString(R.string.key_port_mqtt), "" + remoteConfig.getMqttPort());
        editor.putString(context.getString(R.string.key_username_mqtt), remoteConfig.getMqttUsr());
        editor.putString(context.getString(R.string.key_password_mqtt), remoteConfig.getMqttPwd());
        editor.putString(context.getString(R.string.key_url_api), remoteConfig.getApiUrl());
        editor.putString(context.getString(R.string.key_url_titan), remoteConfig.getEvidenciasApi());
        editor.apply();

        ServiceHelper.startAllServices(context);
    }

    public static RemoteConfig getConfig(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.getString(context.getString(R.string.key_url_streaming), context.getString(R.string.default_url_streaming));

        String urlStreaming = preferences.getString(context.getString(R.string.key_url_streaming), context.getString(R.string.default_url_streaming));
        String portStreaming = preferences.getString(context.getString(R.string.key_port_streaming), context.getString(R.string.default_port_streaming));
        String appNameStreaming = preferences.getString(context.getString(R.string.key_app_name_streaming), context.getString(R.string.default_app_name_streaming));
        String usernameStreaming = preferences.getString(context.getString(R.string.key_username_streaming), context.getString(R.string.default_username_streaming));
        String passwordStreaming = preferences.getString(context.getString(R.string.key_password_streaming), context.getString(R.string.default_password_streaming));

        String deviceId = preferences.getString(context.getString(R.string.key_device_id), context.getString(R.string.default_device_id));
        String passForSettings = preferences.getString(context.getString(R.string.key_password_for_settings), context.getString(R.string.default_password_for_settings));
        String urlMqtt = preferences.getString(context.getString(R.string.key_url_mqtt), context.getString(R.string.default_url_mqtt));
        String portMqtt = preferences.getString(context.getString(R.string.key_port_mqtt), context.getString(R.string.default_port_mqtt));
        String usernameMqtt = preferences.getString(context.getString(R.string.key_username_mqtt), context.getString(R.string.default_username_mqtt));
        String passwordMqtt = preferences.getString(context.getString(R.string.key_password_mqtt), context.getString(R.string.default_password_mqtt));
        String urlTitan = preferences.getString(context.getString(R.string.key_url_titan), context.getString(R.string.default_url_titan));
        String streamingVideoSize = preferences.getString(context.getString(R.string.key_streaming_video_size), context.getString(R.string.default_streaming_video_size));
        String framerate = preferences.getString(context.getString(R.string.key_streaming_framerate), context.getString(R.string.default_streaming_framerate));
        boolean streamingVibrateSound = preferences.getBoolean(context.getString(R.string.key_streaming_vibrate_and_sound), false);
        String videoSize = preferences.getString(context.getString(R.string.key_local_video_size), context.getString(R.string.default_local_video_size));
        String localFramerate = preferences.getString(context.getString(R.string.key_local_framerate), context.getString(R.string.default_local_framerate));
        String videoDuration = preferences.getString(context.getString(R.string.key_local_video_duration), context.getString(R.string.default_local_video_duration));
        boolean localVibrateSound = preferences.getBoolean(context.getString(R.string.key_local_vibrate_and_sound), true);
        boolean postRecord = preferences.getBoolean(context.getString(R.string.key_post_record), false);
        String postVideoDuration = preferences.getString(context.getString(R.string.key_post_video_duration), context.getString(R.string.default_post_video_duration));
        String urlApi = preferences.getString(context.getString(R.string.key_url_api), context.getString(R.string.default_url_api));
        String intervalLocation = preferences.getString(context.getString(R.string.key_interval_location), context.getString(R.string.default_interval_location));
        String intervalLocationSos = preferences.getString(context.getString(R.string.key_interval_location_sos), context.getString(R.string.default_interval_location_sos));

        GeneralParameter generalParameter = new GeneralParameter(urlStreaming, appNameStreaming, usernameStreaming, passwordStreaming, Integer.parseInt(portStreaming));

        RemoteConfig remoteConfig = new RemoteConfig(deviceId, passForSettings, Integer.parseInt(intervalLocation),
                Integer.parseInt(intervalLocationSos), Integer.parseInt(localFramerate), Integer.parseInt(videoDuration),
                Integer.parseInt(postVideoDuration), Integer.parseInt(framerate), Integer.parseInt(streamingVideoSize),
                Integer.parseInt(videoSize), localVibrateSound, postRecord, streamingVibrateSound, urlMqtt, Integer.parseInt(portMqtt),
                usernameMqtt, passwordMqtt, urlApi, urlTitan, generalParameter);
        return remoteConfig;
    }
}
