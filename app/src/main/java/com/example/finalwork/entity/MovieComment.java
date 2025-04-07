package com.example.finalwork.entity;
public class MovieComment {
    private long id;
    private long userId;
    private String username;
    private long movieId;
    private float rating;
    private String content;
    private String createTime;

    // 构造方法
    public MovieComment(long id, long userId, String username, long movieId,
                        float rating, String content, String createTime) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.movieId = movieId;
        this.rating = rating;
        this.content = content;
        this.createTime = createTime;
    }

    // Getter和Setter方法
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public long getMovieId() { return movieId; }
    public void setMovieId(long movieId) { this.movieId = movieId; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}