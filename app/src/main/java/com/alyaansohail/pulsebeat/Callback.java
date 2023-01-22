package com.alyaansohail.pulsebeat;

public interface Callback {
    void callStopIcon(int id,int plyMusicID);
    void callChangeLastPlayedIcon(int lastItemId);
    MusicObject getByMusicIdToMainActivity(int mSId);
}

