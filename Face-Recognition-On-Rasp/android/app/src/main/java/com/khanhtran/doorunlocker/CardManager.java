package com.khanhtran.doorunlocker;

import android.util.Log;

import com.galarzaa.androidthings.Rc522;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class CardManager {
    public interface OnResult<T> {
        void onSuccess(T data);

        void onError();
    }

    private final int SECTOR = 3;

    private Rc522 mRc522;
    private boolean mIsStopAction = false;
    private boolean mIsLock = false;

    public CardManager(SpiDevice spiDevice, Gpio resetPin) {
        try {
            mRc522 = new Rc522(spiDevice, resetPin);
        } catch (IOException e) {
            e.printStackTrace();
            mRc522 = null;
        }
    }

    public void stopAction() {
        mIsStopAction = true;
    }

    public void writeToCard(final String data, final OnResult<Void> result) {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                if(mIsLock){
                    emitter.onError(new Exception());
                    return;
                }
                mIsLock = true;
                boolean result = writeToCard(data);
                mIsLock = false;
                mRc522.stopCrypto();
                if (result) {
                    emitter.onComplete();
                } else {
                    emitter.onError(new Exception("Error"));
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        result.onError();
                    }

                    @Override
                    public void onComplete() {
                        result.onSuccess(null);
                    }
                });
    }

    public void readFromCard(final OnResult<String> result){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                if(mIsLock){
                    emitter.onError(new Exception());
                    return;
                }
                mIsLock = true;
                String data = readFromCard();
                mIsLock = false;
                mRc522.stopCrypto();
                if(data != null){
                    emitter.onNext(data);
                } else{
                    emitter.onError(new Exception());
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        if(s != null){
                            s = s.trim();
                            result.onSuccess(s);

                        } else{
                            result.onError();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        result.onError();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private boolean waitingCard() {
        Log.d("iot", "Waiting card");
        mIsStopAction = false;
        while (!mIsStopAction) {
            boolean success = mRc522.request();
            if (!success) {
                try {
                    Thread.sleep(100);
                } catch (Exception ex) {

                }
                continue;
            }
            success = mRc522.antiCollisionDetect();
            Log.d("iot", "Anti collision detect result " + success);
            if (!success) {
                continue;
            }
            byte[] uid = mRc522.getUid();
            mRc522.selectTag(uid);
            return true;
        }
        return false;
    }

    private boolean writeToCard(String data) {
        boolean cardSuccess = waitingCard();

        if (!cardSuccess) {
            return false;
        }

        byte[] key = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        // Get the address of the desired block
        byte address = Rc522.getBlockAddress(SECTOR, 0);
        //We need to authenticate the card, each sector can have a different key
        boolean result = mRc522.authenticateCard(Rc522.AUTH_A, address, key);

        if (!result) {
            //Authentication failed
            Log.d("iot", "Authen error");
            return false;
        }

        //Buffer to hold read data
        byte[] buffer = data.getBytes(Charset.forName("UTF-8"));
        if (buffer.length > 48) {
            Log.d("iot", "Data is too longer to write.");
            return false;
        } else {
            int remain = 48 - buffer.length;
            StringBuilder builder = new StringBuilder(remain);
            builder.append(data);
            for (int i = 0; i < remain; i++) {
                builder.append(" ");
            }
            buffer = builder.toString().getBytes(Charset.forName("UTF-8"));
        }

        Log.d("iot", "Buffer length " + buffer.length);

        //Since we're still using the same block, we don't need to authenticate again
        result = true;
        for (int i = 0; i < 3; i++) {
            address = Rc522.getBlockAddress(3, i);
            byte[] b = Arrays.copyOfRange(buffer, i * 16, i * 16 + 16);
            result = mRc522.writeBlock(address, b);
            Log.d("iot","Write block " + i + " result " + result + " data length " + b.length);
        }

        if (!result) {
            //Could not read card
            Log.d("iot", "Can't write data");
            return false;
        }
        return true;
    }

    private String readFromCard() {
        boolean cardSuccess = waitingCard();

        if (!cardSuccess) {
            return null;
        }

        byte[] key = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        // Get the address of the desired block
        byte address = Rc522.getBlockAddress(SECTOR, 0);
        //We need to authenticate the card, each sector can have a different key
        boolean result = mRc522.authenticateCard(Rc522.AUTH_A, address, key);

        if (!result) {
            //Authentication failed
            Log.d("iot", "Authen error");
            return null;
        }

        //Buffer to hold read data
        byte[] buffer = new byte[16];
        byte[] data = new byte[48];
        //Since we're still using the same block, we don't need to authenticate again
        result = true;
        for (int i = 0; i < 3; i++) {
            address = Rc522.getBlockAddress(SECTOR, i);
            result &= mRc522.readBlock(address, buffer);
            if (result) {
                System.arraycopy(buffer, 0, data, i * 16, 16);
            }
        }

        if (!result) {
            //Could not read card
            Log.d("iot", "Can't read data");
            return null;
        }
        return new String(data);
    }
}
