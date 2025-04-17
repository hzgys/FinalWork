package com.example.finalwork.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import com.example.finalwork.R;
import com.example.finalwork.entity.Movie;
import com.example.finalwork.entity.MovieComment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "MovieDatabaseHelper";
    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_MOVIES = "movies";
    private static final String TABLE_CLASSIFIED = "classified_movies";
    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_RATINGS = "ratings";
    private Context context;

    public MovieDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MOVIES_TABLE = "CREATE TABLE " + TABLE_MOVIES + " (" +
                "id INTEGER PRIMARY KEY," +
                "title TEXT," +
                "actors TEXT," +
                "image_url TEXT," +
                "director TEXT," +
                "rating REAL," +
                "genre TEXT," +
                "imdb_id TEXT," +
                "language TEXT," +
                "country TEXT," +
                "description TEXT," +
                "year TEXT)";

        String CREATE_CLASSIFIED_TABLE = "CREATE TABLE " + TABLE_CLASSIFIED + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "movie_id INTEGER NOT NULL," +
                "name TEXT," +
                "category TEXT," +
                "category_id INTEGER," + // 新增类型ID字段
                "address TEXT," +
                "FOREIGN KEY(movie_id) REFERENCES " + TABLE_MOVIES + "(id))";




        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "movie_id INTEGER," +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE(user_id, movie_id))";

        String CREATE_RATINGS_TABLE = "CREATE TABLE " + TABLE_RATINGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "movie_id INTEGER," +
                "rating FLOAT," +
                "comment TEXT," + // 添加评论字段
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE(user_id, movie_id))";

        db.execSQL(CREATE_MOVIES_TABLE);

        db.execSQL(CREATE_CLASSIFIED_TABLE);

        db.execSQL(CREATE_FAVORITES_TABLE);
        db.execSQL(CREATE_RATINGS_TABLE);
        importMoviesFromCsv(db);
        importClassifiedMoviesFromCsv(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 先删除有外键约束的表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RATINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASSIFIED);
        // 最后删除主表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
        // 重新创建表
        onCreate(db);
    }

    private String processImageUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return "";
        }

        String imageUrl = rawUrl.trim().replace("\"", "");
        try {
            if (!imageUrl.isEmpty()) {
                if (imageUrl.startsWith("//")) {
                    imageUrl = "https:" + imageUrl;
                } else if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    imageUrl = "https://" + imageUrl;
                }
                return imageUrl.replace(" ", "%20");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing image URL: " + rawUrl, e);
        }
        return "";
    }

    private void importMoviesFromCsv(SQLiteDatabase db) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.movies);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean firstLine = true;

            db.beginTransaction();
            try {
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }

                    String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (parts.length >= 12) {
                        ContentValues values = new ContentValues();
                        values.put("id", Integer.parseInt(parts[0].trim()));
                        values.put("title", parts[1].trim().replace("\"", ""));
                        values.put("actors", parts[2].trim().replace("\"", ""));
                        values.put("image_url", processImageUrl(parts[3]));
                        values.put("director", parts[4].trim().replace("\"", ""));
                        values.put("rating", Double.parseDouble(parts[5].trim()));
                        values.put("genre", parts[6].trim().replace("\"", ""));
                        values.put("imdb_id", parts[7].trim().replace("\"", ""));
                        values.put("language", parts[8].trim().replace("\"", ""));
                        values.put("country", parts[9].trim().replace("\"", ""));
                        values.put("description", parts[10].trim().replace("\"", ""));
                        values.put("year", parts[11].trim().replace("\"", ""));

                        long result = db.insert(TABLE_MOVIES, null, values);
                        if (result == -1) {
                            Log.e(TAG, "Failed to insert movie: " + parts[1]);
                        }
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                reader.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error importing CSV file", e);
        }
    }

    private void importClassifiedMoviesFromCsv(SQLiteDatabase db) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.movies_classified);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean firstLine = true;

            db.beginTransaction();
            try {
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }

                    String[] parts = line.split(",", 4);
                    if (parts.length >= 4) {
                        ContentValues values = new ContentValues();
                        values.put("movie_id", Integer.parseInt(parts[0].trim()));
                        values.put("name", parts[1].trim().replace("\"", ""));
                        String category = parts[2].trim().replace("\"", "");
                        values.put("category", category);
                        values.put("address", parts[3].trim().replace("\"", ""));

                        // 设置category_id
                        int categoryId;
                        switch (category) {
                            case "剧情": categoryId = 1; break;
                            case "喜剧": categoryId = 2; break;
                            case "犯罪": categoryId = 3; break;
                            case "爱情": categoryId = 4; break;
                            case "动画": categoryId = 5; break;
                            case "冒险": categoryId = 6; break;
                            default: categoryId = 0;
                        }
                        values.put("category_id", categoryId);

                        db.insert(TABLE_CLASSIFIED, null, values);
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                reader.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error importing classified CSV", e);
        }
    }


    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MOVIES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                movies.add(createMovieFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return movies;
    }

    public void addToFavorites(long userId, long movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("movie_id", movieId);
        db.replace(TABLE_FAVORITES, null, values);
    }

    public void removeFromFavorites(long userId, long movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, "user_id = ? AND movie_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(movieId)});
    }

    public List<Movie> getFavoriteMovies(long userId) {
        List<Movie> movies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT m.* FROM " + TABLE_MOVIES + " m " +
                "INNER JOIN " + TABLE_FAVORITES + " f ON m.id = f.movie_id " +
                "WHERE f.user_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                movies.add(createMovieFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return movies;
    }

    public boolean isFavorite(long userId, long movieId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES,
                null,
                "user_id = ? AND movie_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(movieId)},
                null, null, null);

        boolean isFavorite = cursor.getCount() > 0;
        cursor.close();
        return isFavorite;
    }

    public void setUserRating(long userId, long movieId, float rating, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("rating", rating);
        values.put("comment", comment);
        values.put("create_time", System.currentTimeMillis()); // 改为 create_time

        // 检查是否已存在评分
        Cursor cursor = db.query("ratings",
                null,
                "user_id = ? AND movie_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(movieId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            // 已存在评分，执行更新
            db.update("ratings", values,
                    "user_id = ? AND movie_id = ?",
                    new String[]{String.valueOf(userId), String.valueOf(movieId)});
        } else {
            // 不存在评分，执行插入
            values.put("user_id", userId);
            values.put("movie_id", movieId);
            db.insert("ratings", null, values);
        }
        cursor.close();
    }

    public String getUserComment(long userId, long movieId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RATINGS,
                new String[]{"comment"},
                "user_id = ? AND movie_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(movieId)},
                null, null, null);

        String comment = null;
        if (cursor.moveToFirst()) {
            comment = cursor.getString(0);
        }
        cursor.close();
        return comment;
    }
    public float getUserRating(long userId, long movieId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RATINGS,
                new String[]{"rating"},
                "user_id = ? AND movie_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(movieId)},
                null, null, null);

        float rating = 0;
        if (cursor.moveToFirst()) {
            rating = cursor.getFloat(0);
        }
        cursor.close();
        return rating;
    }

    public List<Movie> searchMovies(String query) {
        List<Movie> movies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = "title LIKE ? OR director LIKE ? OR actors LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"};

        Cursor cursor = db.query(TABLE_MOVIES, null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                movies.add(createMovieFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return movies;
    }

    private Movie createMovieFromCursor(Cursor cursor) {
        SQLiteDatabase db = this.getReadableDatabase();
        String name = "";
        String category = "";
        String address = "";
        Cursor classifiedCursor = db.query(TABLE_CLASSIFIED,
                new String[]{"name", "category", "address"},
                "movie_id = ?",
                new String[]{String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("id")))},
                null, null, null);

        if (classifiedCursor.moveToFirst()) {
            name = classifiedCursor.getString(classifiedCursor.getColumnIndexOrThrow("name"));
            category = classifiedCursor.getString(classifiedCursor.getColumnIndexOrThrow("category"));
            address = classifiedCursor.getString(classifiedCursor.getColumnIndexOrThrow("address"));
        }
        classifiedCursor.close();
        return new Movie(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("title")),
                cursor.getString(cursor.getColumnIndexOrThrow("actors")),
                cursor.getString(cursor.getColumnIndexOrThrow("image_url")),
                cursor.getString(cursor.getColumnIndexOrThrow("director")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("rating")),
                cursor.getString(cursor.getColumnIndexOrThrow("genre")),
                cursor.getString(cursor.getColumnIndexOrThrow("imdb_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("language")),
                cursor.getString(cursor.getColumnIndexOrThrow("country")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getString(cursor.getColumnIndexOrThrow("year")),
                name,
                category,
                address
        );
    }


    public List<MovieComment> getMovieComments(long movieId) {
        List<MovieComment> comments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        UserDatabaseHelper userDb = new UserDatabaseHelper(context);
        SQLiteDatabase userDatabase = userDb.getReadableDatabase();

        String query = "SELECT r.* FROM " + TABLE_RATINGS + " r " +
                "WHERE r.movie_id = ? " +
                "ORDER BY r.create_time DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(movieId)});

        if (cursor.moveToFirst()) {
            do {
                long userId = cursor.getLong(cursor.getColumnIndexOrThrow("user_id"));

                // 从用户数据库获取评论者的用户名
                String username;
                Cursor userCursor = userDatabase.query(
                        "users",
                        new String[]{"username"},
                        "id = ?",
                        new String[]{String.valueOf(userId)},
                        null, null, null
                );

                if (userCursor.moveToFirst()) {
                    username = userCursor.getString(0);
                } else {
                    username = "用户" + userId;
                }
                userCursor.close();

                MovieComment comment = new MovieComment(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        userId,
                        username,  // 使用评论创建者的用户名
                        movieId,
                        cursor.getFloat(cursor.getColumnIndexOrThrow("rating")),
                        cursor.getString(cursor.getColumnIndexOrThrow("comment")),
                        cursor.getString(cursor.getColumnIndexOrThrow("create_time"))
                );
                comments.add(comment);
            } while (cursor.moveToNext());
        }
        cursor.close();
        userDatabase.close();
        return comments;
    }


    //推荐
    public int getRatingCount(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM ratings WHERE user_id = ?",
                new String[]{String.valueOf(userId)}
        );
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<Movie> getMoviesByQuery(String query, String[] args) {
        List<Movie> movies = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, args);
        if (cursor.moveToFirst()) {
            do {
                movies.add(createMovieFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return movies;
    }

    public List<Long> getUsersByQuery(String query, String[] args) {
        List<Long> users = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, args);
        if (cursor.moveToFirst()) {
            do {
                users.add(cursor.getLong(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }
    public Movie getMovieById(long movieId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MOVIES,
                null,
                "id = ?",
                new String[]{String.valueOf(movieId)},
                null, null, null);

        Movie movie = null;
        if (cursor.moveToFirst()) {
            movie = createMovieFromCursor(cursor);
        }
        cursor.close();
        return movie;
    }


    public Map<Long, Double> getRatingsMap(String query, String[] args) {
        Map<Long, Double> ratingsMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(query, args)) {
            while (cursor.moveToNext()) {
                long movieId = cursor.getLong(cursor.getColumnIndexOrThrow("movie_id"));
                double rating = cursor.getDouble(cursor.getColumnIndexOrThrow("rating"));
                ratingsMap.put(movieId, rating);
            }
        }

        return ratingsMap;
    }

    public List<Long> getOtherUsers(long currentUserId) {
        List<Long> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT DISTINCT user_id FROM ratings WHERE user_id != ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)})) {
            while (cursor.moveToNext()) {
                users.add(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
            }
        }

        return users;
    }

    public boolean hasRated(long userId, long movieId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM ratings WHERE user_id = ? AND movie_id = ?";
        try (Cursor cursor = db.rawQuery(query,
                new String[]{String.valueOf(userId), String.valueOf(movieId)})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        }
        return false;
    }

    public boolean hasFavorited(long userId, long movieId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND movie_id = ?";
        try (Cursor cursor = db.rawQuery(query,
                new String[]{String.valueOf(userId), String.valueOf(movieId)})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        }
        return false;
    }

    //电影的增删改查
    public boolean deleteMovie(long movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        db.beginTransaction();
        try {
            // 先删除关联表中的数据
            db.delete(TABLE_FAVORITES, "movie_id = ?", new String[]{String.valueOf(movieId)});
            db.delete(TABLE_RATINGS, "movie_id = ?", new String[]{String.valueOf(movieId)});
            db.delete(TABLE_CLASSIFIED, "movie_id = ?", new String[]{String.valueOf(movieId)});

            // 删除电影主表中的数据
            int result = db.delete(TABLE_MOVIES, "id = ?", new String[]{String.valueOf(movieId)});
            success = result > 0;
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return success;
    }

    public boolean updateMovie(Movie movie) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        db.beginTransaction();
        try {
            ContentValues movieValues = new ContentValues();
            movieValues.put("title", movie.getTitle());
            movieValues.put("actors", movie.getActors());
            movieValues.put("image_url", movie.getImageUrl());
            movieValues.put("director", movie.getDirector());
            movieValues.put("rating", movie.getRating());
            movieValues.put("genre", movie.getGenre());
            movieValues.put("language", movie.getLanguage());
            movieValues.put("country", movie.getCountry());
            movieValues.put("description", movie.getDescription());
            movieValues.put("year", movie.getYear());

            int movieResult = db.update(TABLE_MOVIES, movieValues, "id = ?",
                    new String[]{String.valueOf(movie.getId())});

            ContentValues classifiedValues = new ContentValues();
            classifiedValues.put("name", movie.getName());
            classifiedValues.put("category", movie.getCategory());
            classifiedValues.put("address", movie.getAddress());

            int classifiedResult = db.update(TABLE_CLASSIFIED, classifiedValues,
                    "movie_id = ?", new String[]{String.valueOf(movie.getId())});

            success = movieResult > 0 && classifiedResult > 0;
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return success;
    }

    public long addMovie(Movie movie) {
        SQLiteDatabase db = this.getWritableDatabase();
        long movieId = -1;
        db.beginTransaction();
        try {
            ContentValues movieValues = new ContentValues();
            movieValues.put("title", movie.getTitle());
            movieValues.put("actors", movie.getActors());
            movieValues.put("image_url", movie.getImageUrl());
            movieValues.put("director", movie.getDirector());
            movieValues.put("rating", movie.getRating());
            movieValues.put("genre", movie.getGenre());
            movieValues.put("language", "中文");
            movieValues.put("country", "中国");
            movieValues.put("description", movie.getDescription());
            movieValues.put("year", movie.getYear());

            movieId = db.insert(TABLE_MOVIES, null, movieValues);

            if (movieId != -1) {
                ContentValues classifiedValues = new ContentValues();
                classifiedValues.put("movie_id", movieId);
                classifiedValues.put("name", movie.getTitle());
                classifiedValues.put("category", movie.getCategory());
                classifiedValues.put("address", movie.getAddress());

                db.insert(TABLE_CLASSIFIED, null, classifiedValues);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return movieId;
    }





}