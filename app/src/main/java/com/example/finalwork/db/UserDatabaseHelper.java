package com.example.finalwork.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {



    private static final String DATABASE_NAME = "user.db";
    private static final int DATABASE_VERSION = 1;

    // SQL语句：创建用户表
    private static final String TABLE_CREATE =
            "CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT)";


    private static final String TABLE_CREATE_PREFERENCES =
            "CREATE TABLE user_preferences (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "preferred_type TEXT, " +         // 保留原有文本类型
                    "preferred_type_id INTEGER, " +   // 新增数字类型ID
                    "FOREIGN KEY(user_id) REFERENCES users(id))";
    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE); // 执行创建表的SQL语句
        db.execSQL(TABLE_CREATE_PREFERENCES);

        generateTestUsers(db);
    }

    private void generateTestUsers(SQLiteDatabase db) {
        String[] movieTypes = {"剧情", "喜剧", "犯罪", "爱情", "动画", "冒险"};

        for (int i = 1; i <= 40; i++) {
            // 插入用户
            ContentValues userValues = new ContentValues();
            userValues.put("username", "user_" + i);
            userValues.put("password", "password" + i);
            long userId = db.insert("users", null, userValues);

            // 插入用户偏好
            if (userId != -1) {
                ContentValues prefValues = new ContentValues();
                prefValues.put("user_id", userId);

                // 随机选择一个电影类型
                String randomType = movieTypes[(int) (Math.random() * movieTypes.length)];
                prefValues.put("preferred_type", randomType);

                // 设置preferred_type_id
                int preferredTypeId;
                switch (randomType) {
                    case "剧情": preferredTypeId = 1; break;
                    case "喜剧": preferredTypeId = 2; break;
                    case "犯罪": preferredTypeId = 3; break;
                    case "爱情": preferredTypeId = 4; break;
                    case "动画": preferredTypeId = 5; break;
                    case "冒险": preferredTypeId = 6; break;
                    default: preferredTypeId = 0;
                }
                prefValues.put("preferred_type_id", preferredTypeId);

                db.insert("user_preferences", null, prefValues);
            }
        }
    }
    public boolean updatePassword(long userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String sql = "UPDATE users SET password = ? WHERE id = ?";
            db.execSQL(sql, new Object[]{newPassword, userId});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果有新版本的数据库，可以在这里进行升级操作
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }}




