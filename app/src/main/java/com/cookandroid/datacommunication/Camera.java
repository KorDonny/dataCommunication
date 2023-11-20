package com.cookandroid.datacommunication;

import com.google.firebase.database.PropertyName;

import java.text.SimpleDateFormat;

public class Camera {
    private boolean isOn;
    private boolean isAlert;
    private long time;
    private String camID;
    public Camera(){
    }
    public Camera(String camID, long time){
        this.isOn = true;
        this.isAlert = false;
        this.time = time;
        this.camID = camID;
    }
    @PropertyName(value="isAlert")
    public boolean isAlert(){return isAlert;}
    @PropertyName(value="isOn")
    public boolean isOn(){return isOn;}
    @PropertyName(value="time")
    public long getTime(){return time;}
    @PropertyName(value="camID")
    public String getCamID(){return camID;}
    public boolean turnOn(){
        long now =  System.currentTimeMillis();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyMMddHHmmss");
        String convertTime = timeFormat.format(now);
        if(Long.parseLong(convertTime)-time/1000<=30){
            isOn = true;
//            System.out.println(ID+" is On");
            return true;
        }
        else{
//            System.out.println(ID+" initiate failed");
            return false;
        }
    }
}
