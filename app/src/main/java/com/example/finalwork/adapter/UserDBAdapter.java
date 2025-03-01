package com.example.finalwork.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.finalwork.db.UserDatabaseHelper;
import com.example.finalwork.entity.User;

public class UserDBAdapter {
    private UserDatabaseHelper dbHelper;

    public UserDBAdapter(Context context) {
        dbHelper = new UserDatabaseHelper(context);
    }

    // 插入用户
    public long insertUser(User user) {
        SQLiteDatabase db = null;
        long result = -1;
        try {
            db = dbHelper.getWritableDatabase(); // 获取可写数据库
            ContentValues values = new ContentValues();
            values.put("username", user.getUsername());
            values.put("password", user.getPassword());
            result = db.insert("users", null, values); // 插入用户数据
        } finally {
            if (db != null && db.isOpen()) {
                db.close(); // 确保关闭数据库
            }
        }
        return result;
    }

    // 用户认证
    public boolean authenticate(String username, String password) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean authenticated = false;
        try {
            db = dbHelper.getReadableDatabase(); // 获取可读数据库
            cursor = db.query("users", new String[]{"id"}, "username = ? AND password = ?",
                    new String[]{username, password}, null, null, null);
            authenticated = (cursor != null && cursor.getCount() > 0); // 验证用户是否存在
        } finally {
            if (cursor != null) {
                cursor.close(); // 确保关闭游标
            }
            if (db != null && db.isOpen()) {
                db.close(); // 确保关闭数据库
            }
        }
        return authenticated;
    }

    // 检查用户名是否已存在
    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean exists = false;
        try {
            db = dbHelper.getReadableDatabase(); // 获取可读数据库
            cursor = db.query("users", new String[]{"id"}, "username = ?",
                    new String[]{username}, null, null, null);
            exists = (cursor != null && cursor.getCount() > 0); // 检查记录是否存在
        } finally {
            if (cursor != null) {
                cursor.close(); // 确保关闭游标
            }
            if (db != null && db.isOpen()) {
                db.close(); // 确保关闭数据库
            }
        }
        return exists;
    }
}
