 package com.example.cse441_project;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

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
    }

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
 }