package com.bpt.tipi.streaming.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.widget.Toast;

import com.bpt.tipi.streaming.ServiceHelper;
import com.bpt.tipi.streaming.helper.IrHelper;
import com.bpt.tipi.streaming.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;

public class USBConnectionReceiver extends BroadcastReceiver {

    public static boolean firstConnect = true;
    public static final String usbStateChangeAction = "android.hardware.usb.action.USB_STATE";

    EventBus bus = EventBus.getDefault();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (firstConnect) {
            firstConnect = false;
            new CountDownTimer(10000, 10000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    String action = intent.getAction();

                    Toast.makeText(context, ".:USB:.", Toast.LENGTH_SHORT).show(); //Debug

                    /*
                    StringBuilder builder = new StringBuilder("Extras:\n");
                    for (String key : intent.getExtras().keySet()) { //extras is the Bundle containing info
                        Object value = intent.getExtras().get(key); //get the current object
                        builder.append(key).append(": ").append(value).append("\n"); //add the key-value pair to the
                    }
                    Toast.makeText(context, builder.toString(), Toast.LENGTH_LONG).show(); //Debug
                    */

                    if (action.equalsIgnoreCase(usbStateChangeAction)) { //Check if change in USB state
                        if (intent.getExtras().getBoolean("connected")) {
                            // USB was connected
                            //bus.post(new MessageEvent(MessageEvent.FINISH_SERVICES));
                            ServiceHelper.stopAllServices(context);
                            IrHelper.setIrState(IrHelper.STATE_OFF);
                            Toast.makeText(context, ".:Se detienen los servicios:.", Toast.LENGTH_SHORT).show();
                        } else {
                            // USB was disconnected
                            ServiceHelper.startAllServices(context);
                            //bus.post(new MessageEvent(MessageEvent.START_SERVICES));
                            Toast.makeText(context, ".:Se inician los servicios:.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    firstConnect = true;
                }
            }.start();
        }
    }
}
