package com.khanhtran.doorunlocker;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ServerManager {
    public interface EventListener {
        void onReceived(Command command);

        void onConnectError();

        void onConnectSuccess();
    }

    private String mHost;
    private int mPort;
    private Socket mSocket;
    private boolean mIsAuthen = false;
    private EventListener mListener = null;
    private boolean mIsConnected = false;

    public ServerManager(String host, int port) {
        mHost = host;
        mPort = port;
    }

    public void setEventListener(EventListener listener) {
        mListener = listener;
    }

    public void connectToServer() {
        mIsConnected = false;
        Log.d("iot","connect to server");
        try {
            mSocket = IO.socket("http://" + mHost + ":" + mPort);
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                   authenRasp();
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {}

            })
            .on("event", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("iot","Received message  " + Arrays.toString(args));
                    Command command;
                    try {
                       String cm = String.valueOf(args[0]);
                        Log.d("iot","Command string " + cm);
                        command = new Command(cm);

                    } catch (Exception ex){
                        Log.d("iot",ex.getMessage());
                        Log.d("iot","Command error");
                        command = null;
                    };
                    if(command != null){
                        onReceivedCommand(command);
                    }
                }
            })
            .on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                   onConnectError();
                }
            });
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private void authenRasp(){
        mIsAuthen = false;
        Command cm = new Command();
        cm.setCode(Command.DOOR_AUTH);
        cm.setData1(DefaultConfig.DOOR_AUTHEN_KEY);
        mSocket.emit("event", cm.toString(), new Ack() {
            @Override
            public void call(Object... args) {
                Log.d("iot","AUTHEN res: " + Arrays.toString(args));

                if(args.length > 0){
                    try {
                        Command command = new Command(String.valueOf(args[0]));
                        if(command.getCode() == Command.SUCCESS){
                            mIsAuthen = true;
                            sendRaspInfo();
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                onConnectError();
            }
        });
    }

    private void sendRaspInfo(){
        if(!mIsAuthen){
            authenRasp();
            return;
        }

        Command cm = new Command();
        cm.setCode(Command.DOOR_INFO);
        cm.setData1(DefaultConfig.DOOR_ID);
        mSocket.emit("event", cm.toString(), new Ack() {
            @Override
            public void call(Object... args) {
                Log.d("iot","Send info " + Arrays.toString(args));
                if(args.length > 0){
                    try {
                        Command command = new Command(String.valueOf(args[0]));
                        if(command.getCode() == Command.SUCCESS){
                            onConnectSuccess();
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                onConnectError();
            }
        });
    }

    public void disconnectToServer() {
        mSocket.disconnect();
    }

    private void onConnectError(){
        mIsConnected = false;
        Observable.just("1")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        if(mListener!=null){
                            mListener.onConnectError();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void onConnectSuccess(){
        mIsConnected = true;
        Observable.just("1")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        if(mListener!=null){
                            mListener.onConnectSuccess();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void onReceivedCommand(Command command){
        Observable.just(command)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Command>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Command command) {
                        if(mListener != null){
                            mListener.onReceived(command);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void sendResponse(Command cm){
        if(mIsConnected){
            mSocket.emit("event",cm.toString());
        }
    }

    public void sendCommand(Command cm){
        if(mIsConnected){
            mSocket.emit("event", cm.toString(), new Ack() {
                @Override
                public void call(Object... args) {
                    Command command;
                    try {
                        String cm = String.valueOf(args[0]);
                        Log.d("iot","Command string " + cm);
                        command = new Command(cm);

                    } catch (Exception ex){
                        Log.d("iot",ex.getMessage());
                        Log.d("iot","Command error");
                        command = null;
                    };
                    if(command != null){
                        onReceivedCommand(command);
                    }
                }
            });
        }
    }
}
