package com.example.finalwork.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.finalwork.db.UserDatabaseHelper;
import com.example.finalwork.entity.User;

public class UserDBAdapter {
    private final UserDatabaseHelper dbHelper;

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

    public String getUserPreference(long userId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        String preferredType = "";

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("user_preferences",
                    new String[]{"preferred_type"},
                    "user_id = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                preferredType = cursor.getString(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return preferredType;
    }

    // 添加保存用户偏好的方法
    public void saveUserPreference(long userId, String preferredType) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("preferred_type", preferredType);

            // 使REPLACE 语法，存在则更新，不存在则插入
            db.replace("user_preferences", null, values);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
}
