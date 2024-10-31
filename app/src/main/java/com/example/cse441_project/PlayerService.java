package com.example.cse441_project;

import static android.app.NotificationManager.IMPORTANCE_HIGH;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.os.Binder;
import android.os.IBinder;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.util.Objects;

public class PlayerService extends Service {
    //member
    private final IBinder serviceBinder = new ServiceBinder();
    //player
    ExoPlayer player;
    PlayerNotificationManager notificationManager;
  // class binder for clients
    public class ServiceBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
  }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        player = new ExoPlayer.Builder(getApplicationContext()).build();

        //audio focus attributed
        AudioAttributes.Builder audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);


        player.setAudioAttributes(audioAttributes, true);

        final String channelId = getResources().getString(R.string.app_name) + "Music Channel";
        final int notificationId = 11111;
        notificationManager = new PlayerNotificationManager.Builder(this, notificationId, channelId)
                .setNotificationListener(notificationListener)
                .setMediaDescriptionAdapter(descriptionAdapter)
                .setChannelImportance(IMPORTANCE_HIGH)
                .setChannelDescriptionResourceId(R.string.app_name)
                .setNextActionIconResourceId(R.drawable.baseline_skip_next_24)
                .setPreviousActionIconResourceId(R.drawable.baseline_skip_previous_24)
                .setPauseActionIconResourceId(R.drawable.ic_baseline_pause_circle_filled_24)
                .setPlayActionIconResourceId(R.drawable.ic_baseline_play_circle_filled_24)
                .setChannelNameResourceId(R.string.app_name)
                .build();


        // set player to notification manager
        notificationManager.setPlayer(player);
        notificationManager.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationManager.setUseRewindAction(false);
        notificationManager.setUseFastForwardAction(false);


    }

    @Override
    public void onDestroy() {
        // release the player
        if (player.isPlaying())
            player.stop();
        notificationManager.setPlayer(null);
        player.release();
        player = null;
        stopForeground(true);
        stopSelf();
         super.onDestroy();
    }

    // notification listener
    PlayerNotificationManager.NotificationListener notificationListener = new PlayerNotificationManager.NotificationListener() {
        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            PlayerNotificationManager.NotificationListener.super.onNotificationCancelled(notificationId, dismissedByUser);
            stopForeground(true);
            if (player.isPlaying()) {
                player.pause();
            }
        }

        @Override
        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
            PlayerNotificationManager.NotificationListener.super.onNotificationPosted(notificationId, notification, ongoing);
            startForeground(notificationId, notification);
        }
    };





    //notification description adapter
    PlayerNotificationManager.MediaDescriptionAdapter descriptionAdapter = new PlayerNotificationManager.MediaDescriptionAdapter() {
        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            return Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title;
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            // inter to the open the app when clicked
            Intent openAppIntent = new Intent(PlayerService.this, MainActivity.class);
            return PendingIntent.getActivity(getApplicationContext(),0,openAppIntent,PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            return null;
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            // try creating an Image view on the fly then get its drawable
            ImageView view = new ImageView(getApplicationContext());
            view.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);

            // get view drawable
            BitmapDrawable bitmapDrawable = (BitmapDrawable) view.getDrawable();
            if (bitmapDrawable == null) {
                bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(getApplicationContext(), R.drawable.musicicon);
            }

            assert bitmapDrawable != null;
            return bitmapDrawable.getBitmap();
        }
    };

}
