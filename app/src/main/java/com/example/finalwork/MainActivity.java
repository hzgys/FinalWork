package com.example.finalwork;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取底部导航栏
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 获取导航控制器
        NavController navController = Navigation.findNavController(this, R.id.fragment_container);

        // 设置 AppBarConfiguration
        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.navigation_home);
        topLevelDestinations.add(R.id.navigation_list);
        topLevelDestinations.add(R.id.navigation_collection);
        topLevelDestinations.add(R.id.navigation_mine);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();

        // 将导航控制器与底部导航栏关联
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }
}