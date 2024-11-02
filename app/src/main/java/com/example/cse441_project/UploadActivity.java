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
    MusicAdapter MusicAdapter;
    SearchView searchView;

    ArrayList<AudioModel> SongsList =  new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));


        recyclerView = findViewById(R.id.recycle_view);
        noMusicTextView = findViewById(R.id.No_Songs);


        MusicAdapter =new MusicAdapter(SongsList, getApplicationContext());
        recyclerView.setAdapter(MusicAdapter);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!checkPermission()) {
                requestPermission();
                return ;
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

        @SuppressLint("Recycle")
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null
        );

        while (Objects.requireNonNull(cursor).moveToNext()) {
            AudioModel songData = new AudioModel(cursor.getString(1), cursor.getString(0), cursor.getString(2));
            if (new File(songData.getPath()).exists())
                SongsList.add(songData);
        }
        if (SongsList.isEmpty()) {
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
        return result == PackageManager.PERMISSION_GRANTED;
    }
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(UploadActivity.this, android.Manifest.permission.READ_MEDIA_AUDIO)){
            Toast.makeText(UploadActivity.this,"PERMISSION NEEDED",Toast.LENGTH_SHORT).show();
        }else
            ActivityCompat.requestPermissions(UploadActivity.this,new String[]{Manifest.permission.READ_MEDIA_AUDIO},200);
    }


    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.search_btn, menu);

        //search btn item
        MenuItem menuItem = menu.findItem(R.id.searchView);
        searchView = (SearchView) menuItem.getActionView();

        //search song method
        SearchSong(Objects.requireNonNull(searchView));

        return super.onCreateOptionsMenu(menu);

    }

    private void SearchSong(SearchView searchView) {

        //search view listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //filter songs
                filterSongs(newText.toLowerCase());
                return true;
            }
        });
    }
    private void filterSongs(String query) {
        ArrayList<AudioModel> filteredList = new ArrayList<>();

        if(!SongsList.isEmpty()){
            for(AudioModel song : SongsList){
                if (song.getTitle().toLowerCase().contains(query)){
                    filteredList.add(song);
                }
            }

            if (MusicAdapter != null){
                MusicAdapter.filterSongs(filteredList);
            }
        }


    }


}
