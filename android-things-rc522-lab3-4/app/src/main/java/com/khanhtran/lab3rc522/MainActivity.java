package com.khanhtran.lab3rc522;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.galarzaa.androidthings.Rc522;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
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
    private Rc522 mRc522;
    private SpiDevice mSpiDevice;
    private Gpio mResetPin;
    private Handler mHandler;
    private Runnable mRunable;
    private Gpio mBlue;
    private Gpio mRed;
    private Http mHttp;

    private final int MAX_BYTE_LENGTH = 16;
    private final String UID = "12365425";
    private int mFlashCount =0 ;
    private int mCurrentCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PeripheralManager pioService = PeripheralManager.getInstance();
        try {
            /* Names based on Raspberry Pi 3 */
            mSpiDevice = pioService.openSpiDevice("SPI0.0");
            mResetPin = pioService.openGpio("BCM25");
            mBlue = pioService.openGpio("BCM4");
            mBlue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mRed = pioService.openGpio("BCM17");
            mRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            /* Names based on NXP Pico i.MX7D I/O */
//            SpiDevice spiDevice = pioService.openSpiDevice("SPI3.0");
//            Gpio resetPin = pioService.openGpio("GPIO5_IO00");
            mRc522 = new Rc522(this, mSpiDevice, mResetPin);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mHandler = new Handler();
        mRunable = new Runnable() {
            @Override
            public void run() {
                readRFid();
            }
        };
        mHandler.post(mRunable);
        mHttp = new Http(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            if(mSpiDevice != null){
                mSpiDevice.close();
            }
            if(mResetPin != null){
                mResetPin.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeData(){
        while(true){
            try {
                mBlue.setValue(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("Lab3","Request");
            boolean success = mRc522.request();
            Log.d("Lab3","Request result " + success);
            if(!success){
                try{
                    mBlue.setValue(true);
                    Thread.sleep(100);
                } catch (Exception ex){

                }
                continue;
            }
            success = mRc522.antiCollisionDetect();
            Log.d("Lab3","Anti collision detect result " + success);
            if(!success){
                continue;
            }
            byte[] uid = mRc522.getUid();
            mRc522.selectTag(uid);
            break;
        }

        Log.d("Lab3","Start write");
        // Factory Key A:
        byte[] key = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        // Data that will be written
        String uid = UID;
        byte[] newData = uid.getBytes();
        if(newData.length > MAX_BYTE_LENGTH){
            Log.d("Lab3","Error - Data are too long to write");
            return;
        }
        byte[] correctData = new byte[MAX_BYTE_LENGTH];
        for(int i = 0;i<newData.length;i++){
            correctData[i] =  newData[i];
        }
        // Get the address of the desired block
        byte block = Rc522.getBlockAddress(3, 2);
        //We need to authenticate the card, each sector can have a different key
        boolean result = mRc522.authenticateCard(Rc522.AUTH_A, block, key);
        if (!result) {
            //Authentication failed
            Log.d("Lab3","Authen error");
            return;
        }
        result = mRc522.writeBlock(block, correctData);
        if(!result){
            //Could not write, key might have permission to read but not write
            Log.d("Lab3","Can't write data");
            return;
        }
        Log.d("Lab3","Write data successfully");
    }

    private interface FlashCallback{
        void finish();
    }
    private void flashLed(final Gpio led, int count, final FlashCallback callback){
        mFlashCount = count;
        mCurrentCount = 0;
        if(led != mBlue){
            try {
                mBlue.setValue(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mFlashCount > mCurrentCount){
                    try {
                        led.setValue(!led.getValue());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCurrentCount++;
                    mHandler.postDelayed(this,200);
                } else{
                    callback.finish();
                }
            }
        });
    }

    private void readRFid(){

        Log.d("Lab3","Waiting card");
        while(true){
            try {
                mBlue.setValue(false);
                mRed.setValue(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            boolean success = mRc522.request();
            if(!success){
                try{
                    mBlue.setValue(true);
                    Thread.sleep(100);
                } catch (Exception ex){

                }
                continue;
            }
            success = mRc522.antiCollisionDetect();
            Log.d("Lab3","Anti collision detect result " + success);
            if(!success){
                continue;
            }
            byte[] uid = mRc522.getUid();
            mRc522.selectTag(uid);
            break;
        }

        byte[] key = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        // Get the address of the desired block
        byte block = Rc522.getBlockAddress(3, 2);
        //We need to authenticate the card, each sector can have a different key
        boolean result = mRc522.authenticateCard(Rc522.AUTH_A, block, key);
        if (!result) {
            //Authentication failed
            Log.d("Lab3","Authen error");
            flashLed(mRed, 10, new FlashCallback() {
                @Override
                public void finish() {
                    readRFid();
                }
            });
            return;
        }

        //Buffer to hold read data
        byte[] buffer = new byte[MAX_BYTE_LENGTH];
        //Since we're still using the same block, we don't need to authenticate again
        result = mRc522.readBlock(block, buffer);

        if(!result){
            //Could not read card
            Log.d("Lab3","Can't read data");
            readRFid();
            return;
        }

        int length = MAX_BYTE_LENGTH;
        for(int i = 0;i<  buffer.length;i++){
            if(buffer[i] == 0){
                length = i;
                break;
            }
        }
        byte[] correctData = new byte[length];
        for(int i = 0;i<  correctData.length;i++){
            correctData[i] = buffer[i];
        }

        String data = new String(correctData);
        Log.d("Lab3","Read result: " +data);
        //Stop crypto to allow subsequent readings
        if(data.equals(UID)){
            Log.d("Lab3","Team's card");
            mHttp.sendData(data, new Http.Callback() {
                @Override
                public void onSuccess(String response) {
                    Log.d("Lab3","Send to server successfully");
                    Log.d("Lab3","Response: " + response);
                    try {
                        mBlue.setValue(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mHandler.postDelayed(mRunable,5000);
                }

                @Override
                public void onError() {
                    Log.d("Lab3","Send to server failure");

                    try {
                        mBlue.setValue(false);
                        mRed.setValue(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mHandler.postDelayed(mRunable,5000);
                }
            });
        } else{
            Log.d("Lab3","Other card");
            flashLed(mRed, 10, new FlashCallback() {
                @Override
                public void finish() {
                    readRFid();
                }
            });
        }

        mRc522.stopCrypto();

    }
}
