package com.example.finalwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.finalwork.dao.UserDao;
import com.example.finalwork.entity.User;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private EditText register_username;
    private EditText register_password;
    private EditText register_confirm_password;
    private Button register_button;
    private UserDao userDao;

    // 电影类型选择框
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 初始化视图和数据库
        register_username = findViewById(R.id.register_username);
        register_password = findViewById(R.id.register_password);
        register_confirm_password = findViewById(R.id.register_confirm_password);
        register_button = findViewById(R.id.register_button);

        userDao = UserDao.getInstance(this);

        // 设置注册按钮点击事件
        register_button.setOnClickListener(v -> register());
    }



    private void register() {
        String username = register_username.getText().toString().trim();
        String password = register_password.getText().toString().trim();
        String confirmPassword = register_confirm_password.getText().toString().trim();

        RadioGroup preferenceGroup = findViewById(R.id.preferenceGroup);
        int selectedId = preferenceGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this, "请选择一个偏好类型", Toast.LENGTH_SHORT).show();
            return;
        }

        String preferredType = "";
        if (selectedId == R.id.radioStory) {
            preferredType = "剧情";
        } else if (selectedId == R.id.radioComedy) {
            preferredType = "喜剧";
        }else if (selectedId == R.id.radioCrime) {
            preferredType = "犯罪";
        }else if (selectedId == R.id.radioLove) {
            preferredType = "爱情";
        }else if (selectedId == R.id.radioAnimation) {
            preferredType = "动画";
        }else if (selectedId == R.id.radioAdventure) {
            preferredType = "冒险";
        }

        // 输入验证
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 1) {
            Toast.makeText(this, "用户名至少需要1个字符", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 3) {
            Toast.makeText(this, "密码至少需要3个字符", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }



        try {
            // 创建新用户
            User user = new User(username, password);
            long userId = userDao.registerUser(user);

            if (userId > 0) {
                // 保存用户信息到SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("user_id", userId);
                editor.putString("username", username);
                editor.putString("password", password);

                userDao.saveUserPreference(userId, preferredType);
                // 保存用户选择的电影类型

                editor.apply();

                Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();

                // 直接跳转到主界面
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "注册失败，用户名可能已存在", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}