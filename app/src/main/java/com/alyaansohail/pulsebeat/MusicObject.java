package com.alyaansohail.pulsebeat;

import java.util.ArrayList;

public class MusicObject{
    public String getMsAuthor() {
        return msAuthor;
    }

    public void setMsAuthor(String msAuthor) {
        this.msAuthor = msAuthor;
    }

    public String getMsName() {
        return msName;
    }

    public void setMsName(String msName) {
        this.msName = msName;
    }

    public long getMsUpTime() {
        return msUpTime;
    }

    public void setMsUpTime(long msUpTime) {
        this.msUpTime = msUpTime;
    }

    public String getMsUrl() {
        return msUrl;
    }

    public void setMsUrl(String msUrl) {
        this.msUrl = msUrl;
    }

    public String getMsColTheme1() {
        return msColTheme1;
    }

    public void setMsColTheme1(String msColTheme1) {
        this.msColTheme1 = msColTheme1;
    }

    public String getMsColTheme2() {
        return msColTheme2;
    }

    public void setMsColTheme2(String msColTheme2) {
        this.msColTheme2 = msColTheme2;
    }

    public int getMsMusicId() {
        return msMusicId;
    }

    public void setMsMusicId(int msMusicId) {
        this.msMusicId = msMusicId;
    }

    String msAuthor;
    String msName;
    long msUpTime;
    String msUrl;
    String msColTheme1;
    String msColTheme2;
    int msMusicId;

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    long itemId;

}
