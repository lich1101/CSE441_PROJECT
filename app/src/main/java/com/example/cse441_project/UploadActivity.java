package com.example.cse441_project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView noMusicTextView;

    ArrayList<AudioModel> SongsList =  new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        recyclerView = findViewById(R.id.recycle_view);
        noMusicTextView = findViewById(R.id.No_Songs);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!checkPermission()) {
                requestPermission();
                return;
            }
        }
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.SIZE
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null
        );

        while (cursor.moveToNext()) {
            AudioModel songData = new AudioModel(cursor.getString(1), cursor.getString(0), cursor.getString(2));
            if (new File(songData.getPath()).exists())
                SongsList.add(songData);
        }
        if (SongsList.size() == 0) {
            noMusicTextView.setVisibility(View.VISIBLE);
        } else {
            //recyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new MusicAdapter(SongsList, getApplicationContext()));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(UploadActivity.this, android.Manifest.permission.READ_MEDIA_AUDIO);
        if(result == PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            return false;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(UploadActivity.this, android.Manifest.permission.READ_MEDIA_AUDIO)){
            Toast.makeText(UploadActivity.this,"PERMISSION NEEDED",Toast.LENGTH_SHORT).show();
        }else
            ActivityCompat.requestPermissions(UploadActivity.this,new String[]{Manifest.permission.READ_MEDIA_AUDIO},200);
    }
}
