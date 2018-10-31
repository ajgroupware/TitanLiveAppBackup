package com.bpt.tipi.streaming.model;

public class Device {

    String deviceName, status;
    Double lat, lng;
    int batteryLevel;

    public Device(String mDeviceName, String mStatus, Double mLat, Double mLng,int mBatteryLevel) {
        deviceName = mDeviceName;
        status = mStatus;
        lat = mLat;
        lng = mLng;
        batteryLevel = mBatteryLevel;
    }
}