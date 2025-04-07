package com.example.finalwork;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalwork.dao.UserDao;
import com.example.finalwork.entity.User;

public class LoginActivity extends AppCompatActivity {
    private EditText login_username;
    private EditText login_password;
    private UserDao userDao;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 清除之前的登录状态
        clearLoginState();

        setContentView(R.layout.activity_login);

        // 初始化视图和数据库
        login_username = findViewById(R.id.login_username);
        login_password = findViewById(R.id.login_password);
        userDao = UserDao.getInstance(this);

        // 设置登录按钮点击事件
        findViewById(R.id.login).setOnClickListener(v -> login());

        // 设置注册按钮点击事件
        findViewById(R.id.login_register).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void clearLoginState() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void login() {
        String username = login_username.getText().toString().trim();
        String password = login_password.getText().toString().trim();

        // 输入验证
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 先判断是否是管理员账号
            if ("admin".equals(username) && "admin123".equals(password)) {
                // 保存管理员信息
                SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("user_id", -1); // 使用特殊ID表示管理员
                editor.putString("username", username);
                editor.putString("password", password);
                editor.apply();

                Toast.makeText(this, "管理员登录成功", Toast.LENGTH_SHORT).show();

                // 跳转到管理员界面
                Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            // 普通用户验证
            User user = userDao.loginUser(username, password);
            if (user != null && user.getId() > 0) {
                // 保存用户信息
                SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("user_id", user.getId());
                editor.putString("username", user.getUsername());
                editor.putString("password", password);
                editor.apply();

                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();

                // 跳转到主界面
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "登录失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}