package com.example.cse441_project;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class UploadActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView noMusicTextView;
    MusicAdapter musicAdapter;
    SearchView searchView;

    ArrayList<AudioModel> songsList = new ArrayList<>();
    ArrayList<AudioModel> songsListAll = new ArrayList<>(); // New list to keep all songs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));

        recyclerView = findViewById(R.id.recycle_view);
        noMusicTextView = findViewById(R.id.No_Songs);

        musicAdapter = new MusicAdapter(songsList, getApplicationContext());
        recyclerView.setAdapter(musicAdapter);

        // Kiểm tra và yêu cầu quyền truy cập
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!checkPermission()) {
                requestPermission();
                return;
            }
        }

        loadSongs();

        if (songsList.isEmpty()) {
            noMusicTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(musicAdapter);
        }
    }

    private void loadSongs() {
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        @SuppressLint("Recycle")
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);

        while (Objects.requireNonNull(cursor).moveToNext()) {
            AudioModel songData = new AudioModel(cursor.getString(1), cursor.getString(0), cursor.getString(2));
            if (new File(songData.getPath()).exists()) {
                songsList.add(songData);
                songsListAll.add(songData); // Add to both lists
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.READ_MEDIA_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(UploadActivity.this, Manifest.permission.READ_MEDIA_AUDIO)) {
            Toast.makeText(UploadActivity.this, "PERMISSION NEEDED", Toast.LENGTH_SHORT).show();
        } else
            ActivityCompat.requestPermissions(UploadActivity.this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 200);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_btn, menu);

        MenuItem menuItem = menu.findItem(R.id.searchView);
        searchView = (SearchView) menuItem.getActionView();

        SearchSong(searchView);

        return super.onCreateOptionsMenu(menu);
    }

    private void SearchSong(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText.toLowerCase());
                return true;
            }
        });
    }

    private void filterSongs(String query) {
        ArrayList<AudioModel> filteredList = new ArrayList<>();

        if (!query.isEmpty()) {
            for (AudioModel song : songsListAll) {
                if (song.getTitle().toLowerCase().contains(query)) {
                    filteredList.add(song);
                }
            }
        } else {
            filteredList.addAll(songsListAll); // Show all if search is cleared
        }

        // Update the adapter with the filtered list
        musicAdapter.filterSongs(filteredList);

        // Show "No Songs" text if filtered list is empty
        if (filteredList.isEmpty()) {
            noMusicTextView.setVisibility(View.VISIBLE);
        } else {
            noMusicTextView.setVisibility(View.GONE);
        }
    }
}
