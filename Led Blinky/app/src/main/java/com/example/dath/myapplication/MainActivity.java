package com.example.dath.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private static final int INTERVAL_BETWEEN_BLINK_MS = 1000;

    private Handler mHandler = new Handler();
    private Gpio mLedGpioRed;
    private Gpio mLedGpioGreen;
    private Gpio mLedGpioBlue;
    private boolean mStateRed = false;
    private boolean mStateGreen = false;
    private boolean mStateBlue = false;
    private boolean mLedState = false;
    private int count = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting BlinkActivity");
        try {
            String pinName = BoardDefault.getGPIOForLED();
            mLedGpioRed = PeripheralManager.getInstance().openGpio("BCM6");
            mLedGpioGreen = PeripheralManager.getInstance().openGpio("BCM17");
            mLedGpioBlue = PeripheralManager.getInstance().openGpio("BCM27");
            mLedGpioRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpioBlue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpioGreen.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            Log.i(TAG, "Start blinking LED GPIO pin");
            mHandler.post(mBlinkRunnable);
        }catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            mLedGpioRed.close();
            mLedGpioGreen.close();
            mLedGpioBlue.close();
        }catch (IOException e){
            Log.e(TAG, "Error on PeripheralIO API ", e);
        }finally {
            mLedGpioRed = null;
            mLedGpioBlue = null;
            mLedGpioGreen = null;
        }
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if(mLedGpioRed == null || mLedGpioBlue == null || mLedGpioBlue == null) {
                return;
            }
            try {
                count +=1;
                mStateRed = count % 2 == 1 ? true: false;
                mStateGreen = count/2 %2 == 1? true: false;
                mStateBlue = count/4 %2 ==1 ? true: false;
                mLedGpioRed.setValue(mStateRed);
                mLedGpioGreen.setValue(mStateGreen);
                mLedGpioBlue.setValue(mStateBlue);
                Log.d(TAG, "State set to "+ count);
                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINK_MS);
            }catch (IOException e){
                Log.e(TAG, "Error on PeripheralIO API "+ e);
            }
        }
    };
}
