package com.bpt.tipi.streaming.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bpt.tipi.streaming.camera_state_machine.StateMachineHandler;
import com.bpt.tipi.streaming.helper.PreferencesHelper;
import com.bpt.tipi.streaming.helper.ServiceHelper;
import com.bpt.tipi.streaming.model.MessageEvent;
import com.bpt.tipi.streaming.model.RemoteConfig;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

public class MqttMessageService extends Service implements MqttCallbackExtended {
    private static final String TAG = "MqttMessageService";

    private static final String TYPE_START_STREAMING = "START_STREAMING";
    private static final String TYPE_STOP_STREAMING = "STOP_STREAMING";
    private static final String TYPE_PARAMS = "PARAMS";

    private MqttAsyncClient mqttAsyncClient;

    private Context context;

    private EventBus bus = EventBus.getDefault();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bus.register(this);
        Log.d(TAG, "onCreate");
        context = MqttMessageService.this;

        try {
            connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(MessageEvent event) {
        switch (event.key) {
            case MessageEvent.MQTT_PARAMETERS_CONFIGURED:
                try {
                    if (isConnected()) {
                        disconnect();
                    }
                    connect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;
            case MessageEvent.ID_DEVICE_CONFIGURED:
                try {
                    if (isConnected()) {
                        disconnect();
                    }
                    connect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "connectionLost ");
        try {
            mqttAsyncClient.reconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(TAG, "messageArrived " + message.toString());
        JSONObject jsonObject = new JSONObject(message.toString());
        MessageEvent event;
        switch (jsonObject.optString("type")) {
            case TYPE_START_STREAMING:
                event = new MessageEvent(MessageEvent.START_STREAMING);
                bus.post(event);
                break;
            case TYPE_STOP_STREAMING:
                event = new MessageEvent(MessageEvent.STOP_STREAMING);
                bus.post(event);
                break;
            case TYPE_PARAMS:
                Gson gson = new Gson();
                RemoteConfig remoteConfig = gson.fromJson(jsonObject.optString("body"), RemoteConfig.class);
                PreferencesHelper.saveConfig(context, remoteConfig);
                break;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(TAG, "deliveryComplete ");
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.d(TAG, "connectComplete ");
        try {
            mqttAsyncClient.subscribe(PreferencesHelper.getDeviceId(context), 0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connect() throws MqttException {
        mqttAsyncClient = new MqttAsyncClient(getUrl(), PreferencesHelper.getDeviceId(context), new MemoryPersistence());
        mqttAsyncClient.setCallback(this);
        IMqttToken token = mqttAsyncClient.connect(getMqttConnectionOption());
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                mqttAsyncClient.setBufferOpts(getDisconnectedBufferOptions());
                Log.d(TAG, "Success");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.d(TAG, "Failure " + exception.toString());
            }
        });
    }

    public void disconnect() throws MqttException {
        IMqttToken mqttToken = mqttAsyncClient.disconnect();
        mqttToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "Successfully disconnected");
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.d(TAG, "Failed to disconnected " + throwable.toString());
            }
        });
    }

    public boolean isConnected() {
        return mqttAsyncClient != null && mqttAsyncClient.isConnected();
    }

    public String getUrl() {
        return "tcp://" + PreferencesHelper.getURLMqtt(context) +
                ":" + PreferencesHelper.getPortMqtt(context);
    }

    @NonNull
    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(false);
        mqttConnectOptions.setUserName(PreferencesHelper.getUsernameMqtt(context));
        mqttConnectOptions.setPassword(PreferencesHelper.getPasswordMqtt(context).toCharArray());
        return mqttConnectOptions;
    }

    @NonNull
    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(false);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(true);
        return disconnectedBufferOptions;
    }
}