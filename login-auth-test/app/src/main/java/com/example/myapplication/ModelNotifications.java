package com.example.myapplication;

public class ModelNotifications {
    String pId, timestamp, pUid, notification, sUid, sName, sEmail;

    public ModelNotifications(String pId, String timestamp, String pUid, String notification, String sUid, String sName, String sEmail) {
        this.pId = pId;
        this.timestamp = timestamp;
        this.pUid = pUid;
        this.notification = notification;
        this.sUid = sUid;
        this.sName = sName;
        this.sEmail = sEmail;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getpUid() {
        return pUid;
    }

    public void setpUid(String pUid) {
        this.pUid = pUid;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getsUid() {
        return sUid;
    }

    public void setsUid(String sUid) {
        this.sUid = sUid;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getsEmail() {
        return sEmail;
    }

    public void setsEmail(String sEmail) {
        this.sEmail = sEmail;
    }
}
