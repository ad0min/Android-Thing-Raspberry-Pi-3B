package com.example.dath.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
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

    private Handler mHandler = new Handler();
    private Gpio mLedGpioRed;
    private Gpio mLedGpioGreen;
    private Gpio mLedGpioBlue;
    private Gpio mButton;
    private boolean mStateRed = false;
    private boolean mStateGreen = false;
    private boolean mStateBlue = false;
    private boolean mLedState = false;
    private int count = 1;
    private int timer = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting BlinkActivity");
        try {
            String pinName = BoardDefault.getGPIOForLED();
            mLedGpioRed = PeripheralManager.getInstance().openGpio("BCM6");
            mLedGpioGreen = PeripheralManager.getInstance().openGpio("BCM17");
            mLedGpioBlue = PeripheralManager.getInstance().openGpio("BCM27");
            mButton = PeripheralManager.getInstance().openGpio("BCM16");

            mLedGpioRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpioBlue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpioGreen.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mButton.setDirection(Gpio.DIRECTION_IN);
            mButton.setActiveType(Gpio.ACTIVE_HIGH);
            mButton.setEdgeTriggerType(Gpio.EDGE_BOTH);

            mButton.registerGpioCallback(mGpioCallback);
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
            mButton.unregisterGpioCallback(mGpioCallback);
            mButton.close();
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
            if(mLedGpioRed == null || mLedGpioBlue == null || mLedGpioBlue == null || mButton == null) {
                return;
            }
            try {
                count +=1;
                mStateRed = count % 2 == 1;
                mStateGreen = count/2 %2 == 1;
                mStateBlue = count/4 %2 ==1;
                mLedGpioRed.setValue(mStateRed);
                mLedGpioGreen.setValue(mStateGreen);
                mLedGpioBlue.setValue(mStateBlue);
                Log.d(TAG, "State set to "+ count);
                mHandler.postDelayed(mBlinkRunnable, timer);
            }catch (IOException e){
                Log.e(TAG, "Error on PeripheralIO API "+ e);
            }
        }
    };
    private GpioCallback mGpioCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                Log.d(TAG, gpio.getValue()+ " pins ");
                if (gpio.getValue()) {
                    switch (timer){
                        case 2000:
                            timer = 1000;
                            break;
                        case 1000:
                            timer = 500;
                            break;
                        case 500:
                            timer = 100;
                            break;
                        default:
                            timer = 2000;
                    }
                }
            }catch (IOException e){
                Log.w(TAG, e);
            }
            return  true;
        }
        @Override
        public void onGpioError(Gpio gpio, int error) {
            Log.w(TAG, gpio + ": Error event" + error);
        }
    };
}
