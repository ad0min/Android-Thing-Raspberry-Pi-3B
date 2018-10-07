package com.example.androidthings.loopback;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

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

public class Exercises {
    private static final String TAG = "MainActivity";

    private static final String PWN_NAME = "PWM1";
    private static final String GPIO_PWN_NAME = "BCM13";
    private static final String GPIO_BLUE_NAME = "BCM17";
    private static final String GPIO_RED_NAME = "BCM27";
    private static final String GPIO_GREEN_NAME = "BCM22";

    private static final String GPIO_BUTTON_NAME = "BCM4";

    private static final int INTERVAL_BETWEEN_BLINK_MS = 1000;

    private static final double MIN_ACTIVE_PULSE_DURATION_MS = 1;
    private static final double MAX_ACTIVE_PULSE_DURATION_MS = 10;
    private static final double PULSE_PERIOD_MS = 10;  // Frequency of 50Hz (1000/20)

    // Parameters for the servo movement over time
    private static final double PULSE_CHANGE_PER_STEP_MS = 0.2;
    private static final int INTERVAL_BETWEEN_STEPS_MS = 10;

    private Handler mHandler = new Handler();
    private Gpio mLedGpioRed;
    private Gpio mLedGpioGreen;
    private Gpio mLedGpioBlue;
    private Gpio mButton;

    private Pwm mPwm;
    private boolean mIsPulseIncreasing = true;
    private double mActivePulseDuration;



