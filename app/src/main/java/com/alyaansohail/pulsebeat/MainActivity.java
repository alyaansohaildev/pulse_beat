package com.alyaansohail.pulsebeat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.telecom.Call;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.scwang.wave.MultiWaveHeader;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity{

    private RelativeLayout linearLayoutBottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView musicNameBS;
    private TextView authorNameBS;

    private NotificationManager notificationManager;

    private ServiceObject srvData;

    Timer timer;

    ProgressBar prgPlay;
    Button mostViewed;
    ImageButton btnStart;
    SeekBar seekBar;

    ImageButton openCloseBtmSheet;
    CoordinatorLayout rootLayout;
    ConnectivityManager manager;

    MultiWaveHeader waveHeader;

    Callback cll;

    int selectedItemId;

    boolean jamClick;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timer = new Timer();

        jamClick = false;

        srvData = new ServiceObject();

        LocalBroadcastManager.getInstance(this).registerReceiver(receiveServiceStatus,new IntentFilter("service-status-to-activity"));

        Intent intentI = new Intent("service-status");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentI);
        rootLayout = findViewById(R.id.main_root_layout);

        linearLayoutBottomSheet = findViewById(R.id.bottom_sheet_layout);
        musicNameBS = findViewById(R.id.music_name_BS);
        authorNameBS = findViewById(R.id.author_name_BS);


        bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBottomSheet);
        bottomSheetBehavior.setDraggable(false);
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        openCloseBtmSheet = findViewById(R.id.bottom_sheet_open_close_button);

        prgPlay = findViewById(R.id.progress_bar_play);
        mostViewed = findViewById(R.id.most_viewed);
        btnStart = findViewById(R.id.play_control);
        seekBar = findViewById(R.id.player_seekbar);
        setSeekBarChangeListener();
        setToDefaultMusicPlayerUi();

        waveHeader = findViewById(R.id.multi_wave_header);

        waveHeader.setVelocity(1.4f);
        waveHeader.setProgress(1);
        waveHeader.isRunning();
        waveHeader.setGradientAngle(0);
        waveHeader.stop();
        waveHeader.setWaveHeight(70);


        startService(new Intent(getBaseContext(),MusicService.class));
        registerSeekBarService();
        registerMsCallsFromService();
        sendSeekBar();

        openCloseButtonListener();
        loadDataWrapper();

//        RotateAnimation rotate = new RotateAnimation(0, 90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        rotate.setDuration(300);
//        rotate.setInterpolator(new LinearInterpolator());

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    openCloseBtmSheet.animate().rotation(270).start();
                    setMusicDetailsOnBtmSheet();
                    srvData.allowSeekBarSend = true;
                    if(srvData != null){
                        System.out.println("not NUllll");
                        if(srvData.mediaPlayerRunning){
                            System.out.println("not NUllll mediaplayer");
                            waveHeader.start();
                        }
                    }
                }else if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                    openCloseBtmSheet.animate().rotation(90).start();
                    srvData.allowSeekBarSend = false;
                    if(srvData == null){
                        waveHeader.stop();
                    }else if(!srvData.mediaPlayerRunning){
                        waveHeader.stop();
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,
                (ViewGroup) findViewById(R.id.toast_layout_id));
        TextView textView = layout.findViewById(R.id.text_toast);
        textView.setText("Something to Say!");

        btnStart.setOnClickListener(v -> {
            sendServiceToPauseMusic();
        });


//            Toast toast = new Toast(getApplicationContext());
//            toast.setGravity(Gravity.BOTTOM|Gravity.FILL_HORIZONTAL, 0, 0);
//            toast.setDuration(Toast.LENGTH_LONG);
//            toast.setView(layout);
//            toast.show();

//        Button button = findViewById(R.id.most_viewed);
//        button.setOnClickListener(v -> {
//            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//        });


    }
