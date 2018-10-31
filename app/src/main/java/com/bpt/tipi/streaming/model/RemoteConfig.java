package com.bpt.tipi.streaming.model;

/**
 * Created by jpujolji on 7/03/18.
 */

public class RemoteConfig {
    public String deviceName;
    public String masterPwd;
    public int locationTimeInterval;
    public int sosTimeInterval;
    public int localFrameRate;
    public int localVideoDurationMin;
    public int postRecordingTimeMin;
    public int streamingFrameRate;
    public int streamingVideoSize;
    public int localVideoSize;
    public boolean vibrateAndSoundOnRecord;
    public boolean postRecordingEnabled;
    public boolean vibrateAndSoundOnStreaming;
    public String mqttUrl;
    public int mqttPort;
    public String mqttUsr;
    public String mqttPwd;
    public String apiUrl;
    public String evidenciasApi;

    public RemoteConfig(String deviceName, String masterPwd, int locationTimeInterval, int sosTimeInterval, int localFrameRate, int localVideoDurationMin, int postRecordingTimeMin, int streamingFrameRate, int streamingVideoSize, int localVideoSize, boolean vibrateAndSoundOnRecord, boolean postRecordingEnabled, boolean vibrateAndSoundOnStreaming, String mqttUrl, int mqttPort, String mqttUsr, String mqttPwd, String apiUrl, String evidenciasApi, GeneralParameter generalParameter) {
        this.deviceName = deviceName;
        this.masterPwd = masterPwd;
        this.locationTimeInterval = locationTimeInterval;
        this.sosTimeInterval = sosTimeInterval;
        this.localFrameRate = localFrameRate;
        this.localVideoDurationMin = localVideoDurationMin;
        this.postRecordingTimeMin = postRecordingTimeMin;
        this.streamingFrameRate = streamingFrameRate;
        this.streamingVideoSize = streamingVideoSize;
        this.localVideoSize = localVideoSize;
        this.vibrateAndSoundOnRecord = vibrateAndSoundOnRecord;
        this.postRecordingEnabled = postRecordingEnabled;
        this.vibrateAndSoundOnStreaming = vibrateAndSoundOnStreaming;
        this.mqttUrl = mqttUrl;
        this.mqttPort = mqttPort;
        this.mqttUsr = mqttUsr;
        this.mqttPwd = mqttPwd;
        this.apiUrl = apiUrl;
        this.evidenciasApi = evidenciasApi;
        this.generalParameter = generalParameter;
    }

    public RemoteConfig(String deviceName, String masterPwd, int locationTimeInterval, int sosTimeInterval, int localFrameRate, int localVideoDurationMin, int postRecordingTimeMin, int streamingFrameRate, int streamingVideoSize, int localVideoSize, boolean vibrateAndSoundOnRecord, boolean postRecordingEnabled, boolean vibrateAndSoundOnStreaming, GeneralParameter generalParameter) {
        this.deviceName = deviceName;
        this.masterPwd = masterPwd;
        this.locationTimeInterval = locationTimeInterval;
        this.sosTimeInterval = sosTimeInterval;
        this.localFrameRate = localFrameRate;
        this.localVideoDurationMin = localVideoDurationMin;
        this.postRecordingTimeMin = postRecordingTimeMin;
        this.streamingFrameRate = streamingFrameRate;
        this.streamingVideoSize = streamingVideoSize;
        this.localVideoSize = localVideoSize;
        this.vibrateAndSoundOnRecord = vibrateAndSoundOnRecord;
        this.postRecordingEnabled = postRecordingEnabled;
        this.vibrateAndSoundOnStreaming = vibrateAndSoundOnStreaming;
        this.generalParameter = generalParameter;
    }

    public GeneralParameter generalParameter;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMasterPwd() {
        return masterPwd;
    }

    public void setMasterPwd(String masterPwd) {
        this.masterPwd = masterPwd;
    }

    public int getLocationTimeInterval() {
        return locationTimeInterval;
    }

    public void setLocationTimeInterval(int locationTimeInterval) {
        this.locationTimeInterval = locationTimeInterval;
    }

    public int getSosTimeInterval() {
        return sosTimeInterval;
    }

    public void setSosTimeInterval(int sosTimeInterval) {
        this.sosTimeInterval = sosTimeInterval;
    }

    public int getLocalFrameRate() {
        return localFrameRate;
    }

    public void setLocalFrameRate(int localFrameRate) {
        this.localFrameRate = localFrameRate;
    }

    public int getLocalVideoDurationMin() {
        return localVideoDurationMin;
    }

    public void setLocalVideoDurationMin(int localVideoDurationMin) {
        this.localVideoDurationMin = localVideoDurationMin;
    }

    public int getPostRecordingTimeMin() {
        return postRecordingTimeMin;
    }

    public void setPostRecordingTimeMin(int postRecordingTimeMin) {
        this.postRecordingTimeMin = postRecordingTimeMin;
    }

    public int getStreamingFrameRate() {
        return streamingFrameRate;
    }

    public void setStreamingFrameRate(int streamingFrameRate) {
        this.streamingFrameRate = streamingFrameRate;
    }

    public int getStreamingVideoSize() {
        return streamingVideoSize;
    }

    public void setStreamingVideoSize(int streamingVideoSize) {
        this.streamingVideoSize = streamingVideoSize;
    }

    public int getLocalVideoSize() {
        return localVideoSize;
    }

    public void setLocalVideoSize(int localVideoSize) {
        this.localVideoSize = localVideoSize;
    }

    public boolean isVibrateAndSoundOnRecord() {
        return vibrateAndSoundOnRecord;
    }

    public void setVibrateAndSoundOnRecord(boolean vibrateAndSoundOnRecord) {
        this.vibrateAndSoundOnRecord = vibrateAndSoundOnRecord;
    }

    public boolean isPostRecordingEnabled() {
        return postRecordingEnabled;
    }

    public void setPostRecordingEnabled(boolean postRecordingEnabled) {
        this.postRecordingEnabled = postRecordingEnabled;
    }

    public boolean isVibrateAndSoundOnStreaming() {
        return vibrateAndSoundOnStreaming;
    }

    public void setVibrateAndSoundOnStreaming(boolean vibrateAndSoundOnStreaming) {
        this.vibrateAndSoundOnStreaming = vibrateAndSoundOnStreaming;
    }

    public String getMqttUrl() {
        return mqttUrl;
    }

    public void setMqttUrl(String mqttUrl) {
        this.mqttUrl = mqttUrl;
    }

    public int getMqttPort() {
        return mqttPort;
    }

    public void setMqttPort(int mqttPort) {
        this.mqttPort = mqttPort;
    }

    public String getMqttUsr() {
        return mqttUsr;
    }

    public void setMqttUsr(String mqttUsr) {
        this.mqttUsr = mqttUsr;
    }

    public String getMqttPwd() {
        return mqttPwd;
    }

    public void setMqttPwd(String mqttPwd) {
        this.mqttPwd = mqttPwd;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getEvidenciasApi() {
        return evidenciasApi;
    }

    public void setEvidenciasApi(String evidenciasApi) {
        this.evidenciasApi = evidenciasApi;
    }

    public GeneralParameter getGeneralParameter() {
        return generalParameter;
    }

    public void setGeneralParameter(GeneralParameter generalParameter) {
        this.generalParameter = generalParameter;
    }
}
