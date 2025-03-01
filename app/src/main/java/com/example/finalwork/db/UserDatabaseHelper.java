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

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE); // 执行创建表的SQL语句
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果有新版本的数据库，可以在这里进行升级操作
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }}