    private boolean mStateRed = false;
    private boolean mStateGreen = false;
    private boolean mStateBlue = false;
    private boolean mLedState = false;
    private int count = 1;
    private int key = 0;
    private int timer = 2000;
    protected void onCreate() {
        Log.i(TAG, "Starting BlinkActivity");
        try {


            mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;

            mPwm = PeripheralManager.getInstance().openPwm(PWN_NAME);

            // Always set frequency and initial duty cycle before enabling PWM
            mPwm.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS);
            mPwm.setPwmDutyCycle(mActivePulseDuration);
            mPwm.setEnabled(true);


            mLedGpioRed = PeripheralManager.getInstance().openGpio(GPIO_RED_NAME);
            mLedGpioGreen = PeripheralManager.getInstance().openGpio(GPIO_GREEN_NAME);
            mLedGpioBlue = PeripheralManager.getInstance().openGpio(GPIO_BLUE_NAME);
            mButton = PeripheralManager.getInstance().openGpio(GPIO_BUTTON_NAME);
            mLedGpioRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioBlue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioGreen.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mButton.setDirection(Gpio.DIRECTION_IN);
            mButton.setActiveType(Gpio.ACTIVE_HIGH);
            mButton.setEdgeTriggerType(Gpio.EDGE_BOTH);


        }catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    protected void onDestroy(){
        try {
            Log.i(TAG, "Destroy ");
            mLedGpioRed.close();
            mLedGpioGreen.close();
            mLedGpioBlue.close();
            mButton.unregisterGpioCallback(mGpioCallbackEx2);
            mButton.close();
            mPwm.close();
            mHandler.removeCallbacks(null);
        }catch (IOException e){
            Log.e(TAG, "Error on PeripheralIO API ", e);
        }finally {
            mLedGpioRed = null;
            mLedGpioBlue = null;
            mLedGpioGreen = null;
            mButton = null;
            mPwm = null;
        }
    }
    public void commandEx(int mode){
        try {
            switch (mode) {
                case 1:
                    Log.i(TAG, "ex1");
//                    mHandler.removeCallbacks();
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.post(mBlinkRunnable);
                    break;
                case 2:
                    Log.i(TAG, "ex2");
                    mHandler.removeCallbacksAndMessages(null);
                    mButton.registerGpioCallback(mGpioCallbackEx2);
                    mHandler.post(mBlinkRunnableEx2);
                    break;
                case 3:
                    Log.i(TAG, "ex3");
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.post(mBlinkRunnable);
                    mHandler.post(mChangePWMRunnableEx3);
                    break;
                case 4:
                    Log.i(TAG,"ex4");
                    mHandler.removeCallbacksAndMessages(null);
                    mButton.registerGpioCallback(mGpioCallbackEx4);
                    mHandler.post(mChangePWMRunnableEx3);
                    mHandler.post(mBrightnessRunnableEx4);
                    break;

            }
        }catch (IOException e){
            Log.i(TAG, e.toString());
        }
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLedGpioRed == null || mLedGpioBlue == null || mLedGpioBlue == null) {
                return;
            }
            try {
                count += 1;
                mStateRed = count % 2 == 1;
                mStateGreen = count / 2 % 2 == 1;
                mStateBlue = count / 4 % 2 == 1;
                mLedGpioRed.setValue(mStateRed);
                mLedGpioGreen.setValue(mStateGreen);
                mLedGpioBlue.setValue(mStateBlue);
                Log.d(TAG, "State set to " + count);
                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINK_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API " + e);
            }
        }
    };

    private Runnable mBlinkRunnableEx2 = new Runnable() {
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
                Log.d(TAG, "State set to "+ count+ " with timer "+ timer);
                mHandler.postDelayed(mBlinkRunnableEx2, timer);
            }catch (IOException e){
                Log.e(TAG, "Error on PeripheralIO API "+ e);
            }
        }
    };
    private GpioCallback mGpioCallbackEx2 = new GpioCallback() {
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



    private Runnable mChangePWMRunnableEx3 = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the port is already closed
            if (mPwm == null) {
                Log.w(TAG, "Stopping runnable since mPwm is null");
                return;
            }

            // Change the duration of the active PWM pulse, but keep it between the minimum and
            // maximum limits.
            // The direction of the change depends on the mIsPulseIncreasing variable, so the pulse
            // will bounce from MIN to MAX.
            if (mIsPulseIncreasing) {
                mActivePulseDuration += PULSE_CHANGE_PER_STEP_MS;
            } else {
                mActivePulseDuration -= PULSE_CHANGE_PER_STEP_MS;
            }

            // Bounce mActivePulseDuration back from the limits
            if (mActivePulseDuration > MAX_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MAX_ACTIVE_PULSE_DURATION_MS;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            } else if (mActivePulseDuration < MIN_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            }

//            Log.d(TAG, "Changing PWM active pulse duration to " + mActivePulseDuration + " ms");

            try {

                // Duty cycle is the percentage of active (on) pulse over the total duration of the
                // PWM pulse
                mPwm.setPwmDutyCycle(100 * mActivePulseDuration / PULSE_PERIOD_MS);

                // Reschedule the same runnable in {@link #INTERVAL_BETWEEN_STEPS_MS} milliseconds
                mHandler.postDelayed(this, INTERVAL_BETWEEN_STEPS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };


    private GpioCallback mGpioCallbackEx4 = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                Log.d(TAG, "Button state: " + gpio.getValue());
                if (gpio.getValue())
                    key +=1;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    };

    private Runnable mBrightnessRunnableEx4 = new Runnable() {
        @Override
        public void run() {
            try {
                switch (key%3) {
                    case 0:
                        mStateRed = true;
                        mStateBlue= false;
                        mStateGreen= false;
                        break;
                    case 1:
                        mStateBlue= true;
                        mStateRed= false;
                        mStateGreen= false;
                        break;
                    case 2:
                        mStateGreen= true;
                        mStateRed= false;
                        mStateBlue= false;
                        break;
                }
//                key +=1;
                Log.d(TAG, "key for "+ key);
                mLedGpioRed.setValue(mStateRed);
                mLedGpioBlue.setValue(mStateBlue);
                mLedGpioGreen.setValue(mStateGreen);
                mHandler.postDelayed(this, INTERVAL_BETWEEN_BLINK_MS);
            }catch (IOException e){
                Log.d(TAG, "Error exception  " + e);
            }
        }
    };
}