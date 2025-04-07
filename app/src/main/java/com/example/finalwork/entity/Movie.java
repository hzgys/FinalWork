package com.example.finalwork.entity;

import java.io.Serializable;

public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String title;
    private String actors;
    private String imageUrl;
    private String director;
    private double rating;
    private String genre;
    private String imdbId;
    private String language;
    private String country;
    private String description;
    private String year;

    private String name;
    private String category;
    private String address;

    public Movie(long id, String title, String actors, String imageUrl, String director,
                 double rating, String genre, String imdbId, String language, String country,
                 String description, String year, String name, String category, String address) {
        this.id = id;
        this.title = title;
        this.actors = actors;
        this.imageUrl = imageUrl;
        this.director = director;
        this.rating = rating;
        this.genre = genre;
        this.imdbId = imdbId;
        this.language = language;
        this.country = country;
        this.description = description;
        this.year = year;
        this.name = name;
        this.category = category;
        this.address = address;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    // Getters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getActors() { return actors; }
    public String getImageUrl() { return imageUrl; }
    public String getDirector() { return director; }
    public double getRating() { return rating; }
    public String getGenre() { return genre; }
    public String getImdbId() { return imdbId; }
    public String getLanguage() { return language; }
    public String getCountry() { return country; }
    public String getDescription() { return description; }
    public String getYear() { return year; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setActors(String actors) { this.actors = actors; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDirector(String director) { this.director = director; }
    public void setRating(double rating) { this.rating = rating; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }
    public void setLanguage(String language) { this.language = language; }
    public void setCountry(String country) { this.country = country; }
    public void setDescription(String description) { this.description = description; }
    public void setYear(String year) { this.year = year; }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", director='" + director + '\'' +
                ", rating=" + rating +
                ", category='" + category + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return id == movie.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}