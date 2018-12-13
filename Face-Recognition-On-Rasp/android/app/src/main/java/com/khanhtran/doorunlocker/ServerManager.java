package com.khanhtran.doorunlocker;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ServerManager {
    public interface EventListener {
        void onReceived(Command command);

        void onConnectError();

        void onConnectSuccess();
    }

    private String mHost;
    private int mPort;
    private Socket mSocket;
    private boolean mIsConnected = false;
    private EventListener mListener = null;
    BufferedWriter os = null;
    BufferedReader is = null;

    public ServerManager(String host, int port) {
        mHost = host;
        mPort = port;
    }

    public void setEventListener(EventListener listener) {
        mListener = listener;
    }

    public void connectToServer() {
        Observable.create(new ObservableOnSubscribe<Command>() {
            @Override
            public void subscribe(ObservableEmitter<Command> emitter) throws Exception {
                try {
                    Log.d("iot", "Start connect server");
                    mSocket = new Socket(mHost, mPort);
                    os = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
                    is = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    emitter.onNext(new Command("000asdf"));
                    mIsConnected = true;

                    Log.d("iot", "Start received command");
                    while (mIsConnected) {
                        try {
                            String cm = is.readLine();
                            Log.d("iot", "Received command: " + cm);

//                            Command command = new Command(cm);
//                            Log.d("iot", "Received command: " + command.toString());
//                            if (mListener != null) {
//                                mListener.onReceived(command);
//                            }
                        } catch (Exception ex) {
                            Log.d("iot", ex.getMessage());
                            Log.d("iot", "Command error");
                        }
                    }

                } catch (IOException e) {
                    mIsConnected = false;
                    Log.d("iot", "Cannot connect server");
                    Log.d("iot", e.getMessage());
                    emitter.onError(null);
                } catch (Exception ex) {

                    mIsConnected = false;
                    Log.d("iot", ex.getMessage());
                    Log.d("iot", "Server error");
                    emitter.onError(null);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Command>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Command command) {
                        if (command.getCode() == 0) {
                            if (mListener != null) {
                                mListener.onConnectSuccess();
                            }
                        } else {
                            if (mListener != null) {
                                mListener.onReceived(command);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mListener != null) {
                            mListener.onConnectError();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void disconnectToServer() {
        mIsConnected = false;
    }
}
