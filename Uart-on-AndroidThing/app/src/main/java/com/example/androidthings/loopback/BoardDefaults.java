/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.loopback;

import android.os.Build;
@SuppressWarnings("WeakerAccess")
public class BoardDefaults {
    private static final String DEVICE_RPI3 = "rpi3";
    private static final String DEVICE_IMX6UL_PICO = "imx6ul_pico";
    private static final String DEVICE_IMX7D_PICO = "imx7d_pico";

    /**
     * Return the UART for loopback.
     */
    public static String getGPIOForLED() {
        switch (Build.DEVICE){
            case DEVICE_RPI3:
                return "BCM6";
            default:
                throw new IllegalStateException("Unknow Build.DEVICE "+ Build.DEVICE);
        }
    }
    public static String getUartName() {
        switch (Build.DEVICE) {
            case DEVICE_RPI3:
                return "UART0";
            case DEVICE_IMX6UL_PICO:
                return "UART3";
            case DEVICE_IMX7D_PICO:
                return "UART6";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }
}
