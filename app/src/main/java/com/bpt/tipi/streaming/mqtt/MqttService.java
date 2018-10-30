package com.bpt.tipi.streaming.mqtt;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.bpt.tipi.streaming.ConfigHelper;
import com.bpt.tipi.streaming.UnCaughtException;
import com.bpt.tipi.streaming.model.MessageEvent;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MqttService extends Service {

    private static final String URL = "tcp://10.50.4.3:1883"; //Tigo Avantel
    //private static final String URL = "tcp://35.165.11.242:1883";
    //private static final String URL = "tcp://10.80.63.236:1883"; //Movistar
    //private static final String URL = "tcp://10.126.0.229:1883";

    private static final String MQTT_THREAD_NAME = "MqttService";

    private Handler mConnHandler;
    private MqttClient mqttClient;

    private EventBus bus = EventBus.getDefault();
    CounterConnection counterConnection;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(this));
        HandlerThread thread = new HandlerThread(MQTT_THREAD_NAME);
        thread.start();
        mConnHandler = new Handler(thread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bus.register(this);
        connect();
        counterConnection = new CounterConnection(30000, 1000);
        counterConnection.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        bus.unregister(this);
        counterConnection.cancel();
        mConnHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mqttClient.disconnect(0);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Realiza la conexión al server MQTT.
     */
    private synchronized void connect() {
        mConnHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isConnected()) {
                    try {
                        String idDevice = ConfigHelper.getDeviceName(MqttService.this);
                        mqttClient = new MqttClient(URL, idDevice, new MemoryPersistence());
                        mqttClient.setCallback(new MqttCallbackHandler(MqttService.this));

                        MqttConnectOptions options = new MqttConnectOptions();
                        options.setCleanSession(false);
                        //options.setUserName("tipi");
                        //options.setUserName("mqadmin");
                        //options.setPassword("Br0k3rM4gmnt".toCharArray());
                        options.setUserName("titanlive");
                        options.setPassword("T1t4nL1v3".toCharArray());
                        IMqttToken conToken = mqttClient.connectWithResult(options);
                        conToken.waitForCompletion();

                        mqttClient.subscribe(idDevice);
                        Log.i("Depuracion", "IsConnected " + mqttClient.isConnected());
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    /**
     * Método para capturar los eventos registrados en EventBus
     *
     * @param event evento que llega.
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(final MessageEvent event) {
        switch (event.key) {
            case MessageEvent.RECONNECT:
                if (!isConnected()) {
                    connect();
                }
                break;
            case MessageEvent.ID_DEVICE_CONFIGURED:
                mConnHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mqttClient.disconnect(0);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                });
                connect();
                break;
        }
    }

    private class CounterConnection extends CountDownTimer {

        long mMillisInFuture;

        CounterConnection(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            mMillisInFuture = millisInFuture;
        }

        @Override
        public void onFinish() {
            if (!isConnected()) {
                connect();
            }
            counterConnection.start();
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }
    }
}