//    Notify and Internet Connection--------------------------------------
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void notifySimple(String notifyString){
        Snackbar snackbar = Snackbar
                .make(rootLayout, notifyString, Snackbar.LENGTH_INDEFINITE);
        View snBarView = snackbar.getView();
        snBarView.setBackgroundResource(R.color.primary_one);
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.show();
    }
    private void notifyAction(String notifyString){
        Snackbar snackbar = Snackbar
                .make(rootLayout, notifyString, Snackbar.LENGTH_INDEFINITE);
        View snBarView = snackbar.getView();
        snBarView.setBackgroundResource(R.color.primary_one);
        snackbar.setTextColor(getResources().getColor(R.color.white));
        snackbar.setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
                loadDataWrapper();
            }
        });
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.show();
    }
    public void loadDataWrapper(){
        if (isNetworkConnected()){
            loadData();
        } else {
            notifyAction("No Connection");
        }
    }
//_______________________________________________________________________________
    private void openCloseButtonListener(){
        openCloseBtmSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
    }
    protected void setLinearLayoutBottomSheetToOpen(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    protected void setLinearLayoutBottomSheetToClose(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void registerCallback(Callback callback){
        cll = callback;
    }

    private void loadData(){
        System.out.println("CAllled again __________________");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("topLevel/secondLevel/thirdLevel/musicData");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String response = new Gson().toJson(dataSnapshot.getValue());
                System.out.println(response + "is resource __________________");
                Bundle bundle = new Bundle();
                bundle.putString("objectMain",response);

                FragmentManager fragmentManager = getSupportFragmentManager();

                MostPlayedFragment firstFragment = new MostPlayedFragment(bundle);

                registerCallback(firstFragment);

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container,firstFragment);
                fragmentTransaction.commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if(DatabaseError.NETWORK_ERROR == error.getCode()){
                    System.out.println("wwwwwww wwwwwwwwwww");
                }else{
                    System.out.println(error);
                }
            }

        });
    }

    private final BroadcastReceiver receiveServiceStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean service_running = intent.getBooleanExtra("service_running",false);
            if(service_running){
                srvData.serviceRunning = true;
                boolean mediaplayer_running = intent.getBooleanExtra("mediaplayer_running",false);
                System.out.println(mediaplayer_running + "Is thattttttttttt");

                if(mediaplayer_running){
                    srvData.mediaPlayerRunning = true;
                    sendSeekBar();
                    System.out.println("Media Player is running _________________________________--");
                }
            }
        }
    };
//    private ServiceConnection musicService = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            BindedMusicService.MyBinder mLocalBinder = (BindedMusicService.MyBinder) service;
//            musicServiceBounded = true;
//            musicServiceM = mLocalBinder.getService();
//            musicServiceM.setCallbacks(MainActivity.this);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            musicServiceBounded = false;
//            musicServiceM = null;
//        }
//    };

    @Override
    protected void onStart() {
        super.onStart();
//        Intent mIntent = new Intent(this, BindedMusicService.class);
//        startService(mIntent);
//        bindService(mIntent, musicService, BIND_AUTO_CREATE);
    }
    public void endMusicService(){
        cancelSeekBar();
        if(srvData != null){
            if(!srvData.mediaPlayerRunning || srvData.isMediaPlayerPaused){
                stopService(new Intent(getBaseContext(),MusicService.class));
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiveServiceStatus);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(seekBarFunction);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(onMusicStartReceiver);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(musicWasEnded);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(musicWasResumed);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(musicWasPaused);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(musicWasStartedAg);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(onMusicIdListReceived);
            }
        }else{
            stopService(new Intent(getBaseContext(),MusicService.class));
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiveServiceStatus);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(seekBarFunction);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(onMusicStartReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(musicWasEnded);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(musicWasResumed);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(musicWasPaused);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(musicWasStartedAg);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(onMusicIdListReceived);
        }
//        if(musicServiceBounded != null) {
//            unbindService(musicService);
//            musicServiceBounded = false;
//        }
    }
//    @Override
//    protected void onStop() {
//        super.onStop();
//        endMusicService();
//    };

