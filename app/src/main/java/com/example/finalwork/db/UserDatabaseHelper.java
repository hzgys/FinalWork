package com.example.finalwork.db;

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




