package com.bpt.tipi.streaming.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.widget.Toast;

import com.bpt.tipi.streaming.ServiceHelper;
import com.bpt.tipi.streaming.helper.IrHelper;
import com.bpt.tipi.streaming.model.MessageEvent;
import com.bumptech.glide.util.Util;

import org.greenrobot.eventbus.EventBus;

import javax.xml.datatype.Duration;

public class USBConnectionReceiver extends BroadcastReceiver {

    public static boolean firstConnect = true;
    public static final String usbStateChangeAction = "android.hardware.usb.action.USB_STATE";

    EventBus bus = EventBus.getDefault();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (firstConnect) {
            firstConnect = false;
            new CountDownTimer(1000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    String action = intent.getAction();
                    if (action.equalsIgnoreCase(usbStateChangeAction)) { //Check if change in USB state
                        if (intent.getExtras().getBoolean("connected")) {
                            // USB was connected
                            //bus.post(new MessageEvent(MessageEvent.FINISH_SERVICES));
                            ServiceHelper.stopAllServices(context);
                            IrHelper.setIrState(IrHelper.STATE_OFF);
                        } else {
                            // USB was disconnected
                            ServiceHelper.startAllServices(context);
                            //bus.post(new MessageEvent(MessageEvent.START_SERVICES));
                        }
                    }
                    firstConnect = true;
                }
            }.start();
        }
    }
}
