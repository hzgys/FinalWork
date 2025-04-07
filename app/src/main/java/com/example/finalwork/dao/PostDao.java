package com.example.finalwork.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.finalwork.db.ChatDatabaseHelper;
import com.example.finalwork.entity.Post;

import java.util.ArrayList;
import java.util.List;

public class PostDao {
    private static PostDao instance;
    private final ChatDatabaseHelper dbHelper;

    private PostDao(Context context) {
        dbHelper = new ChatDatabaseHelper(context);
    }

    public static synchronized PostDao getInstance(Context context) {
        if (instance == null) {
            instance = new PostDao(context.getApplicationContext());
        }
        return instance;
    }

    public long insertPost(Post post) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ChatDatabaseHelper.COLUMN_TITLE, post.getTitle());
            values.put(ChatDatabaseHelper.COLUMN_CONTENT, post.getContent());
            values.put(ChatDatabaseHelper.COLUMN_AUTHOR, post.getAuthor());
            values.put(ChatDatabaseHelper.COLUMN_USER_ID, post.getUserId());
            values.put(ChatDatabaseHelper.COLUMN_TIMESTAMP, post.getTimestamp());
            return db.insert(ChatDatabaseHelper.TABLE_POSTS, null, values);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query(ChatDatabaseHelper.TABLE_POSTS, null, null, null, null, null,
                    ChatDatabaseHelper.COLUMN_TIMESTAMP + " DESC");

            while (cursor.moveToNext()) {
                Post post = new Post(
                        cursor.getString(cursor.getColumnIndexOrThrow(ChatDatabaseHelper.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ChatDatabaseHelper.COLUMN_CONTENT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ChatDatabaseHelper.COLUMN_AUTHOR)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(ChatDatabaseHelper.COLUMN_USER_ID))
                );
                post.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ChatDatabaseHelper.COLUMN_ID)));
                post.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(ChatDatabaseHelper.COLUMN_TIMESTAMP)));
                posts.add(post);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return posts;
    }

    public boolean deletePost(long postId) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            int deletedRows = db.delete(
                    ChatDatabaseHelper.TABLE_POSTS,
                    ChatDatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(postId)}
            );
            return deletedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
}