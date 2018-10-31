package com.bpt.tipi.streaming.mqtt;

import android.content.Context;
import android.util.Log;

import com.bpt.tipi.streaming.ConfigHelper;
import com.bpt.tipi.streaming.ServiceHelper;
import com.bpt.tipi.streaming.model.MessageEvent;
import com.bpt.tipi.streaming.model.RemoteConfig;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

public class MqttCallbackHandler implements MqttCallback {

    private EventBus bus = EventBus.getDefault();

    private static final String TYPE_PARAMS = "PARAMS";
    private static final String TYPE_START_STREAMING = "START_STREAMING";
    private static final String TYPE_STOP_STREAMING = "STOP_STREAMING";
    private Context mContext;

    MqttCallbackHandler(Context context) {
        mContext = context;
    }

    @Override
    public void connectionLost(Throwable cause) {
        MessageEvent event = new MessageEvent(MessageEvent.RECONNECT);
        bus.post(event);
    }

    @Override
    public void messageArrived(String topic, final MqttMessage message) throws Exception {
        Log.i("Depuracion", "messageArrived " + message.toString());
        JSONObject jsonObject = new JSONObject(message.toString());
        MessageEvent event;
        switch (jsonObject.optString("type")) {
            case TYPE_PARAMS:
                Gson gson = new Gson();
                RemoteConfig remoteConfig = gson.fromJson(jsonObject.optString("body"), RemoteConfig.class);
                ConfigHelper.saveConfig(mContext, remoteConfig);
                ServiceHelper.stopLocationService(mContext);
                ServiceHelper.startLocationService(mContext);
                break;
            case TYPE_START_STREAMING:
                event = new MessageEvent(MessageEvent.START_STREAMING);
                bus.post(event);
                break;
            case TYPE_STOP_STREAMING:
                event = new MessageEvent(MessageEvent.STOP_STREAMING);
                bus.post(event);
                break;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
