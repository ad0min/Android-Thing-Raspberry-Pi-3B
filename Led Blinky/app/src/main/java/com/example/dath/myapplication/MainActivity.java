package com.example.dath.myapplication;

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
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    // Parameters of the servo PWM
    private static final double MIN_ACTIVE_PULSE_DURATION_MS = 1;
    private static final double MAX_ACTIVE_PULSE_DURATION_MS = 10;
    private static final double PULSE_PERIOD_MS = 10;  // Frequency of 50Hz (1000/20)

    // Parameters for the servo movement over time
    private static final double PULSE_CHANGE_PER_STEP_MS = 0.2;
    private static final int INTERVAL_BETWEEN_STEPS_MS = 10;

    private static final int DELAY_LED_TIME = 1000;

    private static final String RED_LED_PORT = "BCM4";
    private static final String GREEN_LED_PORT = "BCM17";
    private static final String BLUE_LED_PORT = "BCM27";

    private static final String BUTTON_PORT = "BCM16";

    private Handler mHandler = new Handler();
    private Pwm mPwm;
    private boolean mIsPulseIncreasing = true;
    private double mActivePulseDuration;

    private Gpio mLedGpioRed;
    private Gpio mLedGpioGreen;
    private Gpio mLedGpioBlue;

    private Gpio mGpioButton;

    private boolean mLedStateRed = false;
    private boolean mLedStateGreen =false;
    private boolean mLedStateBlue = false;

    private int key = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting PwmActivity");

        try {
            String pinName = "PWM1";
            mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;

            mPwm = PeripheralManager.getInstance().openPwm(pinName);

            // Always set frequency and initial duty cycle before enabling PWM
            mPwm.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS);
            mPwm.setPwmDutyCycle(mActivePulseDuration);
            mPwm.setEnabled(true);


            mLedGpioRed = PeripheralManager.getInstance().openGpio(RED_LED_PORT);
            mLedGpioGreen = PeripheralManager.getInstance().openGpio(GREEN_LED_PORT);
            mLedGpioBlue = PeripheralManager.getInstance().openGpio(BLUE_LED_PORT);

            mLedGpioRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpioBlue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpioGreen.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            mGpioButton = PeripheralManager.getInstance().openGpio(BUTTON_PORT);
            mGpioButton.setDirection(Gpio.DIRECTION_IN);
            mGpioButton.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mGpioButton.registerGpioCallback(new GpioCallback() {
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
            });


            // Post a Runnable that continuously change PWM pulse width, effectively changing the
            // servo position
//            Log.d(TAG, "Start changing PWM pulse vs Led ...");
            mHandler.post(mChangePWMRunnable);
            mHandler.post(mBrightnessRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove pending Runnable from the handler.
        mHandler.removeCallbacks(mChangePWMRunnable);
        // Close the PWM port.
        Log.i(TAG, "Closing port");
        try {
            mPwm.close();
            mLedGpioRed.close();
            mLedGpioGreen.close();
            mLedGpioBlue.close();
            mGpioButton.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mPwm = null;
            mLedGpioRed = null;
            mLedGpioGreen = null;
            mLedGpioBlue = null;
            mGpioButton =null;
        }
    }

    private Runnable mChangePWMRunnable = new Runnable() {
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

    private Runnable mBrightnessRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                switch (key%3) {
                    case 0:
                        mLedStateRed = true;
                        mLedStateBlue = false;
                        mLedStateGreen = false;
                        break;
                    case 1:
                        mLedStateBlue = true;
                        mLedStateRed = false;
                        mLedStateGreen = false;
                        break;
                    case 2:
                        mLedStateGreen = true;
                        mLedStateRed = false;
                        mLedStateBlue = false;
                        break;
                }
//                key +=1;
                Log.d(TAG, "key for "+ key);
                mLedGpioRed.setValue(mLedStateRed);
                mLedGpioBlue.setValue(mLedStateBlue);
                mLedGpioGreen.setValue(mLedStateGreen);
                mHandler.postDelayed(this, DELAY_LED_TIME);
            }catch (IOException e){
                Log.d(TAG, "Error exception  " + e);
            }
        }
    };


}
