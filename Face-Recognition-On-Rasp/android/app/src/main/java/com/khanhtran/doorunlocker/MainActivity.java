package com.khanhtran.doorunlocker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends Activity {
    private DoorbellCamera mCamera;
    /**
     * A {@link Handler} for running Camera tasks in the background.
     */
    private Handler mCameraHandler;

    /**
     * An additional thread for running Camera tasks that shouldn't block the UI.
     */
    private HandlerThread mCameraThread;

    private CardManager mCardManager;
    private ServerManager mServerManager;
    private SpiDevice mSpiDevice;
    private Gpio mResetPin;
    private Handler mHandler;
    private Runnable mRunnable;
    private Gpio mStatusBlue;
    private Gpio mStatusGreen;
    private Gpio mStatusRed;
    private Gpio mOpenDoor;
    private Gpio mCloseDoor;
    private Gpio mLockedDoor;

    private final int MAX_BYTE_LENGTH = 16;
    private final String UID = "12365425";
    private int mFlashCount =0 ;
    private int mCurrentCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCamera();
        PeripheralManager pioService = PeripheralManager.getInstance();
        try {
            /* Names based on Raspberry Pi 3 */
            mSpiDevice = pioService.openSpiDevice("SPI0.0");
            mResetPin = pioService.openGpio(DefaultConfig.RESET_PIN);
            mStatusBlue = pioService.openGpio("BCM4");
            mStatusBlue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mStatusGreen = pioService.openGpio("BCM17");
            mStatusGreen.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            /* Names based on NXP Pico i.MX7D I/O */
//            SpiDevice spiDevice = pioService.openSpiDevice("SPI3.0");
//            Gpio resetPin = pioService.openGpio("GPIO5_IO00");
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCardManager = new CardManager(mSpiDevice,mResetPin);
        mServerManager = new ServerManager(DefaultConfig.SERVER_ADDRESS,DefaultConfig.SERVER_PORT);
        mServerManager.setEventListener(new ServerManager.EventListener() {
            @Override
            public void onReceived(Command command) {
                Log.d("iot","Received command at main: " + command.toString());
            }

            @Override
            public void onConnectError() {
                Log.d("iot","Connect server error at main");
            }

            @Override
            public void onConnectSuccess() {
                Log.d("iot","Connect server success at main");
            }
        });
        mServerManager.connectToServer();

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
//                writeToCard("Khanh abc");
//                readFromCard();
            }
        };
        mHandler.postDelayed(mRunnable,1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.shutDown();

        mCameraThread.quitSafely();

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

    /**
     * Init camera
     */
    private void initCamera(){
        // Creates new handlers and associated threads for camera and networking operations.
        mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

        // Camera code is complicated, so we've shoved it all in this closet class for you.
        mCamera = DoorbellCamera.getInstance();
        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);
    }

    /**
     * Listener for new camera images.
     */
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    // get image bytes
                    ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
                    final byte[] imageBytes = new byte[imageBuf.remaining()];
                    imageBuf.get(imageBytes);
                    image.close();

                    onPictureTaken(imageBytes);
                }
            };


    private void onPictureTaken(final byte[] imageBytes) {
        if (imageBytes != null) {
            Log.d("iot","Take picture successfully");
            try {
                final Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                final ImageView image = (ImageView) findViewById(R.id.iv_photo);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        image.setImageBitmap(bmp);
                    }
                });
            } catch (Exception ex){
                Log.e("iot",ex.getMessage());
            }
        } else{
            Log.d("iot","Take picture error");
        }
    }

    private void openDoor(){

    }

    private void closeDoor(){

    }

    private void lockedDoor(){

    }

    private void takePicture(){
        mCamera.takePicture();
    }

    private void writeToCard(String data){
        mCardManager.writeToCard(data, new CardManager.OnResult<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d("iot","Write successfully");
            }

            @Override
            public void onError() {
                Log.d("iot","Write error");
            }
        });
    }

    private void readFromCard(){
        mCardManager.readFromCard(new CardManager.OnResult<String>() {
            @Override
            public void onSuccess(String data) {
                Log.d("iot","read data from card success");
                Log.d("iot","Data: " + data);
            }

            @Override
            public void onError() {
                Log.d("iot","read data from card error");

            }
        });
    }

    private void waitingCard(){

    }

}
