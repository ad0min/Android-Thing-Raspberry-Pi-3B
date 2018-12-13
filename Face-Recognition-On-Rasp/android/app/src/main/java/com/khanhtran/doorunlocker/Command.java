package com.khanhtran.doorunlocker;

public class Command {
    private int code;
    private String data;

    public Command(String cm) throws Exception{
        try{
            String c = cm.substring(0,3);
            data = cm.substring(3);
            code = Integer.valueOf(c);
        } catch (Exception ex){
            throw  ex;
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Code : " + code + " - data: " + data;
    }
}
