package com.example.dath.spiwithmrfc522;

import android.app.Activity;
import android.os.Bundle;

import com.galarzaa.androidthings.Rc522;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.SpiDevice;
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
    private Rc522 mRc522;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManager pioService = PeripheralManager.getInstance();
        try {
            /* Names based on Raspberry Pi 3 */
            SpiDevice spiDevice = pioService.openSpiDevice("SPI0.0");
            Gpio resetPin = pioService.openGpio("BCM25");
            mRc522 = new Rc522(this, spiDevice, resetPin);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
    }

    private void readRFid(){
        while(true){
            boolean success = mRc522.request();
            if(!success){
                continue;
            }
            success = mRc522.antiCollisionDetect();
            if(!success){
                continue;
            }
            byte[] uid = mRc522.getUid();
            mRc522.selectTag(uid);
            break;
        }
        // Factory Key A:
        byte[] key = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        // Data that will be written
        byte[] newData = {0x0F,0x0E,0x0D,0x0C,0x0B,0x0A,0x09,0x08,0x07,0x06,0x05,0x04,0x03,0x02,0x01,0x00};
        // Get the address of the desired block
        byte block = Rc522.getBlockAddress(3, 2);
        //We need to authenticate the card, each sector can have a different key
        boolean result = mRc522.authenticateCard(Rc522.AUTH_A, block, key);
        if (!result) {
            //Authentication failed
            return;
        }
        result = mRc522.writeBlock(block, newData);
        if(!result){
            //Could not write, key might have permission to read but not write
            return;
        }
        //Buffer to hold read data
        byte[] buffer = new byte[16];
        //Since we're still using the same block, we don't need to authenticate again
        result = mRc522.readBlock(block, buffer);
        if(!result){
            //Could not read card
            return;
        }
        //Stop crypto to allow subsequent readings
        mRc522.stopCrypto();


    }
}
