package com.example.finalwork;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalwork.adapter.MovieAdapter;
import com.example.finalwork.db.MovieDatabaseHelper;
import com.example.finalwork.entity.Movie;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private MovieDatabaseHelper dbHelper;
    private String[] categories = {"剧情", "喜剧", "犯罪", "爱情", "动画", "冒险"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        dbHelper = new MovieDatabaseHelper(this);
        setupViews();
        loadMovies();
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.movieRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        movieAdapter = new MovieAdapter(this);
        recyclerView.setAdapter(movieAdapter);


        EditText etSearch = findViewById(R.id.etSearch);
        // 移除搜索按钮相关代码

        // 添加文本变化监听器
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    loadMovies(); // 如果搜索框为空，显示所有电影
                } else {
                    List<Movie> searchResults = dbHelper.searchMovies(query);
                    movieAdapter.setMovies(searchResults);
                    if (searchResults.isEmpty()) {
                        Toast.makeText(AdminActivity.this, "未找到相关电影", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });



        // 设置长按监听器
        movieAdapter.setOnItemLongClickListener((movie, position) -> {
            showMovieOptions(movie);
            return true;
        });

        FloatingActionButton fabAdd = findViewById(R.id.fabAddMovie);
        fabAdd.setOnClickListener(v -> showAddMovieDialog());

        Button btnExit = findViewById(R.id.butexit);
        btnExit.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            // 清除任务栈中的其他 Activity，确保返回登录界面后不能返回管理界面
            startActivity(intent);
            finish();
        });
    }

    private void loadMovies() {
        movieAdapter.setMovies(dbHelper.getAllMovies());
    }

    private void showAddMovieDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_movie, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDirector = dialogView.findViewById(R.id.etDirector);
        EditText etActors = dialogView.findViewById(R.id.etActors);
        EditText etRating = dialogView.findViewById(R.id.etRating);
        EditText etYear = dialogView.findViewById(R.id.etYear);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etImageUrl = dialogView.findViewById(R.id.etImageUrl);
        EditText etVideoUrl = dialogView.findViewById(R.id.etVideoUrl);
        Spinner spCategory = dialogView.findViewById(R.id.spCategory);

        // 设置分类适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("添加电影")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String director = etDirector.getText().toString().trim();
                    String actors = etActors.getText().toString().trim();
                    String ratingStr = etRating.getText().toString().trim();
                    String year = etYear.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String imageUrl = etImageUrl.getText().toString().trim();
                    String videoUrl = etVideoUrl.getText().toString().trim();
                    String category = spCategory.getSelectedItem().toString();

                    if (title.isEmpty() || director.isEmpty() || ratingStr.isEmpty() || imageUrl.isEmpty()) {
                        Toast.makeText(this, "请填写必要信息", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double rating = Double.parseDouble(ratingStr);
                        if (rating < 0 || rating > 10) {
                            Toast.makeText(this, "评分范围应在0-10之间", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Movie movie = new Movie(
                                System.currentTimeMillis(),
                                title, actors, imageUrl, director, rating,
                                category, "", "中文", "中国",
                                description, year,
                                title, category, videoUrl
                        );

                        long newMovieId = dbHelper.addMovie(movie);
                        if (newMovieId != -1) {
                            loadMovies();
                            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "评分格式不正确", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showMovieOptions(Movie movie) {
        String[] options = {"编辑", "删除"};
        new AlertDialog.Builder(this)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditMovieDialog(movie);
                    } else {
                        showDeleteConfirmDialog(movie);
                    }
                })
                .show();
    }
    private void showEditMovieDialog(Movie movie) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_movie, null);
        // 设置已有数据
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDirector = dialogView.findViewById(R.id.etDirector);
        EditText etActors = dialogView.findViewById(R.id.etActors);
        EditText etRating = dialogView.findViewById(R.id.etRating);
        EditText etYear = dialogView.findViewById(R.id.etYear);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etImageUrl = dialogView.findViewById(R.id.etImageUrl);
        EditText etVideoUrl = dialogView.findViewById(R.id.etVideoUrl);
        Spinner spCategory = dialogView.findViewById(R.id.spCategory);

        // 填充现有数据
        etTitle.setText(movie.getTitle());
        etDirector.setText(movie.getDirector());
        etActors.setText(movie.getActors());
        etRating.setText(String.valueOf(movie.getRating()));
        etYear.setText(movie.getYear());
        etDescription.setText(movie.getDescription());
        etImageUrl.setText(movie.getImageUrl());
        etVideoUrl.setText(movie.getAddress());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        int position = adapter.getPosition(movie.getCategory());
        if (position >= 0) {
            spCategory.setSelection(position);
        }

        new AlertDialog.Builder(this)
                .setTitle("编辑电影")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    movie.setTitle(etTitle.getText().toString().trim());
                    movie.setDirector(etDirector.getText().toString().trim());
                    movie.setActors(etActors.getText().toString().trim());
                    movie.setRating(Double.parseDouble(etRating.getText().toString().trim()));
                    movie.setYear(etYear.getText().toString().trim());
                    movie.setDescription(etDescription.getText().toString().trim());
                    movie.setImageUrl(etImageUrl.getText().toString().trim());
                    movie.setAddress(etVideoUrl.getText().toString().trim());
                    movie.setCategory(spCategory.getSelectedItem().toString());

                    if (dbHelper.updateMovie(movie)) {
                        loadMovies();
                        Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDeleteConfirmDialog(Movie movie) {
        new AlertDialog.Builder(this)
                .setTitle("删除确认")
                .setMessage("确定要删除电影《" + movie.getTitle() + "》吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    if (dbHelper.deleteMovie(movie.getId())) {
                        loadMovies();
                        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

}