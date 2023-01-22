package com.alyaansohail.pulsebeat;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

public class PlayerSingle implements MediaPlayer.OnPreparedListener {

    private static PlayerSingle instance = null;
    private MediaPlayer mediaPlayer = null;
    Context context;
    private PlayerSingle() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
    }

    public static PlayerSingle getInstance() {
        if (instance == null) {
            instance = new PlayerSingle();
        }
        return instance;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        resume();
    }

    /**
     * This will load the given URL into the media player.
     * @param url The URL string from the Firebase Uri.
     */
    public void play(String url) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void resume() {
        mediaPlayer.start();
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public void release() {
        mediaPlayer.release();
    }
}