// -----------------  D ----------------------------

    private void setCurrentlyPlayingMusic(int msId){
        srvData.currentlyPlayingMsId = msId;
    }


    protected void playMusic(String msUrl,String msName,
                             String msAuth,long msUpTime,
                             String msColTh1,String msColTh2,
                             int msMusicId,
                             int startType,int itemId,int OldMusicId){
        selectedItemId = itemId;

        cll.callChangeLastPlayedIcon(OldMusicId);

        setIconToLoadForMainMusicPlayer();

        final FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(msUrl);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Intent intent = new Intent("check-event");
            // You can also include some extra data.
            if(startType == 0){
                intent.putExtra("message", "start_new");
            }else if(startType == 1){
                intent.putExtra("message", "add_queue");
            }
            intent.putExtra("url",String.valueOf(uri));
            intent.putExtra("msName",msName);
            intent.putExtra("msAuth",msAuth);
            intent.putExtra("msUpTime",msUpTime);
            intent.putExtra("msColTh1",msColTh1);
            intent.putExtra("msColTh2",msColTh2);
            intent.putExtra("msMusicId",msMusicId);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        })
        .addOnFailureListener(e -> Log.i("TAG", e.getMessage()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endMusicService();
//        if(isRegisteredMpStateReceiver) {
//            LocalBroadcastManager.getInstance(this).unregisterReceiver(seekBarFunction);
//        }
//        stopService(new Intent(this, MusicService.class));
//        PlayerSingle.getInstance().release();
    }

    public void setSeekBarChangeListener(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    Intent intentA= new Intent("change-seekbar");
                    intentA.putExtra("seek_bar_change_position",progress);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentA);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

//        --------------------- Sending Services ---------------------------------------
    private void sendSeekBar(){
        if(srvData != null){
            if(!srvData.seekBarTimer){
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (srvData.allowSeekBarSend) {
                            Intent intent = new Intent("seek-bar-send");
                            // You can also include some extra data.
                            intent.putExtra("need_duration", "send_seek_bar_position");
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        }
                    }
                }, 1000, 1000);
                srvData.seekBarTimer = true;
            }

        }
    }
    private void cancelSeekBar(){
        if(timer != null) timer.cancel();
    }
    private void sendServiceToPauseMusic(){
        Intent intent = new Intent("called-stop-from-activity");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

// ------------------------Receiving services ---------------------------------------
    private void registerSeekBarService(){
        LocalBroadcastManager.getInstance(this).registerReceiver(seekBarFunction,new IntentFilter("seekBar-change"));
//        LocalBroadcastManager.getInstance(this).registerReceiver(musicEndedButton,new IntentFilter("music-stopped-changed-icon"));
        LocalBroadcastManager.getInstance(this).registerReceiver(onMusicStartReceiver,new IntentFilter("on-music-start"));
//        LocalBroadcastManager.getInstance(this)
    }

    private final BroadcastReceiver seekBarFunction = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean mediaPlayerState = intent.getBooleanExtra("mpStateRunning",false);
            if(mediaPlayerState) {
                int seekTo = intent.getIntExtra("seekTo", 0);
                seekTo(seekTo);
            }
        }
    };

    private final BroadcastReceiver onMusicStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int playingIndexMusicId = intent.getIntExtra("started_music_id",-1);
            int resDuration = intent.getIntExtra("music_duration",0);
            startSeekBar(resDuration,playingIndexMusicId);
        }
    };


//________________________________________________________________________________________________

//    MusicPlayer Calls from Service---------------------------------------------------------------
    private void registerMsCallsFromService(){
        LocalBroadcastManager.getInstance(this).registerReceiver(musicWasEnded,
                new IntentFilter("music-ended-MS"));
        LocalBroadcastManager.getInstance(this).registerReceiver(musicWasResumed,
                new IntentFilter("music-resumed-MS"));
        LocalBroadcastManager.getInstance(this).registerReceiver(musicWasPaused,
                new IntentFilter("music-paused-MS"));
        LocalBroadcastManager.getInstance(this).registerReceiver(musicWasStartedAg,
                new IntentFilter("music-next-MS"));
        LocalBroadcastManager.getInstance(this).registerReceiver(onMusicIdListReceived,
                new IntentFilter("all-queue-music-ids"));
    }
    private final BroadcastReceiver musicWasEnded = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showPlayIcon();
        }
    };
    private final BroadcastReceiver musicWasPaused = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showPlayIcon();
            System.out.println("Paused was callleedddddddd");
        }
    };
    private final BroadcastReceiver musicWasResumed = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showStopIcon();
            System.out.println("Stooped  was callleedddddddd");
        }
    };
    private final BroadcastReceiver musicWasStartedAg = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
    private final BroadcastReceiver onMusicIdListReceived = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
