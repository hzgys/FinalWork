package com.example.finalwork;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalwork.adapter.UserDBAdapter;
import com.example.finalwork.db.UserDatabaseHelper;
import com.example.finalwork.entity.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText et_username;
    private EditText et_password;
    private UserDBAdapter userDBAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        et_username=findViewById(R.id.et_username);
        et_password=findViewById(R.id.et_password);

        //初始化数据库帮助类
        userDBAdapter = new UserDBAdapter(this);

        //返回的点击事件
        findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //不要跳转到登录 直接销毁就行 不然会新开一个登录界面
                finish();
            }
        });


        //点击注册时间
        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });




    }
    private void registerUser() {
        String username = et_username.getText().toString();
        String password = et_password.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入用户名或密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userDBAdapter.checkUsernameExists(username)) { // 实例调用
            Toast.makeText(this, "用户名已存在，请选择其他用户名", Toast.LENGTH_SHORT).show();

            return;
        }

        User user = new User(username, password);
        long newRowId = userDBAdapter.insertUser(user);

        if (newRowId != -1) {
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            finish(); // 注册成功后关闭注册页面
        } else {
            Toast.makeText(this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }


}