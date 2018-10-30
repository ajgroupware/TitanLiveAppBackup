package com.bpt.tipi.streaming.helper;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class IrHelper {
    private static final String TAG = "LightsHelper";

    private static final String IR_NAME = "dev/aka";

    public static final int STATE_ON = 1;
    public static final int STATE_OFF = 0;

    private static FileOutputStream openIr(String file) {
        FileOutputStream mFileOutputStream = null;
        File device = new File(file);
        if (!device.exists()) {
            Log.e(TAG, "device /dev/aka is not exist \n");
        }
        if (!device.canRead() || !device.canWrite()) {
            Log.e(TAG, "device RW not ok \n");
            try {

            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }
        try {
            mFileOutputStream = new FileOutputStream(device);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return mFileOutputStream;
    }

    public static void setIrState(int val) {
        FileOutputStream mFileOutputStream = openIr(IR_NAME);
        int aka_state = val;

        int level = 2;
        val = (aka_state << 2) + (level & 0x0003);
        if (mFileOutputStream != null) {
            byte[] buffer = {'1'};
            buffer[0] = (byte) ('0' + val);
            try {
                mFileOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}