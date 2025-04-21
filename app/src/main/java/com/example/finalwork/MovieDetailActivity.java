package com.example.finalwork;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalwork.adapter.MovieCommentAdapter;
import com.example.finalwork.db.MovieDatabaseHelper;
import com.example.finalwork.entity.Movie;
import com.example.finalwork.entity.MovieComment;

import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {


    private ImageView posterImage;
    private TextView titleText, directorText, genreText, actorsText, descriptionText, ratingText;
    private Button favoriteButton;
    private VideoView videoView;
    private Button playButton;
    private MovieDatabaseHelper dbHelper;
    private Movie movie;
    private long userId;

    private RecyclerView commentsRecyclerView;
    private MovieCommentAdapter commentAdapter;
    private Button btnAddComment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // 初始化数据库Helper
        dbHelper = new MovieDatabaseHelper(this);

        // 获取电影对象
        movie = (Movie) getIntent().getSerializableExtra("movie");
        if (movie == null) {
            Toast.makeText(this, "获取电影信息失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化基础视图组件
        initViews();

        // 获取当前用户ID
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        userId = sharedPreferences.getLong("user_id", -1);

        // 设置返回按钮
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // 初始化评论列表
        commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new MovieCommentAdapter();
        commentsRecyclerView.setAdapter(commentAdapter);

        // 设置添加评论按钮
        btnAddComment = findViewById(R.id.btn_add_comment);
        btnAddComment.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(this, "请先登录后再评论", Toast.LENGTH_SHORT).show();
                return;
            }
            showRatingDialog();
        });

        // 显示电影详情
        displayMovieDetails();

        // 更新收藏按钮状态
        updateFavoriteButton();

        // 设置视频播放器
        setupVideoPlayer();

        // 加载评论数据
        loadComments();
    }

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating_comment, null);

        EditText ratingEdit = dialogView.findViewById(R.id.rating_edit_text);
        EditText commentEdit = dialogView.findViewById(R.id.comment_edit_text);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit);

        // 设置当前评分和评论(如果有)
        float currentRating = dbHelper.getUserRating(userId, movie.getId());
        String currentComment = dbHelper.getUserComment(userId, movie.getId());

        if (currentRating > 0) {
            ratingEdit.setText(String.valueOf(Math.round(currentRating))); // 直接显示0-100的评分
        }

        if (currentComment != null) {
            commentEdit.setText(currentComment);
        }

        AlertDialog dialog = builder.setView(dialogView).create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String ratingStr = ratingEdit.getText().toString().trim();
            String comment = commentEdit.getText().toString().trim();

            if (ratingStr.isEmpty()) {
                Toast.makeText(this, "请输入评分", Toast.LENGTH_SHORT).show();
                return;
            }

            int rating;
            try {
                rating = Integer.parseInt(ratingStr);
                if (rating < 0 || rating > 100) {
                    Toast.makeText(this, "评分必须在0-100之间", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "请输入有效的评分", Toast.LENGTH_SHORT).show();
                return;
            }

            // 直接保存用户输入的评分(0-100)
            dbHelper.setUserRating(userId, movie.getId(), rating, comment);
            loadComments();
            dialog.dismiss();
            Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void loadComments() {
        List<MovieComment> comments = dbHelper.getMovieComments(movie.getId());
        commentAdapter.setComments(comments);
    }
    private void initViews() {
        posterImage = findViewById(R.id.movie_detail_poster);
        titleText = findViewById(R.id.movie_detail_title);
        directorText = findViewById(R.id.movie_detail_director);
        actorsText = findViewById(R.id.movie_detail_actors);
        genreText = findViewById(R.id.movie_detail_genre);
        descriptionText = findViewById(R.id.movie_detail_description);
        ratingText = findViewById(R.id.movie_detail_rating);
        favoriteButton = findViewById(R.id.btn_favorite);
        videoView = findViewById(R.id.video_view);
        playButton = findViewById(R.id.btn_play_video);

        favoriteButton.setOnClickListener(v -> toggleFavorite());
        playButton.setOnClickListener(v -> playVideo());
    }

    private void setupVideoPlayer() {
        String videoUrl = movie.getAddress();
        if (videoUrl == null || videoUrl.isEmpty()) {
            playButton.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
        } else {
            playButton.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.VISIBLE);
        }
    }

    private void playVideo() {
        String videoUrl = movie.getAddress();
        if (videoUrl != null && !videoUrl.isEmpty()) {
            try {
                // 设置进度对话框
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("加载中...");
                progressDialog.setCancelable(true);
                progressDialog.show();

                // 设置媒体控制器
                MediaController mediaController = new MediaController(this);
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);

                // 设置视频路径
                videoView.setVideoURI(Uri.parse(videoUrl));

                // 准备完成时的监听器
                videoView.setOnPreparedListener(mp -> {
                    progressDialog.dismiss();
                    playButton.setVisibility(View.GONE);
                    videoView.start();
                });

                // 错误监听器
                videoView.setOnErrorListener((mp, what, extra) -> {
                    progressDialog.dismiss();
                    playButton.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "视频加载失败", Toast.LENGTH_SHORT).show();
                    return true;
                });

                // 播放完成监听器
                videoView.setOnCompletionListener(mp -> {
                    playButton.setVisibility(View.VISIBLE);
                });

            } catch (Exception e) {
                Toast.makeText(this, "无法播放视频", Toast.LENGTH_SHORT).show();
                playButton.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(this, "没有可用的视频地址", Toast.LENGTH_SHORT).show();
            playButton.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
        }
    }

    private void displayMovieDetails() {
        titleText.setText(movie.getTitle());
        directorText.setText("导演: " + movie.getDirector());
        genreText.setText("类型: " + movie.getCategory());
        actorsText.setText("演员: " + movie.getActors());
        descriptionText.setText(movie.getDescription());
        ratingText.setText(String.format("评分: %.1f", movie.getRating()));

        String imageUrl = movie.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_movie)
                    .error(R.drawable.placeholder_movie)
                    .into(posterImage);
        } else {
            posterImage.setImageResource(R.drawable.placeholder_movie);
        }
    }

    private void toggleFavorite() {
        if (userId != -1) {
            boolean isFavorite = dbHelper.isFavorite(userId, movie.getId());
            if (isFavorite) {
                dbHelper.removeFromFavorites(userId, movie.getId());
            } else {
                dbHelper.addToFavorites(userId, movie.getId());
            }
            updateFavoriteButton();
            Toast.makeText(this,
                    isFavorite ? "已取消收藏" : "已添加到收藏",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFavoriteButton() {
        boolean isFavorite = userId != -1 && dbHelper.isFavorite(userId, movie.getId());
        favoriteButton.setText(isFavorite ? "取消收藏" : "收藏");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}