package com.example.finalwork.entity;

public class User {
    private long id;
    private String username;
    private String password;
    private String preferredType;
    private float storyRating;
    private float comedyRating;
    private float crimeRating;
    private float loveRating;
    private float animationRating;
    private float adventureRating;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getPreferredType() { return preferredType; }
    public void setPreferredType(String preferredType) { this.preferredType = preferredType; }
    public float getStoryRating() { return storyRating; }
    public void setStoryRating(float storyRating) { this.storyRating = storyRating; }
    public float getComedyRating() { return comedyRating; }
    public void setComedyRating(float comedyRating) { this.comedyRating = comedyRating; }
    public float getCrimeRating() { return crimeRating; }
    public void setCrimeRating(float crimeRating) { this.crimeRating = crimeRating; }
    public float getLoveRating() { return loveRating; }
    public void setLoveRating(float loveRating) { this.loveRating = loveRating; }
    public float getAnimationRating() { return animationRating; }
    public void setAnimationRating(float animationRating) { this.animationRating = animationRating; }
    public float getAdventureRating() { return adventureRating; }
    public void setAdventureRating(float adventureRating) { this.adventureRating = adventureRating; }

}