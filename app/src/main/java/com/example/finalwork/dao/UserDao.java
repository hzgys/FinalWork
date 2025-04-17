package com.example.finalwork.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.finalwork.db.UserDatabaseHelper;
import com.example.finalwork.entity.User;

public class UserDao {
    private UserDatabaseHelper dbHelper;
    private static UserDao instance;

    private UserDao(Context context) {
        dbHelper = new UserDatabaseHelper(context);
    }

    public static synchronized UserDao getInstance(Context context) {
        if (instance == null) {
            instance = new UserDao(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUserPreference(long userId, String preferredType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("preferred_type", preferredType.trim());

        // 设置preferred_type_id
        int preferredTypeId;
        switch (preferredType.trim()) {
            case "剧情": preferredTypeId = 1; break;
            case "喜剧": preferredTypeId = 2; break;
            case "犯罪": preferredTypeId = 3; break;
            case "爱情": preferredTypeId = 4; break;
            case "动画": preferredTypeId = 5; break;
            case "冒险": preferredTypeId = 6; break;
            default: preferredTypeId = 0;
        }
        values.put("preferred_type_id", preferredTypeId);



        db.insert("user_preferences", null, values);
    }

    public void updateTypeRating(long userId, String type, float rating) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(type + "_rating", rating);

        db.update("user_preferences",
                values,
                "user_id = ?",
                new String[]{String.valueOf(userId)});
    }

    public long registerUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());

        long id = db.insert("users", null, values);
        user.setId(id);
        return id;
    }

    public boolean updatePassword(long userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("password", newPassword);

            int result = db.update("users", values,
                    "id = ?",
                    new String[]{String.valueOf(userId)});
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public User loginUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        // 先查询用户表
        Cursor userCursor = db.query("users",
                new String[]{"id", "username", "password"},
                "username = ? AND password = ?",
                new String[]{username, password},
                null, null, null);

        if (userCursor != null && userCursor.moveToFirst()) {
            user = new User(
                    userCursor.getString(userCursor.getColumnIndexOrThrow("username")),
                    userCursor.getString(userCursor.getColumnIndexOrThrow("password"))
            );
            user.setId(userCursor.getLong(userCursor.getColumnIndexOrThrow("id")));
            userCursor.close();

            // 再查询偏好表
            Cursor prefCursor = db.query("user_preferences",
                    new String[]{"preferred_type"},  // 只查询需要的字段
                    "user_id = ?",
                    new String[]{String.valueOf(user.getId())},
                    null, null, null);

            if (prefCursor != null && prefCursor.moveToFirst()) {
                user.setPreferredType(prefCursor.getString(prefCursor.getColumnIndexOrThrow("preferred_type")));
                prefCursor.close();
            }
        }
        return user;
    }
}