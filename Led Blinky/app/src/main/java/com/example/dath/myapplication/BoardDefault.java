package com.example.dath.myapplication;

import android.os.Build;

public class BoardDefault {
    private static final String DEVICE_RP3 = "rpi3";
    public static String getGPIOForLED() {
        switch (Build.DEVICE){
            case DEVICE_RP3:
                return "BCM6";
            default:
                throw new IllegalStateException("Unknow Build.DEVICE "+ Build.DEVICE);
        }
    }
}
