package com.khanhtran.doorunlocker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MainActivity extends Activity {
    private TakePictureListener mTakePictureListener;

    interface TakePictureListener{
        void onSuccess(byte[] data);
        void onError();
    }

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
    private int mFlashCount = 0;
    private int mCurrentCount = 0;
    private Runnable mFlashStatusRunnable;
    private boolean mAllowFlashStatus = false;
    private boolean mIsReading = false;
    private boolean mIsWriting = false;
    private Command mCurrentCommand;
    private boolean mIsLockedDoor = false;


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

            mStatusBlue = pioService.openGpio(DefaultConfig.STATUS_BLUE_PIN);
            mStatusBlue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mStatusGreen = pioService.openGpio(DefaultConfig.STATUS_GREEN_PIN);
            mStatusGreen.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mStatusRed = pioService.openGpio(DefaultConfig.STATUS_RED_PIN);
            mStatusRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mStatusRed.setValue(false);
            mStatusBlue.setValue(false);
            mStatusGreen.setValue(false);

            //Door status
            mOpenDoor = pioService.openGpio(DefaultConfig.OPEN_DOOR_PIN);
            mCloseDoor = pioService.openGpio(DefaultConfig.CLOSE_DOOR_PIN);
            mLockedDoor = pioService.openGpio(DefaultConfig.LOCKED_DOOR_PIN);

            //Init state
            mOpenDoor.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mCloseDoor.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLockedDoor.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mOpenDoor.setValue(false);
            mCloseDoor.setValue(false);
            mLockedDoor.setValue(false);

        } catch (IOException e) {
            e.printStackTrace();
        }

        mHandler = new Handler();

        closeDoor();
        changeLedToSystemInitialize();
        mCardManager = new CardManager(mSpiDevice, mResetPin);
        mServerManager = new ServerManager(DefaultConfig.SERVER_ADDRESS, DefaultConfig.SERVER_PORT);
        mServerManager.setEventListener(new ServerManager.EventListener() {
            @Override
            public void onReceived(Command command) {
                Log.d("iot", "Received command at main: " + command.toString());
                switch (command.getCode()) {
                    case Command.OPEN_DOOR:
                        openDoor();
                        break;
                    case Command.CLOSE_DOOR:
                        closeDoor();
                        break;
                    case Command.LOCK_DOOR:
                        lockedDoor();
                        break;
                    case Command.UNLOCK_DOOR:
                        unlockDoor();
                        break;
                    case Command.REQUEST_TAKE_PICTURE:
                        takePicture();
                        break;
                    case Command.SUCCESS:
                        Log.d("iot", "Success " + command.toString());
                        if (mCurrentCommand != null && mCurrentCommand.getCode() == Command.CHECK_USER_PERMISSION) {
                            openDoor();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    closeDoor();
                                }
                            }, 5000);
                        }
                        break;
                    case Command.ERROR:
                        Log.d("iot", "Error " + command.toString());
                        flashStatusLed(new boolean[]{true, false, false});
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                changeLedToSystemReady();
                            }
                        }, 3000);
                        break;
                    case Command.REQUEST_WRITE_DATA:
                        mIsWriting = true;
                        writeToCard(command.getData1());
                        break;
                }
            }

            @Override
            public void onConnectError() {
                Log.d("iot", "Connect server error at main");
                changeLedToSystemError();
            }

            @Override
            public void onConnectSuccess() {
                Log.d("iot", "Connect server success at main");
                changeLedToSystemReady();
                mIsReading = true;
                readFromCard();
            }
        });
        mServerManager.connectToServer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.shutDown();

        mCameraThread.quitSafely();

        try {
            if (mSpiDevice != null) {
                mSpiDevice.close();
            }
            if (mResetPin != null) {
                mResetPin.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Init camera
     */
    private void initCamera() {
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
            Log.d("iot", "Take picture successfully");
            try {
                final Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                final ImageView image = (ImageView) findViewById(R.id.iv_photo);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        image.setImageBitmap(bmp);

                        if(mTakePictureListener != null){
                            mTakePictureListener.onSuccess(imageBytes);
                        }
                    }
                });
                return;
            } catch (Exception ex) {
                Log.e("iot", ex.getMessage());
            }
        } else {
            Log.d("iot", "Take picture error");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mTakePictureListener != null){
                    mTakePictureListener.onError();
                }
            }
        });
    }

    private void openDoor() {
        mIsReading = true;
        unlockDoor();
        try {
            mOpenDoor.setValue(true);
            mCloseDoor.setValue(false);
            mLockedDoor.setValue(false);
        } catch (Exception ex) {

        }
    }

    private void closeDoor() {
        unlockDoor();
        try {
            mOpenDoor.setValue(false);
            mCloseDoor.setValue(true);
            mLockedDoor.setValue(false);
        } catch (Exception ex) {

        }
    }

    private void lockedDoor() {
        try {
            mOpenDoor.setValue(false);
            mCloseDoor.setValue(false);
            mLockedDoor.setValue(true);

            mIsReading = false;
            mIsWriting = false;
            mIsLockedDoor = true;
        } catch (Exception ex) {

        }

    }

    private void unlockDoor() {
        Log.d("iot","Unlock door");
        try {
            mOpenDoor.setValue(false);
            mCloseDoor.setValue(true);
            mLockedDoor.setValue(false);
            mIsLockedDoor = false;
            readFromCard();
        } catch (Exception ex) {

        }
    }

    private void takePicture() {
        mCamera.takePicture();
    }

    private void writeToCard(final String data) {
        if(!mIsWriting){
            return;
        }

        mIsReading = false;
        mCardManager.writeToCard(data, new CardManager.OnResult<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d("iot", "Write successfully");

                Command command = new Command();
                command.setCode(Command.SUCCESS);
                command.setData1(String.valueOf(Command.REQUEST_WRITE_DATA));
                mIsReading = true;
                readFromCard();
            }

            @Override
            public void onError() {
                if(mIsWriting){
                    writeToCard(data);
                } else{
                    Log.d("iot", "Write error");
                    Command command = new Command();
                    command.setCode(Command.ERROR);
                    command.setData1(String.valueOf(Command.REQUEST_WRITE_DATA));
                    mIsReading = true;
                    readFromCard();
                }

            }
        });
    }

    private void readFromCard() {
        if(!mIsReading || mIsLockedDoor){
            return;
        }
        mCardManager.readFromCard(new CardManager.OnResult<String>() {
            @Override
            public void onSuccess(final String userId) {
                Log.d("iot", "read data from card success");
                Log.d("iot", "Data: " + userId);
                if(mIsLockedDoor){
                    Log.d("iot", "Door is locked: ");
                    return;
                }

                final Command command = new Command();
                command.setCode(Command.CHECK_USER_PERMISSION);
                mCurrentCommand = command;
                mTakePictureListener = new TakePictureListener() {
                    @Override
                    public void onSuccess(byte[] data) {
                        JSONObject sendData = new JSONObject();
                        try {
                            sendData.put("id",userId);
                            sendData.put("buffer", encodeImage(data));
                            command.setData1(sendData.toString());
                        } catch (Exception ex){
                            return;
                        }
                        mServerManager.sendCommand(command);
                    }

                    @Override
                    public void onError() {
                        JSONObject sendData = new JSONObject();
                        try {
                            sendData.put("id",userId);
                            command.setData1(sendData.toString());
                        } catch (Exception ex){
                            return;
                        }
                        mServerManager.sendCommand(command);
                    }
                };
                takePicture();
                if (mIsReading) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            readFromCard();
                        }
                    }, 1000);
                }
            }

            @Override
            public void onError() {
//                Log.d("iot", "read data from card error");
                if (mIsReading) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            readFromCard();
                        }
                    }, 1000);
                }
            }
        });
    }

    private void changeLedToSystemInitialize() {
        flashStatusLed(new boolean[]{true, true, false});
    }

    private void changeLedToWaitingCard() {
        try {
            mStatusGreen.setValue(true);
            mStatusRed.setValue(true);
            mStatusBlue.setValue(false);
        } catch (Exception ex) {

        }
    }

    private void changeLedToSystemReady() {
        stopFlashLed();
        try {
            mStatusGreen.setValue(true);
            mStatusRed.setValue(false);
            mStatusBlue.setValue(false);
        } catch (Exception ex) {

        }
    }

    private void changeLedToSystemError() {
        stopFlashLed();
        try {
            mStatusGreen.setValue(false);
            mStatusRed.setValue(true);
            mStatusBlue.setValue(false);
        } catch (Exception ex) {

        }
    }

    private void flashStatusLed(final boolean[] status) {
        Log.d("iot", "Flash status " + Arrays.toString(status));
        mAllowFlashStatus = true;
        mFlashStatusRunnable = new Runnable() {
            @Override
            public void run() {
                if (mAllowFlashStatus) {
                    try {

                        if (!mStatusBlue.getValue() && !mStatusGreen.getValue() && !mStatusRed.getValue()) {
                            mStatusGreen.setValue(status[1]);
                            mStatusRed.setValue(status[0]);
                            mStatusBlue.setValue(status[2]);
                        } else {
                            mStatusBlue.setValue(false);
                            mStatusGreen.setValue(false);
                            mStatusRed.setValue(false);
                        }
                    } catch (Exception ex) {
                        Log.i("iot", "flash error " + ex.getMessage());
                    }

                    mHandler.postDelayed(this, 100);
                }
            }
        };


        try {
            mStatusBlue.setValue(false);
            mStatusGreen.setValue(false);
            mStatusRed.setValue(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mHandler.post(mFlashStatusRunnable);
    }

    private void stopFlashLed() {
        mAllowFlashStatus = false;
    }


    private String encodeImage(byte[] data)
    {
        String encImage = Base64.encodeToString(data, Base64.DEFAULT);
        //Base64.de
        return encImage;
    }

}
