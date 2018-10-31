package com.bpt.tipi.streaming.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jpujolji on 10/03/18.
 */

public class LaserHelper {

    private static final int LED_STATE_ON = 3553280;
    private static final int VALUE_ON = 0x0404;
    private static final int VALUE_OFF = 0x0400;

    private static FileOutputStream openLed() {
        FileOutputStream mFileOutputStream = null;
        File device = new File("/dev/gpio_leds");
        try {
            mFileOutputStream = new FileOutputStream(device);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return mFileOutputStream;
    }

    private static FileInputStream openLed2() {
        FileInputStream fileInputStream = null;
        File device = new File("/dev/gpio_leds");
        try {
            fileInputStream = new FileInputStream(device);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fileInputStream;
    }

    public static void setLaserState() {
        FileOutputStream mFileOutputStream = openLed();
        int leds_state;
        if (getLedsState() == LED_STATE_ON) {
            leds_state = VALUE_OFF;
        } else {
            leds_state = VALUE_ON;
        }
        String s = Integer.toString(leds_state);
        byte[] buffer = s.getBytes();
        if (mFileOutputStream != null) {
            try {
                mFileOutputStream.write(buffer);
                mFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getLedsState() {
        byte[] buffer = new byte[16];
        int ret = 0;
        int leds_state = 0;
        FileInputStream mFileInputStream = openLed2();
        if (mFileInputStream == null) {
            return leds_state;
        }
        try {
            ret = mFileInputStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        leds_state = 0;
        if (ret < 4) {
            for (int i = 0; i < ret; i++) {
                leds_state |= buffer[i];
                leds_state = leds_state << 8;
            }
        } else {
            leds_state = (buffer[0] & 0xff) | ((buffer[1] << 8) & 0xff00) | ((buffer[2] << 24) >>> 8) | (buffer[3] << 24);
        }
        return leds_state;
    }
}
