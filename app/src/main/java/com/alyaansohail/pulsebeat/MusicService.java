package com.alyaansohail.pulsebeat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Timer;

public class MusicService extends Service {

    MediaPlayer mediaPlayer;

    boolean mpIsPaused;
    int msLength = 0;

    int currentPlayingIndex = -1;
    int currentPlayingMusicId = -1;
    String currentPlayingUri = "null";

    ArrayList<MusicObject> musicList;

    private boolean mediaPlayerState;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        System.out.println("Started service");
        mpIsPaused = false;
        musicList = new ArrayList<MusicObject>();
//        MusicObject mOne = new MusicObject();
//        mOne.musicName = "awdwad";

//        AudioManager aManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
//        int maxVolume = aManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        aManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
//
//        mediaPlayer = new MediaPlayer()
//     ;
        initializeMusicPlayer();
        startNotification();
//        musicList.add(mOne);
        LocalBroadcastManager.getInstance(this).registerReceiver(getSeekBar,
                new IntentFilter("seek-bar-send"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("check-event"));
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceStatus,
                new IntentFilter("service-status"));
        LocalBroadcastManager.getInstance(this).registerReceiver(getSeekBarChange,
                new IntentFilter("change-seekbar"));
        registerForMusicPlayerBasicFunction();
    }

    private final BroadcastReceiver serviceStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentI = new Intent("service-status-to-activity");
            intentI.putExtra("service_running", true);
            intentI.putExtra("mediaplayer_running",mediaPlayer.isPlaying());
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentI);
            if(mediaPlayer.isPlaying()){
                musicStarted();
            }
        }
    };

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            String msUrl = intent.getStringExtra("url");
            String msName = intent.getStringExtra("msName");
            String msAuthor = intent.getStringExtra("msAuth");
            long msUpTime = intent.getLongExtra("msUpTime",0);
            String msColTh1 = intent.getStringExtra("msColTh1");
            String msColTh2 = intent.getStringExtra("msColTh2");
            int msMusicId = intent.getIntExtra("msMusicId",-1);
            if(message.equals("start_new")){
                musicList.add(createObject(msUrl,msName,msAuthor,msUpTime,msColTh1,msColTh2,msMusicId));
                setMusicPlayerOnMusicAdded(true,musicList.size()-1);
                System.out.println(msAuthor);
                System.out.println(msName);
                System.out.println(msUrl);
                System.out.println(msUpTime);
                System.out.println(msColTh1);
                System.out.println(msColTh2);
                System.out.println(msMusicId);
            }else if(message.equals("add_queue")){
                musicList.add(createObject(msUrl,msName,msAuthor,msUpTime,msColTh1,msColTh2,msMusicId));
            }
        }
    };
    private final BroadcastReceiver getSeekBar = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String res = intent.getStringExtra("need_duration");
            if(res.equals("send_seek_bar_position")){
                sendSeekBar();
            }
        }
    };

    private final BroadcastReceiver getSeekBarChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int positionSeekBar = intent.getIntExtra("seek_bar_change_position",0);
            if(mediaPlayer.isPlaying() || mpIsPaused){
                mediaPlayer.seekTo(positionSeekBar);
            }
        }
    };
//    Laterrrrrrrrrrr+________________-

//Receiving services from Activity---------------------------------------------------------------
    public void registerForMusicPlayerBasicFunction(){
        LocalBroadcastManager.getInstance(this).registerReceiver(calledStopFromActivity,
                new IntentFilter("called-stop-from-activity"));
    }
    private BroadcastReceiver calledStopFromActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMusicPlayer();
        }
    };
//    __________________________________________________________________________________________

