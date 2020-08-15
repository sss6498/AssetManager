package com.example.assetmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.assetsFragment);

    }

    assetsFragment assets = new assetsFragment();
    budgetFragment budget = new budgetFragment();
    profileFragment profile = new profileFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.assetsFragment:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, assets).commit();
                return true;
            case R.id.budgetFragment:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, budget).commit();
                return true;
            case R.id.profileFragment:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, profile).commit();
                return true;
        }
        return false;
    }
}