//    _________________________________________________________________________
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
//        showNotification(getApplicationContext(),"it works","starting text",getIntent());

    }


//    @Override


//    @Override



//    Music UX Functions-----------------------------------------------------------------------
    public void setToDefaultMusicPlayerUi(){
        seekBar.setMax(100);
        seekBar.setProgress(0);
    }
    public void startSeekBar(int duration,int playingIndexMusicId) {
        setLinearLayoutBottomSheetToOpen();
        setIconToStopForMainMusicPlayer();
        seekBar.setMax(duration);
        cll.callStopIcon(selectedItemId,playingIndexMusicId);
        setCurrentlyPlayingMusic(playingIndexMusicId);
    }
    public void setIconToLoadForMainMusicPlayer(){
        btnStart.setVisibility(View.GONE);
        prgPlay.setVisibility(View.VISIBLE);
        srvData.mediaPlayerRunning = false;
    }
    public void setIconToStopForMainMusicPlayer(){
        btnStart.setVisibility(View.VISIBLE);
        prgPlay.setVisibility(View.GONE);
        btnStart.setImageResource(R.drawable.ic_baseline_stop_24);
        srvData.mediaPlayerRunning = true;
    }
    public void seekTo(int position) {
        seekBar.setProgress(position);
    }
    public void showStopIcon(){
        btnStart.setImageResource(R.drawable.ic_baseline_stop_24);
//        srvData.mediaPlayerRunning = true;
        waveHeader.start();
    }
    public void showPlayIcon(){
        btnStart.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        srvData.mediaPlayerRunning = false;
        waveHeader.stop();
//        srvData.mediaPlayerRunning = false;
    }

    public void setMusicDetailsOnBtmSheet(){
        if(srvData!= null){
            if(srvData.currentlyPlayingMsId > -1){
                MusicObject currentlyPlMsObject = cll.getByMusicIdToMainActivity(srvData.currentlyPlayingMsId);
                musicNameBS.setText(currentlyPlMsObject.getMsName());
                musicNameBS.setVisibility(View.INVISIBLE);
                musicNameBS.setVisibility(View.VISIBLE);
                musicNameBS.setAlpha(0.0f);
                musicNameBS.setTranslationY(20);
                musicNameBS.animate()
                        .translationY(0)
                        .alpha(1.0f)
                        .setDuration(400)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                            }
                        });
                authorNameBS.setText(currentlyPlMsObject.getMsAuthor());
                authorNameBS.setVisibility(View.INVISIBLE);
                authorNameBS.setVisibility(View.VISIBLE);
                authorNameBS.setAlpha(0.0f);
                authorNameBS.setTranslationY(20);
                authorNameBS.animate()
                        .translationY(0)
                        .alpha(1.0f)
                        .setDuration(400)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                            }
                        });
            }
        }
    }
//    ___________________________________________________________________________________________

// Service Object----------------------------------------------------------------------------------
    private static class ServiceObject{
        boolean serviceRunning;
        boolean mediaPlayerRunning;
        boolean isMediaPlayerPaused;
        boolean allowSeekBarSend;
        boolean seekBarTimer;

        int currentlyPlayingMsId;
        ArrayList<Integer> allMusicIds;
        ServiceObject(){
            serviceRunning = false;
            mediaPlayerRunning = false;
            isMediaPlayerPaused = false;
            allowSeekBarSend = false;
            seekBarTimer = false;

            currentlyPlayingMsId = -1;
            allMusicIds = new ArrayList<>();

        }
    }

}