//    End register broadcast services______________________________________---
    private MusicObject createObject(String msUrl,String msName,String msAuth,long msUpTime,String msColTh1,String msColTh2,int msMusicId){
        MusicObject mO = new MusicObject();
        mO.setMsUrl(msUrl);
        mO.setMsName(msName);
        mO.setMsAuthor(msAuth);
        mO.setMsUpTime(msUpTime);
        mO.setMsColTheme1(msColTh1);
        mO.setMsColTheme2(msColTh2);
        mO.setMsMusicId(msMusicId);
        return  mO;
    }

    private void setMusicPlayerOnMusicAdded(boolean playNew, int recentlyAdded){
        if(mediaPlayer != null){
            if(playNew){
                System.out.println(recentlyAdded + "was recently added");
                    currentPlayingIndex = recentlyAdded;
                    currentPlayingUri = musicList.get(recentlyAdded).getMsUrl();
                    currentPlayingMusicId = musicList.get(recentlyAdded).getMsMusicId();
                try {
                    startMusicPlayer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
//                if(mediaPlayer.isPlaying() || mpIsPaused){
//
//                } else {
//
//                }
            }
        }else{

        }
    }

    private void sendSeekBar(){
        Intent intent = new Intent("seekBar-change");
        intent.putExtra("mpStateRunning", mediaPlayer.isPlaying());
        intent.putExtra("seekTo",mediaPlayer.getCurrentPosition());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    protected void startMusicPlayer() throws IOException {
        mediaPlayer.reset();
        mediaPlayer.setDataSource(currentPlayingUri);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if(mp == mediaPlayer){
                    mp.start();
                    mpIsPaused = false;
                    musicStarted();
                }
            }

        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                startNextMusicOnUserChangeForward();
            }
        });
        mediaPlayer.prepareAsync();
    }
//    protected void setMusicFile(String uri,int recentlyAdded) throws IOException {
//        mediaPlayer.reset();
//        mediaPlayer.setDataSource(String.valueOf(uri));
//        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                if(mp == mediaPlayer){
//                    mp.start();
//                    currentPlayingIndex = recentlyAdded;
//                    musicStarted();
//                }
//            }
//
//        });
//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                startNextMusicOnUserChangeForward();
//            }
//        });
//        mediaPlayer.prepareAsync();
//    }
//    MediaPlayer Service Functions-----------------------------------------
    public void pauseMusicPlayer(){
        if(mediaPlayer.isPlaying()){
            if(!mpIsPaused){
                msLength = mediaPlayer.getDuration();
                mediaPlayer.pause();
                mpIsPaused = true;
                musicPaused();
            }else{
                resumeMusicPlayer();
            }
        } else{
            resumeMusicPlayer();
        }
    }
    public void resumeMusicPlayer(){
        System.out.println("Doneeeeeeeeeeeeeeeeee");
        if(mediaPlayer != null){
            if(mpIsPaused){
//                mediaPlayer.seekTo(msLength);
                System.out.println("Callleeeeeeeeeeed");
                mediaPlayer.start();
                mpIsPaused = false;
                musicResumed();
            }
        }
    }
    public void startNextMusicOnUserChangeForward(){
        int totalMusic = musicList.size();
        if(currentPlayingIndex >= totalMusic - 1){
            setMusicPlayerOnMusicAdded(true,0);
//            currentPlayingIndex = 0;
        }else{
            currentPlayingIndex++;
            setMusicPlayerOnMusicAdded(true,currentPlayingIndex);
        }
    }
//    _______________________________________________________________________

//    MediaPlayer Calls TO Activity------------------------------------------------------
    public void musicStarted(){
        Intent intent = new Intent("on-music-start");
        // You can also include some extra data.
        intent.putExtra("start_music", "start_new");
        intent.putExtra("started_music_id",musicList.get(currentPlayingIndex).getMsMusicId());
        intent.putExtra("music_duration",mediaPlayer.getDuration());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    public void musicEnded(){
        Intent intent = new Intent("music-ended-MS");

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    public void musicPaused(){
        Intent intent = new Intent("music-paused-MS");

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    public void musicResumed(){
        Intent intent = new Intent("music-resumed-MS");

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    public void playingNext(){
        Intent intent = new Intent("music-next-MS");

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    public void sendAllMusicQueueIds(){
        Intent intent = new Intent("all-queue-music-ids");
        intent.putExtra("musicIds",new Gson().toJson(musicList));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
//________________________________________________________________________________
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;

    }

    public int testFunc(){
        System.out.println("background)_____________________");
        return 566;
    }
    @Override
    public void onDestroy() {
        System.out.println("cancellable was called-------------");
        mediaPlayer.stop();
        mediaPlayer = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(getSeekBar);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceStatus);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(getSeekBarChange);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(calledStopFromActivity);
        stopSelf();
        stopForeground(true);
    }



    public void initializeMusicPlayer(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
    }
    public void changeIconToStopped(){
        Intent intentI = new Intent("music-stopped-changed-icon");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentI);
    }
    public void startNotification(){
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setChannelId(channelId)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setNotificationSilent()
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);
    }
}
