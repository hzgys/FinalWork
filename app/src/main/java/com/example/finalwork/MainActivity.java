package com.example.finalwork;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查登录状态
        if (!isLoggedIn()) {
            // 未登录则跳转到登录界面
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // 获取 NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        NavController navController = navHostFragment.getNavController();

        // 获取底部导航栏实例
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 将底部导航栏与 NavController 关联
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        long userId = sharedPreferences.getLong("user_id", -1);
        String username = sharedPreferences.getString("username", null);
        return userId != -1 && username != null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}