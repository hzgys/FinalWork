package com.example.finalwork.entity;

public class Post {
    private long id;
    private String title;
    private String content;
    private String author;
    private long userId;  // 添加用户ID字段
    private long timestamp;

    public Post(String title, String content, String author, long userId) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }

    // 添加 getter 和 setter
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}