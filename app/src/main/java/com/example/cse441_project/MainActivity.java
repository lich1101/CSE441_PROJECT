package com.example.cse441_project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cse441_project.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        funBottomNavigation();
    }

    private void funBottomNavigation(){
        //Set Default item: when app open
        bottomNavigationView.setSelectedItemId(R.id.library);
        //Set Default Fragment with item: when app open
        loadNavFragment(new LibraryFragment());

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id= item.getItemId();

                // in my project "menuHome" is id in Menu file
                if (id==R.id.library){
                    loadNavFragment(new LibraryFragment());
                } else if (id==R.id.discover) {
                    loadNavFragment(new DiscoverFragment());
                } else if (id==R.id.chart) {
                    loadNavFragment(new ChartFragment());
                } else if (id==R.id.radio) {
                    loadNavFragment(new RadioFragment());
                } else {
                    loadNavFragment(new PersonFragment());
                }
                // true then changes effect of items
                return true;
            }
        });
    }

    private void  loadNavFragment(Fragment fragment){
        //This funcation for load fragment directly

        FragmentManager fragmentManager= getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();

        //use replace
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}