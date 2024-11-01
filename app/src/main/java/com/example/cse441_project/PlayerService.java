package com.example.cse441_project;

import static android.app.NotificationManager.IMPORTANCE_HIGH;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.IBinder;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.util.Objects;

public class PlayerService extends Service {
    private final IBinder serviceBinder = new ServiceBinder();
    ExoPlayer player;
    PlayerNotificationManager notificationManager;

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
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA) // Sử dụng hằng số từ ExoPlayer
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC) // Sử dụng hằng số từ ExoPlayer
                .build();
        player.setAudioAttributes(audioAttributes, true);

        String channelId = getResources().getString(R.string.app_name) + "Music Channel";
        int notificationId = 11111;
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

        notificationManager.setPlayer(player);
        notificationManager.setPriority(NotificationCompat.PRIORITY_MAX);
        notificationManager.setUseRewindAction(false);
        notificationManager.setUseFastForwardAction(false);

        // Khởi chạy Foreground Service với thông báo
        startForeground(notificationId, createNotification());
    }

    private Notification createNotification() {
        // Tạo một thông báo cơ bản cho dịch vụ nền
        return new NotificationCompat.Builder(this, getResources().getString(R.string.app_name) + "Music Channel")
                .setContentTitle("Music Player")
                .setContentText("Playing music in the background")
                .setSmallIcon(R.drawable.musicicon) // Icon cho thông báo
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();
    }
    @Override
    public void onDestroy() {
        if (player.isPlaying()) player.stop();
        notificationManager.setPlayer(null);
        player.release();
        player = null;
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    private final PlayerNotificationManager.NotificationListener notificationListener = new PlayerNotificationManager.NotificationListener() {
        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            stopForeground(true);
            if (player.isPlaying()) player.pause();
        }

        @Override
        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
            startForeground(notificationId, notification);
        }
    };

    private final PlayerNotificationManager.MediaDescriptionAdapter descriptionAdapter = new PlayerNotificationManager.MediaDescriptionAdapter() {
        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            return Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title;
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            Intent openAppIntent = new Intent(PlayerService.this, MainActivity.class);
            return PendingIntent.getActivity(getApplicationContext(), 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            return null;
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            ImageView view = new ImageView(getApplicationContext());
            view.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);
            BitmapDrawable bitmapDrawable = (BitmapDrawable) view.getDrawable();
            if (bitmapDrawable == null) {
                bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(getApplicationContext(), R.drawable.musicicon);
            }
            assert bitmapDrawable != null;
            return bitmapDrawable.getBitmap();
        }
    };
}