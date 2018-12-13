package com.khanhtran.doorunlocker;

public class Command {
    public static final int DOOR_AUTH = 1;
    public static final int DOOR_INFO = 2;
    public static final int OPEN_DOOR = 110;
    public static final int CLOSE_DOOR = 111;
    public static final int LOCK_DOOR = 112;
    public static final int REQUEST_TAKE_PICTURE = 120;
    public static final int REQUEST_DOOR_INFO = 100;
    public static final int REQUEST_WRITE_DATA = 130;
    public static final int CHECK_USER_PERMISSION = 300;
    public static final int ERROR = 500;
    public static final int SUCCESS = 200;

    private int code;
    private String data1;
    private String data2;

    public Command(String cm) throws Exception {
        try {
            String[] arr = cm.split(";");
            code = Integer.valueOf(arr[0]);
            if (arr.length > 2) {
                data2 = arr[2];
                data1 = arr[1];
            } else if (arr.length > 1) {
                data1 = arr[1];
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public Command() {

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getData1() {
        return data1;
    }

    public void setData1(String data1) {
        this.data1 = data1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String c = "00" + code;

        builder.append(c.substring(c.length() - 3));
        if (data1 != null) {
            builder.append(";");
            builder.append(data1);
        }
        if (data2 != null) {
            builder.append(";");
            builder.append(data2);
        }
        return builder.toString();
    }

    public String getData2() {
        return data2;
    }

    public void setData2(String data2) {
        this.data2 = data2;
    }
}
