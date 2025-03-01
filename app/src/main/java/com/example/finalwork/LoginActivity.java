package com.example.finalwork;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalwork.adapter.UserDBAdapter;

public class LoginActivity extends AppCompatActivity {


    private EditText login_username;
    private EditText login_password;
    private UserDBAdapter userDBAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_username = findViewById(R.id.login_username);
        login_password = findViewById(R.id.login_password);
        userDBAdapter = new UserDBAdapter(this);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        findViewById(R.id.login_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void login() {
        String username = login_username.getText().toString();
        String password = login_password.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入用户名或密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userDBAdapter.authenticate(username, password)) {
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            // 这里可以跳转到主页面或者其他操作
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "用户名或密码错误，请重试", Toast.LENGTH_SHORT).show();
        }
    }

}