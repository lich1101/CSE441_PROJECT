 package com.example.cse441_project;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

 public class MusicPlayerActivity extends AppCompatActivity{

    TextView titleTv,currentTv,totalTv;
    SeekBar seekBar;
    ImageView pause,play,pause_play,music_Icon;
    NotificationManager notificationManager;
    ArrayList<AudioModel> songsList;
    AudioModel currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();

    int position = 0;
    boolean isPlaying = false;

     boolean isBound = false;
     ExoPlayer player;
     private static final String permission = android.Manifest.permission.READ_EXTERNAL_STORAGE;
     private ActivityResultLauncher<String> storagePermissionLauncher;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        titleTv = findViewById(R.id.Songtitle);
        currentTv = findViewById(R.id.current_time);
        totalTv = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pause = findViewById(R.id.previous);
        play = findViewById(R.id.next);
        pause_play = findViewById(R.id.pause_play);
        music_Icon = findViewById(R.id.music_image);

        titleTv.setSelected(true);

        songsList = (ArrayList<AudioModel>) getIntent().getSerializableExtra("LIST");
        setResourceWithMusic();

        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTv.setText(convertToMMSS(mediaPlayer.getCurrentPosition() + ""));

                    if (mediaPlayer.isPlaying()) {
                        pause_play.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                    } else {
                        pause_play.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                    }
                }
                new Handler(Looper.getMainLooper()).postDelayed(this, 100);

            }
        });
        pause_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    pausePlay();
                }else{
                    pausePlay();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean FromUser) {
                if (mediaPlayer != null && FromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
         // Khởi tạo launcher
         storagePermissionLauncher = registerForActivityResult(
                 new ActivityResultContracts.RequestPermission(),
                 isGranted -> {
                     if (isGranted) {
                         // Quyền đã được cấp, bạn có thể tiếp tục các tác vụ cần thiết
                         doBindService();
                     } else {
                         // Quyền bị từ chối, hiển thị thông báo cho người dùng
                         Toast.makeText(this, "Permission denied to access storage", Toast.LENGTH_SHORT).show();
                     }
                 }
         );
    }


     private void doBindService() {
         Intent playerServiceIntent = new Intent(this, PlayerService.class);
         bindService(playerServiceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE);
         isBound = true;
     }
     ServiceConnection playerServiceConnection = new ServiceConnection() {
         @Override
         public void onServiceConnected(ComponentName name, IBinder service) {
             PlayerService.ServiceBinder binder = (PlayerService.ServiceBinder) service;
             player = binder.getService().player;
             isBound = true;
             storagePermissionLauncher.launch(permission);
         }

         @Override
         public void onServiceDisconnected(ComponentName name) {

         }
     };

    void setResourceWithMusic(){
        currentSong = songsList.get(MyMediaPlayer.currentIndex);
        titleTv.setText(currentSong.getTitle());
        totalTv.setText(convertToMMSS(currentSong.getDuration()));
        pause_play.setOnClickListener(v-> pausePlay());
        play.setOnClickListener(v-> playNextSong());
        pause.setOnClickListener(v-> playPreviousSong());

        if(!mediaPlayer.isPlaying()) {
            playMusic();
        }
    }
    public void playMusic(){
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
            isPlaying = true;

            setAlbumArt(currentSong.getPath());
            startAnimation();
        }
        catch (IOException e){
            e.printStackTrace();
        }
     }
     public void playNextSong(){
        if (MyMediaPlayer.currentIndex == songsList.size()-1)
            return;

        MyMediaPlayer.currentIndex +=1;
        mediaPlayer.reset();
        setResourceWithMusic();

     }
     public void playPreviousSong(){
        if (MyMediaPlayer.currentIndex==0)
            return;
        MyMediaPlayer.currentIndex -=1;
        mediaPlayer.reset();
        setResourceWithMusic();

     }
     public void pausePlay(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            isPlaying = false;
            pause_play.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
            music_Icon.clearAnimation();
        }else{
            mediaPlayer.start();
            isPlaying = true;
            pause_play.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
            startAnimation();
        }

     }

     @SuppressLint("DefaultLocale")
     public static String convertToMMSS(String Duration) {
         long millis = Long.parseLong(Duration);

         return String.format("%02d:%02d",
                 TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                 TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
     }

     private void startAnimation() {
         RotateAnimation rotateAnimation = new RotateAnimation(
                 0f, 360f,
                 RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                 RotateAnimation.RELATIVE_TO_SELF, 0.5f
         );
         rotateAnimation.setDuration(5000);
         rotateAnimation.setRepeatCount(Animation.INFINITE);
         rotateAnimation.setInterpolator(new LinearInterpolator());
         music_Icon.startAnimation(rotateAnimation);
     }

     private void setAlbumArt(String filepath) throws IOException {
         MediaMetadataRetriever retriever = new MediaMetadataRetriever();
         retriever.setDataSource(filepath);
         byte[] art = retriever.getEmbeddedPicture();
         if (art != null) {
             music_Icon.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
         } else {
             music_Icon.setImageResource(R.drawable.musicicon);
         }

         retriever.release();
     }

 }